
package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Dynamic function call.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class DynFuncCall extends FuncCall {
  /** Updating flag. */
  private final boolean updating;
  /** Nondeterministic flag. */
  private boolean ndt;

  /**
   * Function constructor.
   * @param info input info (can be {@code null})
   * @param expr function expression
   * @param args arguments
   */
  public DynFuncCall(final InputInfo info, final Expr expr, final Expr... args) {
    this(info, false, false, expr, args);
  }

  /**
   * Function constructor.
   * @param info input info (can be {@code null})
   * @param updating updating flag
   * @param ndt nondeterministic flag
   * @param expr function expression
   * @param args arguments
   */
  public DynFuncCall(final InputInfo info, final boolean updating, final boolean ndt,
      final Expr expr, final Expr... args) {

    super(info, ExprList.concat(args, expr));
    this.updating = updating;
    this.ndt = ndt;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    if(body().has(Flag.NDT)) ndt = true;
    return super.compile(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final Expr func = body();

    // ()() → ()
    if(func.seqType().zero()) return func;

    // unroll calls with multiple functions
    // example: (true#0, false#0)() → true#0(), false#0()
    final ExprList unroll = cc.unroll(func, false);
    if(unroll != null) {
      final ExprList results = new ExprList(unroll.size());
      for(final Expr ex : unroll) {
        results.add(new DynFuncCall(info, ex, Arrays.copyOf(exprs, exprs.length - 1)).optimize(cc));
      }
      return List.get(cc, info, results.finish());
    }

    // assign function type
    final int nargs = exprs.length - 1;
    final FuncType ft = func.funcType();
    if(ft != null) {
      if(ft.argTypes != null) {
        final int arity = ft.argTypes.length;
        if(nargs != arity) throw arityError(func, nargs, arity, false, info);
        for(int a = 0; a < arity; ++a) {
          final SeqType st = func.seqType().type instanceof MapType
              ? Types.ANY_ATOMIC_TYPE_O : ft.argTypes[a];
          exprs[a] = new TypeCheck(info, exprs[a], st).compile(cc);
        }
      }
      if(!sc().mixUpdates && !updating && ft.anns.contains(Annotation.UPDATING)) {
        throw FUNCUP_X.get(info, func);
      }
      exprType.assign(ft.declType);
    }

    if(func instanceof XQStruct) {
      // lookup key must be atomic
      if(nargs == 1) arg(0, arg -> arg.simplifyFor(Simplify.DATA, cc));
      // pre-evaluation is safe as maps and arrays contain values
      if(values(false, cc)) return cc.preEval(this);
    }

    // try to inline the function; avoid recursive inlining
    if(func instanceof final XQFunctionExpr fe) {
      if(!cc.inlined.contains(fe)) {
        checkUp(fe, updating);
        cc.inlined.push(fe);
        try {
          final Expr inlined = fe.inline(Arrays.copyOf(exprs, nargs), cc);
          if(inlined != null) return inlined;
        } finally {
          cc.inlined.pop();
        }
      }
    }
    return this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item item : body().value(qc)) {
      vb.add(eval(item, qc));
    }
    return vb.value(this);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter iter = body().iter(qc);
      Iter value;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(value == null) {
            final Item item = iter.next();
            if(item == null) return null;
            value = eval(item, qc).iter();
          }
          final Item item = value.next();
          if(item != null) return item;
          value = null;
        }
      }
    };
  }

  /**
   * Evaluates a function item.
   * @param item function to be evaluated
   * @param qc query context
   * @return the function
   * @throws QueryException query exception
   */
  Value eval(final Item item, final QueryContext qc) throws QueryException {
    if(!(item instanceof final FItem func)) throw INVFUNCITEM_X_X.get(info, item.seqType(), item);

    checkUp(func, updating);
    final int nargs = exprs.length - 1, arity = func.arity();
    if(nargs != arity) throw arityError(func, nargs, arity, false, info);
    return evalFunc(func, qc);
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(Arrays.copyOf(exprs, exprs.length - 1));
    body().checkUp();
  }

  /**
   * Returns the function body expression.
   * @return body
   */
  private Expr body() {
    return exprs[exprs.length - 1];
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final Expr[] copy = copyAll(cc, vm, exprs);
    final int last = copy.length - 1;
    final Expr[] args = Arrays.copyOf(copy, last);
    return copyType(new DynFuncCall(info, updating, ndt, copy[last], args));
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.UPD.oneOf(flags) && (updating || sc().mixUpdates) ||
           Flag.NDT.oneOf(flags) && (ndt || updating || sc().mixUpdates) ||
           super.has(Flag.remove(flags, Flag.UPD));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final DynFuncCall dfc && updating == dfc.updating &&
        super.equals(obj);
  }

  @Override
  public String description() {
    return body().description() + "(...)";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, TAILCALL, tco), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    final int el = exprs.length - 1;
    qs.token(exprs[el]).token('(');
    for(int e = 0; e < el; e++) {
      if(e > 0) qs.token(SEP);
      qs.token(exprs[e]);
    }
    qs.token(')');
  }
}
