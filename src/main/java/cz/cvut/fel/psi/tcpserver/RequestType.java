package cz.cvut.fel.psi.tcpserver;

import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Matej
 */
public enum RequestType {
        
    USERNAME("(^Robot)(\\s*\\w+)"),
    PASSWORD("(^\\d+$)"),
    INFO("(^INFO)\\s([^\\r\\n])*"),
    PHOTO("^FOTO\\s\\d+\\s.+$");
    
    private final Pattern syntax;
        
    private RequestType(String syntax){
        this.syntax = Pattern.compile(syntax);
        
    }
    
    public boolean checkSyntax(String message){
        Matcher m = syntax.matcher(message);
        return m.find();
    }
}
