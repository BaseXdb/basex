package org.basex.query;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.list.*;

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

  /**
   * Returns the databases that may be touched by this query. The returned information
   * will be more accurate if the function is called after parsing the query.
   * @param lr lock result
   * @see Progress#databases(LockResult)
   * @param ctx query context
   */
  public void databases(final LockResult lr, final QueryContext ctx) {
    lr.read.add(ctx.userReadLocks);
    lr.write.add(ctx.userWriteLocks);

    final StringList sl = ctx.updating ? lr.write : lr.read;

    final ASTVisitor visitor = new ASTVisitor() {
      /** Already visited scopes. */
      final IdentityHashMap<Scope, Object> funcs = new IdentityHashMap<Scope, Object>();
      /** Focus level. */
      int level = ctx.ctxItem == null ? 0 : 1;

      @Override
      public boolean lock(final String db) {
        if(db == null) return false;
        if(level == 0 || db != DBLocking.CTX) sl.add(db);
        return true;
      }

      @Override
      public void enterFocus() {
        level++;
      }

      @Override
      public void exitFocus() {
        level--;
      }

      @Override
      public boolean staticVar(final StaticVar var) {
        if(funcs.containsKey(var)) return true;
        funcs.put(var, null);
        return var.visit(this);
      }

      @Override
      public boolean funcCall(final StaticFuncCall call) {
        return func(call.func());
      }

      @Override
      public boolean inlineFunc(final Scope sub) {
        enterFocus();
        final boolean ac = sub.visit(this);
        exitFocus();
        return ac;
      }

      @Override
      public boolean funcItem(final FuncItem func) {
        return func(func);
      }

      /**
       * Visits a scope.
       * @param scp scope
       * @return if more expressions should be visited
       */
      private boolean func(final Scope scp) {
        if(funcs.containsKey(scp)) return true;
        funcs.put(scp, null);
        enterFocus();
        final boolean ac = scp.visit(this);
        exitFocus();
        return ac;
      }
    };
    if (!expr.accept(visitor))
      if(ctx.updating) lr.writeAll = true;
      else lr.readAll = true;
  }

  @Override
  public boolean compiled() {
    return compiled;
  }
}
