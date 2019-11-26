package robot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import org.apache.commons.lang.ArrayUtils; -- unused due to old Java version being used at Baryk server

/**
 * Represents the server.
 * 
 * @author Matej Barton (bartom47@fel.cvut.cz)
 */
public class Robot {
    /**
     * Line separator specification.
     */
    public static final String LINE_SEPARATOR = "\r\n";
    
    /**
     * Default port specification. Default value should be {@code 3999}.
     */
    public static final int DEFAULT_PORT = 3999;
    
    /**
     * Default timeout in seconds.
     */
    public static final int DEFAULT_TIMEOUT_SECONDS = 45;
    
    /**
     * Actual timeout in seconds set by the server startup.
     */
    public static int TIMEOUT_SECONDS = DEFAULT_TIMEOUT_SECONDS;
    
    /**
     * Running flag for testing purposes.
     */
    private static boolean isRunning;
        
    private static final Logger LOG = Logger.getLogger(Robot.class.getName());

    /**
     * @param args the command line arguments {@code <serverPortNumber> <timeout>}     
     *   
     */
    public static void main(String[] args){
       
        // Logger settings
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.INFO);
        for (Handler h : rootLogger.getHandlers()) {
            h.setLevel(Level.INFO);
        }
        
        int serverPort;
        if(args.length >= 1){            
            serverPort = Integer.parseInt(args[0]);
            LOG.log(Level.FINEST, "Initialized server with server port {0}", serverPort);
            if(args.length == 2){
                TIMEOUT_SECONDS = Integer.parseInt(args[1]);
                if(TIMEOUT_SECONDS < 0){
                    LOG.log(Level.SEVERE, "Invalid argument. Timeout must be greater or equal to zero");
                    throw new IllegalArgumentException("Argument must be greater than or equal to zero");
                }
                LOG.log(Level.FINEST, "Used custom timeout for sessions of {0} seconds.", TIMEOUT_SECONDS);
            }
            
            if(args.length > 2){
                LOG.log(Level.SEVERE, "Invalid arguments for the server found.");
                throw new IllegalArgumentException("Invalid argument captured");
            }
        } else {
            serverPort = DEFAULT_PORT;
            LOG.log(Level.FINEST, "Initialized server with default server port and default timeout.");
        }
        
        try {
            final ServerSocket serverSocket = new ServerSocket(serverPort);
            isRunning = true;
            LOG.log(Level.INFO, "Started server at port {0}", serverPort);
            
            while (isRunning) {
                Session session = new Session(serverSocket.accept());                
                Thread thread = new Thread(session);
                thread.start();
                LOG.log(Level.FINEST, "Started new session");
            }
        } catch (IOException iOException) {
            LOG.log(Level.SEVERE, "Exception {0} occured while running the server.", iOException);
            throw new RuntimeException("Error while running the server", iOException);
        }
    }

    /**
     * Stop method of the server. Used only for testing purposes.
     */
    public static void stop(){        
        isRunning = false;
        LOG.log(Level.INFO, "Server stopped from the runtime.");
    }
}

/**
 * Represents the client's session.
 * 
 * @author Matej Barton (bartom47@fel.cvut.cz)
 */
class Session implements Runnable{
    private static Logger LOG = Logger.getLogger(Session.class.getName());
    private Socket socket;
    //private BufferedInputStream in;
    private InputStream in;
    private OutputStream out;
    private State state;
    private User user;
    private boolean isRunning;
    
    /**
     * Creates new {@code Session} with socket given by the server.
     * @param socket socket given to session by the server     
     * @throws IOException if the instance is not able to read/write from/to the socket
     */
    public Session(Socket socket) throws IOException{
        state = State.ACCEPTING_USERNAME;
        this.socket = socket;
        in = socket.getInputStream();
        //in = new BufferedInputStream(socket.getInputStream());
        out = socket.getOutputStream();
        this.socket.setSoTimeout(Robot.TIMEOUT_SECONDS * 1000);
        isRunning = true;
    }
    
