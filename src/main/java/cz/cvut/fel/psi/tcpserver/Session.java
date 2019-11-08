package cz.cvut.fel.psi.tcpserver;

import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;
import cz.cvut.fel.psi.tcpserver.exceptions.BadChecksumException;
import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionClosedException;
import cz.cvut.fel.psi.tcpserver.requests.Request;
import cz.cvut.fel.psi.tcpserver.requests.RequestFactory;
import cz.cvut.fel.psi.tcpserver.requests.RequestOffset;
import cz.cvut.fel.psi.tcpserver.responses.Response;
import cz.cvut.fel.psi.tcpserver.responses.AcceptingUsername;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.ArrayUtils;

/**
 * Represents session of client at the server.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class Session extends Thread implements Serializable{
    private static final Logger LOG = Logger.getLogger(Session.class.getName());    
    
    private Server srv;    
    private Socket socket; 
    private InputStream in;
    private OutputStream out;
    private RequestFactory rf;
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
        rf = new RequestFactory(in);
        sc = new Scanner(in).useDelimiter("\\r\\n");       
    }
    
    /**
     * Represents the session runtime
     */
    @Override
    public void run(){                    
        LOG.log(Level.INFO, "Connected session {0} to socket: {1}", new Object[]{this, socket.getPort()});
        LOG.log(Level.FINE, "Session {0} established: {1}", new Object[]{this, established});
        isRunning = true;
        try{
            socket.setSoTimeout(Server.SESSION_TIMEOUT_SECONDS * 1000);
            Response response = new AcceptingUsername(this);
            sendResponse(response);            
            while((response = response.next()) != null && socket.isConnected() && isRunning){         
                LOG.log(Level.FINEST, "Session {0} response: {1}", new Object[]{this, response});
                LOG.log(Level.FINEST, "Session {0} socket is connected {1}", new Object[]{this, socket.isConnected()});
            }            
            close();
        } catch (SessionRunException ex){
            LOG.log(Level.SEVERE, "Unknown error catched. {0}", ex);
            ex.printStackTrace(); 
            
        } catch (IOException ex) {            
            LOG.log(Level.SEVERE, "Unknown error catched. {0}", ex);
            ex.printStackTrace();
            
        } catch (SessionClosedException ex) {
            try {
                close();
            } catch (SessionRunException ex1) {
               LOG.log(Level.SEVERE, "Unknown error catched. {0}", ex);
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Closes the session
     * @throws cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException
     */
    public synchronized void close() throws SessionRunException{
        try{
            if(isRunning){
                LOG.log(Level.INFO, "Session {0}: Closing session.", this);
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
     * @throws java.net.SocketTimeoutException if socket cannot be read due to timeout
     * @throws cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException if reading fails
     * @throws cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException if the syntax of request does not match
     * @throws cz.cvut.fel.psi.tcpserver.exceptions.SessionClosedException
     */
    public Request acceptRequest() throws SocketTimeoutException, RequestSyntaxException, SessionRunException, SessionClosedException{        
        try {
            Request req;
            int checksum = 0;
            List<Byte> bytes = new ArrayList();
            Byte previousByte = 0;
            boolean passwordRequest = false;

            while (true) {                
                if(bytes.size() == 5) break;
                int val = in.read();
                if(val < 0) throw new SessionClosedException("Session was closed by client");
                byte[] lineseparator = new byte[]{previousByte, (byte) val};
                
                
                // Line separator detection
                if (new String(lineseparator).equals("\r\n")) {
                    bytes.remove(previousByte);
                    checksum -= previousByte;
                    passwordRequest = true;
                    break;
                }                
                
                checksum += val;
                bytes.add((byte) val);
                // Debug purposes only
                // Byte[] rawBytes0 = bytes.toArray(new Byte[bytes.size()]);
                // String keyword0 = new String(ArrayUtils.toPrimitive(rawBytes0));   
                previousByte = (byte) val;
            }
            Byte[] rawBytes = bytes.toArray(new Byte[bytes.size()]);
            String keyword = new String(ArrayUtils.toPrimitive(rawBytes));            
            RequestOffset data = new RequestOffset(keyword, checksum, previousByte);
            if(passwordRequest) return rf.getPasswordRequest(data);
                else return rf.parseRequest(keyword.split("\\s")[0], data);                        
            
        } catch (SocketTimeoutException ex){
            throw ex;
            
        } catch (IOException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            throw new SessionRunException("Cannot read from input stream.", ex);
        }
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
     * Generates session name as a MD5 hash of Session.established, Session.socket.port and some random number
     * @return hashed String
     */
    private String generateName(){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            Random randomizer = new Random();
            md.update(("Session " + socket.getPort() + " " + established + randomizer.nextInt()).getBytes());
            byte[] digest = md.digest();
            return Base64.getEncoder().encodeToString(digest).toUpperCase();
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
        if(photo.validate()){
            LOG.log(Level.INFO, "Session {0}: Photo {1} uploaded.", new Object[]{this, photo});
            //photos.add(photo); Not used for the current implementation.
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
