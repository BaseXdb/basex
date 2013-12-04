package org.basex.query;

import java.util.*;

import org.basex.core.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * An XQuery main module.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public class MainModule extends StaticScope {
  /** Declared type, {@code null} if not specified. */
  private final SeqType declType;

  /**
   * Constructor.
   * @param rt root expression
   * @param scp variable scope
   * @param xqdoc documentation
   * @param sctx static context
   */
  public MainModule(final Expr rt, final VarScope scp, final String xqdoc,
      final StaticContext sctx) {
    this(rt, scp, null, xqdoc, sctx, null);
  }

  /**
   * Constructor.
   * @param rt root expression
   * @param scp variable scope
   * @param xqdoc documentation
   * @param type optional type
   * @param sctx static context
   * @param ii input info
   */
  public MainModule(final Expr rt, final VarScope scp, final SeqType type, final String xqdoc,
      final StaticContext sctx, final InputInfo ii) {

    super(scp, xqdoc, sctx, ii);
    expr = rt;
    declType = type;
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
      final Value v = ctx.value(expr);
      return declType != null ? declType.treat(v, info) : v;
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
    if(declType != null) return value(ctx).iter();

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
   * Adds the names of the databases that may be touched by the module.
   * @param lr lock result
   * @param ctx query context
   * @return result of check
   * @see Proc#databases(LockResult)
   */
  public boolean databases(final LockResult lr, final QueryContext ctx) {
    return expr.accept(new LockVisitor(lr, ctx));
  }

  /**
   * Lock visitor.
   * @author Leo Woerteler
   */
  static class LockVisitor extends ASTVisitor {
    /** Already visited scopes. */
    private final IdentityHashMap<Scope, Object> funcs =
        new IdentityHashMap<Scope, Object>();
    /** List of databases to be locked. */
    private final StringList sl;
    /** Focus level. */
    private int level;

    /**
     * Constructor.
     * @param lr lock result
     * @param ctx query context
     */
    LockVisitor(final LockResult lr, final QueryContext ctx) {
      sl = ctx.updating ? lr.write : lr.read;
      level = ctx.ctxItem == null ? 0 : 1;
    }

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
  }
}
