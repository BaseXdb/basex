package org.basex.query;

import org.basex.data.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;

/**
 * An XQuery main module.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class MainModule extends ExprInfo implements Scope {
  /** Variable scope of this module. */
  final VarScope scope;
  /** Root expression of this module. */
  public Expr expr;
  /** Compilation flag. */
  private boolean compiled;

  /**
   * Constructor.
   * @param rt root expression
   * @param scp variable scope
   */
  public MainModule(final Expr rt, final VarScope scp) {
    expr = rt;
    scope = scp;
  }

  @Override
  public void compile(final QueryContext ctx) throws QueryException {
    if(compiled) return;
    try {
      compiled = true;
      scope.enter(ctx);
      expr = expr.compile(ctx, scope);
      scope.cleanUp(this);
    } finally {
      scope.exit(ctx, 0);
    }
  }

  /**
   * Evaluates this module and returns the result as a value.
   * @param ctx query context
   * @return result
   * @throws QueryException evaluation exception
   */
  public Value value(final QueryContext ctx) throws QueryException {
    try {
      scope.enter(ctx);
      return ctx.value(expr);
    } finally {
      scope.exit(ctx, 0);
    }
  }

  /**
   * Creates a result iterator which lazily evaluates this module.
   * @param ctx query context
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter(final QueryContext ctx) throws QueryException {
    scope.enter(ctx);
    final Iter iter = expr.iter(ctx);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = iter.next();
        if(it == null) scope.exit(ctx, 0);
        return it;
      }

      @Override
      public long size() {
        return iter.size();
      }

      @Override
      public Item get(final long i) throws QueryException {
        return iter.get(i);
      }

      @Override
      public boolean reset() {
        return iter.reset();
      }
    };
  }

  @Override
  public String toString() {
    return expr.toString();
  }

  @Override
  public void plan(final FElem e) {
    expr.plan(e);
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    return expr.accept(visitor);
  }

  @Override
  public boolean compiled() {
    return compiled;
  }
}
