/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver;

import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;
import cz.cvut.fel.psi.tcpserver.exceptions.BadChecksumException;
import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import cz.cvut.fel.psi.tcpserver.states.Response;
import cz.cvut.fel.psi.tcpserver.states.AcceptingUsername;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matej
 */
public class Session extends Thread implements Serializable{
    private static final Logger LOG = Logger.getLogger(Session.class.getName());
    private static int TIMEOUT_SECONDS = 0;
    
    private Server srv;    
    private Socket socket; 
    private InputStream in;
    private OutputStream out;    
    protected Date established;
    private final String name;
    private boolean isRunning;
    
    private Scanner sc;
    
    private final List<Photo> photos = new ArrayList();   
    
    private User user;
       
    
    /**
     * Initializes new session with specified socket and reference to server where the session is run.
     * @param socket socket extracted from {@link Server.srvSocket}
     * @param server reference to server
     * @throws IOException if the session is not able to read/write to socket
     */
    public Session(Socket socket, Server server) throws IOException{
        super();        
        established = new Date();
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
        name = generateName();        
        srv = server;
        sc = new Scanner(in).useDelimiter("\\r\\n");       
    }
    
    /**
     * Represents the session runtime
     */
    @Override
    public void run(){                    
        LOG.log(Level.INFO, "Connected session {0} to socket: {1}", new Object[]{this, socket});
        LOG.log(Level.FINE, "Session {0} established: {1}", new Object[]{this, established});
        try{
            Response response = new AcceptingUsername(this);
            sendResponse(response);            
            while((response = response.next()) != null && !socket.isClosed() && isRunning){         
                LOG.log(Level.FINEST, "Session {0} response: {1}", new Object[]{this, response});
            }
            close();
        } catch (SessionRunException ex){
            ex.printStackTrace();            
        }
    }
    
    /**
     * Closes the session
     * @throws cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException
     */
    public synchronized void close() throws SessionRunException{
        try{
            if(isRunning){
                isRunning = false;
                in.close();
                out.close();
                socket.close();
            }
        }   catch (IOException ex) {
            LOG.log(Level.SEVERE, "Cannot close Session {0}", this);
            throw new SessionRunException("Cannot close session " + this, ex);
        }
    }
    
    /**
     * Sends response to client
     * @param response response to be sent. 
     * @throws SessionRunException when the process fails
     */
    public void sendResponse(Response response) throws SessionRunException{        
        try {
            out.write(response.toString().getBytes());
            LOG.log(Level.FINEST, "Sesion {0} response message sent: {1}", new Object[]{this, response});
        } catch (IOException ex) {
            throw new SessionRunException("Cannot send response.", ex);
        }
    }
    
    /**
     * Scans the input stream until there is a request to capture.
     * @return new request captured from Socket.inputStream     
     * @throws NoSuchElementException if there is no element
     * @throws RequestSyntaxException if the syntax does not match
     */
    public Request acceptRequest() throws NoSuchElementException, RequestSyntaxException{        
        String message = sc.next();        
        return new Request(message);        
    }

    /**
     * <i>Method for testing.</i> Returns the input stream of the session.
     * @return input stream of the session
     */
    protected InputStream getIn() {
        return in;
    }

    /**
     * <i>Method for testing.</i> Sets the input stream of the session.
     * @param in input stream to mock the session.
     */
    protected void setIn(InputStream in) {
        this.in = in;
    }

    /**
     * <i>Method for testing.</i> Returns the output stream of the session.
     * @return output stream of the session
     */
    protected OutputStream getOut() {
        return out;
    }

    /**
     * <i>Method for testing.</i> Sets the output stream of the session.
     * @param out output stream to mock the session.
     */
    protected void setOut(OutputStream out) {
        this.out = out;
    }
    
    /**
     * Returns hashed session name from established date and socket local port number.
     * @return session name
     */
    @Override
    public String toString() {
        return name;        
    }
    
    /**
     * Generates session name as a MD5 hash of Session.established, Session.socket.localPort
     * @return hashed String
     */
    private String generateName(){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(("Session " + socket.getLocalPort() + " " + established).getBytes());
            byte[] digest = md.digest();
            return new String(digest).toUpperCase();
        } catch (NoSuchAlgorithmException ex) {
            LOG.log(Level.SEVERE, "Something is terribly wrong!!!", ex);
            throw new RuntimeException("Session.generateName() failed. " + ex);
        }
    }

    /**
     * Returns hashCode of Session.name
     * @return hashCode of Session.name
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
        
    /**
     * Gets logged in user of the session
     * @return user object or null if not initialised yet
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets user to the session
     * @param user to be set
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    /**
     * Adds photo to session save queue. The save of photo is currenty temporary.
     * @param photo retrieved from request
     * @throws BadChecksumException when the photo is not complete
     */
    public void addPhoto(Photo photo) throws BadChecksumException{
        if(photo.validateChecksum()){
            photos.add(photo);
        } else {
            throw new BadChecksumException("Incomplete transfer for photo " + photo);
        }
    }
    
    /**
     * Return number of photos uploaded by this session.
     * @return number of photos in the buffer
     */
    public int getPhotoCounter(){
        return photos.size();
    }
}
