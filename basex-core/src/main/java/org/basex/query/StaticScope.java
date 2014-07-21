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
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public abstract class StaticScope extends ExprInfo implements Scope {
  /** Static context. */
  public final StaticContext sc;
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
   * @param scope variable scope
   * @param info input info
   * @param doc xqdoc documentation (may be {@code null} or empty)
   * @param sc static context
   */
  StaticScope(final VarScope scope, final String doc, final StaticContext sc,
      final InputInfo info) {
    this.sc = sc;
    this.scope = scope;
    this.info = info;
    this.doc = doc != null && !doc.isEmpty() ? Token.token(doc) : null;
  }

  @Override
  public final boolean compiled() {
    return compiled;
  }

  /**
   * Returns a map with all documentation tags found for this scope, or {@code null} if
   * no documentation exists. The main description is flagged with the "description" key.
   * The supported tags are defined in {@link QueryText#DOC_TAGS} (other tags will be
   * included in the map, too).
   * @return documentation
   */
  public TokenObjMap<TokenList> doc() {
    if(doc == null) return null;

    final TokenObjMap<TokenList> map = new TokenObjMap<>();
    byte[] key = null;
    final TokenBuilder val = new TokenBuilder();
    final TokenBuilder line = new TokenBuilder();
    try {
      final NewlineInput nli = new NewlineInput(new IOContent(doc));
      while(nli.readLine(line)) {
        String l = line.toString().replaceAll("^\\s*: ?", "");
        if(l.startsWith("@")) {
          add(key, val, map);
          key = Token.token(l.replaceAll("^@(\\w*).*", "$1"));
          l = l.replaceAll("^@\\w+ *", "");
        }
        val.add(l).add('\n');
      }
    } catch(final IOException ex) {
      throw Util.notExpected(ex);
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
  private static void add(final byte[] key, final TokenBuilder val,
      final TokenObjMap<TokenList> map) {

    final byte[] k = key == null ? DOC_TAGS[0] : key;
    TokenList tl = map.get(k);
    if(tl == null) {
      tl = new TokenList();
      map.put(k, tl);
    }
    tl.add(val.trim().next());
  }
}
