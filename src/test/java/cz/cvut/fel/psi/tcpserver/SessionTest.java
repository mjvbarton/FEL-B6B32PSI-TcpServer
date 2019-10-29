/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver;

import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.NoSuchElementException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Matej
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionTest {
    private ByteArrayInputStream in;
    private ByteArrayOutputStream out;
    private final int localPort = 3999;
    
    @Mock
    private Socket socket;

    @Mock
    private Server srv;
    
    private Session instance;
        
    public SessionTest() {
        
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException {
        in = new ByteArrayInputStream(new byte[]{});
        Mockito.when(socket.getInputStream()).thenReturn(in); 
        Mockito.when(socket.getOutputStream()).thenReturn(out);
        Mockito.when(socket.getLocalPort()).thenReturn(localPort);                
    }
    
    @After
    public void tearDown() {
        instance = null;
    }
   
    /**
     * Test of acceptRequest method, of class Session.
     */
    @Test
    public void testAcceptRequest_validInfoMessage_infoRequestReturned() throws Exception{
        String inString = "INFO 2019-10-28-8:34 Lorem ipsum\r\n INFO 2019-10-28-8:38 dolor sit amet";
        String expectedData = "2019-10-28-8:34 Lorem ipsum";
        in = new ByteArrayInputStream(inString.getBytes());
        Mockito.when(socket.getInputStream()).thenReturn(in);
        instance = new Session(socket, srv);        
        Request req = instance.acceptRequest();
        assertEquals("RequestType matches for input stream sequence: " + inString, RequestType.INFO, req.getType());
        assertEquals("Request data matches for input stream sequence: " + inString, expectedData, req.getData());
    }
    
    /**
     * Test of acceptRequest method, of class Session.
     */
    @Test
    public void testAcceptRequest_twoValidInfoMessages_infoRequestReturned() throws Exception{
        String inString = "INFO 2019-10-28-8:34 Lorem ipsum\r\nINFO 2019-10-28-8:38 dolor sit amet";
        String expectedData = "2019-10-28-8:34 Lorem ipsum";
        String expectedData2 = "2019-10-28-8:38 dolor sit amet";
        in = new ByteArrayInputStream(inString.getBytes());
        Mockito.when(socket.getInputStream()).thenReturn(in);
        instance = new Session(socket, srv);
        Request req = instance.acceptRequest();
        assertEquals("MSG1: RequestType matches for input stream sequence: " + inString, RequestType.INFO, req.getType());
        assertEquals("MSG1: Request data matches for input stream sequence: " + inString, expectedData, req.getData());
        Request req2 = instance.acceptRequest();
        assertEquals("MSG2: RequestType matches for input stream sequence: " + inString, RequestType.INFO, req2.getType());
        assertEquals("MSG2: Request data matches for input stream sequence: " + inString, expectedData2, req2.getData());
    }
    
    /**
     * Test of acceptRequest method, of class Session.
     */
    @Test
    public void testAcceptRequest_nonValidInfoMessage1_RequestSyntaxExceptionThrown() throws Exception{
        String inString = "INFO2019-10-28-8:34 Lorem ipsum\r\n INFO 2019-10-28-8:38 dolor sit amet";
        in = new ByteArrayInputStream(inString.getBytes());
        Mockito.when(socket.getInputStream()).thenReturn(in);
        instance = new Session(socket, srv);
        try{
            instance.acceptRequest();
            fail("Missing RequestSyntaxException for non-valid message: " + inString);
        } catch(RequestSyntaxException ex) {
            assertTrue("Exception " + ex + " thrown for message: " + inString, true);
        }
    }
    
    /**
     * Test of acceptRequest method, of class Session.
     */
    @Test
    public void testAcceptRequest_nonValidInfoMessage2_RequestSyntaxExceptionThrown() throws Exception{
        String inString = "INFORMACE 2019-10-28-8:34 Lorem ipsum\r\n INFO 2019-10-28-8:38 dolor sit amet";
        in = new ByteArrayInputStream(inString.getBytes());
        Mockito.when(socket.getInputStream()).thenReturn(in);
        instance = new Session(socket, srv);     
        try{
            instance.acceptRequest();
            fail("Missing RequestSyntaxException for non-valid message: " + inString);
        } catch(RequestSyntaxException ex) {
            assertTrue("Exception " + ex + " thrown for message: " + inString, true);
        }
    }
    
    /**
     * Test of acceptRequest method, of class Session.
     */
    @Test
    public void testAcceptRequest_nonValidInfoMessage3_RequestSyntaxExceptionThrown() throws Exception {
        String inString = "2019-10-28-8:34 INFO Lorem ipsum\r\n INFO 2019-10-28-8:38 dolor sit amet";
        in = new ByteArrayInputStream(inString.getBytes());
        Mockito.when(socket.getInputStream()).thenReturn(in);
        instance = new Session(socket, srv);
        try {
            instance.acceptRequest();
            fail("Missing RequestSyntaxException for non-valid message: " + inString);
        } catch (RequestSyntaxException ex) {
            assertTrue("Exception " + ex + " thrown for message: " + inString, true);
        }
    }
    
    /**
     * Test of acceptRequest method, of class Session.
     */
    @Test
    public void testAcceptRequest_emptyMessage_NoSuchElementExceptionThrown() throws Exception {
        String inString = "";
        in = new ByteArrayInputStream(inString.getBytes());
        Mockito.when(socket.getInputStream()).thenReturn(in);
        instance = new Session(socket, srv);
        try {
            instance.acceptRequest();
            fail("Missing NoSuchElementException for non-valid message: " + inString);
        } catch (NoSuchElementException ex) {
            assertTrue("Exception " + ex + " thrown for message: " + inString, true);
        }
    }

    /**
     * Test of toString method, of class Session.
     */    
    @Test
    public void testToString_md5HashOfSessionLocalPortDateEstabishedReturned() throws NoSuchAlgorithmException, IOException {
        Date established = new Date();
        Mockito.when(socket.getInputStream()).thenReturn(in);
        instance = new Session(socket, srv);        
        instance.established = established;
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(("Session " + localPort + " " + established).getBytes());
        String expected = new String(md.digest()).toUpperCase();
        assertEquals("Expected session name equals session name given.", expected, instance.toString());
    }

    /**
     * Test of hashCode method, of class Session.
     */    
    @Test
    public void testHashCode() throws NoSuchAlgorithmException, IOException {
        Date established = new Date();
        Mockito.when(socket.getInputStream()).thenReturn(in);
        instance = new Session(socket, srv);
        instance.established = established;
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(("Session " + localPort + " " + established).getBytes());
        String expected = new String(md.digest()).toUpperCase();
        assertEquals("Expected session name equals session name given.", expected.hashCode(), instance.hashCode());
    }
    
}
