package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
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
 * @author BaseX Team 2005-19, BSD License
 * @author Leo Woerteler
 */
public class TypeCheck extends Single {
  /** Static context. */
  final StaticContext sc;
  /** Flag for function conversion. */
  public final boolean promote;

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
    final SeqType at = expr.seqType(), st = seqType();

    // remove redundant type check
    if(expr instanceof TypeCheck) {
      final TypeCheck tc = (TypeCheck) expr;
      if(promote == tc.promote && st.instanceOf(at)) {
        return cc.replaceWith(this, get(tc.expr, st).optimize(cc));
      }
    }

    // skip check if return type is already correct
    if(at.instanceOf(st)) {
      cc.info(OPTTYPE_X_X, st, expr);
      return expr;
    }

    // function item coercion
    final FuncType ft = expr.funcType();
    if(ft != null && expr instanceof FuncItem) {
      if(!st.occ.check(1)) throw typeError(expr, st, null, info, error());
      return cc.replaceWith(this, ((FuncItem) expr).coerceTo(ft, cc.qc, info, true));
    }

    // we can type check immediately
    if(expr instanceof Value) return cc.preEval(this);

    // check at each call
    if(at.type.instanceOf(st.type) && at.occ.intersect(st.occ) == null)
      throw typeError(expr, st, null, info, error());

    final Expr opt = expr.typeCheck(this, cc);
    if(opt != null) {
      cc.info(OPTTYPE_X_X, st, opt);
      return opt;
    }

    return this;
  }

  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final SeqType st = seqType();
    final Iter iter = expr.iter(qc);

    return new Iter() {
      final ItemList items = new ItemList();
      int c, i;

      @Override
      public Item next() throws QueryException {
        while(c == items.size()) {
          items.reset();
          c = 0;

          final Item item = qc.next(iter);
          if(item == null || st.instance(item)) {
            items.add(item);
          } else if(promote) {
            st.promote(item, null, items, qc, sc, info, false);
          } else {
            throw typeError(expr, st, null, info, error());
          }
        }

        final Item item = items.get(c);
        items.set(c++, null);
        if(item == null && i < st.occ.min || i > st.occ.max)
          typeError(expr, st, null, info, error());
        i++;
        return item;
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final Value value = expr.value(qc);
    final SeqType st = seqType();
    if(st.instance(value)) return value;
    if(promote) return st.promote(value, null, qc, sc, info, false);
    throw typeError(value, st, null, info, error());
  }

  @Override
  public final Expr simplifyFor(final AtomType type, final CompileContext cc)
      throws QueryException {
    return promote ? simplifyCast(type, cc) : super.simplifyFor(type, cc);
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
   * @return the resulting expression
   * @throws QueryException query exception
   */
  public final Expr check(final Expr ex, final CompileContext cc) throws QueryException {
    final SeqType at = ex.seqType(), st = seqType();
    return at.instanceOf(st) ? ex : get(ex, st).optimize(cc);
  }

  /**
   * Return the used error code.
   * @return error code
   */
  public QueryError error() {
    return promote ? INVPROMOTE_X_X_X : INVTREAT_X_X_X;
  }

  /**
   * Return a new instance of this class.
   * @param ex expression
   * @param st sequence type
   * @return error code
   */
  public TypeCheck get(final Expr ex, final SeqType st) {
    return new TypeCheck(sc, info, ex, st, promote);
  }

  @Override
  public final Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return get(expr.copy(cc, vm), seqType());
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
  public final String toString() {
    final StringBuilder sb = new StringBuilder().append('(').append(expr).append(' ');
    if(promote) sb.append(PROMOTE).append(' ').append(TO);
    else sb.append(TREAT).append(' ').append(AS);
    return sb.append(' ').append(seqType()).append(')').toString();
  }
}
