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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class EditorArea extends TextPanel {
  /** File label. */
  final BaseXLabel label;
  /** File in tab. */
  private IOFile file;
  /** Flag for modified content. */
  private boolean modified;
  /** Last input. */
  byte[] last;

  /** View reference. */
  private final EditorView view;
  /** Timestamp. Set to {@code 0} if the editor contents are not saved on disk. */
  private long timeStamp;

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

    addFocusListener((FocusGainedListener) e -> {
      // refresh query path and working directory
      gui.gopts.setFile(GUIOptions.WORKPATH, this.file.parent());
      // reload file if it has been changed
      SwingUtilities.invokeLater(() -> reopen(false));
    });
  }

  /**
   * Returns {@code true} if the editor contents are saved on disk.
   * @return result of check
   */
  public boolean opened() {
    return timeStamp != 0;
  }

  /**
   * Returns {@code true} if the editor contents were modified.
   * @return result of check
   */
  public boolean modified() {
    return modified;
  }

  /**
   * Sets the modified flag.
   * @param mod modified flag
   */
  void modified(final boolean mod) {
    modified = mod;
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
    final boolean exec = gui.editor.go.isEnabled();
    if(EXEC1.is(e)) {
      if(exec) release(Action.EXECUTE);
    } else if(UNIT.is(e)) {
      if(exec) release(Action.TEST);
    } else if((!e.isActionKey() || MOVEDOWN.is(e) || MOVEUP.is(e)) && !modifier(e)) {
      release(Action.CHECK);
    }
  }

  @Override
  protected void release(final Action action) {
    view.run(this, action);
  }

  /**
   * Reverts the contents of the currently opened editor.
   * @param enforce enforce reload
   */
  public void reopen(final boolean enforce) {
    // skip if editor contents are saved on disk, or if they are up-to-date
    final long ts = file.timeStamp();
    if(!opened() || timeStamp == ts && !enforce) return;

    // reset timestamp if file will not be reopened
    if(modified && !BaseXDialog.confirm(gui, Util.info(REOPEN_FILE_X, file.name()))) {
      timeStamp = 0;
      return;
    }

    try {
      // reopens the file
      setText(file.read());
      file(file, false);
      release(Action.PARSE);
      timeStamp = ts;
    } catch(final IOException ex) {
      // reset timestamp if file could not be opened
      timeStamp = 0;
      Util.debug(ex);
      BaseXDialog.error(gui, Util.info(FILE_NOT_OPENED_X, file));
    }
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
    if(rename || modified || timeStamp == 0) {
      try {
        final byte[] text = getText();
        final boolean xquery = io.hasSuffix(IO.XQSUFFIXES);
        final boolean library = xquery && QueryProcessor.isLibrary(Token.string(text));
        io.write(text);
        file(io, true);
        view.project.save(io, rename, xquery, library);
        return true;
      } catch(final Exception ex) {
        Util.debug(ex);
        BaseXDialog.error(gui, Util.info(FILE_NOT_SAVED_X, io));
      }
    }
    return false;
  }

  /**
   * Jumps to the specified string.
   * @param string search string
   */
  public void jump(final String string) {
    search.activate(string, false, false);
    jump(SearchDir.CURRENT, true);
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
    timeStamp = file.timeStamp();
    hist.save();
    view.refreshHistory(file);
    view.refreshControls(this, true);
    if(save) gui.saveOptions();
  }
}
