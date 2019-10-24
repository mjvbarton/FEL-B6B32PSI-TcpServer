/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver;

import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
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
public class RequestTypeTest {
    @Parameter(value = 0)
    public RequestType messageType;
       
    @Parameter(value = 1)
    public String message;

    @Parameter(value = 2)
    public boolean hasCorrectSyntax;
    
    public RequestTypeTest() {
        
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
     * Test of checkSyntax method, of class RequestType.
     */
    @Test
    public void testCheckSyntax_validMessage_trueReturned() {
        Assert.assertEquals("Message " + message, 
                hasCorrectSyntax, messageType.checkSyntax(message));
    }                   
}
