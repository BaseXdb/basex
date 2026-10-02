package org.basex.gui.view.editor;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.listener.*;
import org.basex.gui.text.*;
import org.basex.gui.text.SearchBar.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * This class extends the text panel by editor features.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class EditorArea extends TextPanel {
  /** File label. */
  final BaseXLabel label;
  /** File in tab. */
  private IOFile file;
  /** Flag indicating that the editor contents are assigned to a file. */
  private boolean opened;
  /** Modification state shown in the tab label. */
  boolean marked;
  /** Last input. */
  byte[] last;

  /** View reference. */
  private final EditorView view;
  /** Timestamp of the assigned file when it was last read or written. */
  private long timeStamp;
  /** Timestamp of the assigned file when it was last checked. */
  private long checked;
  /** Timestamp of the assigned file when it was last polled. */
  private long polled;

  /**
   * Constructor.
   * @param view view reference
   * @param file file reference
   */
  EditorArea(final EditorView view, final IOFile file) {
    super(view.gui, true);
    this.view = view;
    this.file = file;
    label = new BaseXLabel(file.name());
    label.setIcon(BaseXImages.file(new IOFile(IO.XQSUFFIX)));
    setSyntax(file, false);
    setEditListener(() -> view.run(this, Action.CHECK));

    addFocusListener((FocusGainedListener) e -> {
      // refresh query path and working directory
      gui.gopts.setFile(GUIOptions.WORKPATH, this.file.parent());
      // reload file if it has been changed
      SwingUtilities.invokeLater(() -> reopen(false));
    });
  }

  /**
   * Returns {@code true} if the editor contents are assigned to a file.
   * @return result of check
   */
  public boolean opened() {
    return opened;
  }

  /**
   * Returns {@code true} if the editor contents were modified.
   * @return result of check
   */
  public boolean modified() {
    return hist.modified();
  }

  /**
   * Returns the file reference.
   * @return file reference
   */
  public IOFile file() {
    return file;
  }

  /**
   * Initializes the text.
   * @param text text to be set
   */
  void initText(final byte[] text) {
    last = text;
    super.setText(text);
    hist.init(getText());
  }

  @Override
  public void setText(final byte[] text) {
    last = getText();
    super.setText(text);
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    super.mouseReleased(e);
    view.posCode.invokeLater();
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    final byte[] text = editor.text();
    super.keyPressed(e);
    if(text != editor.text()) resetError();
    view.posCode.invokeLater();
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    final byte[] text = editor.text();
    super.keyTyped(e);
    if(text != editor.text()) resetError();
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    if(TESTS.is(e)) {
      if(gui.editor.test.isEnabled()) view.run(this, Action.TEST);
    } else if(HISTORY.is(e)) {
      gui.editor.historyPopup(0);
    } else if((!e.isActionKey() || MOVEDOWN.is(e) || MOVEUP.is(e)) && !modifier(e)) {
      view.run(this, Action.CHECK);
    }
  }

  /**
   * Reverts the contents of the currently opened editor.
   * @param enforce enforce reload
   */
  public void reopen(final boolean enforce) {
    // skip if editor contents are not assigned to a file, or if they are up-to-date
    final long ts = file.timeStamp();
    if(!opened || checked == ts && !enforce) return;
    checked = ts;

    // do not discard modifications without confirmation (skipped if file was deleted)
    if(file.exists() && modified() &&
        !BaseXDialog.confirm(gui, Util.info(REVERT_FILE_X, file.name()))) return;

    if(!load(true)) {
      // file was deleted or cannot be accessed: flag editor contents as modified
      hist.invalidate();
      view.refreshControls(this, true);
    }
  }

  /**
   * Reloads unmodified editor contents if the assigned file has been changed on disk.
   */
  void refresh() {
    if(!opened || modified() || !isShowing()) return;
    // skip deleted files and files that are still being written
    final long ts = file.timeStamp(), pl = polled;
    polled = ts;
    // inaccessible files are retried with the next poll
    if(ts != 0 && ts != checked && ts == pl) load(false);
  }

  /**
   * Reads the assigned file into the editor.
   * @param report report errors
   * @return success flag
   */
  private boolean load(final boolean report) {
    // take timestamp before reading: concurrent changes are detected by the next check
    final long ts = file.timeStamp();
    try {
      setText(file.read());
    } catch(final IOException ex) {
      Util.debug(ex);
      if(report) BaseXDialog.error(gui, Util.info(FILE_NOT_OPENED_X, file));
      return false;
    }
    file(file, false);
    timeStamp = ts;
    checked = ts;
    view.run(this, Action.PARSE);
    return true;
  }

  /**
   * Saves the specified editor contents.
   * @return success flag
   */
  boolean save() {
    return save(file);
  }

  /**
   * Saves the editor contents.
   * @param io file to save
   * @return success flag
   */
  boolean save(final IOFile io) {
    final boolean rename = io != file;
    // file was changed on disk: confirm overwrite, or adopt changes before tidying the contents
    final long ts = file.timeStamp();
    if(!rename && opened && ts != 0 && ts != timeStamp && (modified() ?
        !BaseXDialog.confirm(gui, Util.info(FILE_CHANGED_X, file)) : !load(true))) return false;

    final GUIOptions gopts = gui.gopts;
    final boolean trim = gopts.get(GUIOptions.TRIMLINES), nl = gopts.get(GUIOptions.FINALNL);
    final boolean tidied = (trim || nl) && tidy(trim, nl);
    if(rename || modified() || tidied || !opened) {
      if(!write(io, rename)) return false;
      file(io, true);
      return true;
    }
    return false;
  }

  /**
   * Saves a copy of the editor contents, leaving the assigned file untouched.
   * @param io file to save
   * @return success flag
   */
  boolean saveCopy(final IOFile io) {
    return write(io, true);
  }

  /**
   * Writes the editor contents to the specified file.
   * @param io file to write
   * @param rename file has been renamed
   * @return success flag
   */
  private boolean write(final IOFile io, final boolean rename) {
    try {
      final byte[] text = getText();
      final boolean xquery = io.hasSuffix(IO.XQSUFFIXES);
      final boolean library = xquery && QueryParser.isLibrary(Token.string(text));
      io.write(text);
      view.project.save(io, rename, xquery, library);
      view.gui.saveOptions();
      return true;
    } catch(final Exception ex) {
      Util.debug(ex);
      BaseXDialog.error(gui, Util.info(FILE_NOT_SAVED_X, io));
      return false;
    }
  }

  /**
   * Jumps to the specified string, adopting the specified search flags.
   * @param string search string
   * @param flags search flags
   */
  public void jump(final String string, final SearchFlags flags) {
    search.find(string, flags, SearchDir.CURRENT);
  }

  /**
   * Updates the file reference, timestamp and history.
   * @param io file
   * @param save save option files
   */
  void file(final IOFile io, final boolean save) {
    if(io != file) {
      file = io;
      label.setIcon(BaseXImages.file(io));
      setSyntax(io, true);
      repaint();
    }
    opened = true;
    timeStamp = file.timeStamp();
    checked = timeStamp;
    hist.save();
    view.refreshHistory(file);
    view.refreshControls(this, true);
    if(save) gui.saveOptions();
  }
}
