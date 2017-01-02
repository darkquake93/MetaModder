package ku.piii.musictableviewfxml;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

    @FXML
    private Label label;

    @FXML
    private TextField selectedfolder;

    @FXML
    private String pathScannedOnLoad;

    @FXML
    private TableView<MusicMedia> tableView;

    @FXML
    private void handleAboutAction(final ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setContentText("Music Manager by Dan \n **FEAR WHAT YOU HEAR**");
        alert.showAndWait();
    }

    @FXML
    private void handleFileOpenPrivate(final ActionEvent event) {
        libraryChooser(System.getProperty("user.home") + "\\Music");
    }
    
    @FXML
    private void handleFileOpenPublic(final ActionEvent event) {
        libraryChooser("C:\\Users\\Public\\Music");
    }
    
    private void libraryChooser(String dirName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(dirName));
        chooser.setDialogTitle("Browse to Media Library");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

        pathScannedOnLoad = chooser.getSelectedFile().toString();
        selectedfolder.setText(pathScannedOnLoad);

        final MusicMediaCollection collection = 
            MUSIC_SERVICE.createMusicMediaCollection(Paths.get(pathScannedOnLoad));

        dataForTableView = FXCollections.observableArrayList(collection.getMusic());
        dataForTableView.addListener(makeChangeListener(collection));
        List<MusicMediaColumnInfo> myColumnInfoList = TableViewFactory.makeColumnInfoList();
        tableView.setItems(dataForTableView);
        TableViewFactory.makeTable(tableView, myColumnInfoList);
        tableView.setEditable(true);
    }

    @FXML
    private void handleKeyInput(final InputEvent event) {
        if (event instanceof KeyEvent) {
            final KeyEvent keyEvent = (KeyEvent) event;
            if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A) {
                System.out.println("sfsds");
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

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
