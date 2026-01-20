package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
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
    super(type);
    this.info = info;
    this.expr = expr;
    this.params = params;
    this.anns = anns;
    this.stackSize = stackSize;
    this.name = name;
    this.focus = focus;
    simple = !expr.has(Flag.CTX);
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
  public void refineType(final Expr exp) {
    final Type tp = funcType().intersect(exp.seqType().type);
    if(tp != null) type = tp;
  }

  @Override
  public Value invokeInternal(final QueryContext qc, final InputInfo ii, final Value[] args)
      throws QueryException {

    final int arity = arity();
    for(int a = 0; a < arity; a++) qc.set(params[a], args[a]);

    // use shortcut if focus is not accessed
    if(simple) return expr.value(qc);

    // reset context and evaluate function
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
  boolean updating() {
    return anns.contains(Annotation.UPDATING) || expr.has(Flag.UPD);
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
    if(!cc.inlineable(anns, expr) || expr.has(Flag.CTX)) return null;
    cc.info(QueryText.OPTINLINE_X, this);

    // create let bindings for all variables
    final LinkedList<Clause> clauses = new LinkedList<>();
    final IntObjectMap<Var> vm = new IntObjectMap<>();
    final int arity = arity();
    for(int a = 0; a < arity; a++) {
      clauses.add(new Let(cc.copy(params[a], vm), exprs[a]).optimize(cc));
    }

    // create the return clause
    final Expr rtrn = expr.copy(cc, vm).optimize(cc);
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
  public boolean deepEqual(final Item item, final DeepEqual deep) {
    if(this == item) return true;
    if(item instanceof final FuncItem func) {
      // functions must have same body and same parameters types (its names can differ)
      int a = arity();
      if(a == func.arity()) {
        while(--a >= 0 && params[a].seqType().eq(func.params[a].seqType()));
        return a == -1 && expr.equals(func.expr);
      }
    }
    return false;
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
   * @param cc compilation context
   * @return constant value, early-exit expressions, or {@code null}
   * @throws QueryException query exception
   */
  public Object fold(final Expr input, final Expr init, final boolean left, final boolean array,
      final CompileContext cc) throws QueryException {

    final int arity = arity();
    if(!input.has(Flag.NDT)) {
      final IntFunction<Var> param = i -> i < arity ? params[i] : null;
      final Var value = param.apply(left ? 1 : 0), result = param.apply(left ? 0 : 1);
      final BiPredicate<Expr, Var> isRef = (ex, var) -> ex instanceof final VarRef vr &&
          vr.var.equals(var);

      // fold-left(INPUT, INIT, fn($result, $value) { $result }) → INIT
      if(isRef.test(expr, result)) return init;

      if(input.seqType().oneOrMore()) {
        // fold-left(INPUT, INIT, fn($result, $value) { VALUE }) → VALUE
        if(!array && expr instanceof Value) return expr;
        // fold-left(INPUT, INIT, fn($result, $value) { $value }) → foot($value)
        if(isRef.test(expr, value)) return cc.function(
            left ? array ? _ARRAY_FOOT : FOOT : array ? _ARRAY_HEAD : HEAD, info, input);
      }

      Expr exit = null, action = null;
      final Expr expr1 = expr.arg(0), expr2 = expr.arg(1);
      final boolean ref1 = isRef.test(expr1, result), ref2 = isRef.test(expr2, result);
      if(expr instanceof final If iff) {
        if(!(iff.cond.uses(value) || iff.cond.has(Flag.NDT))) {
          if(ref1) {
            // if(COND) then $result else ACTION → exit on COND
            exit = iff.cond;
            action = expr2;
          } else if(ref2) {
            // if(COND) then ACTION else $result → exit on not(COND)
            exit = cc.function(NOT, info, iff.cond);
            action = expr1;
          } else if(iff.cond instanceof final CmpG cmp) {
            final Expr op1 = cmp.arg(0), op2 = cmp.arg(1);
            final SeqType st1 = op1.seqType(), st2 = op2.seqType();
            if(isRef.test(op1, result) && op2 instanceof Item && st1.eq(st2) &&
                (st1.instanceOf(Types.DECIMAL_O) || st1.instanceOf(Types.STRING_O))) {
              if(cmp.cmpOp() == CmpOp.EQ && op2.equals(expr1)) {
                // if($result = ITEM) then ITEM else ACTION → exit on equality
                exit = iff.cond;
                action = expr2;
              } else if(cmp.cmpOp() == CmpOp.NE && op2.equals(expr2)) {
                // if($result != ITEM) then ACTION else ITEM → exit on not(inequality)
                exit = cc.function(NOT, info, iff.cond);
                action = expr1;
              }
            }
          }
        }
      } else if(init.seqType().eq(Types.BOOLEAN_O) && expr instanceof final Logical logical &&
          !expr.has(Flag.NDT) && (ref1 || ref2)) {
        // $result or  ACTION → exit on boolean($result)
        // $result and ACTION → exit on not($result)
        exit = cc.function(logical instanceof Or ? BOOLEAN : NOT, info, ref1 ? expr1 : expr2);
        action = cc.function(BOOLEAN, info, ref1 ? expr2 : expr1);
      }

      if(exit != null) {
        return new FuncItem[] {
          new FuncItem(info, exit, params, anns, funcType(), stackSize, null, focus),
          new FuncItem(info, action, params, anns, funcType(), stackSize, null, focus)
        };
      }
    }
    return null;
  }

  /**
   * Creates a new function item with refined types.
   * @param argTypes argument types
   * @param cc compilation context
   * @return original or refined function item
   * @throws QueryException query context
   */
  public FuncItem refine(final SeqType[] argTypes, final CompileContext cc) throws QueryException {
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

      // drop redundant types
      final Var[] vars = fitem.params;
      for(int a = 0; a < arity; a++) {
        final SeqType vt = vars[a].declType;
        if(vt != null && argTypes[a].instanceOf(vt)) vars[a].declType = null;
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
      qs.concat(name.prefixId(), "#", arity());
    } else {
      if(name != null) {
        qs.concat("(: ", name.prefixId(), "#", arity(), " :)");
      }
      qs.token(anns).token(QueryText.FN).params(params);
      qs.token(QueryText.AS).token(funcType().declType).brace(expr);
    }
  }
}