    @Override
    public void run() {
        LOG.log(Level.INFO, "Session {0} opened at socket {1}", new Object[]{this, socket.getPort()});
        try {
            while (true) {
                out.write(state.getMessage().getBytes());
                LOG.log(Level.FINEST, "Session {0} sent response: {1}", new Object[]{this, state.getMessage()});
                Request req;
                try {                    
                    switch (state) {
                        /*
                        * Processing username message from the client
                        */
                        case ACCEPTING_USERNAME:
                            req = acceptRequest();
                            //LOG.log(Level.FINE, "Session {0} accepted request message: {1}", new Object[]{this, new String(req.getData())});
                            //String username = parseRequestData(req.getData());                            
                            user = new User(req.getIsValid(), req.getChecksum());
                            state = State.ACCEPTING_PASSWORD;
                            //LOG.log(Level.FINE, "Session {0} accepted username {1}.", new Object[]{this, username});
                            break;
                        
                        /*
                        * Processing password message from the client    
                        */
                        case ACCEPTING_PASSWORD:
                            req = acceptRequest();
                            LOG.log(Level.FINEST, "Session {0} accepted request message: {1}", new Object[]{this, new String(req.getData())});
                            String password = parseRequestData(req.getData());
                            /*
                            * Authentication
                            */
                            try {
                                int passcode = Integer.parseInt(password);
                                LOG.log(Level.FINEST, "Session {0} accepted passcode {1}.", new Object[]{this, passcode});
                                if (user.authenticate(passcode)) {
                                    state = State.ACCEPTING_MESSAGES;
                                    LOG.log(Level.INFO, "Session {0} authentication successful.", this);
                                } else {
                                    state = State.LOGIN_FAILED;
                                }                                
                            } catch (NumberFormatException ex) {
                                state = State.LOGIN_FAILED;                                
                            }
                            break;
                        
                        /*
                        * Processing of INFO and PHOTO messages.
                        */
                        case BAD_CHECKSUM:
                        case ACCEPTING_MESSAGES:
                            req = acceptRequest();
                            switch (req.getRequestType()) {
                                case INFO:
                                    state = State.ACCEPTING_MESSAGES;
                                    LOG.log(Level.INFO, "Session {0} accepted info message: {1}", new Object[]{this, new String(req.getData())});
                                    break;
                                
                                case PHOTO:
                                    if (req instanceof Photo) {
                                        LOG.log(Level.FINEST, "Session {0} accepted photo message: {1}", new Object[]{this, new String(req.getData())});
                                        Photo photo = (Photo) req;
                                        if (photo.validate()) {
                                            state = State.ACCEPTING_MESSAGES;
                                        } else {
                                            state = State.BAD_CHECKSUM;
                                        }
                                    } else {
                                        state = State.SYNTAX_ERROR;
                                    }
                                    break;
                                
                                /*
                                * Default point - it is present just for security reasons.
                                */
                                default:
                                    state = State.SYNTAX_ERROR;
                                
                            }
                            break;
                        
                        /*
                        * Errors handling
                        */
                        case LOGIN_FAILED:
                        case SYNTAX_ERROR:                            
                        case TIMEOUT:
                            throw new SessionClosedException("Session closed because by the server.");

                        /*
                        * Safety reasons - if thrown something is terribly wrong
                        */
                        default:
                            throw new IllegalStateException("Illegal state of the state machine.");
                    }
                
                } catch (SyntaxErrorException syntaxErrorException) {     
                    state = State.SYNTAX_ERROR;                    
                    LOG.log(Level.WARNING, "Session {0} catched Syntax Error.", this);
                    
                } catch (SocketTimeoutException socketTimeoutException){                    
                    state = State.TIMEOUT;
                    LOG.log(Level.WARNING, "Session {0} was timeouted.", this);
                }              
            
            }
        } catch (IOException iOException) {
            LOG.log(Level.SEVERE, "Session {0} cannot write to socket due to {1}", new Object[] {this, iOException});
            throw new RuntimeException("Server error. Cannot write to socket.", iOException);        
            
        } catch (SessionClosedException sessionClosedException) {
            try {
                Thread.sleep(100);
                socket.close();
                LOG.log(Level.INFO, "Session {0} closed at port {1}", new Object[]{this, socket.getPort()});
                
            } catch (IOException iOException) {
                LOG.log(Level.SEVERE, "Session {0} cannot close the socket due to {1}", new Object[] {this, iOException});                
                throw new RuntimeException("Server error. Cannot close the socket.", iOException);        
                
            } catch (InterruptedException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            }
                        
        } catch (IllegalStateException illegalStateException) {
            LOG.log(Level.SEVERE, "Illegal state of the Session.run().");
            throw new RuntimeException("Illegal state of the Session.run()", illegalStateException);
        }
    }
    
