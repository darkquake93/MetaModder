package ku.piii.musictableviewfxml;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import de.umass.lastfm.Artist;
import de.umass.lastfm.Caller;
import de.umass.lastfm.User;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.Chart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javax.swing.JFileChooser;
import ku.piii.model.MusicMedia;
import ku.piii.model.MusicMediaCollection;
import ku.piii.model.MusicMediaColumnInfo;
import ku.piii.music.MusicService;
import ku.piii.music.MusicServiceFactory;

public class FXMLController implements Initializable {

    private final static MusicService MUSIC_SERVICE = MusicServiceFactory.getMusicServiceInstance();

    private ObservableList<MusicMedia> dataForTableView;

//    @FXML
//    private String attrSelect;
    @FXML
    private boolean alertShowing;

    @FXML
    private boolean alertResult;

    @FXML
    private ImageView imageView;

    @FXML
    double scale = Font.getDefault().getSize() / 12.0;

    @FXML
    private boolean isRecursive;

    @FXML
    private Label label;

    @FXML
    private URI bpmLink;

    @FXML
    private TextField selectedfolder;

    @FXML
    private ComboBox selectMeta;

    @FXML
    private TextField setTo;

    @FXML
    private String pathScannedOnLoad;

    @FXML
    private TableView<MusicMedia> tableView;

    @FXML
    private void clearSetTo(MouseEvent event) {
        setTo.clear();
    }

    @FXML
    private void handleAboutAction(final ActionEvent event) {
        showAlert("About MetaModder",
                "A Music Manager by Daniel Carnovale \n\n"
                + "Created to help manage your audio library Metadata \n\n"
                + "**Listen to yourself**", "/soundbars.gif");
    }

    @FXML
    private void handleFileOpenPrivate(final ActionEvent event) {
        libraryChooser(System.getProperty("user.home") + "\\Music");
    }

    @FXML
    private void handleFileOpenPublic(final ActionEvent event) {
        pathScannedOnLoad = "C:\\";
        libraryChooser("C:\\Users\\Public\\Music");
    }

    @FXML
    private void handleFileOpenA(final ActionEvent event) {
        libraryChooser("../test-music-files/collection-A");
    }

    @FXML
    private void handleFileOpenB(final ActionEvent event) {
        libraryChooser("../test-music-files/collection-B");
    }

    @FXML
    private void ExitApp(final ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void CopyTo(final ActionEvent event) {
        if (pathScannedOnLoad == null) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setContentText("Sorry, the current path is null");
            alert.showAndWait();
        } else {
            libraryChooser(pathScannedOnLoad);
        }
    }

    @FXML
    private void invokeTool(String url, String tool) {
        MusicMedia media = tableView.getSelectionModel().getSelectedItem();
        if (media == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText("Sorry, there is no selection");
            alert.showAndWait();
            return;
        }
        if (media.getTitle() == null && !tool.equals("FT")) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText("Sorry, the Title attribute is missing");
            alert.showAndWait();
            return;
        }
        String fileName = media.getTitle();

