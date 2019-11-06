/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.states;

import cz.cvut.fel.psi.tcpserver.Request;
import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;
import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.util.NoSuchElementException;

/**
 *
 * @author Matej
 */
public class AcceptingPassword extends Response{
    private boolean invalidUsername;

    public AcceptingPassword(Session session) {
        this(session, false);
    }

    public AcceptingPassword(Session session, boolean invalidUsername) {
        super(session, 201, "password");
        this.invalidUsername = invalidUsername;
    }
    
    private boolean validateUser(Request req) {
        if(invalidUsername){
            return false;
        } else {
            return session.getUser().getPassword().equals(req.getData());
        }
    }
        
    @Override
    public Response next(){
        try {
            Request req = session.acceptRequest();
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
            
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public boolean isInvalidUsername() {
        return invalidUsername;
    }   
}
