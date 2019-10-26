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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matej
 */
public class Session extends Thread implements Serializable{
    private static final Logger LOG = Logger.getLogger(Session.class.getName());
    private static int TIMEOUT_SECONDS = 0;
    
    private Socket socket; 
    private InputStream in;
    private OutputStream out;    
    private final Date established;   
    
    private User user;
    
    private Response state;
        
    public Session(Socket socket) throws IOException{
        super();
        state = ACCEPTING_USERNAME;
        established = new Date();
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
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
                        && socket.isInputShutdown()) {
                    req = acceptRequest();
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
                state = SYNTAX_ERROR;
                out.write(state.toString().getBytes());
                
            } finally {                                
                LOG.log(Level.INFO, "Session {0} closed at {1}.", new Object[]{this, new Date()});
                socket.close();
            }
        
            
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error while writing to input/output stream.", ex);
        }
    }
    
    
    public Request acceptRequest() throws RequestSyntaxException{
        
    } 

    @Override
    public String toString() {
        
    }

    @Override
    public int hashCode() {
        return super.hashCode(); //To change body of generated methods, choose Tools | Templates.
    }
    
}
