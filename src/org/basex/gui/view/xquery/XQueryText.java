package org.basex.gui.view.xquery;

import static org.basex.Text.*;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.core.proc.XQuery;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXLayout;
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
public class XQueryText extends BaseXText {
  /** Error pattern. */
  private static final Pattern ERRPATTERN = Pattern.compile(
      ".* line ([0-9]+), column ([0-9]+).*", Pattern.DOTALL);

  /** Last Query. */
  private byte[] last = Token.EMPTY;
  /** View reference. */
  private XQueryView view;

  /** Last error position. */
  int error = -1;
  /** Thread counter. */
  int threadID;

  /**
   * Constructor.
   * @param v view reference
   */
  public XQueryText(final XQueryView v) {
    super(HELPXQUERY, true, v.gui);

    view = v;
    setSyntax(new XQuerySyntax());
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
    final boolean mod = isModule(qu);
    if(GUIProp.execrt && !mod) {
      query(false);
    } else {
      try {
        last = qu;
        final String xq = Token.string(qu);
        final QueryContext ctx = new QueryContext();
        if(isModule(qu)) ctx.module(xq);
        else ctx.parse(xq);
        view.info("", true);
      } catch(final QueryException ex) {
        view.info(ex.getMessage(), false);
      }
    }
  }

  /**
   * Performs a query.
   * @param force force query
   */
  void query(final boolean force) {
    final byte[] qu = getText();
    if(force || !Token.eq(qu, last)) {
      last = qu;
      if(isModule(qu)) return;
      BaseXLayout.enable(view.stop, true);
      final String xq = Token.string(qu);
      gui.execute(new XQuery(xq.trim().length() == 0 ? "." : xq));
    } else {
      showError();
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
  public int error(final String inf, final boolean ok) {
    if(ok) {
      error = -1;
    } else {
      error = last.length;
      
      final Matcher m = ERRPATTERN.matcher(inf);
      int el = 0;
      int ec = 0;
      if(m.matches()) {
        el = Integer.parseInt(m.group(1));
        ec = Integer.parseInt(m.group(2));
      }
  
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
    showError();
    return error;
  }
  
  /**
   * Highlights the error.
   */
  private void showError() {
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
