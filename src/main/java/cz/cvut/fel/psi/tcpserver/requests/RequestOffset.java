package cz.cvut.fel.psi.tcpserver.requests;

import java.util.ArrayList;
import java.util.List;

/**
 * This class acts as a helper to provide continuity for reading {@code InputStream} for {@code CrLfReader}
 * <p>
 * The {@code InputStream} is read at the first time in the session. But the reading continues at the child classes.
 * This class stores raw data, checksum and previous byte of the input stream.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class RequestOffset {
    private final String data;
    private final int checksum;
    private final Byte previousByte;

    /**
     * Creates new {@code RequestOffset}
     * @param data already read data as {@code String} from the session's input stream
     * @param checksum already counted checksum of read data
     * @param previousByte pointer to previous byte of data (necessary to stop reading)
     */
    public RequestOffset(String data, int checksum, Byte previousByte) {
        this.data = data;
        this.checksum = checksum;
        this.previousByte = previousByte;
    }

    public String getData() {
        return data;
    }

    public int getChecksum() {
        return checksum;
    }

    public Byte getPreviousByte() {
        return previousByte;
    }
    
    /**
     * Gets {@code java.util.collection.List} of bytes from {@code this.data}
     * @return {@code java.util.collection.List} of {@code Byte}
     */
    public List<Byte> getBytes(){
        ArrayList<Byte> lst = new ArrayList();
        for(byte b : data.getBytes()){
            lst.add(b);
        }
        return lst;
    }
}
