package ku.piii.musictableviewfxml;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
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
        showAlert("About MetaModder", "A Music Manager by Daniel Carnovale \n Created to help manage your audio library Metadata \n **Listen to yourself**", "/soundbars.gif");
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
        libraryChooser("resources/test-music-files/collection-A");
    }

    @FXML
    private void handleFileOpenB(final ActionEvent event) {
        libraryChooser("resources/test-music-files/collection-B");
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
    private void callBrowser(String url, String urlStyle) {
        MusicMedia media = tableView.getSelectionModel().getSelectedItem();
        if (media == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText("Sorry, there is no selection");
            alert.showAndWait();
            return;
        }
        if (media.getTitle() == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText("Sorry, the Title attribute is missing");
            alert.showAndWait();
            return;
        }
        String fileName = media.getTitle();
        String query = "";
        if (urlStyle.equals("B")) {
            String fileNameNoSpaces = fileName.replaceAll(" ", "+");
            query = url + fileNameNoSpaces;
        } else if (urlStyle.equals("W")) {
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
    }

    @FXML
    private void getBPM(final ActionEvent event) {

        callBrowser("https://www.bpmdatabase.com/music/search/?q=", "B");
    }

    @FXML
    private void getWiki(final ActionEvent event) {

        callBrowser("https://en.wikipedia.org/w/index.php?search=", "W");

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
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setContentText("Sorry, something went wrong");
                alert.showAndWait();
                return;
            }
        }

        // now rebuild all rows in the table by re-reading all files in the folder..
        final MusicMediaCollection collection
                = MUSIC_SERVICE.createMusicMediaCollection(Paths.get(pathScannedOnLoad));
        dataForTableView = FXCollections.observableArrayList(collection.getMusic());
        dataForTableView.addListener(makeChangeListener(collection));
        tableView.setItems(dataForTableView);
    }

    private void libraryChooser(String dirName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(dirName));
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
        alert.setGraphic(imageView);
        setIconScene(alert);
//        alert.showAndWait();
        alert.show();
        alertShowing = alert.isShowing();
        alert.close();
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
