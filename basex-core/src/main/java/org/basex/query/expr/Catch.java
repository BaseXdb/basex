package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Catch clause.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Catch extends Single {
  /** Error tests. */
  private final ArrayList<Test> tests;
  /** Error variables. */
  private final Var[] vars;
  /** Indexes of the error variables that are referenced by the expression. */
  private BitSet used = new BitSet();

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr expression
   * @param vars variables to be bound
   * @param tests error tests
   */
  public Catch(final InputInfo info, final Expr expr, final Var[] vars,
      final ArrayList<Test> tests) {
    super(info, expr, Types.ITEM_ZM);
    this.tests = tests;
    this.vars = vars;
    this.expr = expr;
    used.set(0, vars.length);
  }

  @Override
  public Catch compile(final CompileContext cc) throws QueryException {
    expr = cc.compileOrError(expr, false);
    return optimize(cc);
  }

  @Override
  public Catch optimize(final CompileContext cc) {
    final BitSet bs = new BitSet();
    final int vl = vars.length;
    for(int v = 0; v < vl; v++) {
      if(expr.count(vars[v]) != VarUsage.NEVER) bs.set(v);
    }
    used = bs;
    return (Catch) adoptType(expr);
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    expr.markTailCalls(cc);
  }

  /**
   * Returns the value of the caught expression.
   * @param qc query context
   * @param ex caught exception
   * @return resulting item
   * @throws QueryException query exception
   */
  Value value(final QueryContext qc, final QueryException ex) throws QueryException {
    if(!used.isEmpty()) {
      final ValueList values = values(ex);
      for(int v = used.nextSetBit(0); v != -1; v = used.nextSetBit(v + 1)) {
        qc.set(vars[v], values.get(v));
      }
    }
    return expr.value(qc);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final Var[] vrs = QueryException.variables(cc.qc, info);
    final int vl = vrs.length;
    for(int v = 0; v < vl; v++) {
      vm.put(vars[v].id, cc.vs().add(vrs[v]));
    }
    return copyType(new Catch(info, expr.copy(cc, vm), vrs, new ArrayList<>(tests)));
  }

  @Override
  public Catch inline(final InlineContext ic) {
    try {
      final Expr inlined = expr.inline(ic);
      if(inlined == null) return null;
      expr = inlined;
    } catch(final QueryException ex) {
      expr = FnError.get(ex);
    }
    return this;
  }

  /**
   * Returns the catch expression with inlined exception values.
   * @param ex caught exception
   * @param cc compilation context
   * @return expression
   * @throws QueryException query exception
   */
  Expr inline(final QueryException ex, final CompileContext cc) throws QueryException {
    if(expr instanceof Value || used.isEmpty()) return expr;

    final ValueList values = values(ex);
    Expr inlined = expr;
    for(int v = used.nextSetBit(0); v != -1; v = used.nextSetBit(v + 1)) {
      inlined = new InlineContext(vars[v], values.get(v), cc).inline(inlined);
    }
    return inlined;
  }

  /**
   * Returns the error values, including the location values only if they are referenced.
   * @param ex caught exception
   * @return values
   * @throws QueryException query exception
   */
  private ValueList values(final QueryException ex) throws QueryException {
    return ex.values(used.length() > QueryException.LOCATION);
  }

  /**
   * Removes redundant tests.
   * @param list current tests
   * @param cc compilation context
   * @return if catch clause contains relevant tests
   */
  boolean simplify(final ArrayList<Test> list, final CompileContext cc) {
    final Checks<Test> wildcard = t -> t instanceof final NameTest nt &&
        nt.scope == NameTest.Scope.ALL;

    // check if all errors are already caught
    if(Checks.any(list, wildcard)) {
      cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, "*");
      return false;
    }

    // drop remaining tests in favor of wildcard test
    final int w = Checks.index(tests, wildcard);
    if(w != -1 && tests.size() != 1) {
      final Test test = tests.get(w);
      tests.clear();
      tests.add(test);
      cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, "*");
    }

    // remove redundant tests
    final Iterator<Test> iter = tests.iterator();
    while(iter.hasNext()) {
      final Test test = iter.next();
      if(list.contains(test)) {
        cc.info(OPTREMOVE_X_X, test, (Supplier<?>) this::description);
        iter.remove();
      } else {
        list.add(test);
      }
    }
    return !tests.isEmpty();
  }

  /**
   * Checks if all errors are caught by this cause.
   * @return result of check
   */
  boolean global() {
    return tests.size() == 1 && tests.getFirst() instanceof final NameTest nt &&
        nt.scope == NameTest.Scope.ALL;
  }

  /**
   * Checks if one of the specified errors match the thrown error.
   * @param ex caught exception
   * @return result of check
   */
  boolean matches(final QueryException ex) {
    final QNm name = ex.qname();
    for(final Test test : tests) {
      if(test instanceof final NameTest nt && nt.matches(name)) return true;
    }
    return false;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Var var : vars) {
      if(!visitor.declared(var)) return false;
    }
    return visitAll(visitor, expr);
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Catch ctch && Array.equals(vars, ctch.vars) &&
        tests.equals(ctch.tests) && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(CATCH);
    int c = 0;
    for(final Test test : tests) {
      if(c++ > 0) qs.token('|');
      qs.token(test.toString(false));
    }
    qs.brace(expr);
  }
}
