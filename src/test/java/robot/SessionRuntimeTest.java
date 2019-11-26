package robot;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
public class SessionRuntimeTest {
    
    final static Server SERVER = new Server();
    
    Client client;
    
    private class Client{
        Socket socket;
        InputStream in;
        OutputStream out;
        final List<State> expectedResponses= new ArrayList();
        
        Client() throws Exception{
            socket = new Socket(InetAddress.getByName("127.0.0.1"), 3999);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }
        
        public void close() throws Exception{
            socket.close();
        }
        
        public void addResponse(State expectedResponse){
            expectedResponses.add(expectedResponse);
        }
        
        public void sendMessage(String rawMessage, State expectedResponse) throws Exception{
            addResponse(expectedResponse);
            out.write(rawMessage.getBytes());            
        }
        
        public String getResponses() throws Exception{
            String responses = new String(in.readAllBytes());
            return responses;
        }
        
        public String getExpectedResponses(){
            StringBuilder sb = new StringBuilder();
            expectedResponses.forEach(response -> sb.append(response.getMessage()));
            return sb.toString();
        }
        
    }
    
    private static class Server extends Thread{

        @Override
        public void run() {
            Robot.main(new String[]{});            
        }
                
        public void halt(){
            Robot.stop();
        }
        
    }
    
    @BeforeClass
    public static void startServer(){
        SERVER.start();
    }
    
    @AfterClass
    public static void stopServer() {
        SERVER.halt();
    }
    
    @Before
    public void connectClient() throws Exception{
        client = new Client();
    }
    
    @After
    public void closeClient() throws Exception{
        client.close();
    }
    
    @Test
    public void testBaryk02() throws Exception{
        client.addResponse(State.ACCEPTING_USERNAME);
        client.sendMessage("Robot Karel\r\n", State.ACCEPTING_PASSWORD);
        client.sendMessage("1045\r\n", State.ACCEPTING_MESSAGES);
        client.sendMessage("INFO \r \n\r \n \r\n", State.ACCEPTING_MESSAGES);
        client.sendMessage("INFO \r\nX", State.ACCEPTING_MESSAGES);
        client.addResponse(State.SYNTAX_ERROR);
        assertEquals(client.getExpectedResponses(), client.getResponses());
    }
    
    @Test
    public void testBaryk11() throws Exception{
        client.addResponse(State.ACCEPTING_USERNAME);
        client.sendMessage("Robot Karel\r\n", State.ACCEPTING_PASSWORD);
        client.sendMessage("1045\r\n", State.ACCEPTING_MESSAGES);
        client.sendMessage("FOTO 8 ABCDEFGH\u0001\u0000\u0002$", State.BAD_CHECKSUM);
        client.sendMessage("FOTO 2 XYabcd", State.BAD_CHECKSUM);
        client.sendMessage("FOTO 0 \u0000\u0000\u0000\u0000\u0000\u0000", State.SYNTAX_ERROR);
        assertEquals(client.getExpectedResponses(), client.getResponses());        
    }
}
