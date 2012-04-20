package org.basex.gui.view.editor;

import org.basex.core.Prop;
import org.basex.core.cmd.XQuery;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXEditor;

import static org.basex.gui.layout.BaseXKeys.*;
import org.basex.gui.layout.BaseXLabel;
import org.basex.io.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.util.Performance;
import static org.basex.util.Token.*;
import static org.basex.core.Text.*;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * This class extends the text editor by XQuery features.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class EditorArea extends BaseXEditor {
  /** File label. */
  final BaseXLabel label;

  /** File in tab. */
  IOFile file;
  /** Timestamp. */
  long tstamp;
  /** Flag for modified content. */
  boolean modified;
  /** Thread counter. */
  int threadID;
  /** Last input. */
  byte[] last = EMPTY;
  /** This flag indicates if the input is an executable XQuery main module. */
  boolean executable = true;

  /** View reference. */
  private final EditorView view;

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
        if(opened() && !modified) {
          try {
            // reload file that has been modified
            final long t = tstamp;
            if(file.timeStamp() != t) {
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
   * (i.e., has a valid timestamp and filename).
   * @return result of check
   */
  boolean opened() {
    return tstamp != 0;
  }

  @Override
  public void setText(final byte[] t) {
    super.setText(t);
    last = getText();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    super.mouseReleased(e);
    view.pos.setText(pos());
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
  public void keyReleased(final KeyEvent e) {
    super.keyReleased(e);
    if(!e.isActionKey() && !modifier(e)) release(EXEC.is(e));
  }

  @Override
  protected void release(final boolean force) {
    final byte[] in = getText();
    final boolean eq = eq(in, last);
    if(eq && !force) return;
    last = in;
    view.refresh(modified || !eq, false);
    threadID++;

    view.pos.setText(pos());
    gui.context.prop.set(Prop.QUERYPATH, file.path());

    if(opened() && !file.hasSuffix(IO.XQSUFFIXES)) {
      // non-executable input
      view.info(OK, true);
      executable = false;
    } else {
      // check if input is/might be an xquery main module
      final String qu = in.length == 0 ? "()" : string(in);
      executable = !module(in);
      if(executable && (force || gui.gprop.is(GUIProp.EXECRT))) {
        // execute query if forced, or if realtime execution is activated
        gui.execute(true, new XQuery(qu));
      } else {
        // parse query
        final QueryContext ctx = new QueryContext(gui.context);
        try {
          if(!executable) ctx.module(qu);
          else ctx.parse(qu);
          view.info(OK, true);
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
    tstamp = f.timeStamp();
    setSyntax(file);
  }

  /**
   * Highlights the error.
   * @param pos error position
   * @param cursor move cursor to error position
   */
  void markError(final int pos, final boolean cursor) {
    if(cursor) {
      requestFocusInWindow();
      setCaret(pos);
    }

    final int thread = threadID;
    new Thread() {
      @Override
      public void run() {
        Performance.sleep(pos == -1 ? 0 : 500);
        if(thread == threadID) error(pos);
      }
    }.start();
  }

  /**
   * Verifies if the specified query is a module.
   * @param qu query to check
   * @return result of check
   */
  private static boolean module(final byte[] qu) {
    final String mod = "module namespace ";
    return QueryProcessor.removeComments(string(qu), mod.length() + 1).startsWith(mod);
  }
}
