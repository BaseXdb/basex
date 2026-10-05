package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function item.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class FuncItem extends FItem implements Scope {
  /** Function expression. */
  public final Expr expr;

  /** Parameters. */
  private final Var[] params;
  /** Annotations. */
  private final AnnList anns;
  /** Size of the stack frame needed for this function. */
  private final int stackSize;
  /** Input information (can be {@code null}). */
  private final InputInfo info;
  /** Function name (can be {@code null}). */
  private final QNm name;
  /** Query focus (can be {@code null}). */
  private final QueryFocus focus;
  /** Indicates if the query focus is accessed or modified. */
  private final boolean simple;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr function body
   * @param params parameters
   * @param anns function annotations
   * @param type function type
   * @param stackSize stack-frame size
   * @param name function name (can be {@code null})
   */
  public FuncItem(final InputInfo info, final Expr expr, final Var[] params, final AnnList anns,
      final FuncType type, final int stackSize, final QNm name) {
    this(info, expr, params, anns, type, stackSize, name, null);
  }

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr function body
   * @param params parameters
   * @param anns function annotations
   * @param type function type
   * @param stackSize stack-frame size
   * @param name function name (can be {@code null})
   * @param focus query focus (can be {@code null})
   */
  public FuncItem(final InputInfo info, final Expr expr, final Var[] params, final AnnList anns,
      final FuncType type, final int stackSize, final QNm name, final QueryFocus focus) {
    this(info, expr, params, anns, type, stackSize, name, focus, !expr.has(Flag.CTX));
  }

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr function body
   * @param params parameters
   * @param anns function annotations
   * @param type function type
   * @param stackSize stack-frame size
   * @param name function name (can be {@code null})
   * @param focus query focus (can be {@code null})
   * @param simple indicates if the query focus is neither accessed nor modified by the body
   */
  public FuncItem(final InputInfo info, final Expr expr, final Var[] params, final AnnList anns,
      final FuncType type, final int stackSize, final QNm name, final QueryFocus focus,
      final boolean simple) {
    super(type);
    this.info = info;
    this.expr = expr;
    this.params = params;
    this.anns = anns;
    this.stackSize = stackSize;
    this.name = name;
    this.focus = focus;
    this.simple = simple;
  }

  @Override
  public boolean instanceOf(final Type tp, final boolean coerce) {
    // coercion rebuilds records in the result
    return type.instanceOf(tp) && !(coerce && ShapeType.rebuilds(type, tp));
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
  public String paramString() {
    return XQFunctionExpr.paramString(this, params.length);
  }

  @Override
  public String funcIdentity() {
    final QNm qnm = funcName();
    final TokenBuilder tb = new TokenBuilder();
    tb.add(qnm != null ? qnm.prefixId() : "fn").add('#').addInt(arity());
    if(focus != null || qnm == null) tb.add('-').addInt(hashCode());
    return tb.toString();
  }

  @Override
  public AnnList annotations() {
    return anns;
  }

  @Override
  public FuncItem materialize(final Predicate<Data> test, final boolean funcs, final InputInfo ii,
      final QueryContext qc) throws QueryException {
    if(!funcs) throw BASEX_FUNCTION_X.get(info(ii), this);
    final FuncItem func = closure(test, ii, qc);
    TransferVisitor.check(func, info(ii));
    return func;
  }

  /**
   * Returns a function item in which the captured values and the captured query focus are
   * materialized.
   * @param test test for data references that can be shared
   * @param ii input info (can be {@code null})
   * @param qc query context
   * @return function item, or this item if nothing needed to be rewritten
   * @throws QueryException query exception
   */
  private FuncItem closure(final Predicate<Data> test, final InputInfo ii, final QueryContext qc)
      throws QueryException {

    final TypeCheck check = expr instanceof final TypeCheck tc ? tc : null;
    final Expr body = check != null ? check.expr : expr;
    Expr copy = null;
    if(body instanceof final GFLWOR gflwor) {
      final GFLWOR mat = gflwor.materialize(test, ii, qc);
      if(mat != gflwor) copy = check != null ? new TypeCheck(info, mat, check.seqType()) : mat;
    }

    QueryFocus qf = null;
    if(!simple && focus != null && focus.value != null) {
      final Value value = focus.value.materialize(test, true, ii, qc);
      if(value != focus.value) {
        qf = focus.copy();
        qf.value = value;
      }
    }

    if(copy == null && qf == null) return this;
    return new FuncItem(info, copy != null ? copy : expr, params, anns, funcType(), stackSize,
        name, qf != null ? qf : focus, simple);
  }

  /**
   * Returns the query focus that was captured when this function item was created.
   * @return query focus (can be {@code null})
   */
  public QueryFocus focus() {
    return focus;
  }

  @Override
  public void refineType(final Expr exp) {
    final Type tp = funcType().intersect(exp.seqType().type);
    if(tp != null) type = tp;
  }

  @Override
  public Value invokeInternal(final QueryContext qc, final InputInfo ii, final Value[] args)
      throws QueryException {
    return qc.invoke(params, args, expr, simple, focus);
  }

  @Override
  public int stackFrameSize() {
    return stackSize;
  }

  @Override
  boolean updating() {
    return anns.contains(Annotation.UPDATING) || expr.has(Flag.UPD);
  }

  /**
   * Checks if the function body is non-deterministic.
   * @return result of check
   */
  public boolean ndt() {
    return expr.has(Flag.NDT);
  }

  /**
   * Indicates if the query focus is left untouched by the function body.
   * @return result of check
   */
  public boolean simple() {
    return simple;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.funcItem(this);
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    return visitor.declared(params) && expr.accept(visitor);
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
    if(!cc.inlineable(anns, expr) || expr.has(Flag.CTX)) return null;
    cc.info(QueryText.OPTINLINE_X, this);
    return cc.inline(params, exprs, null, expr, null, info);
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
    if(!(item instanceof final FuncItem func) || !Var.equalTypes(params, func.params) ||
        deep == null && (!Objects.equals(name, func.name) ||
        !Objects.equals(funcType().declType, func.funcType().declType))) return false;
    // same body, and same captured focus if either body accesses the context
    final Expr body = body(), fbody = func.body();
    return bodyEqual(body, fbody, deep) && (simple && func.simple || focusEqual(func, body, deep));
  }

  /**
   * Checks if the bodies of two function items are equal.
   * @param body first function body
   * @param fbody second function body
   * @param deep comparator (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  private static boolean bodyEqual(final Expr body, final Expr fbody, final DeepEqual deep)
      throws QueryException {
    if(deep != null) {
      // captured values are compared with deep equality: bound in a closure, or inlined
      if(body instanceof final GFLWOR gflwor) return gflwor.deepEqual(fbody, deep);
      if(body instanceof final Value value && fbody instanceof final Value fvalue)
        return deep.equal(value, fvalue);
    }
    return body.equals(fbody);
  }

  /**
   * Checks if the focus components that are accessed by the body of two function items are
   * deep-equal.
   * @param func second function item
   * @param body function body
   * @param deep comparator (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean focusEqual(final FuncItem func, final Expr body, final DeepEqual deep)
      throws QueryException {
    final QueryFocus qf1 = focus, qf2 = func.focus;
    if(qf1 == null || qf2 == null) return qf1 == qf2;
    // fn:last and fn:position require a context value, but access only its size or position
    final boolean lst = LAST.is(body);
    if(lst || POSITION.is(body)) return (qf1.value == null) == (qf2.value == null) &&
        (lst ? qf1.size == qf2.size : qf1.pos == qf2.pos);
    // position and size are relevant if the body performs positional access
    if(body.has(Flag.POS) && (qf1.pos != qf2.pos || qf1.size != qf2.size)) return false;
    final Value v1 = qf1.value, v2 = qf2.value;
    return v1 == null || v2 == null ? v1 == v2 : deep != null ? deep.equal(v1, v2) : v1.equals(v2);
  }

  /**
   * Returns the function body: if this item is a function reference that has not been inlined,
   * the body of the referenced function is returned.
   * @return function body
   */
  private Expr body() {
    if(expr instanceof final StaticFuncCall call) {
      final StaticFunc sf = call.func();
      final int pl = params.length;
      if(sf != null && sf.expr != null && call.exprs.length == pl) {
        int p = pl;
        while(--p >= 0 && call.exprs[p] instanceof final VarRef ref && ref.var == params[p]);
        if(p == -1) return sf.expr;
      }
    }
    return expr;
  }

  @Override
  public boolean vacuousBody() {
    final SeqType st = expr.seqType();
    return st != null && st.zero() && !expr.has(Flag.UPD);
  }

  /**
   * Derives constant values or early exit operations for fold actions.
   * @param input input sequence
   * @param init initial expression
   * @param left indicates if this is a left/right fold
   * @param array indicates if an array is processed
   * @param empty only check if the iteration can be exited when the result gets empty
   * @param cc compilation context
   * @return constant value, early-exit expressions, or {@code null}
   * @throws QueryException query exception
   */
  public Object fold(final Expr input, final Expr init, final boolean left, final boolean array,
      final boolean empty, final CompileContext cc) throws QueryException {

    if(input.has(Flag.NDT)) return null;

    final int arity = arity();
    final IntFunction<Var> param = i -> i < arity ? params[i] : null;
    final Var value = param.apply(left ? 1 : 0), result = param.apply(left ? 0 : 1),
        pos = param.apply(2);
    final Predicate<Expr> isResult = ex -> ex instanceof final VarRef vr &&
        vr.var.equals(result);

    Expr exit = null, action = null;
    if(empty) {
      if(result != null && !result.seqType().oneOrMore() &&
          !expr.has(Flag.NDT, Flag.UPD, Flag.CTX)) {
        // ACTION yields () for $result := () → exit on empty($result)
        final InlineContext ic = new InlineContext(result, Empty.VALUE, cc);
        final Expr copy = expr.copy(cc, new IntObjectMap<>());
        try {
          if(ic.inlineable(copy) && ic.inline(copy) == Empty.VALUE) {
            exit = cc.function(EMPTY, info, new VarRef(info, result));
            action = expr;
          }
        } catch(final QueryException ignore) {
          // ACTION raises an error for $result := (): no early exit
        }
      }
      return exitOrAction(exit, action);
    }

    // fold-left(INPUT, INIT, fn($result, $value) { $result }) → INIT
    if(isResult.test(expr)) return init;

    if(input.seqType().oneOrMore()) {
      // fold-left(INPUT, INIT, fn($result, $value) { VALUE }) → VALUE
      if(!array && expr instanceof Value) return expr;
      // fold-left(INPUT, INIT, fn($result, $value) { $value }) → foot($value)
      if(expr instanceof final VarRef vr && vr.var.equals(value)) return cc.function(
          left ? array ? _ARRAY_FOOT : FOOT : array ? _ARRAY_HEAD : HEAD, info, input);
    }

    if(expr instanceof final If iff &&
        !(iff.cond.uses(value) || iff.cond.uses(pos) || iff.cond.has(Flag.NDT))) {
      // checks if a branch returns the unchanged result
      final BiPredicate<Expr, CmpOp> keeps = (branch, op) -> {
        if(isResult.test(branch)) return true;
        if(!(iff.cond instanceof final CmpG cmp) || cmp.cmpOp() != op) return false;
        final Expr op1 = cmp.arg(0), op2 = cmp.arg(1);
        final SeqType st1 = op1.seqType();
        // strings: restrict to default collation (value equality must imply identity)
        return isResult.test(op1) && op2 instanceof Item && op2.equals(branch) &&
            st1.eq(op2.seqType()) && (st1.instanceOf(Types.DECIMAL_O) ||
            st1.instanceOf(Types.STRING_O) && cmp.sc().collation == null);
      };
      if(keeps.test(iff.arg(0), CmpOp.EQ)) {
        // if(COND) then $result else ACTION → exit on COND
        // if($result = ITEM) then ITEM else ACTION → exit on COND
        exit = iff.cond;
        action = iff.arg(1);
      } else if(keeps.test(iff.arg(1), CmpOp.NE)) {
        // if(COND) then ACTION else $result → exit on not(COND)
        // if($result != ITEM) then ACTION else ITEM → exit on not(COND)
        exit = cc.function(NOT, info, iff.cond);
        action = iff.arg(0);
      }
    } else if(expr instanceof final Logical logical && init.seqType().eq(Types.BOOLEAN_O) &&
        !expr.has(Flag.NDT)) {
      // $result or  ACTION → exit on boolean($result)
      // $result and ACTION → exit on not($result)
      final ExprList ops = new ExprList().add(logical.exprs);
      int r = ops.size();
      while(--r >= 0 && !isResult.test(ops.get(r)));
      if(r != -1 && ops.size() > 1) {
        final boolean or = logical instanceof Or;
        exit = cc.function(or ? BOOLEAN : NOT, info, ops.remove(r));
        action = (or ? new Or(info, ops.finish()) : new And(info, ops.finish())).optimize(cc);
      }
    } else if(expr instanceof final Otherwise otherwise && isResult.test(otherwise.exprs[0])) {
      // $result otherwise ACTION → exit on exists($result)
      final Expr[] ops = otherwise.exprs;
      exit = cc.function(EXISTS, info, ops[0]);
      action = new Otherwise(info, Arrays.copyOfRange(ops, 1, ops.length)).optimize(cc);
    }
    return exitOrAction(exit, action);
  }

  /**
   * Creates function items for an early exit condition and the resulting action.
   * @param exit exit condition (can be {@code null})
   * @param action action (can be {@code null})
   * @return function items, or {@code null} if no exit condition is supplied
   */
  private FuncItem[] exitOrAction(final Expr exit, final Expr action) {
    return exit == null ? null : new FuncItem[] {
      new FuncItem(info, exit, params, anns, funcType(), stackSize, null, focus),
      new FuncItem(info, action, params, anns, funcType(), stackSize, null, focus)
    };
  }

  @Override
  public FuncItem refineFunc(final CompileContext cc, final SeqType... argTypes)
      throws QueryException {
    // skip refinement if function has too many parameters
    final int nargs = argTypes.length, arity = arity();
    if(nargs >= arity) {
      // select more specific types arguments and return types
      final FuncType oldType = funcType();
      final SeqType[] oldArgTypes = oldType.argTypes, newArgTypes = new SeqType[arity];
      for(int a = 0; a < arity; a++) {
        final SeqType at = argTypes[a], oat = oldArgTypes[a];
        newArgTypes[a] = at.instanceOf(oat) ? at : oat;
      }
      final FuncType newType = FuncType.get(oldType.declType, newArgTypes);
      // coerce to refined function type
      final FuncItem fitem = newType.eq(oldType) ? this :
        (FuncItem) coerceTo(newType, cc.qc, cc, info);

      // drop redundant type checks, adopt the refined types
      final Var[] vars = fitem.params;
      for(int a = 0; a < arity; a++) {
        final SeqType vt = vars[a].declType;
        if(vt != null && argTypes[a].instanceOf(vt)) vars[a].refineType(argTypes[a], cc);
      }
      return fitem;
    }
    return this;
  }

  @Override
  public InputInfo info() {
    return info;
  }

  @Override
  public String description() {
    return QueryText.FUNCTION + ' ' + QueryText.ITEM;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, QueryText.NAME, name == null ? null : name.prefixId()),
        params, expr);
  }

  @Override
  public void toString(final QueryString qs) {
    if(qs.error() && name != null) {
      qs.token(funcLabel());
    } else {
      if(name != null) qs.concat("(: ", funcLabel(), " :)");
      qs.token(anns).token(QueryText.FN).params(params);
      qs.token(QueryText.AS).token(funcType().refinedType).brace(expr);
    }
  }
}
