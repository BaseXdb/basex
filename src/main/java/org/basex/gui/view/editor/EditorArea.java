package org.basex.gui.view.editor;

import static org.basex.gui.layout.BaseXKeys.*;
import static org.basex.util.Token.*;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

import org.basex.core.Prop;
import org.basex.core.cmd.XQuery;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXEditor;
import org.basex.gui.layout.BaseXLabel;
import org.basex.io.IOFile;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.util.Performance;

/**
 * This class extends the text editor by XQuery features.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class EditorArea extends BaseXEditor {
  /** File label. */
  final BaseXLabel label;
  /** View reference. */
  final EditorView view;
  /** File in tab. */
  IOFile file;
  /** Timestamp. */
  long tstamp;
  /** Flag for modified content. */
  boolean mod;

  /** Last error position. */
  int error = -1;
  /** Thread counter. */
  int threadID;

  /** Last input. */
  byte[] last = EMPTY;
  /** This flag indicates if the current input is executable, i.e.,
   *  no XML file and no XQuery module. */
  boolean exec = true;

  /**
   * Constructor.
   * @param v view reference
   * @param f file reference
   */
  EditorArea(final EditorView v, final IOFile f) {
    super(true, v.gui);
    view = v;
    file = f;
    label = new BaseXLabel(f.name());
    setSyntax(f);

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        if(opened() && !mod) {
          try {
            // reload file that has been modified
            final long t = tstamp;
            if(file.date() != t) {
              setText(new IOFile(file.path()).read());
              tstamp = t;
            }
          } catch(final IOException ex) { /* ignored */ }
        }
      }
    });
  }

  /**
   * Returns {@code true} if a file has been opened from disk
   * (i.e., has a valid timestamp).
   * @return result of check
   */
  boolean opened() {
    return tstamp != 0;
  }

  @Override
  public void setText(final byte[] t) {
    super.setText(t);
    last = t;
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    super.keyPressed(e);
    if(Character.isDefined(e.getKeyChar())) {
      error(-1);
    } else {
      view.pos.setText(pos());
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    super.mouseReleased(e);
    view.pos.setText(pos());
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    super.keyReleased(e);
    if(!e.isActionKey() && !modifier(e)) release(EXEC.is(e));
  }

  @Override
  protected void release(final boolean force) {
    final byte[] in = getText();
    final boolean eq = eq(in, last);
    if(eq && !force) return;
    view.refresh(mod || !eq, false);
    view.pos.setText(pos());
    gui.context.prop.set(Prop.QUERYPATH, file.path());
    last = in;

    if(file.isXML()) {
      view.info("", true);
      exec = false;
    } else {
      final String qu = in.length == 0 ? "()" : string(in);
      exec = !module(in);
      if(exec && (force || gui.gprop.is(GUIProp.EXECRT))) {
        gui.execute(true, new XQuery(qu));
      } else {
        final QueryContext ctx = new QueryContext(gui.context);
        try {
          if(!exec) ctx.module(qu);
          else ctx.parse(qu);
          view.info("", true);
        } catch(final QueryException ex) {
          view.info(ex.getMessage(), false);
        }
      }
    }
  }

  /**
   * Performs the current query.
   */
  void query() {
    release(true);
  }

  /**
   * Returns the currently assigned file.
   * @return file
   */
  IOFile file() {
    return file;
  }

  /**
   * Sets the file reference.
   * @param f file
   */
  void file(final IOFile f) {
    file = f;
    tstamp = f.date();
    setSyntax(file);
  }

  /**
   * Highlights the error.
   */
  void markError() {
    final int thread = ++threadID;
    final int sleep = error == -1 ? 0 : 500;
    new Thread() {
      @Override
      public void run() {
        Performance.sleep(sleep);
        if(thread == threadID) error(error);
      }
    }.start();
  }

  /**
   * Verifies if the specified query is a module.
   * @param qu query to check
   * @return result of check
   */
  private boolean module(final byte[] qu) {
    return QueryProcessor.removeComments(string(qu), 20).startsWith(
        "module namespace ");
  }
}
