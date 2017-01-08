/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ku.piii.model;

import ku.piii.model.MusicMedia;

/**
 *
 * @author James
 */
public class MusicMediaColumnInfo {

    public String getHeading() {
        return heading;
    }

    public MusicMediaColumnInfo setHeading(String heading) {
        this.heading = heading;
        return this;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public MusicMediaColumnInfo setMinWidth(int minWidth) {
        this.minWidth = minWidth;
        return this;
    }

    public String getProperty() {
        return property;
    }

    public MusicMediaColumnInfo setProperty(String property) {
        this.property = property;
        return this;
    }

    public String getStyle() {
        return style;
    }

    public MusicMediaColumnInfo setStyle(String style) {
        this.style = style;
        return this;
    }

    public boolean getVisible() {
        return visible;
    }

    public MusicMediaColumnInfo setVisible(boolean new_visible) {
        this.visible = new_visible;
        return this;
    }
    
    public boolean getEditable() {
        return editable;
    }

    public MusicMediaColumnInfo setEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    private boolean visible = true;
    private boolean editable = true;
    private String heading;
    private int minWidth;
    private String property;
    private String style = "-fx-background-color:#fcc";

    
}
