/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Matej
 */
public class Robot {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ServerRunException {
        // TODO code application logic here
        Server.main(args);
    }
    
}

/**
 * Represents a session manager of the server.
 *
 * @author Matej
 */
class Server {

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
     *
     * @param args the command line arguments
     * @throws cz.cvut.fel.psi.tcpserver.exceptions.ServerRunException if the
     * process fails
     */
    public static void main(String[] args) throws ServerRunException {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);
        for (Handler h : rootLogger.getHandlers()) {
            h.setLevel(Level.INFO);
        }
        int port;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
                if (port < 1024) {
                    throw new ServerRunException("Please enter port number higher than 1024.");
                }
            } catch (NumberFormatException ex) {
                throw new ServerRunException("Please enter valid port number.", ex);
            }
        } else {
            port = DEFAULT_PORT_NUMBER;
        }

        Server srv = new Server(port);
        LOG.log(Level.INFO, "Started server at port {0}", port);
        while (true) {
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
    public void acceptSession() throws IOException {
        Socket socket = srvSocket.accept();
        Session session = new Session(socket, this);
        session.start();
    }

    /**
     * Saves photos given at the server.
     *
     * @param photos list of photos to be saved at the server
     * @throws cz.cvut.fel.psi.tcpserver.exceptions.ServerRunException
     */
    public synchronized void savePhotos(List<Photo> photos) throws ServerRunException {
        throw new UnsupportedOperationException("Method not implemented yet. Needed to be consulted.");
    }
}

/**
 *
 * @author Matej
 */
class Photo {

    private final int size;
    private final byte[] photo;
    private final Integer checksum;

    public Photo(Request request, Session session) throws RequestSyntaxException {
        String[] data = request.getData().split("\\s");
        size = Integer.parseInt(data[0]);
        String[] nextData = data[1].split("\\\\x");
        photo = nextData[0].getBytes();
        String rawChecksum = "";
        for (int i = 1; i <= 4; i++) {
            rawChecksum += nextData[i];
        }
        checksum = Integer.parseInt(rawChecksum, 16);
    }

