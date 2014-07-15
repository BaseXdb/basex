package org.basex.gui.layout;

import static org.basex.core.Text.*;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.basex.gui.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Project specific File Chooser implementation.
 *
 * @author BaseX Team 2005-14, BSD License
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
  public BaseXFileChooser(final String title, final String path, final GUI main) {
    final IOFile file = new IOFile(path);
    if(main.gopts.get(GUIOptions.SIMPLEFD)) {
      fd = new FileDialog(main, title);
      fd.setDirectory(file.path());
    } else {
      fc = new JFileChooser(path);
      if(!file.isDir()) fc.setSelectedFile(file.file());
      fc.setDialogTitle(title);
      gui = main;
    }
  }

  /**
   * Convenience method for setting textual filters.
   * @return self reference
   */
  public BaseXFileChooser textFilters() {
    filter(XML_DOCUMENTS, IO.XMLSUFFIXES);
    filter(XSL_DOCUMENTS, IO.XSLSUFFIXES);
    filter(HTML_DOCUMENTS, IO.HTMLSUFFIXES);
    filter(JSON_DOCUMENTS, IO.JSONSUFFIX);
    filter(CSV_DOCUMENTS, IO.CSVSUFFIX);
    filter(PLAIN_TEXT, IO.TXTSUFFIXES);
    return this;
  }

  /**
   * Sets a file filter.
   * @param dsc description
   * @param suf suffix
   * @return self reference
   */
  public BaseXFileChooser filter(final String dsc, final String... suf) {
    if(fc != null) {
      final FileFilter ff = fc.getFileFilter();
      fc.addChoosableFileFilter(new Filter(suf, dsc));
      fc.setFileFilter(ff);
    }
    return this;
  }

  /**
   * Sets a file suffix, which will be added if the typed in file has no suffix.
   * @param suf suffix
   * @return self reference
   */
  public BaseXFileChooser suffix(final String suf) {
    suffix = suf;
    return this;
  }

  /**
   * Allow multiple choice.
   * @return self reference
   */
  public BaseXFileChooser multi() {
    if(fc != null) fc.setMultiSelectionEnabled(true);
    return this;
  }

  /**
   * Selects a file or directory.
   * @param mode type defined by {@link Mode}
   * @return resulting input reference, or {@code null} if no file was selected
   */
  public IOFile select(final Mode mode) {
    final IOFile[] files = selectAll(mode);
    return files.length == 0 ? null : files[0];
  }

  /**
   * Selects a file or directory.
   * @param mode type defined by {@link Mode}
   * @return resulting input reference
   */
  public IOFile[] selectAll(final Mode mode) {
    if(fd != null) {
      if(mode == Mode.FDOPEN) fd.setFile(" ");
      fd.setMode(mode == Mode.FSAVE || mode == Mode.DSAVE ?
          FileDialog.SAVE : FileDialog.LOAD);
      fd.setVisible(true);
      final String f = fd.getFile();
      if(f == null) return new IOFile[0];
      final String dir = fd.getDirectory();
      return new IOFile[] { new IOFile(mode == Mode.DOPEN || mode == Mode.DSAVE ? dir :
        dir + '/' + fd.getFile()) };
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
    if(state != JFileChooser.APPROVE_OPTION) return new IOFile[0];

    final File[] fl = fc.isMultiSelectionEnabled() ? fc.getSelectedFiles() :
      new File[] { fc.getSelectedFile() };
    final IOFile[] files = new IOFile[fl.length];
    for(int f = 0; f < fl.length; f++) files[f] = new IOFile(fl[f].getPath());

    if(mode == Mode.FSAVE) {
      // add file suffix to files
      final FileFilter ff = fc.getFileFilter();
      if(suffix != null) {
        for(int f = 0; f < files.length; f++) {
          final String path = files[f].path();
          if(!path.contains(".")) files[f] = new IOFile(path + suffix);
        }
      } else if(ff instanceof Filter) {
        final String[] sufs = ((Filter) ff).sufs;
        for(int f = 0; f < files.length && sufs.length != 0; f++) {
          final String path = files[f].path();
          if(!path.contains(".")) files[f] = new IOFile(path + sufs[0]);
        }
      }

      // show replace dialog
      for(final IOFile io : files) {
        if(io.exists() && !BaseXDialog.confirm(gui, Util.info(FILE_EXISTS_X, io))) {
          return new IOFile[0];
        }
      }
    }
    return files;
  }

  /**
   * Defines a file filter for a list of extensions.
   */
  private static class Filter extends FileFilter {
    /** Suffixes. */
    final String[] sufs;
    /** Description. */
    final String desc;

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
