package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpG.*;
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
    this.simple = !expr.has(Flag.CTX);
  }

  /**
   * Construct a new function item from an existing one, with replaced annotations and focus.
   * @param funcItem the existing function item
   * @param anns the new annotations
   * @param focus the new focus
   */
  public FuncItem(final FuncItem funcItem, final AnnList anns, final QueryFocus focus) {
    this(
        funcItem.info, funcItem.expr, funcItem.params, anns, FuncType.get(anns,
            ((FuncType) funcItem.type).declType, ((FuncType) funcItem.type).argTypes),
        funcItem.stackSize, funcItem.name, focus);
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
    if(!StaticFunc.inline(cc, anns, expr) || expr.has(Flag.CTX)) return null;
    cc.info(OPTINLINE_X, this);

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
            // -> if COND: return $result; else $result = ACTION
            action = els;
          } else if(result.test(els)) {
            // if(COND) then ACTION else $result
            // -> if not(COND): return $result; else $result = ACTION
            cnd = cc.function(org.basex.query.func.Function.NOT, info, cond);
            action = thn;
          } else if(cond instanceof CmpG) {
            // if($result = ITEM) then ITEM else ACTION
            // -> if COND: return $result; else $result = ACTION
            final CmpG cmp = (CmpG) cond;
            final Expr op1 = cmp.arg(0), op2 = cmp.arg(1);
            final SeqType st1 = op1.seqType(), st2 = op2.seqType();
            if(result.test(op1) && op2 instanceof Item && op2.equals(thn) &&
              cmp.opG() == OpG.EQ && st1.eq(st2) && (
              st1.instanceOf(SeqType.DECIMAL_O) || st1.instanceOf(SeqType.STRING_O))) {
              action = els;
            }
          }
          if(action != null) return new FuncItem[] {
            new FuncItem(info, cnd, params, anns, funcType(), stackSize, null, focus),
            new FuncItem(info, action, params, anns, funcType(), stackSize, null, focus)
          };
        }
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
      qs.token(anns).token(FN).params(list.finish());
      qs.token(AS).token(funcType().declType).brace(expr);
    }
    return qs.toString();
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(anns);
    if(name != null) qs.concat("(: ", name.prefixId(), "#", arity(), " :)");
    qs.token(FN).params(params).token(AS).token(funcType().declType).brace(expr);
  }
}
