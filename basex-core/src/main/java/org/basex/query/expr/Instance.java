package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Instance test.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class Instance extends Single {
  /** Sequence type to check for. */
  private final SeqType seqType;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param seqType sequence type to check for
   */
  public Instance(final InputInfo info, final Expr expr, final SeqType seqType) {
    super(info, expr, SeqType.BLN_O);
    this.seqType = seqType;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // check value
    if(expr instanceof Value) return cc.preEval(this);

    // check static type
    Expr ex = this;
    if(!expr.has(Flag.NDT)) {
      final SeqType st = expr.seqType();
      // (1, 2)[. = 1] instance of xs:numeric*
      if(st.instanceOf(seqType)) ex = Bln.TRUE;
      // (1, 2)[. = 1] instance of xs:string
      else if(st.intersect(seqType) == null) ex = Bln.FALSE;
    }
    return cc.replaceWith(this, ex);
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // check instance of value
    final Iter iter = expr.iter(qc);
    final Value value = iter.iterValue();
    if(value != null) return Bln.get(seqType.instance(value));

    // if occurrence indicator matches, only check type
    final SeqType st = expr.seqType();
    if(st.occ.instanceOf(seqType.occ)) {
      for(Item item; (item = iter.next()) != null;) {
        if(!seqType.instance(item)) return Bln.FALSE;
      }
      return Bln.TRUE;
    }

    // if type matches, only check occurrence indicator
    final long max = seqType.occ.max;
    if(st.type.instanceOf(seqType.type)) {
      return Bln.get(iter.next() == null ? !seqType.oneOrMore() :
        max > 1 || max > 0 && iter.next() == null);
    }

    // check both occurrence indicator and type
    long c = 0;
    for(Item item; (item = iter.next()) != null;) {
      if(++c > max || !seqType.instance(item)) return Bln.FALSE;
    }
    return Bln.get(c != 0 || !seqType.oneOrMore());
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Instance(info, expr.copy(cc, vm), seqType));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj
        || obj instanceof Instance && seqType.eq(((Instance) obj).seqType) && super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, OF, seqType), expr);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(expr).token(INSTANCE).token(OF).token(seqType);
  }
}
