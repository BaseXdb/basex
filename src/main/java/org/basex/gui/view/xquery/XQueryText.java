package org.basex.gui.view.xquery;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.core.cmd.XQuery;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXText;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This class extends the text editor by XQuery features.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class XQueryText extends BaseXText {
  /** Error pattern. */
  private static final Pattern ERRPATTERN = Pattern.compile((".* " + LINEINFO +
      ", " + COLINFO + ".*").replaceAll("%", "([0-9]+)"), Pattern.DOTALL);

  /** Last error position. */
  int error = -1;
  /** Thread counter. */
  int threadID;

  /** View reference. */
  private final XQueryView view;
  /** Last Query. */
  private byte[] last = Token.EMPTY;

  /**
   * Constructor.
   * @param v view reference
   */
  XQueryText(final XQueryView v) {
    super(true, v.gui);
    view = v;
    setSyntax(new XQuerySyntax());
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
    final boolean module = module(qu);
    final boolean eq = Token.eq(qu, last);
    if(eq && !force) return;
    view.modified(view.modified || !eq, false);
    view.pos.setText(pos());

    if(force || gui.gprop.is(GUIProp.EXECRT) && !module) {
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
   * Performs the current query.
   */
  void query() {
    query(getText(), true);
  }

  /**
   * Performs the specified query.
   * @param query to be processed
   * @param force perform query, even if it has not changed
   */
  private void query(final byte[] query, final boolean force) {
    if(force) {
      last = query;
      if(module(query)) return;
      view.stop.setEnabled(true);
      final String qu = Token.string(query);
      gui.execute(new XQuery(qu.trim().isEmpty() ? "()" : qu), false);
    } else {
      markError();
    }
  }

  /**
   * Returns true if the specified query is a module.
   * @param qu query to check
   * @return result of check
   */
  private boolean module(final byte[] qu) {
    return Token.string(qu).trim().startsWith("module namespace ");
  }

  /**
   * Handles info messages resulting from a query execution.
   * @param inf info message
   * @param ok true if query was successful
   * @return error position in text
   */
  int error(final String inf, final boolean ok) {
    error = -1;
    if(!ok) {
      final Matcher m = ERRPATTERN.matcher(inf);
      int el = 0;
      int ec = 0;
      if(m.matches()) {
        el = Integer.parseInt(m.group(1));
        ec = Integer.parseInt(m.group(2));
        error = last.length;

        // find approximate error position
        final int ll = error;
        for(int e = 0, l = 1, c = 1; e < ll; ++c, ++e) {
          if(l > el || l == el && c == ec) {
            error = e;
            break;
          }
          if(last[e] == '\n') {
            ++l;
            c = 0;
          }
        }
      }
    }
    markError();
    return error;
  }

  /**
   * Highlights the error.
   */
  private void markError() {
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
}
