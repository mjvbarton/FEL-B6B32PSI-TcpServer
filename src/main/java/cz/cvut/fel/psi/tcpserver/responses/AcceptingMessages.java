package cz.cvut.fel.psi.tcpserver.responses;

import cz.cvut.fel.psi.tcpserver.Photo;
import cz.cvut.fel.psi.tcpserver.Request;
import cz.cvut.fel.psi.tcpserver.RequestType;
import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;
import cz.cvut.fel.psi.tcpserver.exceptions.BadChecksumException;
import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matej
 */
public class AcceptingMessages extends Response {

    public AcceptingMessages(Session session) {
        this(session, 202, "ok");
        
    }

    public AcceptingMessages(Session session, int code, String message) {
        super(session, code, message);
        acceptableRequests.add(RequestType.INFO);
        acceptableRequests.add(RequestType.PHOTO);
    }
       

    @Override
    public Response next() {
        try{
            try {
                Request req = session.acceptRequest();
                if(acceptableRequests.contains(req.getType())){
                    processRequest(req);
                    Response next = new AcceptingMessages(session);
                    session.sendResponse(next);
                    return next;
                } else {
                    return new RequestSyntaxError(session);
                }
            } catch (NoSuchElementException ex) {
                return null;

            } catch (RequestSyntaxException ex) {
                return new RequestSyntaxError(session);

            } catch (BadChecksumException ex) {
                Response next = new BadChecksum(session);
                session.sendResponse(next);
                return next;
                
            } catch (SocketTimeoutException ex) {
                return new Timeout(session);
            }     
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void processRequest(Request req) throws BadChecksumException, RequestSyntaxException{
        switch(req.getType()){
            case INFO:
                break;
            
            case PHOTO:
                session.addPhoto(new Photo(req, session));
                break;
            
            default:
                throw new IllegalStateException("Request type must be only of RequestType.INFO or RequestType.PHOTO.");
        }
    }
    
}
