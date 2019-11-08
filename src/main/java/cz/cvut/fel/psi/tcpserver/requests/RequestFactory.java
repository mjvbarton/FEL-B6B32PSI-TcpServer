/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.requests;

import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Factory for generating requests.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class RequestFactory {
    private InputStream in;

    /**
     * Creates new {@code RequestFactory} with {@code InputStream} from the session
     * @param in session's input stream 
     */
    public RequestFactory(InputStream in) {
        this.in = in;
    }
            
    /**
     * Generates new {@code Request} object based on the keyword given. 
     * <p>
     * When the keyword does not match a {@code RequestType.PASSWORD} is 
     * returned by default.
     * @param keyword keyword of the {@code RequestType} retrieved from client's message
     * @param offset helper to provide continuity of reading for {@code CrLfRequest}
     * @return request based on keyword
     * @throws RequestSyntaxException if the request does not follow syntax rules
     * @throws IOException when process fails
     */
    public Request parseRequest(String keyword, RequestOffset offset) throws RequestSyntaxException, IOException{
        RequestType req = RequestType.getFromKeyword(keyword);
        switch(req){
            case INFO:
                return getInfoMessageRequest(offset);
                
            case PHOTO:
                return getPhotoRequest();
                
            case PASSWORD:
                return getPasswordRequest(offset);
                
            case USERNAME:
                return getUsernameRequest(offset);
                
            default:
                throw new IllegalStateException("Application should not be in such a state.");
        }
    }

    /**
     * Creates new {@code InfoMessageRequest} object with offset given.
     * @param offset helper to provide continuity of reading for {@code CrLfRequest}
     * @return info message request object
     * @throws RequestSyntaxException when the request does not follow its syntax rules
     * @throws IOException when the process fails
     */
    public Request getInfoMessageRequest(RequestOffset offset) throws RequestSyntaxException, IOException {
        Request req = new InfoMessageRequest(offset);
        req.loadData(in);
        return req;
    }

    /**
     * Creates new {@code PasswordRequest} object with offset given.
     * @param offset helper to provide continuity of reading for {@code CrLfRequest}
     * @return password request object
     * @throws RequestSyntaxException when the request does not follow its syntax rules
     * @throws IOException when the process fails
     */
    public Request getPasswordRequest(RequestOffset offset) throws RequestSyntaxException, IOException {
        Request req = new PasswordRequest(offset);
        req.loadData(in);
        return req;
    }

    /**
     * Creates new {@code UsernameRequest} object with offset given.
     * @param offset helper to provide continuity of reading for {@code CrLfRequest}
     * @return username request object
     * @throws RequestSyntaxException when the request does not follow its syntax rules
     * @throws IOException when the process fails
     */
    public Request getUsernameRequest(RequestOffset offset) throws RequestSyntaxException, IOException {
        Request req = new UsernameRequest(offset);
        req.loadData(in);
        return req;
    }
    
    /**
     * Creates new {@code PhotoRequest} object.
     * @return photo request object
     * @throws RequestSyntaxException when the request does not follow its syntax rules
     * @throws IOException when the process fails
     */
    public Request getPhotoRequest() throws RequestSyntaxException, IOException{
        Request req = new PhotoRequest();
        req.loadData(in);
        return req;
    }
}
