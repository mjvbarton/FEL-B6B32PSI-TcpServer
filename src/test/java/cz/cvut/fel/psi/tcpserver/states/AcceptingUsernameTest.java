/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.states;

import cz.cvut.fel.psi.tcpserver.Request;
import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.SessionRunException;
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
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Matej
 */
@RunWith(MockitoJUnitRunner.class)
public class AcceptingUsernameTest {
    
    @Mock
    private Session session;
        
    
    public AcceptingUsernameTest() {
    }
    
    /**
     * Test of next method, of class AcceptingUsername.
     */
    @Test
    public void testNextValidUsername() throws SessionRunException, NoSuchElementException, RequestSyntaxException{
        // ARRANGE
        String message = "Robot375";                       
        Mockito.when(session.acceptRequest()).thenReturn(new Request(message));
        
        // ACT
        AcceptingUsername instance = new AcceptingUsername(session);
        Response returned = instance.next();
        
        // ASSERT
        Mockito.verify(session).setUser(any(User.class));
        Mockito.verify(session).sendResponse(returned);
        assertNotNull("Returned is not null.", returned);
        assertTrue("Returned is instance of AcceptingPassword.", returned instanceof AcceptingPassword);
    }
    
    /**
     * Test of next method, of class AcceptingUsername.
     */
    @Test
    public void testNextNoSuchElementExceptionInAcceptRequest() throws SessionRunException, NoSuchElementException, RequestSyntaxException{
        // ARRANGE
        String message = "Robot375";                       
        Mockito.when(session.acceptRequest()).thenThrow(new NoSuchElementException());
        
        // ACT
        AcceptingUsername instance = new AcceptingUsername(session);
        Response returned = instance.next();
        
        // ASSERT        
        assertNotNull("Returned is not null.", returned);
        assertEquals("Returned equals to instance.", instance, returned);
    }
    
    /**
     * Test of next method, of class AcceptingUsername.
     */
    @Test
    public void testNextNonValidUsername() throws SessionRunException, NoSuchElementException, RequestSyntaxException{
        // ARRANGE
        String message = "Robot375";                       
        Mockito.when(session.acceptRequest()).thenReturn(new Request(message));
        
        // ACT
        AcceptingUsername instance = new AcceptingUsername(session);
        Response returned = instance.next();
        
        // ASSERT                
        assertNotNull("Returned is not null.", returned);
        assertTrue("Returned is instance of AcceptingPassword.", returned instanceof AcceptingPassword);
        //assertTrue("Returned has flag of not valid username.", ((AcceptingPassword) returned).isInvalidUsername());
    }

    /**
     * Test of toString for right protocol function.
     */
    @Test
    public void testToString(){
        String expected = "200 LOGIN \r\n";
        Response instance1 = new AcceptingUsername(session);
        AcceptingUsername instance2 = new AcceptingUsername(session);
        assertEquals("Interface method matches expected.", expected, instance1.toString());
        assertEquals("Class method matches expected.", expected, instance2.toString());
    }
    
}
