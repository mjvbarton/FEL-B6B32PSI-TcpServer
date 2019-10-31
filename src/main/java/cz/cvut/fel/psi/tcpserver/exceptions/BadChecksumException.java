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
public class BadChecksumException extends Exception {

    /**
     * Creates a new instance of <code>BadChecksumException</code> without
     * detail message.
     */
    public BadChecksumException() {
    }

    /**
     * Constructs an instance of <code>BadChecksumException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public BadChecksumException(String msg) {
        super(msg);
    }
}
