/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ku.piii.musictableviewfxml;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.*;
import static java.nio.file.StandardCopyOption.*;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import ku.piii.model.MusicMedia;
import ku.piii.model.MusicMediaColumnInfo;

/**
 *
 * @author James
 */
@SuppressWarnings("restriction")
public class TableViewFactory {

    
    static public void processInput(MusicMedia editItem, String newValue, String editProperty)
    {
        if ( !editProperty.equals("year") &&
             !editProperty.equals("genre") &&
             !editProperty.equals("title")) return;
        
        System.out.println("New value is " + newValue + " for property " + editProperty);
        
        // reference:  https://github.com/mpatric/mp3agic
        //             .. shows how to get or set ID3v2 tags.
  
        String oldPath = editItem.getPath();
        String newPath = oldPath + ".tmp";
        Mp3File mp3;
        try {
            mp3 = new Mp3File(oldPath);
        } catch (Exception ex) {
            Logger.getLogger(TableViewFactory.class.getName()).log(Level.SEVERE, null, ex);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Can no longer locate this mp3 file");
            alert.showAndWait();
            return;
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

        if (editProperty.equals("genre")) id3v2Tag.setGenreDescription(newValue);
        if (editProperty.equals("year" )) id3v2Tag.setYear(newValue);
        if (editProperty.equals("title")) id3v2Tag.setTitle(newValue);

        try {
            Logger.getLogger("blah").log(Level.INFO, "writing mp3 to " + newPath);
            mp3.save(newPath);
            Files.move(Paths.get(newPath), Paths.get(oldPath), REPLACE_EXISTING);
        } catch (Exception ex) {
            Logger.getLogger(TableViewFactory.class.getName()).log(Level.SEVERE, null, ex);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Can not write to this mp3 file");
            alert.showAndWait();
            return;
        }
    }

    public static List<MusicMediaColumnInfo> makeColumnInfoList() {
        List<MusicMediaColumnInfo> myColumnInfoList = new ArrayList<MusicMediaColumnInfo>();
        myColumnInfoList.add(new MusicMediaColumnInfo().setHeading("Path")
                                             .setVisible(false)
                                             .setProperty("path")
        );
        myColumnInfoList.add(new MusicMediaColumnInfo().setHeading("Name")
                                             .setMinWidth(200)
                                             .setProperty("name")
        );
        myColumnInfoList.add(new MusicMediaColumnInfo().setHeading("Length (secs)")
                                             .setMinWidth(20)
                                             .setProperty("lengthInSeconds")
        );
        myColumnInfoList.add(new MusicMediaColumnInfo().setHeading("Track Title")
                                             .setMinWidth(100)
                                             .setProperty("title")
        );
        myColumnInfoList.add(new MusicMediaColumnInfo().setHeading("Year")
                                             .setMinWidth(10)
                                             .setProperty("year")
        );
        myColumnInfoList.add(new MusicMediaColumnInfo().setHeading("Genre")
                                             .setMinWidth(100)
                                             .setProperty("genre")
        );
       
        return myColumnInfoList;
        
    }
//    private String path;
//    private Integer lengthInSeconds;
    
//    private Id3Version id3Version;
    // retrieves from the ID tag:
//    private String title;
//    private String year;
//    private String genre;
    
    
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	static public void  makeTable(TableView<MusicMedia>      tableView, 
                                      List<MusicMediaColumnInfo> myColumnInfoList )
    {
        

        for(final MusicMediaColumnInfo myColumnInfo : myColumnInfoList)
        {
            @SuppressWarnings("rawtypes")
			TableColumn thisColumn = new TableColumn(myColumnInfo.getHeading());
            thisColumn.setMinWidth(myColumnInfo.getMinWidth());
            
            thisColumn.setVisible(myColumnInfo.getVisible());
               
            thisColumn.setCellValueFactory(
                new PropertyValueFactory<MusicMedia, String>(myColumnInfo.getProperty())
            );
            thisColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            
            thisColumn.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<MusicMedia, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<MusicMedia, 
                                       String> editEvent) {
                    
                        int editRow = editEvent.getTablePosition().getRow();
                        MusicMedia editItem = editEvent.getTableView()
                                                        .getItems()
                                                        .get(editRow);                    
                        processInput(editItem, 
                                     editEvent.getNewValue(), 
                                     myColumnInfo.getProperty());
                    }
                }
            );
            tableView.getColumns().add(thisColumn);
        }
    }
     
}
