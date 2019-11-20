package cz.cvut.fel.psi.tcpserver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import cz.cvut.fel.psi.tcpserver.exceptions.ServerRunException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Matej
 */
public class ClientTest {    
    private static String IP_ADDRESS = "127.0.0.1";
    private static int PORT_NUMBER = 3999;
    
    private Socket client;
    
    private String message;
    private String expectedResponse;
    private String returnedResponse;
    
    @BeforeClass
    public static void startServer(){
       
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        Server.main(new String[]{});
                    } catch (ServerRunException ex) {
                        Logger.getLogger(ClientTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            Thread t = new Thread(runnable);
            t.start();
        
    }
    
    @AfterClass
    public static void stopServer() throws IOException{
        Server.srv.close();
    }
    
    @Before
    public void setUp() throws UnknownHostException, IOException {
        client = new Socket(InetAddress.getByName(IP_ADDRESS), PORT_NUMBER);
    }
    
    @After
    public void tearDown() throws IOException {        
    }

    @Test
    public void test02() throws Exception{
        
        message = "Robot Karel\r\n1045";
        client.getOutputStream().write(message.getBytes());
        message = "INFO \r \n\r \r\nINFO \r\nX INFO X\r\n";
        client.getOutputStream().write(message.getBytes());        
        
        expectedResponse = "200 LOGIN\r\n201 PASSWORD\r\n202 OK\r\n202 OK\r\n501 SYNTAX ERROR\r\n";
        returnedResponse = new String(client.getInputStream().readAllBytes());
        client.close();
        
        assertEquals(expectedResponse, returnedResponse);
    }
}
