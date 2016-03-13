package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.gflwor.GFLWOR.Clause;
import org.basex.query.func.*;
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
 * @author BaseX Team 2005-16, BSD License
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

  /** Context value. */
  private final Value ctxValue;
  /** Context position. */
  private final long pos;
  /** Context length. */
  private final long size;

  /** Size of the stack frame needed for this function. */
  private final int stackSize;

  /**
   * Constructor.
   * @param sc static context
   * @param anns function annotations
   * @param name function name (may be {@code null})
   * @param params function arguments
   * @param type function type
   * @param expr function body
   * @param stackSize stack-frame size
   */
  public FuncItem(final StaticContext sc, final AnnList anns, final QNm name, final Var[] params,
      final FuncType type, final Expr expr, final int stackSize) {
    this(sc, anns, name, params, type, expr, null, 0, 0, stackSize);
  }

  /**
   * Constructor.
   * @param sc static context
   * @param anns function annotations
   * @param name function name (may be {@code null})
   * @param params function arguments
   * @param type function type
   * @param expr function body
   * @param ctxValue context value
   * @param pos context position
   * @param size context size
   * @param stackSize stack-frame size
   */
  public FuncItem(final StaticContext sc, final AnnList anns, final QNm name, final Var[] params,
      final FuncType type, final Expr expr, final Value ctxValue, final long pos, final long size,
      final int stackSize) {

    super(type, anns);
    this.name = name;
    this.params = params;
    this.expr = expr;
    this.stackSize = stackSize;
    this.sc = sc;
    this.ctxValue = ctxValue;
    this.pos = pos;
    this.size = size;
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
  public QNm argName(final int ps) {
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
  public Value invValue(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    // bind variables and cache context
    final Value cv = qc.value;
    final long ps = qc.pos, sz = qc.size;
    try {
      qc.value = ctxValue;
      qc.pos = pos;
      qc.size = size;
      final int pl = params.length;
      for(int p = 0; p < pl; p++) qc.set(params[p], args[p], ii);
      return qc.value(expr);
    } finally {
      qc.value = cv;
      qc.pos = ps;
      qc.size = sz;
    }
  }

  @Override
  public Item invItem(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    // bind variables and cache context
    final Value cv = qc.value;
    final long ps = qc.pos, sz = qc.size;
    try {
      qc.value = ctxValue;
      qc.pos = pos;
      qc.size = size;
      final int pl = params.length;
      for(int p = 0; p < pl; p++) qc.set(params[p], args[p], ii);
      return expr.item(qc, ii);
    } finally {
      qc.value = cv;
      qc.pos = ps;
      qc.size = sz;
    }
  }

  @Override
  public FItem coerceTo(final FuncType ft, final QueryContext qc, final InputInfo ii,
      final boolean opt) throws QueryException {

    if(params.length != ft.argTypes.length) throw INVPROMOTE_X_X_X.get(ii, seqType(), ft, this);
    final FuncType tp = funcType();
    if(tp.instanceOf(ft)) return this;

    final VarScope vsc = new VarScope(sc);
    final int pl = params.length;
    final Var[] vars = new Var[pl];
    final Expr[] refs = new Expr[pl];
    for(int p = pl; p-- > 0;) {
      vars[p] = vsc.newLocal(qc, params[p].name, ft.argTypes[p], true);
      refs[p] = new VarRef(ii, vars[p]);
    }

    final Expr e = new DynFuncCall(ii, sc, this, refs);
    final Expr optimized = opt ? e.optimize(qc, vsc) : e, checked;
    if(ft.type == null || tp.type != null && tp.type.instanceOf(ft.type)) {
      checked = optimized;
    } else {
      final TypeCheck tc = new TypeCheck(sc, ii, optimized, ft.type, true);
      checked = opt ? tc.optimize(qc, vsc) : tc;
    }
    checked.markTailCalls(null);
    return new FuncItem(sc, anns, name, vars, ft, checked, vsc.stackSize());
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.funcItem(this);
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    for(final Var var : params) if(!visitor.declared(var)) return false;
    return expr.accept(visitor);
  }

  @Override
  public void compile(final QueryContext qc) {
    // nothing to do here
  }

  @Override
  public boolean compiled() {
    return true;
  }

  @Override
  public Object toJava() {
    throw Util.notExpected();
  }

  @Override
  public Expr inlineExpr(final Expr[] exprs, final QueryContext qc, final VarScope scp,
      final InputInfo ii) throws QueryException {

    if(!StaticFunc.inline(qc, anns, expr) || expr.has(Flag.CTX)) return null;
    qc.compInfo(OPTINLINE_X, this);

    // create let bindings for all variables
    final LinkedList<Clause> cls = exprs.length == 0 ? null : new LinkedList<Clause>();
    final IntObjMap<Var> vs = new IntObjMap<>();
    final int pl = params.length;
    for(int p = 0; p < pl; p++) {
      final Var old = params[p], v = scp.newCopyOf(qc, old);
      vs.put(old.id, v);
      cls.add(new Let(v, exprs[p], false, ii).optimize(qc, scp));
    }

    // copy the function body
    final Expr rt = expr.copy(qc, scp, vs);

    rt.accept(new ASTVisitor() {
      @Override
      public boolean inlineFunc(final Scope sub) {
        return sub.visit(this);
      }

      @Override
      public boolean dynFuncCall(final DynFuncCall call) {
        call.markInlined(FuncItem.this);
        return true;
      }
    });
    return cls == null ? rt : new GFLWOR(ii, cls, rt).optimize(qc, scp);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(TYPE, type), params, expr);
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
    tb.addExt(anns).add(FUNCTION).add('(');
    final int pl = params.length;
    for(final Var v : params) {
      tb.addExt(error ? v.toErrorString() : v.toString()).add(v == params[pl - 1] ? "" : ",");
    }
    return tb.add(')').add(ft.type != null ? " as " + ft.type : "").add(" {...}").toString();
  }
}