    /**
     * Parses request data from line request.
     * @param rawData byte array of data
     * @return parsed data as string
     */
    private String parseRequestData(byte[] rawData){
        String data = new String(rawData);
        String[] list = data.split("\r\n");
        return list.length == 0 ? "" : list[0];
    }
    
    /**
     * Accepts request from session's input stream.
     * @return new valid request
     * @throws SyntaxErrorException if the syntax is incorrect
     * @throws SessionClosedException if the session is closed by the client
     * @throws SocketTimeoutException if the timeout expires
     */
    public Request acceptRequest() throws SyntaxErrorException, SessionClosedException, SocketTimeoutException{
        List<Byte> rawMessage = new ArrayList();
        final List<Byte> rawKeyword = new ArrayList();
        final byte[] separator = new byte[2];
        Request request;
        Photo photo = null;
        int photoSeparatorHitCounter = 0;
        RequestType acceptingType = getRequestType();
        int checksm = 0;
        Boolean validationFlag = null;
        LOG.log(Level.FINE, "Session {0}: Started process of accepting new Request.", this);
        
        try {
            while (true) {
                int value = in.read();
                checksm += value;
                
                /*
                * Detection of the client's closure of the session.
                */
                if (value < 0) {
                    throw new SessionClosedException("Socket was closed by the client.");
                }
                
                /*
                * Loading the message without affecting the keyword
                */
                rawMessage.add((byte) value);                                              
                
                /*
                * Loading keyword
                */
                if (rawMessage.size() <= 5) {
                    rawKeyword.add((byte) value);
                }
                
                /*
                * Reading INFO or PHOTO message
                */
                if (acceptingType == RequestType.INFO && rawMessage.size() <= 4) {
                    String keyword = new String(parseBytes(rawKeyword));
                    LOG.log(Level.FINE, "Session {0} captured keyword part: {1}", new Object[]{this, keyword});
                    
                    /*
                    * Validation if the extracted keyword is already part of some keywords of known request types
                    */
                    if(!(RequestType.INFO.isValidKeywordPart(keyword) || RequestType.PHOTO.isValidKeywordPart(keyword)))
                        throw new SyntaxErrorException("Invalid syntax.");
                    
                    /*
                    * Resolving the type of the request
                    */
                    try {
                        acceptingType = RequestType.resolveMessageRequestType(keyword);
                        if(acceptingType == RequestType.PHOTO)
                            continue;
                        LOG.log(Level.FINE, "Session {0} changed its accepting type to {1}", new Object[]{this, acceptingType});
                        
                    } catch (IllegalArgumentException e) {
                        if(rawMessage.size() == 4)
                            throw new SyntaxErrorException("Invalid syntax.", e);
                        else
                            LOG.log(Level.SEVERE, "Uncaught exception !!!");
                    }
                }
                
                /*
                * Validation of the username keyword
                */
                if (acceptingType == RequestType.USERNAME && rawMessage.size() <= 5) {
                    String keyword = new String(parseBytes(rawKeyword));
                    if (!RequestType.USERNAME.isValidKeywordPart(keyword)) {
                        validationFlag = false;
                    }
                    if (rawMessage.size() == 5 && keyword.equals(RequestType.USERNAME.keyword())) {
                        validationFlag = true;
                    }
                }
                
                /*
                * Detecting of the end of the line for non-PHOTO requests
                */
                if (acceptingType != RequestType.PHOTO) {
                    separator[1] = (byte) value;
                    if (Robot.LINE_SEPARATOR.equals(new String(separator))) {
                        checksm -= 23;
                        break;
                    }
                    separator[0] = separator[1];
                }
                
                /*
                * Reading the size of PHOTO and validating the syntax of PHOTO message.
                */
                if (acceptingType == RequestType.PHOTO && photo == null) {
                    LOG.log(Level.FINE, "Session {0} actually captured PHOTO message: {1}", new Object[]{this, new String(parseBytes(rawMessage))});
                    String photoSeparator = new String(new byte[]{(byte) value});
                    if (photoSeparator.matches("\\s")) {
                        photoSeparatorHitCounter++;
                        photo = photoSeparatorHitCounter == 2 ? new Photo(parseBytes(rawMessage)) : null;
                    } else if(!photoSeparator.matches("\\d") && photoSeparatorHitCounter >= 1){
                        throw new SyntaxErrorException("Invalid photo syntax.");                        
                    }
                    
                }                
                
                /*
                * Reading the PHOTO message.
                */
                if (acceptingType == RequestType.PHOTO && photo != null) {
                    //byte[] rawPhoto = in.readNBytes(photo.getSize()); -- unused because of JDK 7 or lower being used by Baryk server
                    int[] rawPhoto = new int[photo.getSize()];   
                    LOG.log(Level.FINE, "Session {0} PHOTO message size: {1}", new Object[]{this, photo.getSize()});
                    for(int i = 0; i < photo.getSize(); i++){
                        rawPhoto[i] = in.read();
                    }
                    /*
                    * Counting the checksum of the photo
                    */
                    int checksum = 0;
                    for (int b : rawPhoto) {
                        checksum += b;
                        rawMessage.add((byte) b);
                    }
                    LOG.log(Level.FINE, "Session {0} PHOTO message counted checksum: {1}", new Object[]{this, checksum});
                    photo.setCountedChecksum(checksum);
                    
                    //byte[] rawChecksum = in.readNBytes(4); -- unused because of JDK 7 or lower being used by Baryk server
                    /*
                    * Extracting the right checksum for the validation
                    */
                    byte[] rawChecksum = new byte[4];
                    for(int i = 0; i < 4; i++){
                        int b = in.read();
                        rawChecksum[i] = (byte) b;
                        rawMessage.add((byte) b);
                    }
                    
                    StringBuilder sb = new StringBuilder();
                    for (byte b : rawChecksum) {
                        sb.append(String.format("%02x", b));
                    }                    
                    int expectedChecksum = Integer.parseInt(sb.toString(), 16);
                    LOG.log(Level.FINE, "Session {0} PHOTO message expected checksum: {1}", new Object[]{this, expectedChecksum});
                    photo.setExpectedChecksum(expectedChecksum);                    
                    return photo;
                }
            }
            request = new Request(acceptingType);
            request.setChecksum(checksm);
            request.setIsValid(validationFlag);
            LOG.log(Level.FINE, "Session {0} checksum of message {1} is: {2}", new Object[]{this, acceptingType, checksm});
            request.setData(parseBytes(rawMessage));
            if(!request.checkSyntax())
                throw new SyntaxErrorException("Invalid syntax of request " + request);
            else
                return request;
        } catch (SocketTimeoutException socketTimeoutException){
            throw socketTimeoutException;
            
        } catch (IOException iOException) {
            throw new SessionClosedException("Error while reading input stream of the session.", iOException);
        
        } catch (NumberFormatException numberFormatException) {
            throw new SyntaxErrorException("Wrong number format of photo valid checksum.", numberFormatException);
        }
    }
    /**
     * Converts {@code List<Byte>} to {@code byte[]}
     * 
     * <p><i>Used due to old version of JDK being run at the Baryk server</i>
     * @param list of bytes
     * @return primitives from the list collection given
     */
    private byte[] parseBytes(List<Byte> bytes){
        Byte[] rawBytes = bytes.toArray(new Byte[bytes.size()]);
        byte[] returnBytes = new byte[rawBytes.length];
        for(int i = 0; i < rawBytes.length; i++){
            returnBytes[i] = (byte) rawBytes[i];
        }
        return returnBytes;
    }
    
