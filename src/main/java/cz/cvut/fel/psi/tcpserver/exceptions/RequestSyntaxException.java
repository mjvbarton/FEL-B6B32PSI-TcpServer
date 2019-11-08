package cz.cvut.fel.psi.tcpserver.exceptions;

/**
 * Exception used when the request syntax does not match.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class RequestSyntaxException extends Exception{
    /**
     * Creates new {@code RequestSyntaxException} with specified message
     * @param message exception message
     */
    public RequestSyntaxException(String message) {
        super(message);
    }    
}
