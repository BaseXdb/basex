package org.basex.query;

import java.util.*;

import org.basex.core.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * An XQuery main module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class MainModule extends StaticScope {
  /** Declared type, {@code null} if not specified. */
  private final SeqType declType;

  /**
   * Constructor for a function call.
   * @param sf static function
   * @param args function arguments
   * @throws QueryException query exception
   */
  public MainModule(final StaticFunc sf, final Expr[] args) throws QueryException {
    this(new StaticFuncCall(sf.name, args, sf.sc, sf.info).init(sf),
        new VarScope(sf.sc), null, sf.sc);
  }

  /**
   * Constructor.
   * @param expr root expression
   * @param scope variable scope
   * @param doc xqdoc documentation
   * @param sc static context
   */
  public MainModule(final Expr expr, final VarScope scope, final String doc,
      final StaticContext sc) {
    this(expr, scope, null, doc, sc, null);
  }

  /**
   * Constructor.
   * @param expr root expression
   * @param scope variable scope
   * @param doc xqdoc documentation
   * @param declType declared type (optional)
   * @param sc static context
   * @param info input info
   */
  public MainModule(final Expr expr, final VarScope scope, final SeqType declType, final String doc,
      final StaticContext sc, final InputInfo info) {

    super(scope, doc, sc, info);
    this.expr = expr;
    this.declType = declType;
  }

  @Override
  public void compile(final QueryContext qc) throws QueryException {
    if(compiled) return;
    try {
      compiled = true;
      expr = expr.compile(qc, scope);
    } finally {
      scope.cleanUp(this);
    }
  }

  /**
   * Evaluates this module and returns the result as a cached value iterator.
   * @param qc query context
   * @return result
   * @throws QueryException evaluation exception
   */
  public ValueBuilder cache(final QueryContext qc) throws QueryException {
    final int fp = scope.enter(qc);
    try {
      final Iter iter = expr.iter(qc);
      final ValueBuilder cache;
      if(iter instanceof ValueBuilder) {
        cache = (ValueBuilder) iter;
      } else {
        cache = new ValueBuilder(Math.max(1, (int) iter.size()));
        for(Item it; (it = iter.next()) != null;) cache.add(it);
      }
      if(declType != null) declType.treat(cache.value(), info);
      return cache;

    } finally {
      scope.exit(qc, fp);
    }
  }

  /**
   * Creates a result iterator which lazily evaluates this module.
   * @param qc query context
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter(final QueryContext qc) throws QueryException {
    if(declType != null) return cache(qc);

    final int fp = scope.enter(qc);
    final Iter iter = expr.iter(qc);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = iter.next();
        if(it == null) scope.exit(qc, fp);
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
   * @param qc query context
   * @return result of check
   * @see Proc#databases(LockResult)
   */
  public boolean databases(final LockResult lr, final QueryContext qc) {
    return expr.accept(new LockVisitor(lr, qc));
  }

  /**
   * Lock visitor.
   * @author Leo Woerteler
   */
  static class LockVisitor extends ASTVisitor {
    /** Already visited scopes. */
    private final IdentityHashMap<Scope, Object> funcs = new IdentityHashMap<>();
    /** List of databases to be locked. */
    private final StringList sl;
    /** Focus level. */
    private int level;

    /**
     * Constructor.
     * @param lr lock result
     * @param qc query context
     */
    LockVisitor(final LockResult lr, final QueryContext qc) {
      sl = qc.updating ? lr.write : lr.read;
      level = qc.ctxItem == null ? 0 : 1;
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
    public boolean staticFuncCall(final StaticFuncCall call) {
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
