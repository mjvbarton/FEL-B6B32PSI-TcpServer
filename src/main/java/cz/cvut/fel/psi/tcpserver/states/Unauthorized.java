/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.states;

import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.SessionRunException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matej
 */
public class Unauthorized extends Response {

    public Unauthorized(Session session) {
        super(session, 500, "login failed");
    }

    @Override
    public Response next() {
        try {
            session.sendResponse(this);            
            return null;
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            return null;
        }
    }    
}
