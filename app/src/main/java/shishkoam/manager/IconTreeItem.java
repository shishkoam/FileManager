package shishkoam.manager;

import java.io.File;

/**
 * Created by ав on 26.12.2015.
 */
public class IconTreeItem {
    private int icon;
    private String text;
    private boolean checked = false;
    private boolean opened = false;
    private boolean isEmpty = false;
    private double contains = 0;
    private long size = 0;
    private File file;

    public IconTreeItem(int icon, String text) {
        this.icon = icon;
        this.text = text;
    }
    public IconTreeItem( String text) {
        this.text = text;
    }
    public boolean isChecked(){
        return checked;
    }
    public boolean isOpened(){
        return opened;
    }
    public boolean isEmpty(){
        return checked;
    }
    public long getSize(){
        return size;
    }
    public String getText(){
        return text;
    }
    public void setChecked(boolean checked){
        this.checked = checked;
    }
    public void setOpened(boolean opened){
        this.opened = opened;
    }
    public void setEmpty(boolean isEmpty){
        this.isEmpty = isEmpty;
    }
    public void setSize(long size){
        this.size = size;
    }
    public void setFile(File file){this.file = file;}
    public File getFile(){return file;}

}