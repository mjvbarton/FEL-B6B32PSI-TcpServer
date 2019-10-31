/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver;

/**
 *
 * @author Matej
 */
@Deprecated
public enum Response_OLD {
    ACCEPTING_USERNAME(200, "LOGIN"),
    ACCEPTING_PASSWORD(201, "PASSWORD"),
    ACCEPTING_MESSAGES(202, "OK"),
    BAD_CHECKSUM(300, "BAD CHECKSUM"),
    UNAUTHORIZED(500, "LOGIN FAILED"),
    SYNTAX_ERROR(501, "SYNTAX ERROR"),
    TIMEOUT(502, "TIMEOUT");
    
    private final String message;
    private final Integer responseCode;
    
    private Response_OLD(int responseCode, String message){
        this.responseCode = responseCode;
        this.message = message;
    }

    @Override
    public String toString() {
        return responseCode.toString() + " " + message + " \r\n";
    }
    
    public int getCode(){
        return responseCode;
    }    
}
