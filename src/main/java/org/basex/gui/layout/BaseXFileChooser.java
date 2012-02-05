package org.basex.gui.layout;

import static org.basex.core.Text.*;
import java.awt.FileDialog;
import java.io.File;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.dialog.Dialog;
import org.basex.io.IOFile;
import org.basex.util.Util;

/**
 * Project specific File Chooser implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseXFileChooser {
  /** File dialog mode. */
  public enum Mode {
    /** Open file.              */ FOPEN,
    /** Open file or directory. */ FDOPEN,
    /** Open directory.         */ DOPEN,
    /** Save file.              */ FSAVE,
    /** Save file or directory. */ DSAVE,
  }

  /** Reference to main window. */
  private GUI gui;
  /** Swing file chooser. */
  private JFileChooser fc;
  /** Simple file dialog. */
  private FileDialog fd;
  /** File suffix. */
  private String suffix;

  /**
   * Default constructor.
   * @param title dialog title
   * @param path initial path
   * @param main reference to main window
   */
  public BaseXFileChooser(final String title, final String path,
      final GUI main) {

    if(main.gprop.is(GUIProp.SIMPLEFD)) {
      fd = new FileDialog(main, title);
      fd.setDirectory(new File(path).getPath());
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
   * @param dsc description
   * @param suf suffix
   */
  public void addFilter(final String dsc, final String... suf) {
    if(fc != null) {
      final FileFilter ff = fc.getFileFilter();
      fc.addChoosableFileFilter(new Filter(suf, dsc));
      fc.setFileFilter(ff);
    } else {
      fd.setFile('*' + suf[0]);
    }
    // treat first filter as default
    if(suffix == null) suffix = suf[0];
  }

  /**
   * Selects a file or directory.
   * @param mode type defined by {@link Mode}
   * @return resulting input reference
   */
  public IOFile select(final Mode mode) {
    IOFile io;
    if(fd != null) {
      if(mode == Mode.FDOPEN) fd.setFile(" ");
      fd.setMode(mode == Mode.FSAVE || mode == Mode.DSAVE ?
          FileDialog.SAVE : FileDialog.LOAD);
      fd.setVisible(true);
      final String f = fd.getFile();
      if(f == null) return null;

      final String dir = fd.getDirectory();
      return new IOFile(mode == Mode.DOPEN || mode == Mode.DSAVE ? dir :
        dir + '/' + fd.getFile());
    }

    int state = 0;
    switch(mode) {
      case FOPEN:
        state = fc.showOpenDialog(gui);
        break;
      case FDOPEN:
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        state = fc.showOpenDialog(gui);
        break;
      case FSAVE:
        state = fc.showSaveDialog(gui);
        break;
      case DOPEN:
      case DSAVE:
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        state = fc.showDialog(gui, null);
        break;
    }
    if(state != JFileChooser.APPROVE_OPTION) return null;
    io = new IOFile(fc.getSelectedFile().getPath());

    if(mode != Mode.FSAVE) return io;

    // add file suffix to file to be saved
    if(suffix != null && !io.path().contains("."))
      io = new IOFile(io.path() + suffix);

    // show replace dialog
    return !io.exists() || Dialog.confirm(gui, Util.info(FILE_EXISTS_X, io)) ?
        io : null;
  }

  /**
   * Defines a file filter for a list of extensions.
   */
  private static class Filter extends FileFilter {
    /** Suffixes. */
    private final String[] sufs;
    /** Description. */
    private final String desc;

    /**
     * Constructor.
     * @param s suffixes
     * @param d description
     */
    Filter(final String[] s, final String d) {
      sufs = s;
      desc = d;
    }

    @Override
    public boolean accept(final File file) {
      if(file.isDirectory()) return true;
      final String name = file.getName().toLowerCase(Locale.ENGLISH);
      for(final String s : sufs) if(name.endsWith(s)) return true;
      return false;
    }

    @Override
    public String getDescription() {
      final StringBuilder sb = new StringBuilder();
      for(final String s : sufs) {
        if(sb.length() != 0) sb.append(", ");
        sb.append('*').append(s);
      }
      return desc + " (" + sb + ')';
    }
  }
}