    /**
     * Gets the request type for actual state of the session.
     * @return request type for actual state
     */
    protected RequestType getRequestType(){
        Objects.requireNonNull(state);
        switch(state){
            case ACCEPTING_USERNAME:
                return RequestType.USERNAME;
            
            case ACCEPTING_PASSWORD:
                return RequestType.PASSWORD;
            
            case ACCEPTING_MESSAGES:
            case BAD_CHECKSUM:
                return RequestType.INFO;
            
            default:
                throw new IllegalStateException("No request type for this state " + state);
        }
    }
    
}

/**
 * Represents the state of the {@link Session}
 * @author Matej Barton (bartom47@fel.cvut.cz)
 */
enum State{
    ACCEPTING_USERNAME(200, "login", false),
    ACCEPTING_PASSWORD(201, "password", false),
    ACCEPTING_MESSAGES(202, "ok", false),   
    BAD_CHECKSUM(300, "bad checksum", false),
    LOGIN_FAILED(500, "login failed", true),
    SYNTAX_ERROR(501, "syntax error", true),
    TIMEOUT(502, "timeout", true);
    
    private final int code;
    private final String message;
    private final boolean finalState;

    private State(int code, String message, boolean finalState) {
        this.code = code;
        this.message = message;
        this.finalState = finalState;
    }

