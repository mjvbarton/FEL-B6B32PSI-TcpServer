/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver;

import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author Matej
 */
@RunWith(Parameterized.class)
public class RequestTest {
    
    @Parameter(value = 0)
    public RequestType requestType;
    
    @Parameter(value = 1)
    public String message;
    
    @Parameter(value = 2)
    public boolean isValid;
    
    public RequestTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Parameters
    public static Collection input(){
        return MessageData.getData();
    }

    /**
     * Test of getType method, of class Request.
     */
    @Test
    public void testGetType_matchingTypeOrExceptionThrown() {               
        try {
            Request instance = new Request(message);
            if(isValid){
                Assert.assertEquals("Type of instance matches the type given:", requestType, instance.getType());
            } else {
                fail("Missing exception for non valid of message: " + message);
            }
            
        } catch (RequestSyntaxException ex) {
            if(isValid){
                fail("Exception should not be thrown. Details: " + ex);
            } else {
                Assert.assertTrue("Exception " + ex + " thrown.", true);
            }
        }        
    }
    
    /**
     * Test of getData method, of class Request.
     */
    @Test
    public void testGetData_dataMatchesExpectedDataOrExceptionThrown() {
        try {
            Request instance = new Request(message);
            if (isValid) {
                String expected = getExpectedData();
                String returned = instance.getData();
                Assert.assertEquals("Expected data matches data returned from request message: " + message, expected, returned);
            } else {
                fail("Missing exception for non valid of message: " + message);
            }

        } catch (RequestSyntaxException ex) {
            if (isValid) {
                fail("Exception should not be thrown. For message: " + message);
            } else {
                Assert.assertTrue("Exception " + ex + " thrown. For message: " + message, true);
            }
        }
    }

    private String getExpectedData(){
        switch(requestType){
            case INFO:
                return message.split("^INFO\\s")[0].trim();
            case PHOTO:
                return message.split("^FOTO\\s")[0].trim();
            case USERNAME:
            case PASSWORD:
                return message.trim();
            default:
                throw new RuntimeException("Non existing enum value");
        }
    }
}
