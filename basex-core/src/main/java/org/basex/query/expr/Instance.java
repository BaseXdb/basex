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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Instance extends Single {
  /** Sequence type to check for. */
  private final SeqType seqType;
  /** Check: 1: only check item type, 2: only check occurrence indicator. */
  private int check;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param seqType sequence type to check for
   */
  public Instance(final InputInfo info, final Expr expr, final SeqType seqType) {
    super(info, expr, SeqType.BOOLEAN_O);
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
    final SeqType et = expr.seqType();
    if(!expr.has(Flag.NDT)) {
      // (1, 2)[. = 1] instance of xs:numeric*
      if(et.instanceOf(seqType)) return cc.replaceWith(this, Bln.TRUE);
      // (1, 2)[. = 1] instance of xs:string
      if(et.intersect(seqType) == null) return cc.replaceWith(this, Bln.FALSE);
    }

    // 1: only check item type, 2: only check occurrence indicator
    check = et.occ.instanceOf(seqType.occ) ? 1 :
      et.type.instanceOf(seqType.type) && et.kindInstanceOf(seqType) ? 2 : 0;
    return this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // check instance of value
    final Iter iter = expr.iter(qc);
    final Value value = iter.iterValue();
    if(value != null) return Bln.get(seqType.instance(value));

    // only check item type
    if(check == 1) {
      for(Item item; (item = iter.next()) != null;) {
        if(!seqType.instance(item)) return Bln.FALSE;
      }
      return Bln.TRUE;
    }

    // only check occurrence indicator
    final long max = seqType.occ.max;
    if(check == 2) return Bln.get(iter.next() == null ? !seqType.oneOrMore() :
      max > 1 || max > 0 && iter.next() == null);

    // check both occurrence indicator and type
    long c = 0;
    for(Item item; (item = iter.next()) != null;) {
      if(++c > max || !seqType.instance(item)) return Bln.FALSE;
    }
    return Bln.get(c != 0 || !seqType.oneOrMore());
  }

  @Override
  public Instance copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Instance ex = copyType(new Instance(info, expr.copy(cc, vm), seqType));
    ex.check = check;
    return ex;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Instance && seqType.eq(((Instance) obj).seqType) &&
        super.equals(obj);
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
