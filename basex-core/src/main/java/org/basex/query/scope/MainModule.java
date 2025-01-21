package org.basex.query.scope;

import java.util.*;
import java.util.List;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;

/**
 * An XQuery main module.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public class MainModule extends AModule {
  /**
   * Constructor.
   * @param expr root expression
   * @param vs variable scope
   * @param sc static context
   */
  public MainModule(final Expr expr, final VarScope vs, final StaticContext sc) {
    super(sc);
    this.expr = expr;
    this.vs = vs;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    cc.pushScope(vs);
    try {
      expr = expr.compile(cc);
    } finally {
      cc.removeScope(this);
    }
    return null;
  }

  /**
   * Creates a result iterator which lazily evaluates this module.
   * @param qc query context
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter(final QueryContext qc) throws QueryException {
    final int fp = vs.enter(qc);
    final Iter iter = expr.iter(qc);
    if(iter.valueIter()) {
      vs.exit(fp, qc);
      return iter;
    }

    return new Iter() {
      boolean more = true;

      @Override
      public Item next() throws QueryException {
        if(more) {
          final Item item = iter.next();
          if(item != null) return item;
          more = false;
          vs.exit(fp, qc);
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
   * Adds the names of the databases that may be touched by the module.
   * @param visitor lock visitor
   * @return result of check
   */
  public final boolean databases(final LockVisitor visitor) {
    return expr.accept(visitor);
  }

  @Override
  public final boolean visit(final ASTVisitor visitor) {
    return expr.accept(visitor);
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    expr.toXml(plan);
  }

  @Override
  public final void toString(final QueryString qs) {
    for(final StaticDecl decl : references()) qs.token(decl).newline();
    qs.token(expr);
  }

  /**
   * Gathers all function and variable declarations that are referenced by the main module.
   * @return referenced declarations
   */
  public final List<StaticDecl> references() {
    final List<StaticDecl> decls = new ArrayList<>();
    final HashSet<Scope> visited = new HashSet<>();
    visit(new ASTVisitor() {
      @Override
      public boolean staticVar(final StaticVar var) {
        if(visited.add(var)) {
          var.visit(this);
          decls.add(var);
        }
        return true;
      }

      @Override
      public boolean staticFuncCall(final StaticFuncCall call) {
        final StaticFunc func = call.func();
        if(func != null && visited.add(func)) {
          func.visit(this);
          decls.add(func);
        }
        return true;
      }

      @Override
      public boolean inlineFunc(final Scope scope) {
        if(visited.add(scope)) scope.visit(this);
        return true;
      }

      @Override
      public boolean funcItem(final FuncItem func) {
        if(visited.add(func)) func.visit(this);
        return true;
      }
    });
    return decls;
  }
}
