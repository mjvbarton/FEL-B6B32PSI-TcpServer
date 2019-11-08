package cz.cvut.fel.psi.tcpserver;

/**
 * Represents photo to be saved during the session.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class Photo {
    private final int size;
    //private final byte[] photo;
    private final int dataChecksum;
    private final Integer validChecksum;
            
//    public Photo(Request request, Session session) throws RequestSyntaxException{
//        String mockData = "aaa asasfasga";
//        String[] data = mockData.split("\\s");
//        size = Integer.parseInt(data[0]);
//        String[] nextData = data[1].split("\\\\x");
//        photo = nextData[0].getBytes();
//        String rawChecksum = "";
//        for(int i = 1; i <= 4; i++){
//            rawChecksum += nextData[i];
//        }
//        checksum = Integer.parseInt(rawChecksum, 16);                       
//    }
    
    /**
     * Creates new photo.
     * @param size size of the photo
     * @param data photo data as {@code ByteArray}
     * @param validChecksum checksum sent after photo data
     */
    public Photo(int size, byte[] data, int validChecksum){
        this.size = size;
        this.dataChecksum = getChecksum(data);
        this.validChecksum = validChecksum;
    }
               
    /**
     * Validates if the photo is complete.
     * @return {@code true} if the checksums matches
     */
    public boolean validate(){
        return validChecksum == dataChecksum;
    }
    
    /*
        Counts the checksum of income data.
    */
    private int getChecksum(byte[] data){
        int checksum = 0;
        for(byte b : data){
            checksum += b;
        }
        return checksum;
    }
}
