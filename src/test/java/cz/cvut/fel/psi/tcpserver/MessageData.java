/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Matej
 */
public class MessageData {           
    public static Collection getData(){
        return Arrays.asList(new Object[][]{
            {RequestType.USERNAME, "Robot Emil cislo 33", true},
            {RequestType.USERNAME, "RobotEmil cislo 33", true},
            {RequestType.USERNAME, "Robot345", true},
            {RequestType.USERNAME, "Ja jsem Robot Emil cislo 33", true},
            {RequestType.PASSWORD, "1645", true},
            {RequestType.PASSWORD, "1645a", false},
            {RequestType.INFO, "INFO 2014-02-19 03:35 blíží se jiný robot", true},
            {RequestType.INFO, "Moje INFO 2014-02-19 03:35 blíží se jiný robot", false},
            {RequestType.INFO, "2014-02-19 03:35 blíží se jiný robot", false},
            {RequestType.INFO, "INFO2014-02-19 03:35 blíží se jiný robot", false},
            {RequestType.PHOTO, "FOTO 823 ABCDEFGH\\x00\\x00\\x02\\x24", true},
            {RequestType.PHOTO, "FOTO 823 ABCDEFGH\\x00\\x00\\x02\\x12", true},
            {RequestType.PHOTO, "FOTO823 ABCDEFGH\\x00\\x00\\x02\\x24", false},
            {RequestType.PHOTO, "FOTO 823a ABCDEFGH\\x00\\x00\\x02\\x24", false},
    });
    }
    
}
