package cz.cvut.fel.psi.tcpserver.requests;

import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.util.regex.Pattern;

/**
 * Represents Password request.
 * <p>
 * The syntax is defined by the regex {@code ^.*}.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class PasswordRequest extends CrLfRequest<String>{

    /**
     * Creates new {@code PasswordRequest} with offset specified.
     * @param offset helper to provide continuity of reading
     */
    public PasswordRequest(RequestOffset offset) {
        super(offset, RequestType.PASSWORD);
    }
        
    @Override
    protected boolean checkSyntax(String rawRequest) {
        final String regex = "^\\d+";
        final Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(rawRequest).matches();
    }

    @Override
    protected void loadData(String rawRequest) throws RequestSyntaxException {
        if(!checkSyntax(rawRequest)) throw new RequestSyntaxException("Invalid syntax of password request.");
        data = rawRequest;
    }
    
}
