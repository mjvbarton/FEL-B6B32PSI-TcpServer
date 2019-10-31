/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.states;

import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matej
 */
public class RequestSyntaxError extends Response{
    public RequestSyntaxError(Session session){
        super(session, 501, "syntax error");
    }
    
    @Override
    public Response next(){
        try {
            session.sendResponse(this);
            return null;
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
}
