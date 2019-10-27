/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver;

import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import cz.cvut.fel.psi.tcpserver.exceptions.UnauthenticatedException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Matej
 */
public class UserTest {
    
    private final String username = "Robot345";
    private final String expectedPassword = "674";
    
    private User instance;
        
    public UserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        instance = new User(username);
    }
    
    @After
    public void tearDown() {
        instance = null;
    }
    
    /**
     * Test of getPassword method, of class User.    
     */
    @Test
    public void testAuthorize_validUsername_validPassword_noExceptionThrown() {              
        try {
            instance.authorize(expectedPassword);
            assertTrue("No exception was thrown during User.authorize() for username: " + username + " password: " + expectedPassword, true);
        } catch (UnauthenticatedException ex) {
            fail("No exception should be thrown for user: " + username + " password: " + expectedPassword);
        }
    }
    
    /**
     * Test of getPassword method, of class User.
     */
    @Test
    public void testAuthorize_nonValidUsername_validPassword_UnauthenticatedExceptionThrown() {
        User user = new User("Ja jsem Robot Emil cislo 33");
        try {
            user.authorize(expectedPassword);
            fail("Unauthenticated exception should be thrown for user: " + user + " password: " + expectedPassword);
        } catch (UnauthenticatedException ex) {
            assertTrue("Exception " + ex + " thrown for user: " + user + " password: " + expectedPassword, true);
        }
    }
    
    /**
     * Test of getPassword method, of class User.
     */
    @Test
    public void testAuthorize_emptyUsername_validPassword_UnauthenticatedExceptionThrown() {
        User user = new User("");
        try {
            user.authorize(expectedPassword);
            fail("Unauthenticated exception should be thrown for user: " + user + " password: " + expectedPassword);
        } catch (UnauthenticatedException ex) {
            assertTrue("Exception " + ex + " thrown for user: " + user + " password: " + expectedPassword, true);
        }
    }
    
    /**
     * Test of getPassword method, of class User.
     */
    @Test
    public void testAuthorize_validUsername_nonValidPassword_UnauthenticatedExceptionThrown() {
        User user = new User(username);
        try {
            user.authorize("1245a");
            fail("Unauthenticated exception should be thrown for user: " + user + " password: " + "1245a");
        } catch (UnauthenticatedException ex) {
            assertTrue("Exception " + ex + " thrown for user: " + user + " password: " + "1245a", true);
        }
    }
    
    /**
     * Test of toString method, of class User.
     */
    @Test
    public void testToString_UsernameReturned() {
        System.out.println("testToString_UsernameReturned");
        Assert.assertEquals("Username returned from User.toString()", username, instance.toString());
    }

    /**
     * Test of equals method, of class User.
     */
    @Test
    public void testEquals_usersInstancesWithSameUsernames_trueReturned() {       
        User user1 = new User("user");
        User user2 = new User("user");
        Assert.assertTrue(user1.equals(user2));        
    }
    
    /**
     * Test of equals method, of class User.
     */
    @Test
    public void testEquals_usersInstancesWithDifferentUsernames_falseReturned() {       
        User user1 = new User("user1");
        User user2 = new User("user2");
        Assert.assertFalse(user1.equals(user2));        
    }

    /**
     * Test of hashCode method, of class User.
     */
    @Test
    public void testHashCode_hashCodeSameAsFromUserNameString_retrievedHashCodeequalsToUsernameHashCode() {       
        assertEquals("User's hashCode equals to username's hashCode:", username.hashCode(), instance.hashCode());        
    }
    
    @Test
    public void testUserFromRequest_validMessagePassedToConstructor_userPasswordMatchesPasswordGiven() throws RequestSyntaxException{
        String message = username;
        Request request = new Request(username);
        User requestUser = new User(request);
        assertEquals("Usernames of user for username: " + username + " matching.", username, requestUser.toString());        
    }
    
}
