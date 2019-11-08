package cz.cvut.fel.psi.tcpserver.responses;

import cz.cvut.fel.psi.tcpserver.requests.Request;
import cz.cvut.fel.psi.tcpserver.requests.RequestType;
import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;
import cz.cvut.fel.psi.tcpserver.exceptions.BadChecksumException;
import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionClosedException;
import cz.cvut.fel.psi.tcpserver.requests.PhotoRequest;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents response {@code '202 OK'}.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class AcceptingMessages extends Response {
    
    /**
     * Creates new {@code AcceptingMessages} response.
     * @param session reference to parent {@code Session}
     */
    public AcceptingMessages(Session session) {
        this(session, 202, "ok");
        
    }
    
    protected AcceptingMessages(Session session, int code, String message) {
        super(session, code, message);
        acceptableRequests.add(RequestType.INFO);
        acceptableRequests.add(RequestType.PHOTO);
    }
       

    @Override
    public Response next() throws SessionClosedException {
        try{
            try {
                Request req = session.acceptRequest();
                if(acceptableRequests.contains(req.getType())){
                    processRequest(req);
                    Response next = new AcceptingMessages(session);
                    session.sendResponse(next);
                    return next;
                } else {
                    return new RequestSyntaxError(session).next();
                }
            } catch (NoSuchElementException ex) {
                return null;

            } catch (RequestSyntaxException ex) {
                return new RequestSyntaxError(session).next();

            } catch (BadChecksumException ex) {
                Response next = new BadChecksum(session);
                session.sendResponse(next);
                return next;
                
            } catch (SocketTimeoutException ex) {
                return new Timeout(session).next();
            }
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    /*
        Processes the request based on its RequestType.
    */
    private void processRequest(Request req) throws BadChecksumException, RequestSyntaxException{
        switch(req.getType()){
            case INFO:
                LOG.log(Level.INFO, "Session {0}: Info message captured: '{1}'", new Object[]{session, req.getData()});
                break;
            
            case PHOTO:
                PhotoRequest phr = (PhotoRequest) req;    
                session.addPhoto(phr.getData());
                break;
            
            default:
                throw new IllegalStateException("Request type must be only of RequestType.INFO or RequestType.PHOTO.");
        }
    }
    
}
