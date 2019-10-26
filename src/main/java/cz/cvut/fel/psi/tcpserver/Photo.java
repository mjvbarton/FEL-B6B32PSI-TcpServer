/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver;

import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.util.regex.Pattern;

/**
 *
 * @author Matej
 */
public class Photo {
    private final int size;
    private byte[] photo;
    private Integer checksum;
            
    public Photo(Request request) throws RequestSyntaxException{
        String[] data = request.getData().split("\\s");
        size = Integer.parseInt(data[0]);
        String[] nextData = data[1].split("\\\\x");
        photo = nextData[0].getBytes();
        String rawChecksum = "";
        for(int i = 1; i <= 4; i++){
            rawChecksum += nextData[i];
        }
        checksum = Integer.parseInt(rawChecksum, 16);                       
    }       
            
    public boolean validateChecksum(){
        int result = 0;
        for(byte b : photo){
            int i = b;
            result += i;
        }
        return checksum.equals(result);
    }

    public int getSize() {
        return size;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public Integer getChecksum() {
        return checksum;
    }        
}