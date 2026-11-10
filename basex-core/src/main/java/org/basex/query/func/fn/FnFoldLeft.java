package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Unfolding.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnFoldLeft extends StandardFunc {
  /** Exit condition and action producing the next result (can be {@code null}). */
  private FuncItem[] exitOrAction;
  /** Indicates if the exit condition checks if the result is empty. */
  private boolean exitOnEmpty;

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem action = action(qc);

    final HofArgs args = new HofArgs(3, action).set(0, arg(1).value(qc));
    for(Item item; (item = input.next()) != null;) {
      args.set(1, item).inc();
      if(exit(qc, args)) break;
      args.set(0, invoke(action, args, qc));
    }
    return args.get(0);
  }

  /**
   * Checks if the iteration can be exited early.
   * @param qc query context
   * @param args arguments
   * @return result of check
   * @throws QueryException query exception
   */
  protected final boolean exit(final QueryContext qc, final HofArgs args) throws QueryException {
    return exitOrAction != null && invoke(exitOrAction[0], args, qc).ebv(qc, info);
  }

  /**
   * Returns the action.
   * @param qc query context
   * @return action
   * @throws QueryException query exception
   */
  public final FItem action(final QueryContext qc) throws QueryException {
    return exitOrAction != null ? exitOrAction[1] : toFunction(arg(2), 3, qc);
  }

  /**
   * Indicates if this is a left fold.
   * @return result of check
   */
  protected boolean left() {
    return true;
  }

  /**
   * Indicates if an array is processed.
   * @return result of check
   */
  protected boolean array() {
    return false;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = optType(cc);
    return expr != this ? expr : unroll(cc);
  }

  /**
   * Unrolls the fold.
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr unroll(final CompileContext cc) throws QueryException {
    if(array()) return this;
    final Expr input = arg(0), init = arg(1), action = arg(2);
    final int arity = arity(action);
    if(arity == 2) {
      final ExprList unroll = cc.unroll(input, true);
      if(unroll != null) {
        final Expr func = coerceFunc(2, cc, arity);
        Expr expr = init;
        if(left()) {
          for(final Expr ex : unroll) {
            expr = new DynFuncCall(info, func, expr, ex).optimize(cc);
          }
        } else {
          for(int es = unroll.size() - 1; es >= 0; es--) {
            expr = new DynFuncCall(info, func, unroll.get(es), expr).optimize(cc);
          }
        }
        return expr;
      }
    }
    return this;
  }

  /**
   * Refines the types.
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr optType(final CompileContext cc) throws QueryException {
    final boolean array = array(), left = left();
    final Expr input = arg(0), init = arg(1), action = arg(2);
    final SeqType ist = input.seqType();
    final long size = array ? input.structSize() : -1;
    // fold-left((), INIT, $f) → INIT
    if(array ? size == 0 : ist.zero()) return cc.voidAndReturn(input, init, info);
    final boolean filled = array ? size > 0 : ist.oneOrMore();

    final SeqType zst = init.seqType();
    if(action instanceof FuncItem || action instanceof Closure) {
      final SeqType i1t = array ? ist.type instanceof final ArrayType at ? at.valueType() :
        Types.ITEM_O : ist.with(Occ.EXACTLY_ONE);
      final SeqType[] types = { i1t, i1t, Types.INTEGER_O };
      SeqType st = zst, ost, rt;
      do {
        types[left ? 0 : 1] = st;
        arg(2, arg -> action.refineFunc(cc, types));
        rt = arg(2).funcType().refinedType;
        ost = st;
        st = st.union(rt);
      } while(!st.eq(ost));
      exprType.assign(filled ? rt : st);

      // analyze refined action (more specific types) and original action (may be wrapped)
      final Expr[] funcs = arg(2) == action ? new Expr[] { action } : new Expr[] { arg(2), action };
      for(final Expr func : funcs) {
        final Expr flat = flatten(func, types, cc);
        if(flat != null) return flat;
      }
      // exits skip the remaining input
      if(!input.has(Flag.NDT)) {
        for(final boolean empty : new boolean[] { false, true }) {
          for(final Expr func : funcs) {
            // exits on empty results are replaced by other exits
            if(func instanceof final FuncItem fi &&
                (exitOrAction == null || exitOnEmpty && !empty)) {
              final Object fold = fi.fold(input, init, left, array, empty, cc);
              if(fold instanceof final Expr expr) return expr;
              if(fold instanceof final FuncItem[] eoa) {
                exitOrAction = eoa;
                exitOnEmpty = empty;
                cc.info(QueryText.OPTEXIT_X, this);
              }
            }
          }
        }
      }
    } else {
      final FuncType ft = action.funcType();
      if(ft != null) exprType.assign(filled ? ft.refinedType : zst.union(ft.refinedType));
    }
    return this;
  }

  /**
   * Rewrites a fold whose action appends, concatenates, adds or puts values into the result.
   * @param func action
   * @param types argument types of the action
   * @param cc compilation context
   * @return rewritten expression (can be {@code null})
   * @throws QueryException query exception
   */
  private Expr flatten(final Expr func, final SeqType[] types, final CompileContext cc)
      throws QueryException {

    // skip inlining if the body cannot have the required shape (static calls: shape is unknown)
    Expr raw = func instanceof final FuncItem fi ? fi.body() :
      func instanceof final Closure closure ? closure.expr : null;
    if(raw instanceof final TypeCheck tc) raw = tc.expr;
    if(!(raw instanceof StaticFuncCall || Unfolding.candidate(raw)) ||
        !(func instanceof final XQFunctionExpr fe)) return null;
    final int arity = fe.arity();
    if(arity < 2 || arity > 3) return null;
    // ACTION($result, $value, $pos): declared types are dropped if the argument types match
    final Var[] vars = new Var[arity];
    final Expr[] refs = new Expr[arity];
    for(int a = 0; a < arity; a++) {
      vars[a] = cc.vs().addNew(fe.paramName(a), null, cc.qc, info);
      vars[a].refineType(types[a], cc);
      refs[a] = new VarRef(info, vars[a]);
    }
    final Expr body = fe.inline(refs, true, cc);
    final boolean left = left(), array = array();
    final Var result = vars[left ? 0 : 1], value = vars[left ? 1 : 0];
    // positions: not supported (fold-right: step counts)
    if(body == null || arity > 2 && body.uses(vars[2]) ||
        body.has(Flag.NDT, Flag.UPD, Flag.CTX)) return null;

    final Expr input = arg(0), init = arg(1);
    final SeqType zst = init.seqType();
    final boolean string = zst.instanceOf(Types.STRING_O);
    final EnumSet<Op> supported = EnumSet.of(Op.LIST);
    // concatenation: the initial value is returned unchanged if no value is concatenated
    if(string || (array ? input.structSize() > 0 : input.seqType().oneOrMore())) {
      supported.add(Op.CONCAT);
    }
    if(zst.instanceOf(Types.DECIMAL_O)) supported.add(Op.ADD);
    if(left && zst.instanceOf(Types.MAP_O)) Collections.addAll(supported, Op.PUT, Op.REMOVE);

    // if(C) then ($result, X) else $result → if(C) then X else ()
    final Unfolding uf = new Unfolding(result, supported, info);
    final Expr action = body.unfold(uf, cc);
    final Op op = uf.op();
    if(!uf.valid() || op == null || action.uses(result) ||
        op == Op.CONCAT && uf.unchanged() && !string) return null;

    // for $value in INPUT return ACTION
    // for $member in array:split(INPUT) let $value := array:items($member) return ACTION
    final LinkedList<Clause> clauses = new LinkedList<>();
    Expr in = input;
    if(array) in = cc.function(Function._ARRAY_SPLIT, info, in);
    // fold-left: $result, X; fold-right: X, $result (integer sums are exact, order is irrelevant)
    if(op != Op.ADD && left != uf.first()) in = cc.function(Function.REVERSE, info, in);
    if(array) {
      final Var member = cc.vs().addNew(new QNm("member"), null, cc.qc, info);
      clauses.add(new For(member, in).optimize(cc));
      final Expr items = cc.function(Function._ARRAY_ITEMS, info, new VarRef(info, member));
      clauses.add(new Let(value, items).optimize(cc));
    } else {
      clauses.add(new For(value, in).optimize(cc));
    }
    // sum((INIT, LOOP)), map:merge((INIT, LOOP), use-last), INIT || LOOP, INIT, LOOP
    return uf.reduce(init, new GFLWOR(info, clauses, action).optimize(cc), cc);
  }

  @Override
  public StandardFunc copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final FnFoldLeft sf = (FnFoldLeft) super.copy(cc, vm);
    sf.exitOrAction = exitOrAction;
    sf.exitOnEmpty = exitOnEmpty;
    return sf;
  }
}
