/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.responses;

import cz.cvut.fel.psi.tcpserver.Request;
import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;
import cz.cvut.fel.psi.tcpserver.User;
import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.net.SocketTimeoutException;
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
            try{
                Request req = session.acceptRequest();
                session.setUser(new User(req));            
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
