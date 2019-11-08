package cz.cvut.fel.psi.tcpserver.responses;

import cz.cvut.fel.psi.tcpserver.requests.Request;
import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;
import cz.cvut.fel.psi.tcpserver.User;
import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionClosedException;
import cz.cvut.fel.psi.tcpserver.requests.UsernameRequest;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents response {@code '200 LOGIN'}
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class AcceptingUsername extends Response{

    /**
     * Creates new {@code AcceptingUsername} response.
     * @param session reference to parent {@code Session}
     */
    public AcceptingUsername(Session session) {
        super(session, 200, "login");
    }        
            
    @Override
    public Response next() throws SessionClosedException{
        try {
            try{
                Request req = session.acceptRequest();
                UsernameRequest urq = (UsernameRequest) req;                
                session.setUser(urq.getData());                 
                Response next = new AcceptingPassword(session);
                session.sendResponse(next);
                return next;
            
            } catch (NoSuchElementException ex) {
                return null;

            } catch (RequestSyntaxException ex) {                
                Response next = new AcceptingPassword(session);
                session.sendResponse(next);
                return new AcceptingPassword(session, true);
                
            } catch (SocketTimeoutException ex) {
                return new Timeout(session);
            }
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            return null;
        }
    }   
}