    public int getCode() {
        return code;
    }

    /**
     * Returns the message according to the protocol specified
     * @return string message for each state
     */
    public String getMessage() {
        return String.format("%s %s%s", code, message.toUpperCase(), Robot.LINE_SEPARATOR);
    }

    /**
     * Flag if the state is final
     * @return the final state
     * @deprecated Actually not used but left there
     */
    @Deprecated
    public boolean isFinalState() {
        return finalState;
    }        
}

/**
 * Represents the type of each {@link Request}
 * @author Matej Barton (bartom47@fel.cvut.cz)
 */
enum RequestType{
    USERNAME("Robot"),
    PASSWORD,
    INFO("INFO", "^INFO .+"), 
    PHOTO("FOTO", "^FOTO \\d+ ");
    
    private final String keyword;
    private final int byteLength;
    private final Pattern syntax;
    
    private RequestType(){
        this("", ".*");
    }
    
    private RequestType(String keyword){
        this(keyword, ".*");
    }
    
    private RequestType(String keyword, String syntax){
        this(keyword, syntax, -1);        
    }
    
    private RequestType(String keyword, String syntax, int byteLength){
        this.keyword = keyword;
        this.syntax = Pattern.compile(syntax, Pattern.DOTALL);
        this.byteLength = byteLength;
    }
    
    /**
     * Resolves the request type
     * @param keyword keyword of the request type
     * @return RequestType enum value with the keyword given
     * @throws IllegalArgumentException when no such keyword is found
     */
    public static RequestType resolveMessageRequestType(String keyword) throws IllegalArgumentException{
        if(keyword.equals(INFO.keyword)){
            return INFO;
        } else if (keyword.equals(PHOTO.keyword)){
            return PHOTO;
        } else {
            throw new IllegalArgumentException("No keyword found");
        }
    }
    
    /**
     * Returns the syntax.
     * @return syntax of the request
     */
    public Pattern getSyntax(){
        return syntax;
    }
    
    /**
     * Returns the keyword
     * @return the keyword
     */
    public String keyword(){
        return keyword;
    }
    
    /**
     * Checks if the keyword part given is already part of the keyword 
     * of the specified enum value.
     * 
     * @param keywordPart part of the keyword
     * @return {@code true} if the keyword part is substring of the keyword,
     * {@code false} otherwise
     */
    public boolean isValidKeywordPart(String keywordPart){
        if(keywordPart.length() > 4 || keywordPart.length() <= 0)
            return false;
        else{
            Pattern pat = Pattern.compile(keyword.substring(0, keywordPart.length()));
            return pat.matcher(keywordPart).matches();            
        }

    }
}

/**
 * Represents the request of from the client.
 * 
 * @author Matej Barton (bartom47@fel.cvut.cz)
 */
class Request{
    private byte[] data;
    private int checksum;
    private Boolean isValid;
    private final RequestType requestType;    

    /**
     * Creates new request with specified request type.
     * @param requestType type of the request
     */
    public Request(RequestType requestType) {        
        Objects.requireNonNull(requestType);        
        this.requestType = requestType;        
    }

    /**
     * Returns data associated with the request
     * @return data if the data is present or {@code null} otherwise
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the stored checksum value
     * @return stored checksum value
     */
    public int getChecksum() {
        return checksum;
    }

    /**
     * Sets the given checksum value to be stored with the request.
     * @param checksum the checksum of the request data
     */
    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    /**
     * Returns the valid flag of the request.
     * @return {@code true} if the request has valid flag, 
     * {@code false} if not, {@code null} if the flag is not used
     */
    public Boolean getIsValid() {
        return isValid;
    }

