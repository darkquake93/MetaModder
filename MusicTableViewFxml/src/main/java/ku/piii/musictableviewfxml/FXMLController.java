package ku.piii.musictableviewfxml;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
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
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
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
    private Label label;

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
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setContentText("Music Manager by Dan \n **Listen to yourself**");
        alert.showAndWait();
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
    private void SetMeta(final ActionEvent event) {

        TableViewFactory myFactory = new TableViewFactory();
        String titleSelection = selectMeta.getItems().get(0).toString();
        String yearSelection = selectMeta.getItems().get(1).toString();
        String genreSelection = selectMeta.getItems().get(2).toString();
//        Alert alert = new Alert(AlertType.INFORMATION);
//        alert.setContentText("Sets Metadata for selected items (todo)");
//        alert.showAndWait();

        System.out.println("First option in attribute drop-down is: " + selectMeta.getItems().get(0));
        System.out.println("Value to set to is: " + setTo.getText());

        ObservableList<MusicMedia> items = tableView.getSelectionModel().getSelectedItems();
        for (MusicMedia media : items) {
            System.out.println("Reference to item is: " + media);
            System.out.println("Original Genre is: " + media.getGenre());
            System.out.println("Original Year is: " + media.getYear());
            System.out.println("Original Title is: " + media.getTitle());
            try {
                if (selectMeta.getSelectionModel().getSelectedItem() == genreSelection) {
                    TableViewFactory.processInput(media, setTo.getText(), "genre");
                }
                if (selectMeta.getSelectionModel().getSelectedItem() == yearSelection) {
                    TableViewFactory.processInput(media, setTo.getText(), "year");
                }
                if (selectMeta.getSelectionModel().getSelectedItem() == titleSelection) {
                    TableViewFactory.processInput(media, setTo.getText(), "title");
                }
            } catch (Exception e) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setContentText("Sorry, something went wrong");
                alert.showAndWait();
            }
        }
tableView.getSelectionModel().getTableView().setVisible(false);
tableView.getSelectionModel().getTableView().setVisible(true);

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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedfolder.setText("Use File > Open..");
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
