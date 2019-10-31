/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.states;

import cz.cvut.fel.psi.tcpserver.Session;

/**
 *
 * @author Matej
 */
public class BadChecksum extends AcceptingMessages {

    public BadChecksum(Session session) {
        super(session, 300, "bad checksum");
    }    
}
