/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.states;

import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;

/**
 *
 * @author Matej
 */
public class Timeout extends Response{

    public Timeout(Session session) {
        super(session, 502, "timeout");
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
