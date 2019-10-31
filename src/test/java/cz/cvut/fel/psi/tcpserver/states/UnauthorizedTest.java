/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.states;

import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.SessionRunException;
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
public class UnauthorizedTest {
    
    @Mock
    private Session session;
    
    public UnauthorizedTest() {
    }

    /**
     * Test of next method, of class RequestSyntaxError.
     */
    @Test
    public void testNext() throws SessionRunException {
        Response instance = new RequestSyntaxError(session);
        Response returned = instance.next();
        Mockito.verify(session).sendResponse(instance);
        assertNull("Null returned as final state", returned);
    }
    
    /**
     * Test of toString for right protocol function.
     */
    @Test
    public void testToString() {
        String expected = "500 LOGIN FAILED \r\n";
        Response instance1 = new Unauthorized(session);
        Unauthorized instance2 = new Unauthorized(session);
        assertEquals("Interface method matches expected.", expected, instance1.toString());
        assertEquals("Class method matches expected.", expected, instance2.toString());
    }
}
