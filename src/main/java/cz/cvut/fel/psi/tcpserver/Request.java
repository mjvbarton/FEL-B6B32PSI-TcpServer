/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver;

import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.util.InputMismatchException;

/**
 *
 * @author Matej
 */
public class Request {
    
    private final RequestType type;    
    private final String data;
    
    public Request(String rawRequest) throws RequestSyntaxException{
        this.type = resolveType(rawRequest);
        this.data = resolveData(this.type, rawRequest);
    }
    
    private RequestType resolveType(String rawRequest) throws RequestSyntaxException{
        RequestType result = null;
        for(RequestType reqType : RequestType.values()){
            if(reqType.checkSyntax(rawRequest)){
                result = reqType;
                break;
            }
        }
        if(result == null){
            throw new RequestSyntaxException("Wrong input format for raw request " + rawRequest);
        } else {
            return result;
        }        
    }
    
    private String resolveData(RequestType type, String rawRequest){
        switch(type){
            case INFO:                
            case PHOTO:                
                return rawRequest.split("^[a-zA-z]+\\s")[1];
            case USERNAME:
            case PASSWORD:
                return rawRequest;
            default:
                throw new InputMismatchException("Uknown type for " + type);
        }
    }

    public RequestType getType() {
        return type;
    }

    public String getData() {
        return data;
    }    
}
