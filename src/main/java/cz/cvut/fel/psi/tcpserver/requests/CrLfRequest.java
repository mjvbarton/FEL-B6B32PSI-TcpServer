package cz.cvut.fel.psi.tcpserver.requests;

import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.ArrayUtils;

/**
 * Line separated request is special sort of {@code Request}. 
 * <p>
 * These requests all always ended by new line character {@code \r\n} and their raw data is made of {@code String}
 * @author Matej Barton (bartom47@fel.cvut.cz}
 * @param <T> data of the request
 */
public abstract class CrLfRequest<T> extends Request<T>{
    protected int checksum = 0;
    protected String rawRequest;
    protected String offset;
    protected Byte previousByte;
    protected List<Byte> bytes;
    
    /**
     * Creates new {@code CrLfRequest}
     * @param offset helper to provide continuity of reading
     * @param keyword keyword determining the type of request
     */
    public CrLfRequest(RequestOffset offset, RequestType keyword) {
        super(keyword);
        this.offset = offset.getData();
        this.checksum = offset.getChecksum();       
        this.previousByte = offset.getPreviousByte();
        this.bytes = offset.getBytes();
    }

    /**
     * Gets the raw value of request (= the value the client has sent to server)
     * @return raw value of request
     */
    public String getRawRequest() {
        return rawRequest;
    }      
    
    @Override
    public void loadData(InputStream in) throws RequestSyntaxException, IOException {                       
        while(bytes.size() > 3){
            int val = in.read();            
            byte[] lineseparator = new byte[]{previousByte, (byte) val};
            if(new String(lineseparator).equals("\r\n")){
                bytes.remove(previousByte);
                checksum -= previousByte;
                break;
            }
            checksum += val;
            bytes.add((byte) val);
            previousByte = (byte) val;
        }
        Byte[] rawBytes = bytes.toArray(new Byte[bytes.size()]);
        rawRequest = new String(ArrayUtils.toPrimitive(rawBytes));
        Logger.getLogger(Request.class.getName()).log(Level.FINEST, "Accepted message {0}", rawRequest);
        loadData(rawRequest);
    }
    
    /**
     * Checks syntax of {@code CrLfRequest}
     * @param rawRequest message to be checked
     * @return {@code true} if the syntax is correct, {@code false} if not
     */
    protected abstract boolean checkSyntax(String rawRequest);
    
    /**
     * Loads data from raw request message generate by {@code loadData(InputStream)}
     * @param rawRequest message from {@code InputStream}
     * @throws RequestSyntaxException if syntax does not match
     */
    protected abstract void loadData(String rawRequest) throws RequestSyntaxException;
}
