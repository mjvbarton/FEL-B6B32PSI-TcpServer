/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.states;

import cz.cvut.fel.psi.tcpserver.Request;
import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;
import cz.cvut.fel.psi.tcpserver.User;
import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.util.NoSuchElementException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Matej
 */
@RunWith(MockitoJUnitRunner.class)
public class AcceptingPasswordTest {
    
    @Mock
    private Session session;
    
    @Mock
    private User user;
    
    public AcceptingPasswordTest() {
    }
    
    
    /**
     * Test of next method, of class AcceptingPassword.
     */
    @Test
    public void testNextValidUsernameAndValidPassword() throws SessionRunException, NoSuchElementException, RequestSyntaxException{
        // ARRANGE
        String message = "1234";
        Mockito.when(session.getUser()).thenReturn(user);
        Mockito.when(user.getPassword()).thenReturn(message);
        Mockito.when(session.acceptRequest()).thenReturn(new Request(message));
        
        // ACT
        AcceptingPassword instance = new AcceptingPassword(session);
        Response returned = instance.next();
        
        // ASSERT
        Mockito.verify(session).sendResponse(returned);
        assertNotNull("Returned is not null.", returned);
        assertTrue("Returned is instance of AcceptingMessages.", returned instanceof AcceptingMessages);
    }
    
    /**
     * Test of next method, of class AcceptingPassword.
     */
    @Test
    public void testNextNonValidUsernameAndPassword() throws SessionRunException, NoSuchElementException, RequestSyntaxException{
        // ARRANGE
        String message = "1234";      
        //Mockito.when(session.getUser().getPassword().equals(Mockito.any(String.class))).thenReturn(true);
        Mockito.when(session.acceptRequest()).thenReturn(new Request(message));
        
        // ACT
        AcceptingPassword instance = new AcceptingPassword(session, true);
        Response returned = instance.next();
        
        // ASSERT        
        assertNotNull("Returned is not null.", returned);
        assertTrue("Returned is instance of Unauthorized.", returned instanceof Unauthorized);
    }
    
    /**
     * Test of next method, of class AcceptingPassword.
     */
    @Test
    public void testNextValidUsernameAndNonValidPassword() throws SessionRunException, NoSuchElementException, RequestSyntaxException {
        // ARRANGE
        String validPassword = "1234";
        String message = "1235";
        Mockito.when(session.getUser()).thenReturn(user);
        Mockito.when(user.getPassword()).thenReturn("1234");
        Mockito.when(session.acceptRequest()).thenReturn(new Request(message));

        // ACT
        AcceptingPassword instance = new AcceptingPassword(session);
        Response returned = instance.next();

        // ASSERT        
        assertNotNull("Returned is not null.", returned);
        assertTrue("Returned is instance of Unauthorized.", returned instanceof Unauthorized);
    }
    
    /**
     * Test of next method, of class AcceptingPassword.
     */
    @Test
    public void testNextNoSuchElementExceptionByAcceptRequest() throws SessionRunException, NoSuchElementException, RequestSyntaxException {
        // ARRANGE
        String message = "1234";
        Mockito.when(session.getUser()).thenReturn(user);
        Mockito.when(user.getPassword()).thenReturn(message);
        Mockito.when(session.acceptRequest()).thenThrow(new NoSuchElementException());

        // ACT
        AcceptingPassword instance = new AcceptingPassword(session);
        Response returned = instance.next();

        // ASSERT        
        assertNotNull("Returned is not null.", returned);
        assertEquals("Returned is equal to instance.", instance, returned);
    }
    
    /**
     * Test of next method, of class AcceptingPassword.
     */
    @Test
    public void testNextRequestSyntaxExceptionByAcceptRequest() throws SessionRunException, NoSuchElementException, RequestSyntaxException {
        // ARRANGE
        String message = "1234";
        Mockito.when(session.getUser()).thenReturn(user);
        Mockito.when(user.getPassword()).thenReturn(message);
        Mockito.when(session.acceptRequest()).thenThrow(new RequestSyntaxException(""));

        // ACT
        AcceptingPassword instance = new AcceptingPassword(session);
        Response returned = instance.next();

        // ASSERT        
        assertNotNull("Returned is not null.", returned);
        assertTrue("Returned is instance of Unauthorized", returned instanceof Unauthorized);
    }
    
    /**
     * Test of toString for right protocol function.
     */
    @Test
    public void testToString(){
        String expected = "201 PASSWORD \r\n";
        Response instance1 = new AcceptingPassword(session);
        AcceptingPassword instance2 = new AcceptingPassword(session);
        assertEquals("Interface method matches expected.", expected, instance1.toString());
        assertEquals("Class method matches expected.", expected, instance2.toString());
    }
    
}
