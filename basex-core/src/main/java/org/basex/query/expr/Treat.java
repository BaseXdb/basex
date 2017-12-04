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
 * @author BaseX Team 2005-17, BSD License
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
    final Item it = iter.next();
    // input is empty
    if(it == null) {
      if(st.mayBeEmpty()) return Empty.ITER;
      throw NOTREAT_X_X_X.get(info, Empty.SEQ.seqType(), st, Empty.SEQ);
    }
    // treat as empty sequence
    if(st.zero()) throw NOTREAT_X_X_X.get(info, it.type, st, it);

    if(st.zeroOrOne()) {
      final Item n = iter.next();
      if(n != null) {
        final ValueBuilder vb = new ValueBuilder(qc).add(it).add(n);
        if(iter.next() != null) vb.add(Str.get(DOTS));
        throw NOTREAT_X_X_X.get(info, expr.seqType(), st, vb.value());
      }
      if(!it.type.instanceOf(st.type)) throw NOTREAT_X_X_X.get(info, it.type, st, it);
      return it.iter();
    }

    return new Iter() {
      Item i = it;

      @Override
      public Item next() throws QueryException {
        if(i == null) return null;
        if(!i.type.instanceOf(st.type)) throw NOTREAT_X_X_X.get(info, i.type, st, i);
        final Item ii = i;
        i = qc.next(iter);
        return ii;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final SeqType st = seqType();
    final Value val = expr.value(qc);

    final long len = val.size();
    // input is empty
    if(len == 0) {
      if(st.mayBeEmpty()) return val;
      throw NOTREAT_X_X_X.get(info, Empty.SEQ.seqType(), st, Empty.SEQ);
    }
    // treat as empty sequence
    if(st.zero()) throw NOTREAT_X_X_X.get(info, val.type, st, val);

    if(st.zeroOrOne()) {
      if(len > 1) throw NOTREAT_X_X_X.get(info, val.seqType(), st, val);
      final Item it = val.itemAt(0);
      if(!it.type.instanceOf(st.type)) throw NOTREAT_X_X_X.get(info, it.type, st, it);
      return it;
    }

    for(long i = 0; i < len; i++) {
      qc.checkStop();
      final Item it = val.itemAt(i);
      if(!it.type.instanceOf(st.type)) throw NOTREAT_X_X_X.get(info, it.type, st, it);
    }
    return val;
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
