package org.basex.query.value.item;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function item.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class FuncItem extends FItem implements Scope {
  /** Static context. */
  private final StaticContext sc;
  /** Function name (may be {@code null}). */
  private final QNm name;
  /** Formal parameters. */
  private final Var[] params;
  /** Function expression. */
  public final Expr expr;

  /** Context value. */
  private final Value ctxVal;
  /** Context position. */
  private final long pos;
  /** Context length. */
  private final long size;

  /** Size of the stack frame needed for this function. */
  private final int stackSize;

  /**
   * Constructor.
   * @param sc static context
   * @param ann function annotations
   * @param name function name (may be {@code null})
   * @param params function arguments
   * @param type function type
   * @param expr function body
   * @param stackSize stack-frame size
   */
  public FuncItem(final StaticContext sc, final Ann ann, final QNm name, final Var[] params,
      final FuncType type, final Expr expr, final int stackSize) {
    this(sc, ann, name, params, type, expr, null, 0, 0, stackSize);
  }

  /**
   * Constructor.
   * @param sc static context
   * @param ann function annotations
   * @param name function name (may be {@code null})
   * @param params function arguments
   * @param type function type
   * @param expr function body
   * @param ctxValue context value
   * @param pos context position
   * @param size context size
   * @param stackSize stack-frame size
   */
  public FuncItem(final StaticContext sc, final Ann ann, final QNm name, final Var[] params,
      final FuncType type, final Expr expr, final Value ctxValue, final long pos, final long size,
      final int stackSize) {

    super(type, ann);
    this.name = name;
    this.params = params;
    this.expr = expr;
    this.stackSize = stackSize;
    this.sc = sc;
    ctxVal = ctxValue;
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
      qc.value = ctxVal;
      qc.pos = pos;
      qc.size = size;
      for(int i = 0; i < params.length; i++) qc.set(params[i], args[i], ii);
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
      qc.value = ctxVal;
      qc.pos = pos;
      qc.size = size;
      for(int i = 0; i < params.length; i++) qc.set(params[i], args[i], ii);
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

    if(params.length != ft.argTypes.length) throw castError(ii, this, ft);
    final FuncType tp = funcType();
    if(tp.instanceOf(ft)) return this;

    final VarScope vsc = new VarScope(sc);
    final Var[] vs = new Var[params.length];
    final Expr[] refs = new Expr[vs.length];
    for(int i = vs.length; i-- > 0;) {
      vs[i] = vsc.newLocal(qc, params[i].name, ft.argTypes[i], true);
      refs[i] = new VarRef(ii, vs[i]);
    }

    final Expr e = new DynFuncCall(ii, sc, false, this, refs);
    final Expr optimized = opt ? e.optimize(qc, vsc) : e, checked;
    if(ft.retType == null || tp.retType != null && tp.retType.instanceOf(ft.retType)) {
      checked = optimized;
    } else {
      final TypeCheck tc = new TypeCheck(sc, ii, optimized, ft.retType, true);
      checked = opt ? tc.optimize(qc, vsc) : tc;
    }
    checked.markTailCalls(null);
    return new FuncItem(sc, ann, name, vs, ft, checked, vsc.stackSize());
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

    if(!(expr.isValue() || expr.exprSize() < qc.context.options.get(MainOptions.INLINELIMIT) &&
        !expr.has(Flag.CTX) && !expr.has(Flag.UPD))) return null;
    qc.compInfo(OPTINLINE, this);

    // create let bindings for all variables
    final LinkedList<GFLWOR.Clause> cls =
        exprs.length == 0 ? null : new LinkedList<GFLWOR.Clause>();
    final IntObjMap<Var> vs = new IntObjMap<>();
    for(int i = 0; i < params.length; i++) {
      final Var old = params[i], v = scp.newCopyOf(qc, old);
      vs.put(old.id, v);
      cls.add(new Let(v, exprs[i], false, ii).optimize(qc, scp));
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
  public String toString() {
    final FuncType ft = (FuncType) type;
    final TokenBuilder tb = new TokenBuilder(FUNCTION).add('(');
    for(final Var v : params) tb.addExt(v).add(v == params[params.length - 1] ? "" : ", ");
    tb.add(')').add(ft.retType != null ? " as " + ft.retType : "");
    return tb.add(" { ").addExt(expr).add(" }").toString();
  }
}
