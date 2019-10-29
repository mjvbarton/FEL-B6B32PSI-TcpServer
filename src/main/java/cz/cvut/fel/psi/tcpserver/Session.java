/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver;

import static cz.cvut.fel.psi.tcpserver.Response.ACCEPTING_USERNAME;
import static cz.cvut.fel.psi.tcpserver.Response.ACCEPTING_PASSWORD;
import static cz.cvut.fel.psi.tcpserver.Response.ACCEPTING_MESSAGES;
import static cz.cvut.fel.psi.tcpserver.Response.BAD_CHECKSUM;
import static cz.cvut.fel.psi.tcpserver.Response.SYNTAX_ERROR;
import static cz.cvut.fel.psi.tcpserver.Response.TIMEOUT;
import static cz.cvut.fel.psi.tcpserver.Response.UNAUTHORIZED;
import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import cz.cvut.fel.psi.tcpserver.exceptions.UnauthenticatedException;
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
    
    private Scanner sc;
    
    private final List<Photo> photos = new ArrayList();
    
    private boolean usernameFailed = false;
    
    private User user;
    
    private Response state;
        
    public Session(Socket socket, Server server) throws IOException{
        super();
        state = ACCEPTING_USERNAME;
        established = new Date();
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
        name = generateName();        
        srv = server;
        sc = new Scanner(in).useDelimiter("\\r\\n");
    }
            
    @Override
    public void run() {
        Request req = null;        
        try {
            LOG.log(Level.INFO, "Connected session {0} to socket: {1}", new Object[]{this, socket});
            LOG.log(Level.FINE, "Session {0} established: {1}", new Object[]{this, established});            
            LOG.log(Level.FINE, "Session {0} state: {1}", new Object[]{this, state});
            
            // Sending user a welcome message
            out.write(ACCEPTING_USERNAME.toString().getBytes());
            try {
                socket.setSoTimeout(TIMEOUT_SECONDS);
                while ((state.equals(ACCEPTING_USERNAME) || state.equals(ACCEPTING_PASSWORD) || state.equals(ACCEPTING_MESSAGES))
                        && !socket.isInputShutdown()) {                    
                    try{
                        req = acceptRequest();
                    } catch (RequestSyntaxException ex){
                        
                        // Username validation
                        if(state == ACCEPTING_USERNAME){
                            usernameFailed = true;
                            state = ACCEPTING_PASSWORD;
                            out.write(state.toString().getBytes());
                            continue;
                        } else if(state == ACCEPTING_PASSWORD) {
                            throw new UnauthenticatedException("Wrong password syntax.");
                        } else {
                            throw ex;
                        }                        
                    } catch (NoSuchElementException ex){
                        LOG.log(Level.FINE, "Session {0}: waiting for request. {1}", new Object[]{this, ex});
                        continue;
                    }
                    switch(state){
                        case ACCEPTING_USERNAME:
                            if(req.getType() == RequestType.USERNAME){
                                user = new User(req);
                                state = ACCEPTING_PASSWORD;
                                out.write(state.toString().getBytes());
                            } else {
                                throw new RequestSyntaxException("Expected USERNAME message.");
                            }
                            break;
                        
                        case ACCEPTING_PASSWORD:
                            if(req.getType() == RequestType.PASSWORD){                                
                                if(usernameFailed || user == null || !user.getPassword().equals(req.getData())){
                                    throw new UnauthenticatedException("Authentication failed. For user: " + user);                                    
                                } else {
                                    state = ACCEPTING_MESSAGES;
                                    out.write(state.toString().getBytes());
                                }
                                
                            } else {
                                throw new RequestSyntaxException("Expected PASSWORD message.");                                
                            }
                            break;
                        
                        case ACCEPTING_MESSAGES:
                            if(null == req.getType()){
                                throw new RequestSyntaxException("Expected PASSWORD message.");
                            } else switch (req.getType()) {
                            case INFO:
                                LOG.log(Level.INFO, "Session {0}: Info message captured. Message info: {1}", new Object[]{this, req});
                                out.write(state.toString().getBytes());
                                break;
                            case PHOTO:
                                LOG.log(Level.INFO, "Session {0}: Photo message captured.", new Object[]{this});
                                // LOG.log(Level.SEVERE, "Session {0}: Photo message processing not impemented yet!", new Object[]{this});
                                // TODO: Add logic of photo capturing here
                                Photo photo = new Photo(req);
                                if(photo.validateChecksum()){
                                    photos.add(photo);
                                    out.write(state.toString().getBytes());
                                } else {
                                    photo.flush();
                                    out.write(BAD_CHECKSUM.toString().getBytes());
                                }                                
                                break;
                                
//                            // This is never reached unless something is seriously wrong.
//                            default:                                
//                                throw new RequestSyntaxException("Incorrect syntax entered.");
                        }                                                                                              
                    }
                }
            } catch (SocketException socketException) {
                LOG.log(Level.WARNING, "Session {0} timeout.", new Object[]{this});
                LOG.log(Level.FINE, "Session {0} exception {1}.", new Object[]{this, socketException});
                state = TIMEOUT;
                out.write(state.toString().getBytes());
                
            } catch (RequestSyntaxException requestSyntaxException) {
                LOG.log(Level.WARNING, "Session {0} syntax failed for request {1}.", new Object[]{this, req});
                LOG.log(Level.FINE, "Session {0} exception {1}.", new Object[]{this, requestSyntaxException});
                state = SYNTAX_ERROR;
                out.write(state.toString().getBytes());
                
            } catch (UnauthenticatedException unauthenticatedException) {
                LOG.log(Level.WARNING, "Session {0} authentication failed for user {1}.", new Object[]{this, user});
                LOG.log(Level.FINE, "Session {0} exception {1}.", new Object[]{this, unauthenticatedException});
                state = UNAUTHORIZED;
                out.write(state.toString().getBytes());
            
            } catch (NoSuchElementException ex){
                LOG.log(Level.FINE, "Session {0} exception {1}.", new Object[]{this, ex});
                
            } finally {                                
                LOG.log(Level.INFO, "Session {0} closed at {1}.", new Object[]{this, new Date()});
                socket.close();
            }                    
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error while writing to input/output stream.", ex);
        }
    }
    
    
    public Request acceptRequest() throws NoSuchElementException, RequestSyntaxException{        
        String message = sc.next();
        return new Request(message);        
    }

    protected InputStream getIn() {
        return in;
    }

    protected void setIn(InputStream in) {
        this.in = in;
    }

    protected OutputStream getOut() {
        return out;
    }

    protected void setOut(OutputStream out) {
        this.out = out;
    }
        
    @Override
    public String toString() {
        return name;        
    }
    
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

    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
}
