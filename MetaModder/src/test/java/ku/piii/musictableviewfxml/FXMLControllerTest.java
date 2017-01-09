/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ku.piii.musictableviewfxml;

import java.net.URL;
import java.util.ResourceBundle;
import org.hamcrest.MatcherAssert;
import static org.hamcrest.Matchers.equalTo;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Daniel
 */
public class FXMLControllerTest {
    
    public FXMLControllerTest() {
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

    /**
     * Test of showAlert method, of class FXMLController.
     */
    @Test
    public void testShowAlert() {
        System.out.println("showAlert test");
        String header = "Hey there";
        String content = "Test message";
        String graphic = "/lasermeta.png";
        FXMLController instance = new FXMLController();
        instance.showAlert(header, content, graphic);
        boolean result = instance.showAlert(header, content, graphic);
        MatcherAssert.assertThat("true", result);
    }
    
}
