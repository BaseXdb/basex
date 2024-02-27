package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Function item.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class FuncItem extends FItem implements Scope {
  /** Static context. */
  public final StaticContext sc;
  /** Function expression. */
  public final Expr expr;

  /** Formal parameters. */
  private final Var[] params;
  /** Annotations. */
  private final AnnList anns;
  /** Size of the stack frame needed for this function. */
  private final int stackSize;
  /** Input information. */
  private final InputInfo info;
  /** Function name (can be {@code null}). */
  private final QNm name;
  /** Query focus. */
  private final QueryFocus focus;
  /** Indicates if the query focus is accessed or modified. */
  private final boolean simple;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr function body
   * @param params formal parameters
   * @param anns function annotations
   * @param type function type
   * @param sc static context
   * @param stackSize stack-frame size
   * @param name function name (can be {@code null})
   */
  public FuncItem(final InputInfo info, final Expr expr, final Var[] params, final AnnList anns,
      final FuncType type, final StaticContext sc, final int stackSize, final QNm name) {
    this(info, expr, params, anns, type, sc, stackSize, name, null);
  }

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr function body
   * @param params formal parameters
   * @param anns function annotations
   * @param type function type
   * @param sc static context
   * @param stackSize stack-frame size
   * @param name function name (can be {@code null})
   * @param focus query focus (can be {@code null})
   */
  public FuncItem(final InputInfo info, final Expr expr, final Var[] params, final AnnList anns,
      final FuncType type, final StaticContext sc, final int stackSize, final QNm name,
      final QueryFocus focus) {

    super(type);
    this.info = info;
    this.expr = expr;
    this.params = params;
    this.anns = anns;
    this.sc = sc;
    this.stackSize = stackSize;
    this.name = name;
    this.focus = focus;
    this.simple = !expr.has(Flag.CTX);
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
  public AnnList annotations() {
    return anns;
  }

  @Override
  public Value invokeInternal(final QueryContext qc, final InputInfo ii, final Value[] args)
      throws QueryException {

    final int arity = arity();
    for(int a = 0; a < arity; a++) qc.set(params[a], args[a]);

    // use shortcut if focus is not accessed
    if(simple) return expr.value(qc);

    final QueryFocus qf = qc.focus;
    qc.focus = focus != null ? focus : new QueryFocus();
    try {
      return expr.value(qc);
    } finally {
      qc.focus = qf;
    }
  }

  @Override
  public int stackFrameSize() {
    return stackSize;
  }

  @Override
  public FuncItem coerceTo(final FuncType ft, final QueryContext qc, final CompileContext cc,
      final InputInfo ii) throws QueryException {

    final int arity = arity(), nargs = ft.argTypes.length;
    if(nargs < arity) throw arityError(this, arity, nargs, false, info);

    // optimize: continue with coercion if current type is only an instance of new type
    FuncType tp = funcType();
    if(cc != null ? tp.eq(ft) : tp.instanceOf(ft)) return this;

    // create new compilation context and variable scope
    final VarScope vs = new VarScope(sc);
    final Var[] vars = new Var[arity];
    final Expr[] args = new Expr[arity];
    for(int a = 0; a < arity; a++) {
      vars[a] = vs.addNew(params[a].name, ft.argTypes[a], true, qc, info);
      args[a] = new VarRef(info, vars[a]).optimize(cc);
    }

    try {
      if(cc != null) cc.pushScope(vs);

      // create new function call (will immediately be inlined/simplified when being optimized)
      final boolean updating = anns != null && anns.contains(Annotation.UPDATING) ||
          expr.has(Flag.UPD);
      Expr body = new DynFuncCall(info, sc, updating, false, this, args);
      if(cc != null) body = body.optimize(cc);

      // add type check if return types differ
      final SeqType dt = ft.declType;
      if(!tp.declType.instanceOf(dt)) {
        body = new TypeCheck(info, body, dt, true);
        if(cc != null) body = body.optimize(cc);
      }

      // adopt type of optimized body if it is more specific than passed on type
      final SeqType bt = body.seqType();
      tp = cc != null && !bt.eq(dt) && bt.instanceOf(dt) ? FuncType.get(bt, ft.argTypes) : ft;
      body.markTailCalls(null);
      return new FuncItem(info, body, vars, anns, tp, sc, vs.stackSize(), name);
    } finally {
      if(cc != null) cc.removeScope();
    }
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
  public boolean compiled() {
    return true;
  }

  @Override
  public Object toJava() {
    return this;
  }

  @Override
  public Expr inline(final Expr[] exprs, final CompileContext cc) throws QueryException {
    if(!StaticFunc.inline(cc, anns, expr) || expr.has(Flag.CTX)) return null;
    cc.info(OPTINLINE_X, this);

    // create let bindings for all variables
    final LinkedList<Clause> clauses = new LinkedList<>();
    final IntObjMap<Var> vm = new IntObjMap<>();
    final int arity = arity();
    for(int a = 0; a < arity; a++) {
      clauses.add(new Let(cc.copy(params[a], vm), exprs[a]).optimize(cc));
    }

    // create the return clause
    final Expr rtrn = expr.copy(cc, vm).optimize(cc);
    rtrn.accept(new InlineVisitor());
    return clauses.isEmpty() ? rtrn : new GFLWOR(info, clauses, rtrn).optimize(cc);
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw FIATOMIZE_X.get(info, this);
  }

  @Override
  public Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw FIATOMIZE_X.get(info, this);
  }

  @Override
  public byte[] string(final InputInfo ii) throws QueryException {
    throw FIATOMIZE_X.get(info, this);
  }

  @Override
  public boolean deepEqual(final Item item, final DeepEqual deep) throws QueryException {
    if(this == item) return true;
    if(item instanceof FuncItem) {
      final FuncItem func = (FuncItem) item;
      return arity() == func.arity() && expr.equals(func.expr);
    }
    return false;
  }

  @Override
  public boolean vacuousBody() {
    final SeqType st = expr.seqType();
    return st != null && st.zero() && !expr.has(Flag.UPD);
  }

  @Override
  public String description() {
    return FUNCTION + ' ' + ITEM;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name == null ? null : name.prefixId()), params, expr);
  }

  @Override
  public String toErrorString() {
    final QueryString qs = new QueryString();
    if(name != null) {
      qs.concat(name.prefixId(), "#", arity());
    } else {
      final StringList list = new StringList(arity());
      for(final Var param : params) list.add(param.toErrorString());
      qs.token(anns).token(FUNCTION).params(list.finish());
      qs.token(AS).token(funcType().declType).brace(expr);
    }
    return qs.toString();
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(anns);
    if(name != null) qs.concat("(: ", name.prefixId(), "#", arity(), " :)");
    qs.token(FUNCTION).params(params).token(AS).token(funcType().declType).brace(expr);
  }

  /**
   * Optimizes the function item for a fold operation.
   * @param input input sequence
   * @param array indicates if an array is processed
   * @param left indicates if this is a left/right fold
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  public Object fold(final Expr input, final boolean array, final boolean left,
      final CompileContext cc) throws QueryException {
    if(arity() == 2 && !input.has(Flag.NDT)) {
      final Var actionVar = params[left ? 1 : 0], resultVar = params[left ? 0 : 1];
      final Predicate<Expr> result = ex -> ex instanceof VarRef &&
          ((VarRef) ex).var.equals(resultVar);

      // fold-left(SEQ, ZERO, f($result, $value) { VALUE })  ->  VALUE
      if(!array && input.seqType().oneOrMore() && expr instanceof Value) return expr;
      // fold-left(SEQ, ZERO, f($result, $value) { $result })  ->  $result
      if(result.test(expr)) return "";

      if(expr instanceof If) {
        final If iff = (If) expr;
        final Expr cond = iff.cond, thn = iff.exprs[0], els = iff.exprs[1];
        if(!(cond.uses(actionVar) || cond.has(Flag.NDT))) {
          Expr cnd = cond, action = null;
          if(result.test(thn)) {
            // if(COND) then $result else ACTION
            // -> if COND($result): break; else $result = ACTION($result, $value)
            action = els;
          } else if(result.test(els)) {
            // if(COND) then ACTION else $result
            // -> if not(COND(result)): break; else $result = ACTION($result, $value)
            action = thn;
            cnd = cc.function(org.basex.query.func.Function.NOT, info, cond);
          }
          if(action != null) return new FuncItem[] {
            new FuncItem(info, cnd, params, anns, funcType(), sc, stackSize, null, focus),
            new FuncItem(info, action, params, anns, funcType(), sc, stackSize, null, focus)
          };
        }
      }
    }
    return null;
  }

  /**
   * A visitor for checking inlined expressions.
   *
   * @author BaseX Team 2005-24, BSD License
   * @author Leo Woerteler
   */
  private final class InlineVisitor extends ASTVisitor {
    @Override
    public boolean inlineFunc(final Scope scope) {
      return scope.visit(this);
    }

    @Override
    public boolean dynFuncCall(final DynFuncCall call) {
      call.markInlined(FuncItem.this);
      return true;
    }
  }
}
