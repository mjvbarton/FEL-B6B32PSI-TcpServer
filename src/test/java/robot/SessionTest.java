package robot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
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
public class SessionTest {    
    private Session instance;
    
    @Mock
    private Socket socket;
    
    private ByteArrayInputStream input;
    
    public SessionTest() {
    }
        
    private void initInstance(RequestType acceptingType) throws IOException {
        Mockito.when(socket.getInputStream()).thenReturn(input);        
        instance = Mockito.spy(new Session(socket));
        Mockito.doReturn(acceptingType).when(instance).getRequestType();
    }
    
    @Test
    public void testAcceptRequestReturnsUsernameRequestForValidInput() throws Exception{
        String message = "Robot Karel\r\n";
        boolean m = message.matches("^.*");
        input = new ByteArrayInputStream(message.getBytes());
        initInstance(RequestType.USERNAME);        
        Request req = instance.acceptRequest();
        assertEquals("Request type matches.", RequestType.USERNAME, req.getRequestType());
        assertEquals("Request message matches.", message, new String(req.getData()));
    }
    
    @Test
    public void testAcceptRequestReturnsPasswordRequestForValidInput() throws Exception{
        String message = "1045\r\n";
        input = new ByteArrayInputStream(message.getBytes());
        initInstance(RequestType.PASSWORD);        
        Request req = instance.acceptRequest();
        assertEquals("Request type matches.", RequestType.PASSWORD, req.getRequestType());
        assertEquals("Request message matches.", message, new String(req.getData()));
    }
    
    @Test
    public void testAcceptRequestReturnsInfoRequestForValidInput() throws Exception{
        String message = "INFO Lorem ipsum\r ipsum \n lorem \r\n";
        input = new ByteArrayInputStream(message.getBytes());
        initInstance(RequestType.INFO);        
        Request req = instance.acceptRequest();
        assertEquals("Request type matches.", RequestType.INFO, req.getRequestType());
        assertEquals("Request message matches.", message, new String(req.getData()));
    }
    
    @Test(expected = SyntaxErrorException.class)
    public void testAcceptRequestThrowsSyntaxExceptionForSeparatorOnlyMessageSent() throws Exception{
        String message = "\r\n";
        input = new ByteArrayInputStream(message.getBytes());
        initInstance(RequestType.INFO);
        instance.acceptRequest();     
    }
    
    @Test
    public void testAcceptRequestReturnsPhotoForValidInput() throws Exception{
        String message = "FOTO 8 ABCDEFGH\u0000\u0000\u0000\u0000";
        input = new ByteArrayInputStream(message.getBytes());
        initInstance(RequestType.INFO);
        Request req = instance.acceptRequest();
        if(req instanceof Photo){
            Photo photo = (Photo) req;
            assertEquals("Size matches.", 8, photo.getSize());
            assertEquals("Counted checksum matches.", 548, photo.getCountedChecksum());
            assertEquals("Expected checksum matches.", 0, photo.getExpectedChecksum());
        } else
            fail("No photo request returned.");
    }
    
    @Test
    public void testAcceptRequestReturnsRequestForUsernameWithEmptyLineSeparator() throws Exception{
        String message = "\r\n";
        input = new ByteArrayInputStream(message.getBytes());
        initInstance(RequestType.USERNAME);
        Request req = instance.acceptRequest();
        assertEquals("Request type matches.", RequestType.USERNAME, req.getRequestType());
        assertEquals("Request message matches.", message, new String(req.getData()));
    }
    
}
