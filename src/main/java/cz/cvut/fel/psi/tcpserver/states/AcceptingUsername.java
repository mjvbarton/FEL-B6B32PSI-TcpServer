/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.states;

import cz.cvut.fel.psi.tcpserver.Request;
import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.SessionRunException;
import cz.cvut.fel.psi.tcpserver.User;
import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matej
 */
public class AcceptingUsername extends Response{

    public AcceptingUsername(Session session) {
        super(session, 200, "login");
    }        
            
    @Override
    public Response next(){
        try {
            Request req = session.acceptRequest();
            session.setUser(new User(req));            
            Response next = new AcceptingPassword(session);
            session.sendResponse(next);
            return next;
            
        } catch (NoSuchElementException ex) {
            return this;
            
        } catch (RequestSyntaxException ex) {
            //session.setUser(new User(""));
            return new AcceptingPassword(session, true);
            
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            return null;
        }
    }   
}
