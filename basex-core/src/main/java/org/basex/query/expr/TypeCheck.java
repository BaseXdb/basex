package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Checks the argument expression's result type.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public class TypeCheck extends Single {
  /** Static context. */
  final StaticContext sc;
  /** Flag for function conversion. */
  public final boolean promote;
  /** Only check occurrence indicator. */
  private boolean occ;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param expr expression to be promoted
   * @param seqType type to promote to
   * @param promote flag for function promotion
   */
  public TypeCheck(final StaticContext sc, final InputInfo info, final Expr expr,
      final SeqType seqType, final boolean promote) {
    super(info, expr, seqType);
    this.sc = sc;
    this.promote = promote;
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    final SeqType st = seqType();
    if(st.type.instanceOf(AtomType.ANY_ATOMIC_TYPE)) {
      expr = expr.simplifyFor(Simplify.DATA, cc);
    }

    if((ZERO_OR_ONE.is(expr) || EXACTLY_ONE.is(expr) || ONE_OR_MORE.is(expr)) &&
        st.occ.instanceOf(expr.seqType().occ)) expr = expr.arg(0);

    final SeqType et = expr.seqType();
    occ = et.type.instanceOf(st.type) && et.kindInstanceOf(st);

    // remove redundant type check
    if(expr instanceof TypeCheck) {
      final TypeCheck tc = (TypeCheck) expr;
      if(promote == tc.promote && st.instanceOf(et)) {
        return cc.replaceWith(this, get(tc.expr, st).optimize(cc));
      }
    }

    // skip check if return type is already correct
    if(et.instanceOf(st)) {
      cc.info(OPTTYPE_X_X, st, expr);
      return expr;
    }

    // function item coercion
    final FuncType ft = expr.funcType();
    if(ft != null && expr instanceof FuncItem) {
      if(!st.occ.check(1)) throw error(expr, st);
      return cc.replaceWith(this, ((FuncItem) expr).coerceTo(ft, cc.qc, info, true));
    }

    // pre-evaluate (check value and result size)
    final long es = expr.size();
    if(expr instanceof Value && es <= CompileContext.MAX_PREEVAL) {
      return cc.preEval(this);
    }

    // push type check inside expression
    final Expr checked = expr.typeCheck(this, cc);
    if(checked != null) {
      cc.info(OPTTYPE_X_X, st, checked);
      return checked;
    }

    // refine occurrence indicator and result size
    if(!expr.seqType().mayBeArray()) {
      final Occ o = et.occ.intersect(st.occ);
      if(o == null) throw error(expr, st);
      exprType.assign(st, o, et.occ == st.occ ? es : -1);
    }

    return this;
  }

  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final SeqType st = seqType();
    final Iter iter = expr.iter(qc);
    final Value value = iter.iterValue();
    if(value != null) {
      if(st.instance(value)) return iter;
      if(!promote) throw error(value, st);
    }

    // only check occurrence indicator
    if(occ) {
      return new Iter() {
        int c;

        @Override
        public Item next() throws QueryException {
          final Item item = qc.next(iter);
          if(item != null ? ++c > st.occ.max : c < st.occ.min) throw error(expr, st);
          return item;
        }
      };
    }

    // check item type and (optionally) occurrence indicator
    return new Iter() {
      final ItemList items = new ItemList();
      int i, c;

      @Override
      public Item next() throws QueryException {
        while(i == items.size()) {
          items.reset();
          i = 0;

          final Item item = qc.next(iter);
          if(item == null || st.instance(item)) {
            items.add(item);
          } else {
            if(!promote) throw error(expr, st);
            st.promote(item, null, items, qc, sc, info, false);
          }
        }

        final Item item = items.get(i);
        items.set(i++, null);
        if(item != null ? ++c > st.occ.max : c < st.occ.min) throw error(expr, st);
        return item;
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final Value value = expr.value(qc);
    final SeqType st = seqType();

    // only check occurrence indicator
    if(occ) {
      if(!st.occ.check(value.size())) throw error(value, st);
      return value;
    }
    // check occurrence indicator and item type
    if(st.instance(value)) return value;

    if(!promote) throw error(value, st);
    return st.promote(value, null, qc, sc, info, false);
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {
    return promote ? simplifyForCast(mode, cc) : super.simplifyFor(mode, cc);
  }

  /**
   * Checks if this type check is redundant if the result is bound to the given variable.
   * @param var variable
   * @return result of check
   */
  public final boolean isRedundant(final Var var) {
    return (!promote || var.promotes()) && var.declaredType().instanceOf(seqType());
  }

  /**
   * Creates an expression that checks the given expression's return type.
   * @param ex expression to check
   * @param cc compilation context
   * @return the resulting expression, or {@code null} if no type check is necessary
   * @throws QueryException query exception
   */
  public final Expr check(final Expr ex, final CompileContext cc) throws QueryException {
    final SeqType st = seqType();
    return ex.seqType().instanceOf(st) ? null : get(ex, st).optimize(cc);
  }

  /**
   * Returns the used error code.
   * @return error code
   */
  public QueryError error() {
    return promote ? INVPROMOTE_X_X_X : INVTREAT_X_X_X;
  }

  /**
   * Throws a type error.
   * @param ex expression that triggers the error
   * @param st target type
   * @return query exception
   */
  private QueryException error(final Expr ex, final SeqType st) {
    return typeError(ex, st, null, info, error());
  }

  /**
   * Returns a new instance of this class ({@link TypeCheck} or ({@link Treat}).
   * @param ex expression
   * @param st sequence type
   * @return error code
   */
  TypeCheck get(final Expr ex, final SeqType st) {
    return new TypeCheck(sc, info, ex, st, promote);
  }

  @Override
  public final Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final TypeCheck ex = copyType(get(expr.copy(cc, vm), seqType()));
    ex.occ = occ;
    return ex;
  }

  @Override
  public final Data data() {
    return seqType().type instanceof NodeType ? expr.data() : null;
  }

  @Override
  public final boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof TypeCheck)) return false;
    final TypeCheck tc = (TypeCheck) obj;
    return seqType().eq(tc.seqType()) && promote == tc.promote && super.equals(obj);
  }

  @Override
  public final void plan(final QueryPlan plan) {
    final FElem elem = plan.create(this, AS, seqType());
    if(promote) plan.addAttribute(elem, PROMOTE, true);
    plan.add(elem, expr);
  }

  @Override
  public final void plan(final QueryString qs) {
    qs.token("(").token(expr);
    if(promote) qs.token(PROMOTE).token(TO);
    else qs.token(TREAT).token(AS);
    qs.token(seqType()).token(')');
  }
}
