package cz.cvut.fel.psi.tcpserver.requests;

import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.util.regex.Pattern;

/**
 * Represents info message.
 * <p>
 * The syntax is defined by regex {@code ^INFO\s.+}
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class InfoMessageRequest extends CrLfRequest<String>{
    
    /**
     * Creates new {@code InfoMessageRequest} with offset specified
     * @param offset helper to provide continuity of reading
     */
    public InfoMessageRequest(RequestOffset offset) {
        super(offset, RequestType.INFO);
    }
    
    
    @Override
    protected boolean checkSyntax(String rawRequest) {
        final String regex = "^" + keyword.getKeyword() + ".*";
        final Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(rawRequest).matches();
    }

    @Override
    protected void loadData(String rawRequest) throws RequestSyntaxException {
        if(!checkSyntax(rawRequest)) throw new RequestSyntaxException("Invalid syntax for info message request.");
        data = rawRequest.split("^INFO\\s")[1];
    }
    
}
