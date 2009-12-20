package org.basex.gui.view.xquery;

import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.core.proc.XQuery;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXText;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This class extends the text editor by XQuery features.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class XQueryText extends BaseXText {
  /** Error pattern. */
  private static final Pattern ERRPATTERN = Pattern.compile(
      ".* line ([0-9]+), column ([0-9]+).*", Pattern.DOTALL);

  /** View reference. */
  private final XQueryView view;
  /** Last Query. */
  private byte[] last = Token.EMPTY;

  /** Last error position. */
  int error = -1;
  /** Thread counter. */
  int threadID;

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
    error(-1);
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    super.keyReleased(e);
    parse();
  }

  /**
   * Parses a query.
   */
  void parse() {
    final byte[] qu = getText();
    final boolean module = isModule(qu);
    final boolean mod = !Token.eq(qu, last);
    view.modified(view.modified || mod, false);

    if(gui.prop.is(GUIProp.EXECRT) && !module && mod) {
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
  void query(final byte[] query, final boolean force) {
    if(force) {
      last = query;
      if(isModule(query)) return;
      view.stop.setEnabled(true);
      final String qu = Token.string(query);
      gui.execute(new XQuery(qu.trim().isEmpty() ? "()" : qu));
    } else {
      markError();
    }
  }

  /**
   * Returns true if the specified query is a module.
   * @param qu query to check
   * @return result of check
   */
  private boolean isModule(final byte[] qu) {
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
        for(int i = 0, l = 1, c = 1; i < ll; c++, i++) {
          if(l > el || l == el && c == ec) {
            error = i;
            break;
          }
          if(last[i] == '\n') {
            l++;
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
