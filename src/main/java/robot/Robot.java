package robot;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author Matej
 */
public class Robot {
    public static final String LINE_SEPARATOR = "\r\n";
    
    public static final int DEFAULT_PORT = 3999;
    
    public static final int DEFAULT_TIMEOUT_SECONDS = 45;
    
    public static int TIMEOUT_SECONDS = DEFAULT_TIMEOUT_SECONDS;
    
    private static boolean isRunning;
    
    private static final Logger LOG = Logger.getLogger(Robot.class.getName());

    /**
     * @param args the command line arguments {@code <serverPortNumber> <timeout>}     
     *   
     */
    public static void main(String[] args){
        int serverPort;
        if(args.length >= 1){            
            serverPort = Integer.parseInt(args[0]);
            LOG.log(Level.FINE, "Initialized server with server port {0}", serverPort);
            if(args.length == 2){
                TIMEOUT_SECONDS = Integer.parseInt(args[1]);
                if(TIMEOUT_SECONDS < 0){
                    LOG.log(Level.SEVERE, "Invalid argument. Timeout must be greater or equal to zero");
                    throw new IllegalArgumentException("Argument must be greater than or equal to zero");
                }
                LOG.log(Level.FINE, "Used custom timeout for sessions of {0} seconds.", TIMEOUT_SECONDS);
            }
            
            if(args.length > 2){
                LOG.log(Level.SEVERE, "Invalid arguments for the server found.");
                throw new IllegalArgumentException("Invalid argument captured");
            }
        } else {
            serverPort = DEFAULT_PORT;
            LOG.log(Level.FINE, "Initialized server with default server port and default timeout.");
        }
        
        try {
            final ServerSocket serverSocket = new ServerSocket(serverPort);
            isRunning = true;
            while (isRunning) {
                
            }
        } catch (IOException iOException) {
            LOG.log(Level.SEVERE, "Exception {0} occured while running the server.", iOException);
            throw new RuntimeException("Error while running the server", iOException);
        }
    }

    public static void stop(){        
        isRunning = false;
    }
}

class Session implements Runnable{
    private Socket socket;
    private BufferedInputStream in;
    private OutputStream out;
    private State state;
    private boolean isRunning;
    
    public Session(Socket socket) throws IOException{
        state = State.ACCEPTING_USERNAME;
        this.socket = socket;
        in = new BufferedInputStream(socket.getInputStream());
        out = socket.getOutputStream();
        isRunning = true;
    }
    
    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public Request acceptRequest() throws SyntaxErrorException, SessionClosedException{
        final List<Byte> rawMessage = new ArrayList();
        final List<Byte> rawKeyword = new ArrayList();
        final byte[] separator = new byte[2];
        Request request;
        RequestType acceptingType = getRequestType();
        
        while(true){
            int value = in.read();
            if(value < 0){
                throw new SessionClosedException("Socket was closed by the client.");
            }
            
            rawMessage.add((byte) value);
            
            if(acceptingType != RequestType.PHOTO){
                separator[1] = (byte) value;
                if(Robot.LINE_SEPARATOR.equals(new String(separator))){
                    break;
                }
                separator[0] = separator[1];
            }           
                        
            if(rawMessage.size() < 5){
                rawKeyword.add((byte) value);
            }
            
            if(acceptingType == RequestType.INFO && rawMessage.size() == 4){
                String keyword = new String(parseBytes(rawKeyword));
                try {
                    acceptingType = RequestType.resolveMessageRequestType(keyword);
                } catch (IllegalArgumentException e) {
                    throw new SyntaxErrorException("Invalid syntax.", e);
                }
            }
            
            if(acceptingType == RequestType.PHOTO){
                
            }
                                    
            
        }
    }
    
    private byte[] parseBytes(List<Byte> bytes){
        Byte[] rawBytes = bytes.toArray(new Byte[bytes.size()]);
        return ArrayUtils.toPrimitive(rawBytes);        
    }
    
    
    private RequestType getRequestType(){
        Objects.requireNonNull(state);
        switch(state){
            case ACCEPTING_USERNAME:
                return RequestType.USERNAME;
            
            case ACCEPTING_PASSWORD:
                return RequestType.PASSWORD;
            
            case ACCEPTING_MESSAGES:
                return RequestType.INFO;
            
            default:
                throw new IllegalStateException("No request type for this state.");
        }
    }
    
}

enum State{
    ACCEPTING_USERNAME(200, "login", false),
    ACCEPTING_PASSWORD(201, "password", false),
    ACCEPTING_MESSAGES(202, "ok", false),   
    BAD_CHECKSUM(300, "bad checksum", false),
    LOGIN_FAILED(500, "login failed", true),
    SYNTAX_ERROR(501, "syntax error", true),
    TIMEOUT(502, "timeout", false);
    
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

    public String getMessage() {
        return String.format("%s %s%s", code, message, Robot.LINE_SEPARATOR);
    }

    public boolean isFinalState() {
        return finalState;
    }        
}

enum RequestType{
    USERNAME,
    PASSWORD,
    INFO("INFO", "^INFO\\s.*"), 
    PHOTO("FOTO", "^FOTO\\s\\d+\\s");
    
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
        this.syntax = Pattern.compile(syntax);
        this.byteLength = byteLength;
    }
    
    public static RequestType resolveMessageRequestType(String keyword){
        if(keyword.equals(INFO.keyword)){
            return INFO;
        } else if (keyword.equals(PHOTO.keyword)){
            return PHOTO;
        } else {
            throw new IllegalArgumentException("No keyword found");
        }
    }
    
    public Pattern getSyntax(){
        return syntax;
    }
}

class Request{
    private byte[] data;
    private final RequestType requestType;
    private final int checksum;

    public Request(RequestType requestType, int checksum) {        
        Objects.requireNonNull(requestType);
        Objects.requireNonNull(checksum);
        this.requestType = requestType;
        this.checksum = checksum;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public int getChecksum() {
        return checksum;
    }
            
    public boolean checkSyntax(){
        Objects.requireNonNull(data);
        final String message = new String(data);
        final Matcher m = requestType.getSyntax().matcher(message);
        return m.matches();
    }
             
}

class User{
    private final String username;
    private final int passcode;
    
    private final Pattern pat = Pattern.compile("^Robot.*");

    public User(String username, int passcode) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(passcode);
        this.username = username;
        this.passcode = passcode;
    }
    
    public boolean authenticate(int passcode){        
        Objects.requireNonNull(passcode);
        boolean validUsername;
        boolean validPassword;
        
        final Matcher m = pat.matcher(username);
        validUsername = m.matches();
        validPassword = passcode == this.passcode;
        
        return validUsername && validPassword;                
    }        
}

class Photo{
    private int expectedChecksum;
    private int countedChecksum;
    private final int size;

    public Photo(int size) {
        this.size = size;
    }

    public int getExpectedChecksum() {
        return expectedChecksum;
    }

    public void setExpectedChecksum(int expectedChecksum) {
        this.expectedChecksum = expectedChecksum;
    }

    public int getCountedChecksum() {
        return countedChecksum;
    }

    public void setCountedChecksum(int countedChecksum) {
        this.countedChecksum = countedChecksum;
    }
    
    public boolean validate(){
        return countedChecksum == expectedChecksum;
    }
}

class SessionClosedException extends Exception{

    public SessionClosedException(String message) {
        super(message);
    }

    public SessionClosedException(String message, Throwable cause) {
        super(message, cause);
    }
    
}

class SyntaxErrorException extends Exception{

    public SyntaxErrorException(String message) {
        super(message);
    }

    public SyntaxErrorException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
