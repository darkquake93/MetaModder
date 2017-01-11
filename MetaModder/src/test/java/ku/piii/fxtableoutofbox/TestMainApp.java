package ku.piii.fxtableoutofbox;

import java.awt.AWTException;
import java.awt.RenderingHints.Key;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;

import org.loadui.testfx.GuiTest;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import ku.piii.model.MusicMedia;
import ku.piii.model.MusicMediaCollection;
import ku.piii.music.MusicService;
import ku.piii.music.MusicServiceFactory;
import static org.hamcrest.Matchers.contains;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

import org.loadui.testfx.controls.TableViews;
import org.loadui.testfx.exceptions.NoNodesFoundException;

@SuppressWarnings("restriction")
public class TestMainApp extends GuiTest {

    String pathScannedOnLoad = "../test-music-files/collection-A";
    static MusicService MUSIC_SERVICE = MusicServiceFactory.getMusicServiceInstance();

    @Override
    protected Parent getRootNode() {
        Parent parent = null;
        try {
            parent = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));
        } catch (IOException ex) {
            Assert.fail("no Scene.fxml definition file");
            return null;
        }
        return parent;
    }

    @Test
    public void isTableViewListCorrect() throws InterruptedException, AWTException {

        //GUI TEST SUITE
        //MUST NOT MOVE MOUSE WHILE THIS TEST PERFORMS!!
        //Note that Collection A (being tested) can be modified from the GUI window by the user and the test will still succeed, because the test is verifying that the data loaded into the tableview is the same as the data on the disk. This is because when the user overtypes any values the change is IMMEDIATELY written to the disk.
        Robot bot = new Robot();
        // seems all key-sequences must be followed by a delay..
        
        // Welcome message causes "timeout exception", though not critical and tests proceed
        // Sequence to dismiss welcome box
        bot.keyPress(KeyEvent.VK_ENTER);
        bot.delay(200);
        bot.keyRelease(KeyEvent.VK_ENTER);
        bot.delay(200);

        // hot-key sequence to load "Collection A" ..
        bot.keyPress(KeyEvent.VK_CONTROL);
        bot.keyPress(KeyEvent.VK_3);
        bot.delay(200);
        bot.keyRelease(KeyEvent.VK_3);
        bot.keyRelease(KeyEvent.VK_CONTROL);
        bot.delay(200);

        // alt-tab sequence to force file-open dialog to front..
        // NOTE: not needed in normal operation, but seems to be necessary to the test environment
        bot.keyPress(KeyEvent.VK_ALT);
        bot.keyPress(KeyEvent.VK_TAB);
        bot.delay(200);
        bot.keyRelease(KeyEvent.VK_TAB);
        bot.keyRelease(KeyEvent.VK_ALT);
        bot.delay(200);

        // sequence to accept this library by pressing Enter..
        bot.keyPress(KeyEvent.VK_ENTER);
        bot.delay(200);
        bot.keyRelease(KeyEvent.VK_ENTER);
        bot.delay(2000);

        //Reports back the path to read from file
        System.out.println("WILL OPEN " + pathScannedOnLoad);

        //Creates collection from that directory
        MusicMediaCollection fromFile = MUSIC_SERVICE
                .createMusicMediaCollection(Paths.get(pathScannedOnLoad));

        //Identify the table control and load up its contents
        TableView t = getTableView("#tableView");
        ObservableList<MusicMedia> view_items = t.getItems();

        //Check that the number of items in the table is equivalent to the exact number known to be in Collection A
        assertEquals(9, view_items.size());

        //Create a true MusicMediaCollection from the ObservableList
        MusicMediaCollection fromView = new MusicMediaCollection();
        view_items.forEach(m -> fromView.addMusicMedia(m));

        //Get simple lists of media from the Collections
        List<MusicMedia> file_list = fromFile.getMusic();
        List<MusicMedia> view_list = fromView.getMusic();
        //Loop over the music media entries and report back the entry number
        //Compare the attributes
        for (int i = 0; i < file_list.size(); i++) {
            System.out.println("checking media entry number " + i);

            assertEquals(file_list.get(i).getTitle(),
                    view_list.get(i).getTitle());
            assertEquals(file_list.get(i).getGenre(),
                    view_list.get(i).getGenre());
            assertEquals(file_list.get(i).getYear(),
                    view_list.get(i).getYear());
        }

    }

    protected static Object cellValue(String tableSelector, int row, int column) {
        return getTableView(tableSelector).getColumns().get(column).getCellData(row);
    }

    protected static TableCell<?, ?> cell(String tableSelector, int row, int column) {
        List<Node> current = row(tableSelector, row).getChildrenUnmodifiable();
        while (current.size() == 1 && !(current.get(0) instanceof TableCell)) {
            current = ((Parent) current.get(0)).getChildrenUnmodifiable();
        }

        Node node = current.get(column);
        if (node instanceof TableCell) {
            return (TableCell<?, ?>) node;
        } else {
            throw new RuntimeException("Expected TableRowSkin with only TableCells as children");
        }
    }

    protected static TableRow<?> row(String tableSelector, int row) {

        TableView<?> tableView = getTableView(tableSelector);

        List<Node> current = tableView.getChildrenUnmodifiable();
        while (current.size() == 1) {
            current = ((Parent) current.get(0)).getChildrenUnmodifiable();
        }

        current = ((Parent) current.get(1)).getChildrenUnmodifiable();
        while (!(current.get(0) instanceof TableRow)) {
            current = ((Parent) current.get(0)).getChildrenUnmodifiable();
        }

        Node node = current.get(row);
        if (node instanceof TableRow) {
            return (TableRow<?>) node;
        } else {
            throw new RuntimeException("Expected Group with only TableRows as children");
        }
    }

    private static <T> TableView<T> getTableView(String tableSelector) {
        Node node = find(tableSelector);
        if (!(node instanceof TableView)) {
            throw new NoNodesFoundException(tableSelector + " selected " + node + " which is not a TableView!");
        }
        return (TableView<T>) node;
    }

    public static class MusicMediaEquality {

        public final MusicMedia musicMedia;

        public MusicMediaEquality(final MusicMedia musicMedia) {
            this.musicMedia = musicMedia;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .append(musicMedia.getPath())
                    .append(musicMedia.getTitle())
                    .append(musicMedia.getYear()).toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            final MusicMediaEquality other = (MusicMediaEquality) obj;
            return new EqualsBuilder()
                    .append(musicMedia.getPath(), other.musicMedia.getPath())
                    .append(musicMedia.getTitle(), other.musicMedia.getTitle())
                    .append(musicMedia.getYear(), other.musicMedia.getYear()).isEquals();
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(musicMedia);
        }
    }

}
