/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.responses;

/**
 *
 * @author Matej
 */
public class NoNextResponseException extends Exception {

    /**
     * Creates a new instance of <code>NoNextResponseException</code> without
     * detail message.
     */
    public NoNextResponseException() {
    }

    /**
     * Constructs an instance of <code>NoNextResponseException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public NoNextResponseException(String msg) {
        super(msg);
    }
}
