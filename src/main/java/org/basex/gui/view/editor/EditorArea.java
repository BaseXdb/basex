package org.basex.gui.view.editor;

import static org.basex.gui.layout.BaseXKeys.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.basex.core.cmd.XQuery;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXEditor;
import org.basex.io.IO;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This class extends the text editor by XQuery features.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class EditorArea extends BaseXEditor {
  /** File label. */
  final BaseXLabel label;
  /** View reference. */
  private final EditorView view;

  /** File in tab. */
  IO file;
  /** Flag for modified content. */
  boolean mod;
  /** Opened flag; states if file was opened from disk. */
  boolean opened;

  /** Last error position. */
  int error = -1;
  /** Thread counter. */
  int threadID;

  /** Last input. */
  byte[] last = Token.EMPTY;

  /**
   * Constructor.
   * @param v view reference
   * @param f file reference
   */
  EditorArea(final EditorView v, final IO f) {
    super(true, v.gui);
    view = v;
    file = f;
    label = new BaseXLabel(f.name());
    setSyntax(f);
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
    final byte[] qu = getText();
    final boolean eq = Token.eq(qu, last);
    if(eq && !force) return;
    view.refresh(mod || !eq, false);
    view.pos.setText(pos());
    if(file.name().endsWith(IO.XMLSUFFIX)) {
      last = qu;
      view.info("", true);
      return;
    }

    final boolean module = module(qu);
    if(!module && (force || gui.gprop.is(GUIProp.EXECRT))) {
      view.waitInfo();
      query(qu, true);
    } else {
      try {
        last = qu;
        final String xq = qu.length == 0 ? "." : Token.string(qu);
        final QueryContext ctx = new QueryContext(gui.context);
        if(module) ctx.module(xq);
        else ctx.parse(xq);
        view.info("", true);
      } catch(final QueryException ex) {
        view.info(ex.getMessage(), false);
      }
    }
  }

  /**
   * Checks if the query is to be evaluated in realtime.
   * @return result of check
   */
  boolean executable() {
    return !module(last) && !file.name().endsWith(IO.XMLSUFFIX);
  }

  /**
   * Performs the current query.
   */
  void query() {
    query(getText(), true);
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
   * Performs the specified query.
   * @param query to be processed
   * @param force perform query, even if it has not changed
   */
  private void query(final byte[] query, final boolean force) {
    gui.context.query = file;
    if(force) {
      last = query;
      if(!executable()) return;
      view.stop.setEnabled(true);
      final String qu = Token.string(query);
      gui.execute(new XQuery(qu.trim().isEmpty() ? "()" : qu), false);
    } else {
      markError();
    }
  }

  /**
   * Verifies if the specified query is a module.
   * @param qu query to check
   * @return result of check
   */
  private boolean module(final byte[] qu) {
    return QueryProcessor.removeComments(Token.string(qu), 20).startsWith(
        "module namespace ");
  }
}
