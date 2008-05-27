package org.basex.gui.layout;

import java.awt.Component;
import java.awt.FileDialog;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import org.basex.gui.GUIProp;
import org.basex.io.IO;

/**
 * Project specific File Chooser implementation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXFileChooser {
  /** Open dialog. */
  public static final int OPEN = JFileChooser.OPEN_DIALOG;
  /** Save dialog. */
  public static final int SAVE = JFileChooser.SAVE_DIALOG;
  /** Directory chooser. */
  public static final int DIR = JFileChooser.CUSTOM_DIALOG;
  /** Parent component. */
  private Component parent;
  /** Swing file chooser. */
  private JFileChooser fc; 
  /** Simple file dialog. */
  private FileDialog fd;
  /** File mode. */
  private int mode;
  
  /**
   * Default Constructor.
   * @param title dialog title
   * @param path initial path
   * @param par parent reference
   */
  public BaseXFileChooser(final String title, final String path,
      final JFrame par) {
    if(GUIProp.simplefd) {
      fd = new FileDialog(par, title);
      fd.setDirectory(path);
    } else {
      fc = new JFileChooser(path);
      fc.setDialogTitle(title);
      parent = par;
    }
  }
  
  /**
   * Sets a file filter.
   * @param suf suffix
   * @param dsc description
   */
  public void addFilter(final String suf, final String dsc) {
    if(fc != null) fc.addChoosableFileFilter(new Filter(suf, dsc));
    else fd.setFile("*" + suf);
  }
  
  /**
   * Selects a file or directory.
   * @param type type ({@link #OPEN}, {@link #SAVE} or {@link #DIR})
   * @return file or directory
   */
  public boolean select(final int type) {
    mode = type;
    
    if(fd != null) {
      fd.setVisible(true);
      return fd.getFile() != null;
    }
    
    int state = 0;
    switch(type) {
      case 0:
        state = fc.showOpenDialog(parent);
        break;
      case 1:
        state = fc.showSaveDialog(parent);
        break;
      case 2:
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        state = fc.showDialog(parent, null);
        break;
    }
    return state == JFileChooser.APPROVE_OPTION;
  }
  
  /**
   * Returns the selected file.
   * @return file
   */
  public IO getFile() {
    return new IO(fc != null ? fc.getSelectedFile().getPath() :
      fd.getDirectory() + "/" + fd.getFile());
  }
  
  /**
   * Returns the selected directory.
   * @return directory
   */
  public String getDir() {
    if(fd != null) return fd.getDirectory();
    return mode == DIR ? getFile().path() : fc.getCurrentDirectory().getPath();
  }


  /**
   * Defines a file filter for XML documents.
   */
  static class Filter extends FileFilter {
    /** Suffix. */
    private String suf;
    /** Description. */
    private String desc;
    
    /**
     * Constructor.
     * @param s suffix
     * @param d description
     */
    Filter(final String s, final String d) {
      suf = s;
      desc = d;
    }
    
    @Override
    public boolean accept(final File file) {
      return file.isDirectory() || file.getName().toLowerCase().endsWith(suf);
    }
    @Override
    public String getDescription() {
      return desc;
    }
  }
}
