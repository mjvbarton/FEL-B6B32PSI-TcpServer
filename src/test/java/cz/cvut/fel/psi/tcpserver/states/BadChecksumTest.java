/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.states;

import cz.cvut.fel.psi.tcpserver.Photo;
import cz.cvut.fel.psi.tcpserver.Request;
import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;
import cz.cvut.fel.psi.tcpserver.exceptions.BadChecksumException;
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
import static org.mockito.Mockito.times;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Matej
 */
@RunWith(MockitoJUnitRunner.class)
public class BadChecksumTest {
    @Mock
    private Session session;
    
    @Mock
    private Request req;
    
    public BadChecksumTest() {
    }
    
    /**
     * Test of next method, of class BadChecksum.
     */   
    @Test
    public void testNextValidInfoMessage() throws NoSuchElementException, RequestSyntaxException, SessionRunException {
        String message = "INFO Lorem ipsum dolor sit amet";
        Mockito.when(session.acceptRequest()).thenReturn(new Request(message));
        
        Response instance = new BadChecksum(session);
        Response returned = instance.next();
        Mockito.verify(session).sendResponse(returned);
        assertNotNull("Returned is not null.", returned);
        assertTrue("Returned is an instance of AcceptingMessages", returned instanceof AcceptingMessages);
    }
    
    /**
     * Test of next method, of class AcceptingMessages.
     */   
    @Test
    public void testNextValidPhotoMessage() throws NoSuchElementException, RequestSyntaxException, SessionRunException, BadChecksumException {
        String message = "FOTO 823 ABCDEFGH\\x00\\x00\\x02\\x24";
        Mockito.when(session.acceptRequest()).thenReturn(new Request(message));        
        Mockito.doNothing().when(session).addPhoto(Mockito.any(Photo.class));
        
        Response instance = new BadChecksum(session);
        Response returned = instance.next();
        Mockito.verify(session, times(1)).sendResponse(returned);
        
        assertNotNull("Returned is not null.", returned);
        assertTrue("Returned is an instance of AcceptingMessages", returned instanceof AcceptingMessages);
    }
    
    /**
     * Test of next method, of class AcceptingMessages.
     */   
    @Test
    public void testNextValidPhotoMessageBadChecksum() throws NoSuchElementException, RequestSyntaxException, SessionRunException, BadChecksumException {
        String message = "FOTO 823 ABCDEFGH\\x00\\x00\\x02\\x24";
        Mockito.when(session.acceptRequest()).thenReturn(new Request(message));        
        Mockito.doThrow(new BadChecksumException()).when(session).addPhoto(Mockito.any(Photo.class));
        
        Response instance = new BadChecksum(session);
        Response returned = instance.next();
        Mockito.verify(session, times(1)).sendResponse(returned);
        
        assertNotNull("Returned is not null.", returned);
        assertTrue("Returned is an instance of Badchecksum", returned instanceof BadChecksum);
    }
    
    /**
     * Test of next method, of class AcceptingMessages.
     */   
    @Test
    public void testNextInvalidMessage() throws NoSuchElementException, RequestSyntaxException, SessionRunException, BadChecksumException {        
        Mockito.when(session.acceptRequest()).thenThrow(new RequestSyntaxException("ex"));
                
        Response instance = new BadChecksum(session);
        Response returned = instance.next();       
        
        assertNotNull("Returned is not null.", returned);
        assertTrue("Returned is an instance of RequestSyntaxError", returned instanceof RequestSyntaxError);
    }
    
    @Test
    public void testToString(){
        String expected = "300 BAD CHECKSUM \r\n";
        Response instance1 = new BadChecksum(session);
        AcceptingMessages instance2 = new BadChecksum(session);
        assertEquals("Interface method matches expected.", expected, instance1.toString());
        assertEquals("Class method matches expected.", expected, instance2.toString());
    }    
}
