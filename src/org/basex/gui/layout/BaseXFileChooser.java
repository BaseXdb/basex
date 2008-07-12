package org.basex.gui.layout;

import static org.basex.Text.*;

import java.awt.Component;
import java.awt.FileDialog;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.basex.BaseX;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.io.IO;

/**
 * Project specific File Chooser implementation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXFileChooser {
  /** File Dialog Mode. */
  public enum MODE {
    /** Open. */ OPEN,
    /** OpenDir.  */ OPENDIR,
    /** Save. */ SAVE,
    /** Dir.  */ DIR
  }
  
  /** Parent component. */
  private Component parent;
  /** Swing file chooser. */
  private JFileChooser fc; 
  /** Simple file dialog. */
  private FileDialog fd;
  /** File mode. */
  private MODE mode;
  
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
      if(path.contains(".")) fc.setSelectedFile(new File(path));
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
   * @param type type defined by {@link MODE}
   * @return file or directory
   */
  public boolean select(final MODE type) {
    mode = type;
    
    if(fd != null) {
      if(type == MODE.OPENDIR) fd.setFile(" ");
      fd.setMode(type == MODE.SAVE ? FileDialog.SAVE : FileDialog.LOAD);
      fd.setVisible(true);
      return fd.getFile() != null;
    }
    
    int state = 0;
    switch(type) {
      case OPEN:
        state = fc.showOpenDialog(parent);
        break;
      case OPENDIR:
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        state = fc.showOpenDialog(parent);
        break;
      case SAVE:
        state = fc.showSaveDialog(parent);
        break;
      case DIR:
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        state = fc.showDialog(parent, null);
        break;
    }
    if(state != JFileChooser.APPROVE_OPTION) return false;

    if(mode == MODE.SAVE) {
      final IO file = getFile();
      if(mode == MODE.SAVE && file.exists()) {
        final int i = JOptionPane.showConfirmDialog(GUI.get(),
            BaseX.info(FILEREPLACE, file.name()),
            DIALOGINFO, JOptionPane.YES_NO_OPTION);
        if(i == JOptionPane.NO_OPTION) return false;
      }
    }
    return true;
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
    return mode == MODE.DIR ? getFile().path() :
      fc.getCurrentDirectory().getPath();
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
