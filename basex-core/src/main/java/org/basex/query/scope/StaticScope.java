package org.basex.query.scope;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.inspect.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Superclass for static functions, variables and the main expression.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Leo Woerteler
 */
public abstract class StaticScope extends ExprInfo implements Scope {
  /** Static context. */
  public final StaticContext sc;
  /** Variable scope. */
  public final VarScope vs;
  /** Input info. */
  public final InputInfo info;

  /** Root expression of this declaration ({@code null} if this is an external function). */
  public Expr expr;
  /** Compilation flag. */
  protected boolean compiled;
  /** Documentation. */
  private final byte[] doc;

  /**
   * Constructor.
   * @param sc static context
   * @param vs variable scope (can be {@code null})
   * @param doc xqdoc documentation (can be {@code null} or empty)
   * @param info input info (can be {@code null})
   */
  StaticScope(final StaticContext sc, final VarScope vs, final String doc, final InputInfo info) {
    this.sc = sc;
    this.vs = vs;
    this.doc = doc != null && !doc.isEmpty() ? Token.token(doc) : null;
    this.info = info;
  }

  @Override
  public final boolean compiled() {
    return compiled;
  }

  /**
   * Returns a map with all documentation tags found for this scope or {@code null} if
   * no documentation exists. The main description is flagged with the "description" key.
   * The supported tags are defined in {@link Inspect#DOC_TAGS} (other tags will be
   * included in the map, too).
   * @return documentation or {@code null}
   */
  public final TokenObjMap<TokenList> doc() {
    if(doc == null) return null;

    final TokenObjMap<TokenList> map = new TokenObjMap<>();
    byte[] key = null;
    final TokenBuilder val = new TokenBuilder(), line = new TokenBuilder();
    try(NewlineInput nli = new NewlineInput(doc)) {
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

    final byte[] k = key == null ? Inspect.DOC_TAGS[0] : key;
    TokenList tl = map.get(k);
    if(tl == null) {
      tl = new TokenList();
      map.put(k, tl);
    }
    tl.add(val.trim().next());
  }
}
