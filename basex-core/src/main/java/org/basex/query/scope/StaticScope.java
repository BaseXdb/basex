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
 * @author BaseX Team 2005-21, BSD License
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
    final TokenBuilder key = new TokenBuilder(), value = new TokenBuilder();
    final Runnable add = () -> {
      final byte[] k = key.isEmpty() ? Inspect.DOC_TAGS[0] : key.next();
      map.computeIfAbsent(k, TokenList::new).add(value.trim().next());
    };

    final TokenBuilder input = new TokenBuilder();
    try(NewlineInput nli = new NewlineInput(doc)) {
      while(nli.readLine(input)) {
        String line = input.toString().replaceAll("^\\s*:? *", "");
        if(line.matches("^@\\w+\\s+.*")) {
          add.run();
          key.add(line.replaceAll("^@|\\s+.*", ""));
          line = line.replaceAll("^@\\w+\\s+", "");
        }
        value.add(line).add('\n');
      }
    } catch(final IOException ex) {
      throw Util.notExpected(ex);
    }
    add.run();
    return map;
  }
}
