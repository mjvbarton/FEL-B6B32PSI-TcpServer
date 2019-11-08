package cz.cvut.fel.psi.tcpserver.exceptions;

/** 
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class BadChecksumException extends Exception {

    /**
     * Creates a new instance of <code>BadChecksumException</code> without
     * detail message.
     */
    public BadChecksumException() {
    }

    /**
     * Constructs an instance of <code>BadChecksumException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public BadChecksumException(String msg) {
        super(msg);
    }
}
