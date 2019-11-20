package cz.cvut.fel.psi.tcpserver.requests;

import cz.cvut.fel.psi.tcpserver.Photo;
import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang.ArrayUtils;

/**
 * Represents photo request.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class PhotoRequest extends Request<Photo> {

    public PhotoRequest() {
        super(RequestType.PHOTO);
    }
        
    @Override
    public void loadData(InputStream in) throws RequestSyntaxException, IOException {
        int size = getSize(in);                
        byte[] photo = in.readNBytes(size);
        byte[] rawChecksum = in.readNBytes(4);
        int validChecksum = getValidChecksum(rawChecksum);        
        data = new Photo(size, photo, validChecksum);            
        //in.skipNBytes(2); // TODO: Consider removing this when Baryk testing fails.
    }      
    
    /*
        Retrives size from the message
    */
    private int getSize(InputStream in) throws RequestSyntaxException, IOException{
        final Pattern separator = Pattern.compile("[^\\d]");
        final List<Byte> bytes = new ArrayList();
        String rawSize;
        while(true){
            byte input = (byte) in.read();
            String val = new String(new byte[]{input});
            if(separator.matcher(val).matches()){
                if(" ".equals(val)) break;
                    else throw new RequestSyntaxException("Invalid Photo request syntax");
            }
            bytes.add(input);
            
        }
        Byte[] rawBytes = bytes.toArray(new Byte[bytes.size()]);
        rawSize = new String(ArrayUtils.toPrimitive(rawBytes));
        return Integer.parseInt(rawSize);        
    }
    /*
        Validates the size given.
    */
    private boolean validateSize(String size){
        final Pattern pat = Pattern.compile("^\\d*");
        return pat.matcher(size).matches();
    }
    
    /*
        Retrieves valid checksum from the message.
    */
    private int getValidChecksum(byte[] rawChecksum){
        StringBuilder sb = new StringBuilder();
        for(byte b : rawChecksum){
            sb.append(String.format("%02x", b));
        }
        return Integer.parseInt(sb.toString(), 16);
    }
    
}
