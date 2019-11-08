package cz.cvut.fel.psi.tcpserver.requests;

import cz.cvut.fel.psi.tcpserver.User;
import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.util.regex.Pattern;

/**
 * Represents username request.
 * <p>
 * The syntax is defined by regex {@code ^Robot.*}
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class UsernameRequest extends CrLfRequest<User>{

    /**
     * Creates new {@code UsernameRequest} with specified offset.
     * @param offset helper to provide continuity of reading
     */
    public UsernameRequest(RequestOffset offset) {
        super(offset, RequestType.USERNAME);
    }
    
    
    @Override
    protected boolean checkSyntax(String rawRequest) {
        final String regex = "^" + keyword.getKeyword() + ".*";
        final Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(rawRequest).matches();
    }

    @Override
    protected void loadData(String rawRequest) throws RequestSyntaxException {
        if(!checkSyntax(rawRequest)) throw new RequestSyntaxException("Syntax error for username request.");
        data = new User(rawRequest, Integer.toString(checksum));
    }
    
}