    /**
     * Sets the valid flag of the request.
     * @param isValid boolean value, {@code true} if the request is valid,
     * {@code false} if not, {@code null} if the flag is not used
     */
    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }
                
    /**
     * Sets the request data
     * @param data data of the request
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Returns the request type of the request.
     * @return the request type of the request
     */
    public RequestType getRequestType() {
        return requestType;
    }
            
    /**
     * Checks syntax associated with the request type.
     * @return {@code true} if the syntax is correct,
     * {@code false} otherwise
     */
    public boolean checkSyntax(){
        Objects.requireNonNull(data);
        final String message = new String(data);
        final Matcher m = requestType.getSyntax().matcher(message);
        return m.matches();
    }
             
}

/**
 * Represents the user, who is logging in the system.
 * 
 * @author Matej Barton (bartom47@fel.cvut.cz)
 */
class User{
    private final boolean isValidUsername;
    private final int passcode;
          
    /**
     * Creates new user.
     * 
     * @param isValidUsername flag if valid username was captured by previous request
     * @param passcode passcode for the user computed
     */
    public User(Boolean isValidUsername, int passcode){
        Objects.requireNonNull(isValidUsername);
        this.isValidUsername = isValidUsername;
        this.passcode = passcode;
    }
    
    /**
     * Authenticates the user for passcode given.
     * 
     * @param passcode passcode sent by the client
     * @return {@code true} if the passcode matches the passcode stored
     * and the username is valid or {@code false} otherwise
     */
    public boolean authenticate(int passcode){        
        Objects.requireNonNull(passcode);
        boolean validUsername;
        boolean validPassword;
        
        validPassword = passcode == this.passcode;
        
        return isValidUsername && validPassword;                
    }        
}

/**
 * Represents the photo stored at the server.
 * 
 * @author Matej Barton (bartom47@fel.cvut.cz)
 */
final class Photo extends Request{
    private int expectedChecksum;
    private int countedChecksum;
    private final int size;
    
    /**
     * Creates new photo from the request data. Validates the request data.
     * @param data data from the request
     * @throws SyntaxErrorException if the size extracted from the request could
     * not be load or the size is equal or before zero
     */
    public Photo(byte[] data) throws SyntaxErrorException{
        super(RequestType.PHOTO);
        setData(data);
        if(checkSyntax()){
            String message = new String(data);
            String rawSize = message.split(" ")[1];
            size = Integer.parseInt(rawSize);
            if(size <= 0)
                throw new SyntaxErrorException("Invalid size of photo.");
        } else
            throw new SyntaxErrorException("Invalid syntax of photo request.");        
    }

    /**
     * Returns the expected checksum of the photo
     * @return expected checksum
     */
    public int getExpectedChecksum() {
        return expectedChecksum;
    }

    /**
     * Sets the expected checksum to be stored
     * @param expectedChecksum expected checksum of the photo
     */
    public void setExpectedChecksum(int expectedChecksum) {
        this.expectedChecksum = expectedChecksum;
    }

    /**
     * Gets the counted checksum of the photo
     * @return counted checksum
     */
    public int getCountedChecksum() {
        return countedChecksum;
    }

    /**
     * Sets the counted checksum to be stored.
     * @param countedChecksum counted checksum of the photo
     */
    public void setCountedChecksum(int countedChecksum) {
        this.countedChecksum = countedChecksum;
    }
    
    /**
     * Validates the checksums.
     * @return {@code true} if expected checksum is equal to counted checksum or
     * {@code false} otherwise
     */
    public boolean validate(){
        return countedChecksum == expectedChecksum;
    }

    /**
     * Gets the size of the photo.
     * @return size of the photo
     */
    public int getSize() {
        return size;
    }        
}

/**
 * Signal for the session runtime to close the session.
 * 
 * @author Matej Barton (bartom47@fel.cvut.cz)
 */
class SessionClosedException extends Exception{

    public SessionClosedException(String message) {
        super(message);
    }

    public SessionClosedException(String message, Throwable cause) {
        super(message, cause);
    }
    
}

/**
 * This exception is thrown when {@code SYNTAX_ERROR} response
 * is expected.
 * 
 * @author Matej Barton (bartom47@fel.cvut.cz)
 */
class SyntaxErrorException extends Exception{

    public SyntaxErrorException(String message) {
        super(message);
    }

    public SyntaxErrorException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
