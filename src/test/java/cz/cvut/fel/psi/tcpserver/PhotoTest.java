/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver;

import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test of {@link Photo} class.
 * @author Matej
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class PhotoTest {
    
    @Mock
    private Session session;
    
    public PhotoTest() {
    }
    
    /**
     * Test of validateChecksum method, of class Photo.
     */
    @Test
    public void testValidateChecksum_validRequest_validChecksumOfPhoto_trueReturned() throws RequestSyntaxException {
        String msg = "FOTO 823 ABCDEFGH\\x00\\x00\\x02\\x24";
        Request req = new Request(msg);
        
        Mockito.when(session.getPhotoCounter()).thenReturn(0);
        Photo instance = new Photo(req, session);
        Assert.assertTrue("For request " + msg + " expected valid checksum returned.", instance.validateChecksum());
    }
    
    /**
     * Test of validateChecksum method, of class Photo.
     */
    @Test
    public void testValidateChecksum_validRequest_validChecksumOfPhoto_falseReturned() throws RequestSyntaxException {
        String msg = "FOTO 823 ABCDEFGI\\x00\\x00\\x02\\x24";
        Request req = new Request(msg);        
        Mockito.when(session.getPhotoCounter()).thenReturn(0);
        Photo instance = new Photo(req, session);
        Assert.assertFalse("For request " + msg + " expected not-valid checksum returned.", instance.validateChecksum());
    }

    /**
     * Test of getSize method, of class Photo.
     * @throws cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException
     */
    @Test
    public void testGetSize_validRequest_expectedSizeGiven() throws RequestSyntaxException {
        String msg = "FOTO 823 ABCDEFGH\\x00\\x00\\x02\\x24";
        Request req = new Request(msg);
        int expectedSize = 823;
        Mockito.when(session.getPhotoCounter()).thenReturn(0);
        Photo instance = new Photo(req, session);
        Assert.assertEquals("For request " + msg + " expected size equals size given.", expectedSize, instance.getSize());
    }

    /**
     * Test of getPhoto method, of class Photo.
     */
    @Test
    public void testGetPhoto_validRequest_expectedFotoGiven() throws RequestSyntaxException {
        String msg = "FOTO 823 ABCDEFGH\\x00\\x00\\x02\\x24";
        Request req = new Request(msg);
        String expectedPhoto = "ABCDEFGH";
        Mockito.when(session.getPhotoCounter()).thenReturn(0);
        Photo instance = new Photo(req, session);
        Assert.assertEquals("For request " + msg + " expected photo equals photo given.", expectedPhoto, new String(instance.getPhoto()));
    }

    /**
     * Test of getChecksum method, of class Photo.
     */
    @Test
    public void testGetChecksum_validRequest_expectedChecksumGiven() throws RequestSyntaxException {
        String msg = "FOTO 823 ABCDEFGH\\x00\\x00\\x02\\x24";
        Request req = new Request(msg);
        Integer expectedChecksum = 548;
        Mockito.when(session.getPhotoCounter()).thenReturn(0);
        Photo instance = new Photo(req, session);
        Assert.assertEquals("For request " + msg + " expected checksum equals checksum given.", expectedChecksum, instance.getChecksum());
    }
    
}
