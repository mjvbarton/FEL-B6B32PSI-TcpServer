package robot;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Matej
 */
public class UserTest {
        
    @Test
    public void testUserAuthenticateForValidUsernameAndValidPasscode() throws Exception{
        final String username = "Robot Karel";
        final int passcode = 1045;
        User u = new User(username);
        assertTrue(u.authenticate(passcode));
    }
    
    @Test
    public void testUserNotAuthenticateForInvalidUesrnameAndValidPassword() throws Exception{
        final String username = "Hadice 23";
        final int passcode = 1045;
        User u = new User(username);
        assertFalse(u.authenticate(passcode));
    }
    
    @Test
    public void testUserNotAuthenticateForValidUsernameAndInvalidPassword() throws Exception{
        final String username = "Robot Karel";
        final int passcode = 11;
        User u = new User(username);
        assertFalse(u.authenticate(passcode));
    }
    
    @Test
    public void testUserNotAuthenticateForEmptyStringAsUsername() throws Exception{
        final String username = "";
        final int passcode = 0;
        User u = new User(username);
        assertFalse(u.authenticate(passcode));        
    }
    
    @Test
    public void testUserAuthenticateNullBytesDoesNotAffectCorrectPasswordComputation() throws Exception{
        final String username = "Robot Karel\u0000\u0000\u0000\u0000\u0000\u0000";
        final int passcode = 1045;
        User u = new User(username);
        assertTrue(u.authenticate(passcode));
    }
    
}
