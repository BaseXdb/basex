package org.basex.gui.layout;

import static org.basex.core.Text.*;

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
 * @author BaseX Team 2005-21, BSD License
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

  /** Reference to parent window (of type {@link BaseXDialog} or {@link GUI}). */
  private final BaseXWindow win;
  /** Swing file chooser. */
  private final JFileChooser fc;
  /** File suffix. */
  private String suffix;

  /**
   * Default constructor.
   * @param win reference to the main window
   * @param title dialog title
   * @param path initial path
   */
  public BaseXFileChooser(final BaseXWindow win, final String title, final String path) {
    this.win = win;

    final IOFile file = new IOFile(path);
    fc = new JFileChooser(path);
    if(!file.isDir()) fc.setSelectedFile(file.file());
    fc.setDialogTitle(title);
  }

  /**
   * Convenience method for setting textual filters.
   * @return self reference
   */
  public BaseXFileChooser textFilters() {
    filter(XML_DOCUMENTS, false, win.gui().gopts.xmlSuffixes());
    filter(XSL_DOCUMENTS, false, IO.XSLSUFFIXES);
    filter(HTML_DOCUMENTS, false, IO.HTMLSUFFIXES);
    filter(JSON_DOCUMENTS, false, IO.JSONSUFFIX);
    filter(CSV_DOCUMENTS, false, IO.CSVSUFFIX);
    filter(PLAIN_TEXT, false, IO.TXTSUFFIXES);
    return this;
  }

  /**
   * Sets a file filter.
   * @param dsc description
   * @param dflt set as default
   * @param suf suffix
   * @return self reference
   */
  public BaseXFileChooser filter(final String dsc, final boolean dflt, final String... suf) {
    if(fc != null) {
      final FileFilter ff = fc.getFileFilter(), ff2 = new Filter(suf, dsc);
      fc.addChoosableFileFilter(ff2);
      fc.setFileFilter(dflt ? ff2 : ff);
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
   * @return resulting input reference or {@code null} if no file was selected
   */
  public IOFile select(final Mode mode) {
    final IOFile[] files = selectAll(mode);
    return files.length == 0 ? null : files[0];
  }

  /**
   * Returns selected files or directories.
   * @param mode type defined by {@link Mode}
   * @return input files
   */
  public IOFile[] selectAll(final Mode mode) {
    int state = 0;
    switch(mode) {
      case FOPEN:
        state = fc.showOpenDialog(win.component());
        break;
      case FDOPEN:
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        state = fc.showOpenDialog(win.component());
        break;
      case FSAVE:
        state = fc.showSaveDialog(win.component());
        break;
      case DOPEN:
      case DSAVE:
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        state = fc.showDialog(win.component(), null);
        break;
    }
    if(state != JFileChooser.APPROVE_OPTION) return new IOFile[0];

    final File[] fls = fc.isMultiSelectionEnabled() ? fc.getSelectedFiles() :
      new File[] { fc.getSelectedFile() };
    final int fl = fls.length;

    final IOFile[] files = new IOFile[fl];
    for(int f = 0; f < fl; f++) {
      // chop too long paths (usually result of copy'n'paste)
      String path = fls[f].getPath();
      path = path.substring(0, Math.min(path.length(), 512));
      if(fls[f].isDirectory()) path += '/';
      files[f] = new IOFile(path);
    }

    if(mode == Mode.FSAVE) {
      // add file suffix to files
      final FileFilter ff = fc.getFileFilter();
      if(suffix != null) {
        for(int f = 0; f < fl; f++) {
          final String path = files[f].path();
          if(!path.contains(".")) files[f] = new IOFile(path + suffix);
        }
      } else if(ff instanceof Filter) {
        final String[] sufs = ((Filter) ff).suffixes;
        final int sl = sufs.length;
        for(int f = 0; f < fl && sl != 0; f++) {
          final String path = files[f].path();
          if(!path.contains(".")) files[f] = new IOFile(path + sufs[0]);
        }
      }

      // show replace dialog
      for(final IOFile io : files) {
        if(io.exists()) {
          if(!BaseXDialog.confirm(win.gui(), Util.info(FILE_EXISTS_X, io))) return new IOFile[0];
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
    final String[] suffixes;
    /** Description. */
    final String description;

    /**
     * Constructor.
     * @param suffixes suffixes
     * @param description description
     */
    Filter(final String[] suffixes, final String description) {
      this.suffixes = suffixes;
      this.description = description;
    }

    @Override
    public boolean accept(final File file) {
      if(file.isDirectory()) return true;
      final String name = file.getName().toLowerCase(Locale.ENGLISH);
      for(final String suf : suffixes) {
        if(name.endsWith(suf)) return true;
      }
      return false;
    }

    @Override
    public String getDescription() {
      final StringBuilder sb = new StringBuilder();
      for(final String s : suffixes) {
        if(sb.length() != 0) sb.append(", ");
        sb.append('*').append(s);
      }
      return description + " (" + sb + ')';
    }
  }
}
