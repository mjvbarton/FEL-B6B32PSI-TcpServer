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
public class UnauthenticatedException extends Exception {

    /**
     * Creates a new instance of <code>UnauthenticatedException</code> without
     * detail message.
     */
    public UnauthenticatedException() {
    }

    /**
     * Constructs an instance of <code>UnauthenticatedException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public UnauthenticatedException(String msg) {
        super(msg);
    }
}
