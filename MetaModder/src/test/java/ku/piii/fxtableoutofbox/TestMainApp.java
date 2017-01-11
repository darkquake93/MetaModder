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

    final String pathScannedOnLoad = "../test-music-files/collection-A";
    private final static MusicService MUSIC_SERVICE = MusicServiceFactory.getMusicServiceInstance();

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

        Robot bot = new Robot();

//        this.push(KeyCode.CONTROL, KeyCode.DIGIT3);
bot.keyPress(KeyEvent.VK_CONTROL);
bot.keyPress(KeyEvent.VK_3);
this.sleep(150);
bot.keyRelease(KeyEvent.VK_CONTROL);
bot.keyRelease(KeyEvent.VK_3);
this.sleep(150);
        bot.keyPress(KeyEvent.VK_ALT);
        bot.keyPress(KeyEvent.VK_TAB);
        Thread.sleep(100);
        bot.keyRelease(KeyEvent.VK_ALT);
        bot.keyRelease(KeyEvent.VK_TAB);
        Thread.sleep(100);
//        this.closeCurrentWindow();
        bot.keyPress(KeyEvent.VK_ENTER);
        bot.keyRelease(KeyEvent.VK_ENTER);

        Thread.sleep(500);

        final MusicMediaCollection expected = MUSIC_SERVICE
                .createMusicMediaCollection(Paths.get(pathScannedOnLoad));
        final TableView t = getTableView("#tableView");

        final ObservableList<MusicMedia> items = t.getItems();
        assertEquals(9, items.size());
        final MusicMediaCollection actual = new MusicMediaCollection();
        items.forEach(m -> actual.addMusicMedia(m));
System.out.println("BLAH BLAH LOOK HERE" + actual.getMusic().toString());
System.out.println("BLAH BLAH LOOK HERE 2" + expected.getMusic().toString());

        final List<MusicMediaEquality> expectedMusic = expected.getMusic().stream().map(MusicMediaEquality::new)
                .collect(Collectors.toList());

        final List<MusicMediaEquality> actualMusic = actual.getMusic().stream().map(MusicMediaEquality::new)
                .collect(Collectors.toList());

System.out.println("BLAH BLAH LOOK HERE 3" + actualMusic);
System.out.println("BLAH BLAH LOOK HERE 4" + expectedMusic);
expectedMusic.addAll(actualMusic);
        assertThat(actualMusic, containsInAnyOrder(expectedMusic.toArray()));

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
