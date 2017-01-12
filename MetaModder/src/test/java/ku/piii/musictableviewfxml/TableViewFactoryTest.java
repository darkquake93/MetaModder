/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ku.piii.musictableviewfxml;

import java.util.List;
import javafx.scene.control.TableView;
import ku.piii.model.MusicMedia;
import ku.piii.model.MusicMediaColumnInfo;
import org.hamcrest.junit.ExpectedException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;

/**
 *
 * @author Daniel
 */
public class TableViewFactoryTest {
    
    public TableViewFactoryTest() {
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

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testProcessInput1() {
        System.out.println("Test of the ProcessInput method: bad Property arg");
        MusicMedia editItem = null;
        String newValue = "doesn't matter";
        String editProperty = "also doesn't matter";
        exception.expect(IllegalArgumentException.class);
        TableViewFactory.processInput(editItem, newValue, editProperty);
        fail( "Should have thrown!" );
    }

    @Test
    public void testProcessInput2() {
        System.out.println("Test of the ProcessInput method: bad genre text");
        MusicMedia editItem = new MusicMedia();
        editItem.setGenre("Rock");
        editItem.setYear("1999");
        editItem.setTitle("Some Song");
        String newValue = "Unacceptable Genre!";
        String editProperty = "genre";
        exception.expect(IllegalArgumentException.class);
        TableViewFactory.processInput(editItem, newValue, editProperty);
        fail( "Should have thrown!" );
    }

    @Test
    public void testProcessInput3() {
        System.out.println("Test of the ProcessInput method: attempt good data");
        MusicMedia editItem = new MusicMedia();
        editItem.setGenre("Other");
        editItem.setYear("3000");
        editItem.setTitle("Song that is not yet made");
        String newValue = "Rock";
        String editProperty = "genre";
        String msg = "Can no longer locate this mp3 file: null";
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(msg);
        TableViewFactory.processInput(editItem, newValue, editProperty);
        fail( "Should have thrown!" );
    }
}
