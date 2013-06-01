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
import org.basex.util.list.*;

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
  public TokenObjMap<TokenList> doc() {
    if(doc == null) return null;

    final TokenObjMap<TokenList> map = new TokenObjMap<TokenList>();
    byte[] key = null;
    final TokenBuilder val = new TokenBuilder();
    final TokenBuilder line = new TokenBuilder();
    try {
      final NewlineInput nli = new NewlineInput(new IOContent(doc));
      while(nli.readLine(line)) {
        String l = line.toString().replaceAll("^\\s+: *", "");
        if(l.startsWith("@")) {
          add(key, val, map);
          key = Token.token(l.replaceAll("^@(\\w*).*", "$1"));
          l = l.replaceAll("^@\\w+ *", "");
          val.reset();
        }
        val.add(l).add('\n');
      }
    } catch(final IOException ex) {
      Util.notexpected(ex);
    }
    add(key, val, map);
    return map;
  }

  /**
   * Adds a key and a value to the specified map.
   * @param key key
   * @param val value
   * @param map map
   */
  private void add(final byte[] key, final TokenBuilder val,
      final TokenObjMap<TokenList> map) {

    final byte[] k = key == null ? DOC_TAGS[0] : key;
    TokenList tl = map.get(k);
    if(tl == null) {
      tl = new TokenList();
      map.add(k, tl);
    }
    tl.add(val.trim().finish());
  }
}
