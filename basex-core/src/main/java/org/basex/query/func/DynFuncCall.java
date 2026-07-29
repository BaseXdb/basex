
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
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
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
    // the call is nondeterministic if the invoked function is (a function item is a value and so
    // carries no flag of its own, hence the explicit check)
    if(func.has(Flag.NDT) || func instanceof final Value value && mayBeNdt(value)) {
      ndt = true;
    }

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
    final Type ftype = func.seqType().type;
    final boolean struct = ftype instanceof MapType || ftype instanceof ArrayType;
    final FuncType ft = func.funcType();
    if(ft != null) {
      if(ft.argTypes != null) {
        final int arity = ft.argTypes.length;
        if(nargs != arity) throw arityError(func, nargs, arity, false, info);
        // keys of maps and arrays are atomized and checked by the lookup itself
        if(!struct) {
          for(int a = 0; a < arity; ++a) {
            exprs[a] = new TypeCheck(info, exprs[a], ft.argTypes[a]).compile(cc);
          }
        }
      }
      if(!updating && ft.anns.contains(Annotation.UPDATING)) {
        throw FUNCUP_X.get(info, func);
      }
      exprType.assign(ft.refinedType);
    }

    if(struct) {
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

  /**
   * Checks if an expression contains a nondeterministic function item or closure.
   * @param expr expression
   * @return result of check
   */
  public static boolean containsNdtFunction(final Expr expr) {
    return expr instanceof final Value value ? mayBeNdt(value) : !expr.accept(new ASTVisitor() {
      @Override
      public boolean funcItem(final FuncItem func) {
        return !func.ndt();
      }
      @Override
      public boolean inlineFunc(final Scope scope) {
        return !(scope instanceof final Closure cl && cl.has(Flag.NDT));
      }
    });
  }

  /**
   * Checks if a value may contain a nondeterministic function item or closure.
   * @param value value
   * @return result of check (large values are not traversed and yield {@code true})
   */
  private static boolean mayBeNdt(final Value value) {
    if(!atomic(value.seqType())) {
      if(value.size() > CompileContext.MAX_PREEVAL) return true;
      for(final Item item : value) {
        if(item instanceof final FuncItem fi) {
          if(fi.ndt()) return true;
        } else if(item instanceof final XQStruct struct && !atomic(struct.funcType().declType)) {
          if(struct.structSize() > CompileContext.MAX_PREEVAL) return true;
          if(item instanceof final XQArray array) {
            if(Checks.any(array.members(), m -> mayBeNdt(m))) return true;
          } else if(item instanceof final XQMap map) {
            if(Checks.any(map.entries(), e -> mayBeNdt(e.value()))) return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Checks if a sequence type is atomic, i.e., if it cannot contain function items.
   * @param st sequence type
   * @return result of check
   */
  private static boolean atomic(final SeqType st) {
    return st.type.instanceOf(BasicType.ANY_ATOMIC_TYPE);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return body() instanceof final FItem func ? eval(func, qc) : iterate(qc).value(qc, this);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return body() instanceof final FItem func ? eval(func, qc).iter() : iterate(qc);
  }

  /**
   * Evaluates a single function item.
   * @param func function item
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private Value eval(final FItem func, final QueryContext qc) throws QueryException {
    checkUp(func, updating);
    final int nargs = exprs.length - 1, arity = func.arity();
    if(nargs != arity) throw arityError(func, nargs, arity, false, info);
    return evalFunc(func, qc);
  }

  /**
   * Evaluates multiple function items.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter iterate(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter iter = body().unwrappedIter(qc);
      Iter value;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(value == null) {
            final Item item = iter.next();
            if(item == null) return null;
            if(item instanceof final FItem fi) value = eval(fi, qc).iter();
            else throw INVFUNCITEM_X_X.get(info, item.seqType(), item);
          }
          final Item item = value.next();
          if(item != null) return item;
          value = null;
        }
      }
    };
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
    return Flag.UPD.oneOf(flags) && updating || Flag.NDT.oneOf(flags) && (ndt || updating) ||
        super.has(Flag.remove(flags, Flag.UPD));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.dynFuncCall(body()) && super.accept(visitor);
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
    qs.token(exprs[el]).params(Arrays.copyOf(exprs, el));
  }
}
