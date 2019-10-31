/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver;

import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;
import cz.cvut.fel.psi.tcpserver.states.AcceptingMessages;
import cz.cvut.fel.psi.tcpserver.states.AcceptingPassword;
import cz.cvut.fel.psi.tcpserver.states.AcceptingUsername;
import cz.cvut.fel.psi.tcpserver.states.BadChecksum;
import cz.cvut.fel.psi.tcpserver.states.RequestSyntaxError;
import cz.cvut.fel.psi.tcpserver.states.Response;
import cz.cvut.fel.psi.tcpserver.states.Unauthorized;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.After;
import org.junit.Before;
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
    
    private Response uResponse;
    private Response pResponse;
    private Response mResponse;
    private Response bResponse;
    private Response esResponse;
    private Response euResponse;
    
    @Mock
    private Socket socket;

    @Mock
    private Server srv;
    
    private Session instance;
        
    public SessionTest() {
        
    }
    
    private void generateResponses(Session instance){
        uResponse = new AcceptingUsername(instance);
        pResponse = new AcceptingPassword(instance);
        mResponse = new AcceptingMessages(instance);
        bResponse = new BadChecksum(instance);
        esResponse = new RequestSyntaxError(instance);
        euResponse = new Unauthorized(instance);
    }
    
    @Before
    public void setUp() throws IOException {
        in = new ByteArrayInputStream(new byte[]{});
        out = new ByteArrayOutputStream();
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
    
    @Test
    public void testSendResponseWithSingleResponse() throws SessionRunException, IOException{
        instance = new Session(socket, srv);
        Response response = new AcceptingUsername(instance);
        String expected = response.toString();
        instance.sendResponse(response);        
        assertEquals("Expected output equals output returned.", expected, out.toString());
    }    
    
    @Test
    public void testSendResponseWithMultipleResponses() throws SessionRunException, IOException{
        instance = new Session(socket, srv);
        generateResponses(instance);
        List<Response> responses = new ArrayList();
        responses.add(uResponse);
        responses.add(pResponse);
        responses.add(mResponse);
        responses.add(mResponse);
        responses.add(mResponse);
        responses.add(bResponse);
        responses.add(euResponse);
        responses.add(esResponse);
        
        String expected = "";
        for(Response r : responses){
            expected += r.toString();
            instance.sendResponse(r);
        }                       
        assertEquals("Expected output equals output returned.", expected, out.toString());
    }
    
    /**
     * <i>Not implemented yet.</i>
     */
    @Ignore
    @Test
    public void testAddPhoto(){
        
    }
}
