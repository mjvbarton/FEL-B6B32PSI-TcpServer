package cz.cvut.fel.psi.tcpserver.requests;

import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.io.InputStream;

/**
 * Enum defining request types with keywords included.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public enum RequestType {
    USERNAME("Robot"),
    PHOTO("FOTO "),
    INFO("INFO "),
    PASSWORD("");
    
    private String keyword;
    
    private RequestType(String keyword){
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }
    
    /**
     * Resolves {@code RequestType} from the keyword given.
     * @param keyword trimmed keyword of the request
     * @return {@code RequestType} with matching keyword or {@code RequestType.PASSWORD} if the keyword does not match any of them.
     */
    public static RequestType getFromKeyword(String keyword){
        for(RequestType r : RequestType.values()){
            if(r.keyword.trim().equals(keyword)){
                return r;
            }
        }
        return PASSWORD;
    }
       
}
