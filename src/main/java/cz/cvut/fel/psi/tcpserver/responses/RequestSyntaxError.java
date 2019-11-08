package cz.cvut.fel.psi.tcpserver.responses;

import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionClosedException;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents response {@code '501 SYNTAX ERROR'}
 * <p>
 * As this is an error response it leads to cause of {@code Session.close()} by throwing {@code SessionClosedException}.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class RequestSyntaxError extends Response{
    public RequestSyntaxError(Session session){
        super(session, 501, "syntax error");
    }
    
    @Override
    public Response next() throws SessionClosedException{
        try {
            session.sendResponse(this);
            throw new SessionClosedException("Session needs to be closed.");
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            throw new SessionClosedException("Session needs to be closed because of an error.", ex);
        }
    }
    
}
