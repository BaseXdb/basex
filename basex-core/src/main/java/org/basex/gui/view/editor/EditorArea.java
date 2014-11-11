package org.basex.gui.view.editor;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class extends the text panel by editor features.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class EditorArea extends TextPanel {
  /** File label. */
  final BaseXLabel label;
  /** File in tab. */
  private IOFile file;
  /** Flag for modified content. */
  boolean modified;
  /** Last input. */
  byte[] last;
  /** This flag indicates if the input is a command script. */
  boolean script;

  /** View reference. */
  private final EditorView view;
  /** Timestamp. */
  private long tstamp;

  /**
   * Constructor.
   * @param view view reference
   * @param file file reference
   */
  EditorArea(final EditorView view, final IOFile file) {
    super(true, view.gui);
    this.view = view;
    this.file = file;
    label = new BaseXLabel(file.name());
    label.setIcon(BaseXImages.file(new IOFile(IO.XQSUFFIX)));
    setSyntax(file, false);

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        // refresh query path and working directory
        gui.context.options.set(MainOptions.QUERYPATH, file.path());
        gui.gopts.set(GUIOptions.WORKPATH, file.dir());

        // reload file if it has been changed
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            reopen(false);
          }
        });
      }
    });
  }

  /**
   * Returns {@code true} if the file was opened from disk, or was saved to disk.
   * @return result of check
   */
  public boolean opened() {
    return tstamp != 0;
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
   * @param t text to be set
   */
  public void initText(final byte[] t) {
    last = t;
    super.setText(t);
    hist.init(getText());
  }

  @Override
  public void setText(final byte[] t) {
    last = getText();
    super.setText(t);
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
    super.keyReleased(e);
    if(EXEC1.is(e) || EXEC2.is(e)) {
      release(Action.EXECUTE);
    } else if(UNIT.is(e)) {
      release(Action.TEST);
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
   * @return {@code true} if file was opened
   */
  public boolean reopen(final boolean enforce) {
    if(opened()) {
      final long ts = file.timeStamp();
      if((tstamp != ts || enforce) && (!modified ||
          BaseXDialog.confirm(gui, Util.info(REOPEN_FILE_X, file.name())))) {
        try {
          setText(file.read());
          file(file);
          release(Action.PARSE);
          return true;
        } catch(final IOException ex) {
          Util.debug(ex);
          BaseXDialog.error(gui, Util.info(FILE_NOT_OPENED_X, file));
        }
      }
      tstamp = ts;
    }
    return false;
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
    if(rename || modified) {
      try {
        io.write(getText());
        file(io);
        view.project.save(io, rename);
        return true;
      } catch(final Exception ex) {
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
    if(string != null) {
      search.reset();
      search.activate(string, false);
      jump(SearchDir.CURRENT, true);
    } else {
      search.deactivate(true);
    }
    requestFocusInWindow();
  }

  /**
   * Updates the file reference, timestamp and history.
   * @param io file
   */
  void file(final IOFile io) {
    if(io != file) {
      file = io;
      label.setIcon(BaseXImages.file(io));
      setSyntax(io, true);
      repaint();
    }
    tstamp = file.timeStamp();
    hist.save();
    view.refreshHistory(file);
    view.refreshControls(this, true);
  }
}
