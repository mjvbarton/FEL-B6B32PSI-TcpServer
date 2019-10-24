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
public class RequestSyntaxException extends Exception{

    public RequestSyntaxException(String message) {
        super(message);
    }    
}
