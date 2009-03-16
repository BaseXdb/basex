package org.basex.gui.layout;

import static org.basex.Text.*;
import java.awt.FileDialog;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.basex.BaseX;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.io.IO;

/**
 * Project specific File Chooser implementation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BaseXFileChooser {
  /** File Dialog Mode. */
  public enum Mode {
    /** Open. */ OPEN,
    /** OpenDir.  */ OPENDIR,
    /** Save. */ SAVE,
    /** Dir.  */ DIR
  }
  
  /** Reference to main window. */
  private GUI gui;
  /** Swing file chooser. */
  private JFileChooser fc; 
  /** Simple file dialog. */
  private FileDialog fd;
  /** File mode. */
  private Mode mode;
  
  /**
   * Default Constructor.
   * @param title dialog title
   * @param path initial path
   * @param main reference to main window
   */
  public BaseXFileChooser(final String title, final String path,
      final GUI main) {
    
    if(GUIProp.simplefd) {
      fd = new FileDialog(main, title);
      fd.setDirectory(path);
    } else {
      fc = new JFileChooser(path);
      final File file = new File(path);
      if(!file.isDirectory()) fc.setSelectedFile(file);
      fc.setDialogTitle(title);
      gui = main;
    }
  }
  
  /**
   * Sets a file filter.
   * @param suf suffixes
   * @param dsc description
   */
  public void addFilter(final String[] suf, final String dsc) {
    if(fc != null) fc.addChoosableFileFilter(new Filter(suf, dsc));
    else for(final String s : suf) fd.setFile("*" + s);
  }
  
  /**
   * Selects a file or directory.
   * @param type type defined by {@link Mode}
   * @return file or directory
   */
  public boolean select(final Mode type) {
    mode = type;
    
    if(fd != null) {
      if(type == Mode.OPENDIR) fd.setFile(" ");
      fd.setMode(type == Mode.SAVE ? FileDialog.SAVE : FileDialog.LOAD);
      fd.setVisible(true);
      return fd.getFile() != null;
    }
    
    int state = 0;
    switch(type) {
      case OPEN:
        state = fc.showOpenDialog(gui);
        break;
      case OPENDIR:
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        state = fc.showOpenDialog(gui);
        break;
      case SAVE:
        state = fc.showSaveDialog(gui);
        break;
      case DIR:
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        state = fc.showDialog(gui, null);
        break;
    }
    if(state != JFileChooser.APPROVE_OPTION) return false;

    if(mode == Mode.SAVE) {
      final IO file = getFile();
      if(mode == Mode.SAVE && file.exists()) {
        final int i = JOptionPane.showConfirmDialog(gui,
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
    return IO.get(fd != null ? fd.getDirectory() + "/" + fd.getFile() :
      fc.getSelectedFile().getPath());
  }
  
  /**
   * Returns the selected directory.
   * @return directory
   */
  public String getDir() {
    if(fd != null) return fd.getDirectory();
    return mode == Mode.DIR ? getFile().path() :
      fc.getCurrentDirectory().getPath();
  }

  /**
   * Defines a file filter for XML documents.
   */
  static class Filter extends FileFilter {
    /** Suffix. */
    private String[] suf;
    /** Description. */
    private String desc;
    
    /**
     * Constructor.
     * @param s suffix
     * @param d description
     */
    Filter(final String[] s, final String d) {
      suf = s;
      desc = d;
    }
    
    @Override
    public boolean accept(final File file) {
      if(file.isDirectory()) return true;
      final String name = file.getName().toLowerCase();
      for(final String s : suf) if(name.endsWith(s)) return true;
      return false;
    }
    @Override
    public String getDescription() {
      return desc;
    }
  }
}
