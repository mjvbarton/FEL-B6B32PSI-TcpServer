package cz.cvut.fel.psi.tcpserver.responses;

import cz.cvut.fel.psi.tcpserver.requests.Request;
import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;
import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionClosedException;
import cz.cvut.fel.psi.tcpserver.requests.PasswordRequest;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;

/**
 * Represents response {@code '201 PASSWORD'}
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class AcceptingPassword extends Response{
    private boolean invalidUsername;

    /**
     * Creates new {@code AcceptingPassword} with {@code invalidUsername} flag set to {@code false}.
     * @param session 
     */
    public AcceptingPassword(Session session) {
        this(session, false);
    }

    /**
     * Creates new {@code AcceptingPassword} with {@code invalidUsername} flag specified.
     * @param session reference to parent {@code Session}
     * @param invalidUsername boolean flag determining if the validation of username failed in {@code AcceptingUsername}
     */
    public AcceptingPassword(Session session, boolean invalidUsername) {
        super(session, 201, "password");
        this.invalidUsername = invalidUsername;
    }
    
    /*
        Validates user's password
    */
    private boolean validateUser(Request req) {
        if(invalidUsername){
            return false;
        } else {
            return session.getUser().getPassword().equals(req.getData());
        }
    }
        
    @Override
    public Response next() throws SessionClosedException{
        try {
            Request req = session.acceptRequest();
            PasswordRequest prq = (PasswordRequest) req;
            if(validateUser(req)){
                Response next = new AcceptingMessages(session);
                session.sendResponse(next);
                return next;
            } else {
                return new Unauthorized(session);
            }
        } catch (NoSuchElementException ex) {            
            return null;
            
        } catch (RequestSyntaxException ex) {
            return new Unauthorized(session);
            
        } catch (SocketTimeoutException ex) {
            return new Timeout(session);
            
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    /**
    * Returns the {@code invalidUsername} flag.
    * @return the flag as is
    */    
    public boolean isInvalidUsername() {
        return invalidUsername;
    }   
}
