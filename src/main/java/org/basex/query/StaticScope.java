package org.basex.query;

import static org.basex.query.QueryText.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.expr.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Superclass for static functions, variables and the main expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public abstract class StaticScope extends ExprInfo implements Scope {
  /** Variable scope. */
  protected final VarScope scope;
  /** Input info. */
  public final InputInfo info;

  /** Root expression of this declaration. */
  public Expr expr;
  /** Compilation flag. */
  protected boolean compiled;
  /** Documentation. */
  private final byte[] doc;

  /**
   * Constructor.
   * @param scp variable scope
   * @param ii input info
   * @param xqdoc documentation (may be {@code null} or empty)
   */
  public StaticScope(final VarScope scp, final StringBuilder xqdoc, final InputInfo ii) {
    scope = scp;
    info = ii;
    if(xqdoc != null && xqdoc.length() != 0) {
      doc = Token.token(xqdoc.toString());
      xqdoc.setLength(0);
    } else {
      doc = null;
    }
  }

  @Override
  public final boolean compiled() {
    return compiled;
  }

  /**
   * Returns a map with the documentation of this scope, or {@code null} if no
   * documentation exists.
   * @return documentation
   */
  public TokenMap doc() {
    if(doc == null) return null;

    final TokenMap map = new TokenMap();
    byte[] key = null;
    final TokenBuilder val = new TokenBuilder();
    final TokenBuilder line = new TokenBuilder();
    try {
      final NewlineInput nli = new NewlineInput(new IOContent(doc));
      while(nli.readLine(line)) {
        String l = line.toString().replaceAll("^\\s+: *", "");
        if(l.startsWith("@")) {
          map.add(key == null ? DOC_TAGS[0] : key, val.trim().finish());
          key = Token.token(l.replaceAll("^@(\\w*).*", "$1"));
          l = l.replaceAll("^@\\w+ *", "");
          val.reset();
        }
        val.add(l).add('\n');
      }
    } catch(final IOException ex) {
      Util.notexpected(ex);
    }
    map.add(key == null ? DOC_TAGS[0] : key, val.trim().finish());
    return map;
  }
}
