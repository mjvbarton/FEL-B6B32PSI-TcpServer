package cz.cvut.fel.psi.tcpserver.responses;

import cz.cvut.fel.psi.tcpserver.Session;

/**
 * Represents response {@code '300 BAD CHECKSUM'}
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class BadChecksum extends AcceptingMessages {

    /**
     * Creates new {@code BadChecksum} response.
     * @param session reference to parent {@code Session}
     */
    public BadChecksum(Session session) {
        super(session, 300, "bad checksum");
    }    
}
