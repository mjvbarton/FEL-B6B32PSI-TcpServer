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
            {RequestType.USERNAME, "Robot Emil cislo 33", true, "Robot Emil cislo 33"},
            {RequestType.USERNAME, "RobotEmil cislo 33", true, "RobotEmil cislo 33"},         
            {RequestType.USERNAME, "Robot345", true, "Robot345"},
            {RequestType.USERNAME, "Ja jsem Robot Emil cislo 33", false,  null},
            {RequestType.PASSWORD, "1645", true, "1645"},
            {RequestType.PASSWORD, "1645a", false, null},
            {RequestType.INFO, "INFO 2014-02-19 03:35 blíží se jiný robot", true, "2014-02-19 03:35 blíží se jiný robot"},
            {RequestType.INFO, "Moje INFO 2014-02-19 03:35 blíží se jiný robot", false, null},
            {RequestType.INFO, "2014-02-19 03:35 blíží se jiný robot", false, null},
            {RequestType.INFO, "INFO2014-02-19 03:35 blíží se jiný robot", false, null},
            {RequestType.PHOTO, "FOTO 823 ABCDEFGH\\x00\\x00\\x02\\x24", true, "823 ABCDEFGH\\x00\\x00\\x02\\x24"},
            {RequestType.PHOTO, "FOTO 823 ABCDEFGH\\x00\\x00\\x02\\x12", true, "823 ABCDEFGH\\x00\\x00\\x02\\x12"},
            {RequestType.PHOTO, "FOTO 823 ABCDEFGH\\x00\\x0 0\\x02\\x24", false, null},
            {RequestType.PHOTO, "FOTO823 ABCDEFGH\\x00\\x00\\x02\\x24", false, null},
            {RequestType.PHOTO, "FOTO 823a ABCDEFGH\\x00\\x00\\x02\\x24", false, null},
    });
    }
    
}
