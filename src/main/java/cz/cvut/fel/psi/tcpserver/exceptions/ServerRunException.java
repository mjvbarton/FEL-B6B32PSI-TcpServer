package cz.cvut.fel.psi.tcpserver.exceptions;

/**
 * Exception occured during server runtime.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class ServerRunException extends Exception {

    /**
     * Creates a new instance of <code>ServerRunException</code> without detail
     * message.
     */
    public ServerRunException() {
    }

    /**
     * Constructs an instance of <code>ServerRunException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ServerRunException(String msg) {
        super(msg);
    }

    public ServerRunException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
}
