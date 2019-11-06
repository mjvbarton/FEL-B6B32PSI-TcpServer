package cz.cvut.fel.psi.tcpserver;

import cz.cvut.fel.psi.tcpserver.exceptions.ServerRunException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a session manager of the server.
 * @author Matej
 */
public class Server {
    public static final Logger LOG = Logger.getLogger(Server.class.getName());
    public static final int SESSION_TIMEOUT_SECONDS = 45;
    
    public static final String FILE_STORAGE_PATH = "";
    public static final int DEFAULT_PORT_NUMBER = 3999;

    private final int serverPort;
    private ServerSocket srvSocket;

    public Server(int serverPort) throws ServerRunException {
        this.serverPort = serverPort;
        try {
            srvSocket = new ServerSocket(serverPort);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Unable to start server.");
            throw new ServerRunException("Unable to start server", ex);
        }
    }
           
    /**
     * Runs the program.
     * @param args the command line arguments
     * @throws cz.cvut.fel.psi.tcpserver.exceptions.ServerRunException if the process fails
     */
    public static void main(String[] args) throws ServerRunException {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.FINEST);
        for(Handler h : rootLogger.getHandlers()){
            h.setLevel(Level.FINEST);
        }
        int port;
        if(args.length > 0){
            try{
                port = Integer.parseInt(args[0]);
                if(port < 1024){
                    throw new ServerRunException("Please enter port number higher than 1024.");
                }
            } catch(NumberFormatException ex){
                throw new ServerRunException("Please enter valid port number.", ex);
            }
        } else {
            port = DEFAULT_PORT_NUMBER;
        }
        
        Server srv = new Server(port);
        LOG.log(Level.INFO, "Started server at port {0}", port);
        while(true){
            try {
                srv.acceptSession();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Cannot read or write to server socket.", ex);                
                throw new ServerRunException("Cannot accept session.", ex);
            }
        }
        
    }
    
    /**
     * Creates new session at the server.
     */
    public void acceptSession() throws IOException{
        Socket socket = srvSocket.accept();
        Session session = new Session(socket, this);
        session.start();        
    }
    
    /**
     * Saves photos given at the server.
     * @param photos list of photos to be saved at the server
     * @throws cz.cvut.fel.psi.tcpserver.exceptions.ServerRunException
     */
    public synchronized void savePhotos(List<Photo> photos) throws ServerRunException{
        throw new UnsupportedOperationException("Method not implemented yet. Needed to be consulted.");
    }    
}
