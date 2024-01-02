package org.basex.query.scope;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.inspect.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Superclass for static functions, variables and the main expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public abstract class StaticScope extends ExprInfo implements Scope {
  /** Static context. */
  public final StaticContext sc;

  /** Expression of this declaration ({@code null} if this is an external function). */
  public Expr expr;
  /** Resulting value (can be {@code null}). */
  public Value value;

  /** Input info (can be {@code null}). */
  public InputInfo info;
  /** Name of the declaration (can be {@code null}). */
  public QNm name;

  /** Variable scope. */
  protected VarScope vs;
  /** Compilation flag. */
  protected boolean compiled;

  /** Declared type, {@code null} if not specified. */
  protected SeqType declType;

  /** Documentation string. */
  private byte[] doc = Token.EMPTY;

  /**
   * Constructor.
   * @param sc static context
   */
  StaticScope(final StaticContext sc) {
    this.sc = sc;
  }

  /**
   * Assigns a documentation string.
   * @param string xqdoc string (can be {@code null})
   */
  public void doc(final String string) {
    if(string != null) doc = Token.token(string.trim());
  }

  @Override
  public final boolean compiled() {
    return compiled;
  }

  /**
   * Evaluates the expression and returns the resulting value.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  public Value value(final QueryContext qc) throws QueryException {
    if(value == null) {
      final int fp = vs.enter(qc);
      try {
        final Value val = expr.value(qc);
        if(declType != null && !declType.instance(val)) {
          throw typeError(val, declType, name, info, false);
        }
        value = val;
      } finally {
        vs.exit(fp, qc);
      }
    }
    return value;
  }

  /**
   * Returns a map with all documentation tags found for this scope or {@code null} if
   * no documentation exists. The main description is flagged with the "description" key.
   * The supported tags are defined in {@link Inspect#DOC_TAGS} (other tags will be
   * included in the map, too).
   * @return documentation or {@code null}
   */
  public final TokenObjMap<TokenList> doc() {
    if(doc.length == 0) return null;

    final TokenObjMap<TokenList> map = new TokenObjMap<>();
    final TokenBuilder key = new TokenBuilder(), tb = new TokenBuilder();
    final Runnable add = () -> {
      final byte[] k = key.isEmpty() ? Inspect.DOC_TAGS[0] : key.next();
      map.computeIfAbsent(k, TokenList::new).add(tb.trim().next());
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
        tb.add(line).add('\n');
      }
    } catch(final IOException ex) {
      throw Util.notExpected(ex);
    }
    add.run();
    return map;
  }
}
