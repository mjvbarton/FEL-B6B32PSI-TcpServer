/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver;

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
    public void testGetPassword_validUser_gotPasswordMatchesExpectedPassword() {      
        System.out.println("testGetPassword_validUser_gotPasswordMatchesExpectedPassword");
        User user = new User(username);
        String gotPassword = user.getPassword();
        Assert.assertEquals("Expected password matches password retrieved from User.getPassword():", expectedPassword, gotPassword);
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
    
}
