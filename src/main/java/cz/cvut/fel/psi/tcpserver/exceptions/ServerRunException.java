/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.exceptions;

/**
 *
 * @author Matej
 */
public class ServerRunException extends Exception {

    /**
     * Creates a new instance of <code>ServerRunException</code> without detail
     * message.
     */
    public ServerRunException() {
    }

    /**
     * Constructs an instance of <code>ServerRunException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ServerRunException(String msg) {
        super(msg);
    }

    public ServerRunException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
}
