package cz.cvut.fel.psi.tcpserver.exceptions;

/**
 * Severe exception occured during session runtime.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class SessionRunException extends Exception {
    
    /**
     * Creates new {@code SessionRunException} with error message and cause specified.
     * @param message error message
     * @param cause the cause of {@code SessionRunException}
     */
    public SessionRunException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
}