    public boolean validateChecksum() {
        int result = 0;
        for (byte b : photo) {
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

    void flush() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

/**
 *
 * @author Matej
 */
class Request {

    private final RequestType type;
    private final String data;

    public Request(String rawRequest) throws RequestSyntaxException {
        this.type = resolveType(rawRequest);
        this.data = resolveData(this.type, rawRequest);
    }

    private RequestType resolveType(String rawRequest) throws RequestSyntaxException {
        RequestType result = null;
        for (RequestType reqType : RequestType.values()) {
            if (reqType.checkSyntax(rawRequest)) {
                result = reqType;
                break;
            }
        }
        if (result == null) {
            throw new RequestSyntaxException("Wrong input format for raw request " + rawRequest);
        } else {
            return result;
        }
    }

    private String resolveData(RequestType type, String rawRequest) {
        switch (type) {
            case INFO:
            case PHOTO:
                String[] parsedRequest = rawRequest.split("^[A-Z]+\\s");
                return parsedRequest.length > 1 ? parsedRequest[1] : "";
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

/**
 *
 * @author Matej
 */
enum RequestType {

    USERNAME("(^Robot)(\\s*\\w+)"),
    PASSWORD("(^\\d+$)"),
    INFO("(^INFO)\\s([^\\r\\n])*"),
    PHOTO("^FOTO\\s\\d+\\s[^\\s]+$");

    private final Pattern syntax;

    private RequestType(String syntax) {
        this.syntax = Pattern.compile(syntax);

    }

    public boolean checkSyntax(String message) {
        Matcher m = syntax.matcher(message);
        return m.find();
    }
    
}

/**
 *
 * @author Matej
 */
class Session extends Thread implements Serializable{
    private static final Logger LOG = Logger.getLogger(Session.class.getName());    
    
    private Server srv;    
    private Socket socket; 
    private InputStream in;
    private OutputStream out;    
    protected Date established;
    private final String name;
    private boolean isRunning;
    
    private Scanner sc;
    
    private final List<Photo> photos = new ArrayList();   
    
    private User user;
       
    
    /**
     * Initializes new session with specified socket and reference to server where the session is run.
     * @param socket socket extracted from {@link Server.srvSocket}
     * @param server reference to server
     * @throws IOException if the session is not able to read/write to socket
     */
    public Session(Socket socket, Server server) throws IOException{
        super();                
        established = new Date();
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
        name = generateName();        
        srv = server;
        sc = new Scanner(in).useDelimiter("\\r\\n");       
    }
    
    /**
     * Represents the session runtime
     */
    @Override
    public void run(){                    
        LOG.log(Level.INFO, "Connected session {0} to socket: {1}", new Object[]{this, socket.getPort()});
        LOG.log(Level.FINE, "Session {0} established: {1}", new Object[]{this, established});
        isRunning = true;
        try{
            socket.setSoTimeout(Server.SESSION_TIMEOUT_SECONDS * 1000);
            Response response = new AcceptingUsername(this);
            sendResponse(response);            
            while((response = response.next()) != null && socket.isConnected() && isRunning){         
                LOG.log(Level.FINEST, "Session {0} response: {1}", new Object[]{this, response});
                LOG.log(Level.FINEST, "Session {0} socket is connected {1}", new Object[]{this, socket.isConnected()});
            }
            in.read();
            close();
        } catch (SessionRunException ex){
            ex.printStackTrace(); 
            
        } catch (IOException ex) {
            if(ex instanceof SocketTimeoutException){
                try {
                    Response response = new Timeout(this);
                    response.next();
                    close();
                } catch (SessionRunException ex1) {
                    ex.printStackTrace();
                }
            } else {
                LOG.log(Level.SEVERE, "Unknown error catched. {0}", ex);
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Closes the session
     * @throws cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException
     */
    public synchronized void close() throws SessionRunException{
        try{
            if(isRunning){
                LOG.log(Level.INFO, "Session {0}: Closing session.", this);
                isRunning = false;
                in.close();
                out.close();
                socket.close();
            }
        }   catch (IOException ex) {
            LOG.log(Level.SEVERE, "Cannot close Session {0}", this);
            throw new SessionRunException("Cannot close session " + this, ex);
        }
    }
    
    /**
     * Sends response to client
     * @param response response to be sent. 
     * @throws SessionRunException when the process fails
     */
    public void sendResponse(Response response) throws SessionRunException{        
        try {
            out.write(response.toString().getBytes());
            LOG.log(Level.FINEST, "Sesion {0} response message sent: {1}", new Object[]{this, response});
        } catch (IOException ex) {
            throw new SessionRunException("Cannot send response.", ex);
        }
    }
    
    /**
     * Scans the input stream until there is a request to capture.
     * @return new request captured from Socket.inputStream     
     * @throws NoSuchElementException if there is no element
     * @throws RequestSyntaxException if the syntax does not match
     */
    public Request acceptRequest() throws NoSuchElementException, RequestSyntaxException{        
        String message = sc.next();
        LOG.log(Level.FINEST, "Session {0}: Read raw message {1}", new Object[]{this, message});
        return new Request(message);        
    }

    /**
     * <i>Method for testing.</i> Returns the input stream of the session.
     * @return input stream of the session
     */
    protected InputStream getIn() {
        return in;
    }

    /**
     * <i>Method for testing.</i> Sets the input stream of the session.
     * @param in input stream to mock the session.
     */
    protected void setIn(InputStream in) {
        this.in = in;
    }

    /**
     * <i>Method for testing.</i> Returns the output stream of the session.
     * @return output stream of the session
     */
    protected OutputStream getOut() {
        return out;
    }

    /**
     * <i>Method for testing.</i> Sets the output stream of the session.
     * @param out output stream to mock the session.
     */
    protected void setOut(OutputStream out) {
        this.out = out;
    }
    
    /**
     * Returns hashed session name from established date and socket local port number.
     * @return session name
     */
    @Override
    public String toString() {
        return name;        
    }
    
    /**
     * Generates session name as a MD5 hash of Session.established, Session.socket.localPort
     * @return hashed String
     */
    private String generateName(){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            Random randomizer = new Random();
            md.update(("Session " + socket.getLocalPort() + " " + established + randomizer.nextInt()).getBytes());
            byte[] digest = md.digest();
            //return Base64.getEncoder().encodeToString(digest).toUpperCase();
            return new String(digest).toUpperCase();
        } catch (NoSuchAlgorithmException ex) {
            LOG.log(Level.SEVERE, "Something is terribly wrong!!!", ex);
            throw new RuntimeException("Session.generateName() failed. " + ex);
        }
    }

    /**
     * Returns hashCode of Session.name
     * @return hashCode of Session.name
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
        
    /**
     * Gets logged in user of the session
     * @return user object or null if not initialised yet
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets user to the session
     * @param user to be set
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    /**
     * Adds photo to session save queue. The save of photo is currenty temporary.
     * @param photo retrieved from request
     * @throws BadChecksumException when the photo is not complete
     */
    public void addPhoto(Photo photo) throws BadChecksumException{
        if(photo.validateChecksum()){
            LOG.log(Level.INFO, "Session {0}: Photo {1} uploaded.", new Object[]{this, photo});
            //photos.add(photo);
        } else {
            throw new BadChecksumException("Incomplete transfer for photo " + photo);
        }
    }
    
    /**
     * Return number of photos uploaded by this session.
     * @return number of photos in the buffer
     */
    public int getPhotoCounter(){
        return photos.size();
    }
}

/**
 *
 * @author Matej
 */
class User implements Serializable {

    private final String username;

    public User(String username) {
        this.username = username;
    }

    public User(Request request) {
        if (request.getType() == RequestType.USERNAME) {
            this.username = request.getData();
        } else {
            //throw new IllegalStateException();
            this.username = "";
        }
    }

    public String getPassword() {
        byte[] bytes = username.getBytes();
        int sum = 0;
        for (byte b : bytes) {
            sum += b;
        }
        return Integer.toString(sum);
    }

    @Override
    public String toString() {
        return username;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == username.hashCode();
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}

/**
 *
 * @author Matej
 */
class AcceptingMessages extends Response {

    public AcceptingMessages(Session session) {
        this(session, 202, "ok");

    }

    public AcceptingMessages(Session session, int code, String message) {
        super(session, code, message);
        acceptableRequests.add(RequestType.INFO);
        acceptableRequests.add(RequestType.PHOTO);
    }

    @Override
    public Response next() {
        try {
            try {
                Request req = session.acceptRequest();
                if (acceptableRequests.contains(req.getType())) {
                    processRequest(req);
                    Response next = new AcceptingMessages(session);
                    session.sendResponse(next);
                    return next;
                } else {
                    return new RequestSyntaxError(session);
                }
            } catch (NoSuchElementException ex) {
                return null;

            } catch (RequestSyntaxException ex) {
                return new RequestSyntaxError(session);

            } catch (BadChecksumException ex) {
                Response next = new BadChecksum(session);
                session.sendResponse(next);
                return next;
            }
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void processRequest(Request req) throws BadChecksumException, RequestSyntaxException {
        switch (req.getType()) {
            case INFO:
                break;

            case PHOTO:
                session.addPhoto(new Photo(req, session));
                break;

            default:
                throw new IllegalStateException("Request type must be only of RequestType.INFO or RequestType.PHOTO.");
        }
    }

}

/**
 *
 * @author Matej
 */
class AcceptingPassword extends Response {

    private boolean invalidUsername;

    public AcceptingPassword(Session session) {
        this(session, false);
    }

    public AcceptingPassword(Session session, boolean invalidUsername) {
        super(session, 201, "password");
        this.invalidUsername = invalidUsername;
    }

    private boolean validateUser(Request req) {
        if (invalidUsername) {
            return false;
        } else {
            return session.getUser().getPassword().equals(req.getData());
        }
    }

    @Override
    public Response next() {
        try {
            Request req = session.acceptRequest();
            if (validateUser(req)) {
                Response next = new AcceptingMessages(session);
                session.sendResponse(next);
                return next;
            } else {
                return new Unauthorized(session);
            }
        } catch (NoSuchElementException ex) {
            return null;

        } catch (RequestSyntaxException ex) {
            return new Unauthorized(session);

        } catch (SessionRunException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean isInvalidUsername() {
        return invalidUsername;
    }
}

/**
 *
 * @author Matej
 */
class AcceptingUsername extends Response {

    public AcceptingUsername(Session session) {
        super(session, 200, "login");
    }

    @Override
    public Response next() {
        try {
            try {
                Request req = session.acceptRequest();
                session.setUser(new User(req));
                Response next = new AcceptingPassword(session);
                session.sendResponse(next);
                return next;

            } catch (NoSuchElementException ex) {
                return null;

            } catch (RequestSyntaxException ex) {
                Response next = new AcceptingPassword(session);
                session.sendResponse(next);
                return new AcceptingPassword(session, true);
            }
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}

/**
 *
 * @author Matej
 */
class BadChecksum extends AcceptingMessages {

    public BadChecksum(Session session) {
        super(session, 300, "bad checksum");
    }
}

/**
 *
 * @author Matej
 */
class RequestSyntaxError extends Response {

    public RequestSyntaxError(Session session) {
        super(session, 501, "syntax error");
    }

    @Override
    public Response next() {
        try {
            session.sendResponse(this);
            return null;
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            return null;
        }
    }

}

/**
 * Response from the state machine
 *
 * @author Matej
 */
abstract class Response {

    protected final List<RequestType> acceptableRequests = new ArrayList();

    protected int code;
    protected String message;
    protected Session session;

    protected Response(Session session, int code, String message) {
        this.code = code;
        this.message = message;
        this.session = session;
    }

    /**
     * Generates the message in valid syntax for client
     *
     * @return message for client
     */
    @Override
    public String toString() {
        return code + " " + message.toUpperCase() + "\r\n";
    }

    /**
     * Gets next step in valid sequence
     *
     * @return next state or null if the state is final
     */
    public abstract Response next();

}

/**
 *
 * @author Matej
 */
class Timeout extends Response {

    public Timeout(Session session) {
        super(session, 502, "timeout");
    }

    @Override
    public Response next() {
        try {
            session.sendResponse(this);
            return null;
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            return null;
        }
    }

}


/**
 *
 * @author Matej
 */
class Unauthorized extends Response {

    public Unauthorized(Session session) {
        super(session, 500, "login failed");
    }

    @Override
    public Response next() {
        try {
            session.sendResponse(this);
            return null;
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}

/**
 *
 * @author Matej
 */
class BadChecksumException extends Exception {

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

/**
 *
 * @author Matej
 */
class RequestSyntaxException extends Exception {

    public RequestSyntaxException(String message) {
        super(message);
    }
}

/**
 *
 * @author Matej
 */
class ServerRunException extends Exception {

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

/**
 *
 * @author Matej
 */
class SessionRunException extends Exception {

    public SessionRunException(String message, Throwable cause) {
        super(message, cause);
    }

}

/**
 *
 * @author Matej
 */
class UnauthenticatedException extends Exception {

    /**
     * Creates a new instance of <code>UnauthenticatedException</code> without
     * detail message.
     */
    public UnauthenticatedException() {
    }

    /**
     * Constructs an instance of <code>UnauthenticatedException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public UnauthenticatedException(String msg) {
        super(msg);
    }
}
