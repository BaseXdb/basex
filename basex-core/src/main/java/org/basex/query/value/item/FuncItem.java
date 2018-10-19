package org.basex.query.value.item;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function item.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Leo Woerteler
 */
public final class FuncItem extends FItem implements Scope {
  /** Static context. */
  public final StaticContext sc;
  /** Function expression. */
  public final Expr expr;

  /** Function name (may be {@code null}). */
  private final QNm name;
  /** Formal parameters. */
  private final Var[] params;
  /** Query focus. */
  private final QueryFocus focus;
  /** Size of the stack frame needed for this function. */
  private final int stackSize;

  /**
   * Constructor.
   * @param sc static context
   * @param anns function annotations
   * @param name function name (may be {@code null})
   * @param params formal parameters
   * @param type function type
   * @param expr function body
   * @param stackSize stack-frame size
   */
  public FuncItem(final StaticContext sc, final AnnList anns, final QNm name, final Var[] params,
      final FuncType type, final Expr expr, final int stackSize) {
    this(sc, anns, name, params, type, expr, new QueryFocus(), stackSize);
  }

  /**
   * Constructor.
   * @param sc static context
   * @param anns function annotations
   * @param name function name (may be {@code null})
   * @param params formal parameters
   * @param type function type
   * @param expr function body
   * @param focus query focus
   * @param stackSize stack-frame size
   */
  public FuncItem(final StaticContext sc, final AnnList anns, final QNm name, final Var[] params,
      final FuncType type, final Expr expr, final QueryFocus focus, final int stackSize) {

    super(type, anns);
    this.name = name;
    this.params = params;
    this.expr = expr;
    this.stackSize = stackSize;
    this.sc = sc;
    this.focus = focus;
  }

  @Override
  public int arity() {
    return params.length;
  }

  @Override
  public QNm funcName() {
    return name;
  }

  @Override
  public QNm paramName(final int ps) {
    return params[ps].name;
  }

  @Override
  public FuncType funcType() {
    return (FuncType) type;
  }

  @Override
  public int stackFrameSize() {
    return stackSize;
  }

  @Override
  public Value invValue(final QueryContext qc, final InputInfo info, final Value... args)
      throws QueryException {

    // bind variables and cache context
    final QueryFocus qf = qc.focus;
    qc.focus = focus;
    try {
      final int pl = params.length;
      for(int p = 0; p < pl; p++) qc.set(params[p], args[p]);
      return expr.value(qc);
    } finally {
      qc.focus = qf;
    }
  }

  @Override
  public Item invItem(final QueryContext qc, final InputInfo info, final Value... args)
      throws QueryException {
    // bind variables and cache context
    final QueryFocus qf = qc.focus;
    qc.focus = focus;
    try {
      final int pl = params.length;
      for(int p = 0; p < pl; p++) qc.set(params[p], args[p]);
      return expr.item(qc, info);
    } finally {
      qc.focus = qf;
    }
  }

  @Override
  public FuncItem coerceTo(final FuncType ft, final QueryContext qc, final InputInfo info,
      final boolean opt) throws QueryException {

    final int pl = params.length;
    if(pl != ft.argTypes.length) throw QueryError.typeError(this, ft.seqType(), null, info);

    // optimization: only ignore equal types
    final FuncType tp = funcType();
    if(opt ? tp.eq(ft) : tp.instanceOf(ft)) return this;

    final VarScope vs = new VarScope(sc);
    final Var[] vars = new Var[pl];
    final Expr[] args = new Expr[pl];
    for(int p = pl; p-- > 0;) {
      vars[p] = vs.addNew(params[p].name, ft.argTypes[p], true, qc, info);
      args[p] = new VarRef(info, vars[p]).optimize(null);
    }

    final boolean updating = anns.contains(Annotation.UPDATING) || expr.has(Flag.UPD);
    final Expr ex = new DynFuncCall(info, sc, updating, false, this, args);

    final CompileContext cc = new CompileContext(qc);
    cc.pushScope(vs);

    final Expr optimized = opt ? ex.optimize(cc) : ex, checked;
    if(tp.declType.instanceOf(ft.declType)) {
      checked = optimized;
    } else {
      final TypeCheck tc = new TypeCheck(sc, info, optimized, ft.declType, true);
      checked = opt ? tc.optimize(cc) : tc;
    }
    checked.markTailCalls(null);

    return new FuncItem(sc, anns, name, vars, ft, checked, vs.stackSize());
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.funcItem(this);
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    for(final Var param : params) {
      if(!visitor.declared(param)) return false;
    }
    return expr.accept(visitor);
  }

  @Override
  public void comp(final CompileContext cc) {
    // nothing to do here
  }

  @Override
  public boolean compiled() {
    return true;
  }

  @Override
  public Object toJava() {
    return this;
  }

  @Override
  public Expr inlineExpr(final Expr[] exprs, final CompileContext cc, final InputInfo info)
      throws QueryException {

    if(!StaticFunc.inline(cc, anns, expr) || expr.has(Flag.CTX)) return null;
    cc.info(OPTINLINE_X, this);

    // create let bindings for all variables
    final LinkedList<Clause> clauses = exprs.length == 0 ? null : new LinkedList<>();
    final IntObjMap<Var> vm = new IntObjMap<>();
    final int pl = params.length;
    for(int p = 0; p < pl; p++) {
      clauses.add(new Let(cc.copy(params[p], vm), exprs[p], false).optimize(cc));
    }

    // copy the function body
    final Expr rt = expr.copy(cc, vm);

    rt.accept(new ASTVisitor() {
      @Override
      public boolean inlineFunc(final Scope scope) {
        return scope.visit(this);
      }

      @Override
      public boolean dynFuncCall(final DynFuncCall call) {
        call.markInlined(FuncItem.this);
        return true;
      }
    });
    return clauses == null ? rt : new GFLWOR(info, clauses, rt).optimize(cc);
  }

  @Override
  public boolean isVacuousBody() {
    final SeqType st = expr.seqType();
    return st != null && st.zero() && !expr.has(Flag.UPD);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj;
  }

  @Override
  public void plan(final FElem plan) {
    final byte[] nm = name == null ? null : name.prefixId();
    addPlan(plan, planElem(NAME, nm, TYPE, type), params, expr);
  }

  @Override
  public String toErrorString() {
    return toString(true);
  }

  @Override
  public String toString() {
    return toString(false);
  }

  /**
   * Returns a string representation.
   * @param error error flag
   * @return string
   */
  private String toString(final boolean error) {
    final FuncType ft = (FuncType) type;
    final TokenBuilder tb = new TokenBuilder();
    if(name != null) tb.add("(: ").add(name.prefixId()).add("#").addInt(arity()).add(" :) ");
    tb.add(anns).add(FUNCTION).add('(');
    final int pl = params.length;
    for(int p = 0; p < pl; p++) {
      if(p != 0) tb.add(',');
      tb.add(error ? params[p].toErrorString() : params[p]);
    }
    return tb.add(')').add(" as ").add(ft.declType).add(" {...}").toString();
  }
}
