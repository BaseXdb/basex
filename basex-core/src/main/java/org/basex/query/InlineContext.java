package org.basex.query;

import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * Context for inlining expressions.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class InlineContext {
  /** Compilation context. */
  public final CompileContext cc;
  /** Variable reference, or {@code null} for context reference. */
  public final Var var;
  /** Expression to inline. */
  private final Expr expr;

  /**
   * Constructor.
   * @param var variable ({@link Var} reference) or context ({@code null}) to replace
   * @param expr expression to inline
   * @param cc compilation context
   */
  public InlineContext(final Var var, final Expr expr, final CompileContext cc) {
    this.cc = cc;
    this.var = var;
    this.expr = expr;
  }

  /**
   * Inlines an expression into the specified expressions.
   * @param exprs expressions to update
   * @return {@code true} if the array has changed, {@code false} otherwise
   * @throws QueryException query exception
   */
  public boolean inline(final Expr[] exprs) throws QueryException {
    boolean ch = false;
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      final Expr inlined = exprs[e].inline(this);
      if(inlined != null) {
        exprs[e] = inlined;
        ch = true;
      }
    }
    return ch;
  }

  /**
   * Creates a copy from the expression to be inlined.
   * @return copy
   * @throws QueryException query exception
   */
  public Expr copy() throws QueryException {
    return expr instanceof Value ? expr : expr.copy(cc, new IntObjMap<>()).optimize(cc);
  }
}
