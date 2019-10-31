/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
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
import static org.mockito.Mockito.times;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Matej
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class procTest_Session {
    @Mock
    private Server srv;
    
    @Mock
    private Socket socket;
    
    private Session session;
    private ByteArrayInputStream in;
    private ByteArrayOutputStream out;
    
    private final int PORT = 3999;

    private boolean socket_isInputShutdown;    
       
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException {        
        out = new ByteArrayOutputStream();
        socket_isInputShutdown = false;
        
        Mockito.when(socket.getInputStream()).thenReturn(in);
        Mockito.when(socket.getOutputStream()).thenReturn(out);
        Mockito.when(socket.isInputShutdown()).thenReturn(socket_isInputShutdown);
        Mockito.when(socket.getLocalPort()).thenReturn(PORT);                
    }
    
    @After
    public void tearDown() {
        in = null;
        out = null;
        session = null;
    }
        
    @Test
    public void tc1_validPasswordRequest_userNameInWrongFormat() throws IOException, InterruptedException{
        String consoleInput = "Robo345\r\n674\r\n";
        String expectedConsoleOutput = "" + Response_OLD.ACCEPTING_USERNAME + Response_OLD.ACCEPTING_PASSWORD + Response_OLD.UNAUTHORIZED;
        in = new ByteArrayInputStream(consoleInput.getBytes());
        Mockito.when(socket.getInputStream()).thenReturn(in);
        session = new Session(socket, srv);
        
        session.run();
        Mockito.when(socket.isInputShutdown()).thenReturn(true);
        Mockito.verify(socket, times(1)).close();
        assertEquals("For message " + consoleInput, expectedConsoleOutput, out.toString());
    }
        
    @Test
    public void tc2_validPasswordRequest_validUsername_incorrectPassword() throws IOException, InterruptedException{
        String consoleInput = "Robot345\r\n1646\r\n";
        String expectedConsoleOutput = "" + Response_OLD.ACCEPTING_USERNAME + Response_OLD.ACCEPTING_PASSWORD + Response_OLD.UNAUTHORIZED;
        in = new ByteArrayInputStream(consoleInput.getBytes());
        Mockito.when(socket.getInputStream()).thenReturn(in);
        session = new Session(socket, srv);
        session.run();
        Mockito.when(socket.isInputShutdown()).thenReturn(true);
        Mockito.verify(socket, times(1)).close();
        assertEquals("For message " + consoleInput, expectedConsoleOutput, out.toString());
    }
        
    @Test
    public void tc3_nonValidPasswordRequest() throws IOException, InterruptedException{
        String consoleInput = "Robot345\r\n1646asd\r\n";
        String expectedConsoleOutput = "" + Response_OLD.ACCEPTING_USERNAME + Response_OLD.ACCEPTING_PASSWORD + Response_OLD.UNAUTHORIZED;
        in = new ByteArrayInputStream(consoleInput.getBytes());
        Mockito.when(socket.getInputStream()).thenReturn(in);
        session = new Session(socket, srv);
        session.run();
        Mockito.when(socket.isInputShutdown()).thenReturn(true);
        Mockito.verify(socket, times(1)).close();
        assertEquals("For message " + consoleInput, expectedConsoleOutput, out.toString());
    }
    
    
    @Test
    public void tc4_correctLogin_connectionClosed() throws IOException, InterruptedException{
        String consoleInput = "Robot345\r\n674\r\n";
        String expectedConsoleOutput = "" + Response_OLD.ACCEPTING_USERNAME + Response_OLD.ACCEPTING_PASSWORD + Response_OLD.ACCEPTING_MESSAGES;
        in = new ByteArrayInputStream(consoleInput.getBytes());
        Mockito.when(socket.getInputStream()).thenReturn(in);
        session = new Session(socket, srv);
        session.run();
        //session.start();
        //Thread.sleep(5000);        
        Mockito.when(socket.isInputShutdown()).thenReturn(true);
        Mockito.verify(socket, times(1)).close();
        assertEquals("For message " + consoleInput, expectedConsoleOutput, out.toString());        
    }
    
    @Ignore
    @Test
    public void tc5_correctLogin_badRequestAccepted(){
        
    }
    
    @Ignore
    @Test
    public void tc6_correctLogin_validInfoMessageAccepted_connectionClosed(){
        
    }
    
    @Ignore
    @Test
    public void tc7_correctLogin_badChecksumPhotoAccepted_okChecksumPhotoAccepted_validInfoMessageAccepted_validInfoMessageAccepted_badSyntaxRequestCaptured(){
        
    }
    
    @Ignore
    @Test
    public void tc8_correctLogin_okChecksumPhotoAccepted_badSyntaxRequestCaptured(){
        
    }
    
    @Ignore
    @Test
    public void tc9_correctLogin_badChecksumPhotoAccepted_badSyntaxRequestCaptured(){
        
    }
    
    @Ignore
    @Test
    public void tc10_correctLogin_okChecksumPhotoAccepted_connectionClosed(){
        
    }
    
    @Ignore
    @Test
    public void tc11_correctLogin_badChecksumPhotoAccepted_connectionClosed(){
        
    }
}
