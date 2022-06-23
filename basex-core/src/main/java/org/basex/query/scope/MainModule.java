package org.basex.query.scope;

import java.util.*;
import java.util.function.*;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;

/**
 * An XQuery main module.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Leo Woerteler
 */
public final class MainModule extends AModule {
  /** Declared type, {@code null} if not specified. */
  public SeqType declType;

  /**
   * Constructor.
   * @param expr root expression
   * @param vs variable scope
   */
  public MainModule(final Expr expr, final VarScope vs) {
    super(vs.sc);
    this.vs = vs;
    this.expr = expr;
  }

  @Override
  public void comp(final CompileContext cc) throws QueryException {
    if(compiled) return;
    compiled = true;

    cc.pushScope(vs);
    try {
      expr = expr.compile(cc);
    } finally {
      cc.removeScope(this);
    }
  }

  /**
   * Creates a result iterator which lazily evaluates this module.
   * @param qc query context
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter(final QueryContext qc) throws QueryException {
    if(declType != null) return value(qc).iter();

    final int fp = vs.enter(qc);
    final Iter iter = expr.iter(qc);
    return new Iter() {
      boolean more = true;

      @Override
      public Item next() throws QueryException {
        if(more) {
          final Item item = iter.next();
          if(item != null) return item;
          more = false;
          VarScope.exit(fp, qc);
        }
        return null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        return iter.get(i);
      }
      @Override
      public long size() throws QueryException {
        return iter.size();
      }
      @Override
      public Value value(final QueryContext q, final Expr ex) throws QueryException {
        return iter.value(qc, ex);
      }
    };
  }

  /**
   * Creates the result.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  public Value value(final QueryContext qc) throws QueryException {
    final int fp = vs.enter(qc);
    try {
      final Value value = expr.value(qc);
      if(declType != null) declType.treat(value, null, qc, info);
      return value;
    } finally {
      VarScope.exit(fp, qc);
    }
  }

  /**
   * Adds the names of the databases that may be touched by the module.
   * @param locks lock result
   * @param qc query context
   * @return result of check
   */
  public boolean databases(final Locks locks, final QueryContext qc) {
    return expr.accept(new LockVisitor(locks, qc));
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    return expr.accept(visitor);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    expr.toXml(plan);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(expr);
  }

  /**
   * Lock visitor.
   * @author Leo Woerteler
   */
  private static final class LockVisitor extends ASTVisitor {
    /** Already visited scopes. */
    private final IdentityHashMap<Scope, Object> funcs = new IdentityHashMap<>();
    /** Reference to process list of locked databases. */
    private final Locks locks;
    /** Updating flag. */
    private final boolean updating;
    /** Focus level. */
    private int level;

    /**
     * Constructor.
     * @param locks lock result
     * @param qc query context
     */
    private LockVisitor(final Locks locks, final QueryContext qc) {
      this.locks = locks;
      updating = qc.updating;
      level = qc.ctxValue == null ? 0 : 1;
    }

    @Override
    public boolean lock(final String lock) {
      // name is unknown at compile time: return false
      if(lock == null) return false;
      // if context item is found on top level, it will refer to currently opened database
      if(level == 0 || lock != Locking.CONTEXT) {
        (updating ? locks.writes : locks.reads).add(lock);
      }
      return true;
    }

    @Override
    public boolean lock(final Supplier<ArrayList<String>> list) {
      for(final String lock : list.get()) {
        if(!lock(lock)) return false;
      }
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
    public boolean staticFuncCall(final StaticFuncCall call) {
      return func(call.func());
    }

    @Override
    public boolean inlineFunc(final Scope scope) {
      enterFocus();
      final boolean ac = scope.visit(this);
      exitFocus();
      return ac;
    }

    @Override
    public boolean funcItem(final FuncItem func) {
      return func(func);
    }

    /**
     * Visits a scope.
     * @param scope scope
     * @return if more expressions should be visited
     */
    private boolean func(final Scope scope) {
      if(funcs.containsKey(scope)) return true;
      funcs.put(scope, null);
      enterFocus();
      final boolean ac = scope.visit(this);
      exitFocus();
      return ac;
    }
  }
}
