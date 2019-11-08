package cz.cvut.fel.psi.tcpserver.exceptions;

/**
 * This exception is thrown when there is a need to close running session.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class SessionClosedException extends Exception{
    
    /**
     * Creates new {@code SessionClosedException} with error message specified.
     * @param message error message
     */
    public SessionClosedException(String message) {
        super(message);
    }

    /**
     * Creates new {@code SessionClosedException} with error message and cause specified.
     * @param message error message
     * @param cause the cause of session closure
     */
    public SessionClosedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
}