        if (tool.equals("B") || tool.equals("W")) {

            String query = "";

            if (tool.equals("B")) {
                String fileNameNoSpaces = fileName.replaceAll(" ", "+");
                query = url + fileNameNoSpaces;

            } else if (tool.equals("W")) {
                String fileNameNoSpaces = fileName.replaceAll(" ", "_");
                query = url + (fileNameNoSpaces + "_(song)");
            }

            try {
                Desktop.getDesktop().browse(new URI(query));
            } catch (IOException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (URISyntaxException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }

        if (tool.equals("FT")) {
            String oldPath = media.getPath();
            String newPath = oldPath + ".tmp";
            Mp3File mp3;
            try {
                mp3 = new Mp3File(oldPath);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Can no longer locate this mp3 file: " + ex.getMessage());
            }
            ID3v2 id3v2Tag = mp3.hasId3v2Tag()
                    ? mp3.getId3v2Tag()
                    : new ID3v24Tag();

            if (mp3.hasId3v1Tag()) {
                if (!mp3.hasId3v2Tag()) {
                    ID3v1 id3v1Tag;
                    id3v1Tag = mp3.getId3v1Tag();
                    id3v2Tag.setYear(id3v1Tag.getYear());
                    id3v2Tag.setGenre(id3v1Tag.getGenre());
                    id3v2Tag.setTitle(id3v1Tag.getTitle());
                }
                System.out.println("REMOVING V1 TAG");
                mp3.removeId3v1Tag();
            }
            if (!mp3.hasId3v2Tag()) {
                mp3.setId3v2Tag(id3v2Tag);
            }
            id3v2Tag.setTitle(media.getName());
            try {
                mp3.save(newPath);
                Files.move(Paths.get(newPath), Paths.get(oldPath), REPLACE_EXISTING);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Can not write to this mp3 file: " + ex.getMessage());
            }
            reloadTable();
        }
         
         else if (tool.equals("TF")) {
            String oldPath = media.getPath();
            Path p = Paths.get(oldPath);
            String newPath = p.getParent().toString() + "/" + media.getTitle() + ".mp3";
            System.out.println("Moving " + oldPath + " to " + newPath);
            try {
                Files.move(Paths.get(oldPath), Paths.get(newPath), REPLACE_EXISTING);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Can not write to this mp3 file: " + ex.getMessage());
            }
            reloadTable();
        }

    }

    @FXML
    private void getBPM(final ActionEvent event) {
        invokeTool("https://www.bpmdatabase.com/music/search/?q=", "B");
    }

    @FXML
    private void getWiki(final ActionEvent event) {
        invokeTool("https://en.wikipedia.org/w/index.php?search=", "W");
    }

    @FXML
    private void setFilenameTitle(final ActionEvent event) {
        invokeTool("", "FT");
    }

    @FXML
    private void setTitleFilename(final ActionEvent event) {
        invokeTool("", "TF");
    }

    @FXML
    private void getCharts(final ActionEvent event) {
        //TESTING
        Caller.getInstance().setUserAgent("tst");
        String key = "7a013d73c59d6a41fbeb9f0072576a3e";
        String user = "Darkquake93";
        de.umass.lastfm.Chart<Artist> chart = User.getWeeklyArtistChart(user, 10, key);
        Collection<Artist> chartCol = chart.getEntries();
        System.out.println(Arrays.toString(chartCol.toArray()));
        DateFormat format = DateFormat.getDateInstance();
        String from = format.format(chart.getFrom());
        String to = format.format(chart.getTo());
        System.out.printf("Charts for %s for the week from %s to %s:%n", user, from, to);
        Collection<Artist> artists = chart.getEntries();
        for (Artist artist : artists) {
            System.out.println(artist.getName());
        }

    }

    @FXML
    private void SetMeta(final ActionEvent event) throws URISyntaxException {

        TableViewFactory myFactory = new TableViewFactory();

        String selection = selectMeta.getSelectionModel()
                .getSelectedItem()
                .toString()
                .toLowerCase();

//        Alert alert = new Alert(AlertType.INFORMATION);
//        alert.setContentText("Sets Metadata for selected items (todo)");
//        alert.showAndWait();
        System.out.println("First option in attribute drop-down is: " + selectMeta.getItems().get(0));
        System.out.println("Value to set to is: " + setTo.getText());

        ObservableList<MusicMedia> items = tableView.getSelectionModel().getSelectedItems();
        for (MusicMedia media : items) {
//            System.out.println("Reference to item is: " + media);
//            System.out.println("Original Genre is: " + media.getGenre());
//            System.out.println("Original Year is: " + media.getYear());
//            System.out.println("Original Title is: " + media.getTitle());

            try {
                TableViewFactory.processInput(media, setTo.getText(), selection);
            } catch (Exception e) {
                System.out.println("processInput failure: " + e.getMessage());
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setContentText("Unable to change this data");
                alert.showAndWait();
                return;
            }
        }
        reloadTable();
    }

    private void reloadTable() {
        // rebuild all rows in the table by re-reading all files in the folder..
        final MusicMediaCollection collection
                = MUSIC_SERVICE.createMusicMediaCollection(Paths.get(pathScannedOnLoad));
        dataForTableView = FXCollections.observableArrayList(collection.getMusic());
        dataForTableView.addListener(makeChangeListener(collection));
        tableView.setItems(dataForTableView);
    }
    
    private void libraryChooser(String dirName) {
        JFileChooser chooser = new JFileChooser();
        File file = new java.io.File(dirName);
        System.out.println("dirName was " + dirName + ", setting current directory to : " + file.toString());
        chooser.setCurrentDirectory(file);
        chooser.setDialogTitle("Browse to Media Library");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        pathScannedOnLoad = chooser.getSelectedFile().toString();
        selectedfolder.setText(pathScannedOnLoad);

        final MusicMediaCollection collection
                = MUSIC_SERVICE.createMusicMediaCollection(Paths.get(pathScannedOnLoad));

        dataForTableView = FXCollections.observableArrayList(collection.getMusic());
        dataForTableView.addListener(makeChangeListener(collection));
        List<MusicMediaColumnInfo> myColumnInfoList = TableViewFactory.makeColumnInfoList();
        tableView.setItems(dataForTableView);
        tableView.getColumns().clear();
        TableViewFactory.makeTable(tableView, myColumnInfoList);
        tableView.setEditable(true);
    }

    @FXML
    private void handleKeyInput(final InputEvent event) {
        System.out.println("Key Combination Pressed");
        if (event instanceof KeyEvent) {
            final KeyEvent keyEvent = (KeyEvent) event;
            if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.O) {
                System.out.println("Should open Private folder");
            }
        }
    }

    @FXML
    private void setIconScene(Alert alert) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/audio.png").toString()));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedfolder.setText("Use File > Open..");
        showAlert("Welcome to MetaModder!", "By Daniel Carnovale - K1336511", "/soundbars.gif");
    }

    public void showAlert(String header, String content, String graphic) {

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setHeaderText(header);

        alert.setContentText(content);
        imageView = new ImageView(new Image(getClass().getResourceAsStream(graphic)));
        imageView.setFitWidth(48.0 * scale);
        imageView.setFitHeight(48.0 * scale);
        alert.setGraphic(imageView);
        setIconScene(alert);
        alert.showAndWait();
        System.out.println("showAlert method output for alertShowing: " + alertShowing);
    }

    private static ListChangeListener<MusicMedia> makeChangeListener(final MusicMediaCollection collection) {
        return new ListChangeListener<MusicMedia>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends MusicMedia> change) {
                while (change.next()) {
                    if (change.wasAdded()) {
                        for (MusicMedia addedChild : change.getAddedSubList()) {
                            collection.addMusicMedia(addedChild);
                        }
                    }
                    if (change.wasRemoved()) {
                        for (MusicMedia removedChild : change.getRemoved()) {
                            collection.removeMusicMedia(removedChild);
                        }
                    }
                }

            }
        };

    }

}
