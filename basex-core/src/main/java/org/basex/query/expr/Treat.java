package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Treat as expression.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class Treat extends Single {
  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param seqType sequence type
   */
  public Treat(final InputInfo info, final Expr expr, final SeqType seqType) {
    super(info, expr, seqType);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final SeqType st = seqType();
    final Iter iter = expr.iter(qc);
    final Item item = iter.next();
    // input is empty
    if(item == null) {
      if(st.mayBeEmpty()) return Empty.ITER;
      throw NOTREAT_X_X_X.get(info, Empty.VALUE.seqType(), st, Empty.VALUE);
    }
    // treat as empty sequence
    if(st.zero()) throw NOTREAT_X_X_X.get(info, item.type, st, item);

    if(st.zeroOrOne()) {
      final Item next = iter.next();
      if(next != null) {
        final ValueBuilder vb = new ValueBuilder(qc, item, next);
        if(iter.next() != null) vb.add(Str.get(DOTS));
        throw NOTREAT_X_X_X.get(info, expr.seqType(), st, vb.value());
      }
      if(!item.type.instanceOf(st.type)) throw NOTREAT_X_X_X.get(info, item.type, st, item);
      return item.iter();
    }

    return new Iter() {
      Item it = item;

      @Override
      public Item next() throws QueryException {
        if(it == null) return null;
        if(!it.type.instanceOf(st.type)) throw NOTREAT_X_X_X.get(info, it.type, st, it);
        final Item ii = it;
        it = qc.next(iter);
        return ii;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final SeqType st = seqType();
    final Value value = expr.value(qc);

    final long size = value.size();
    // input is empty
    if(size == 0) {
      if(st.mayBeEmpty()) return value;
      throw NOTREAT_X_X_X.get(info, Empty.VALUE.seqType(), st, Empty.VALUE);
    }
    // treat as empty sequence
    if(st.zero()) throw NOTREAT_X_X_X.get(info, value.type, st, value);

    if(st.zeroOrOne()) {
      if(size > 1) throw NOTREAT_X_X_X.get(info, value.seqType(), st, value);
      final Item item = value.itemAt(0);
      if(!item.type.instanceOf(st.type)) throw NOTREAT_X_X_X.get(info, item.type, st, item);
      return item;
    }

    for(long i = 0; i < size; i++) {
      qc.checkStop();
      final Item item = value.itemAt(i);
      if(!item.type.instanceOf(st.type)) throw NOTREAT_X_X_X.get(info, item.type, st, item);
    }
    return value;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Treat(info, expr.copy(cc, vm), seqType());
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Treat &&
        seqType().eq(((Treat) obj).seqType()) && super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(AS, seqType()), expr);
  }

  @Override
  public String toString() {
    return '(' + expr.toString() + ") " + TREAT + ' ' + AS + ' ' + seqType();
  }
}
