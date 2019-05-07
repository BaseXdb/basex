package org.basex.query.expr.constr;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Array constructor.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class CArray extends Arr {
  /** Create sequences as array members. */
  private final boolean seq;

  /**
   * Constructor.
   * @param info input info
   * @param seq create sequences
   * @param exprs array expressions
   */
  public CArray(final InputInfo info, final boolean seq, final Expr... exprs) {
    super(info, SeqType.ARRAY_O, exprs);
    this.seq = seq;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    SeqType dt = null;
    for(final Expr expr : exprs) {
      SeqType st = expr.seqType();
      if(!seq) st = st.with(Occ.ONE);
      dt = dt == null ? st : dt.union(st);
    }
    if(dt != null) exprType.assign(ArrayType.get(dt));

    return allAreValues(true) ? cc.preEval(this) : this;
  }

  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ArrayBuilder builder = new ArrayBuilder();
    if(seq) {
      for(final Expr expr : exprs) {
        builder.append(expr.value(qc));
      }
    } else {
      for(final Expr expr : exprs) {
        final Iter iter = expr.iter(qc);
        for(Item item; (item = qc.next(iter)) != null;) builder.append(item);
      }
    }
    return builder.freeze();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CArray(info, seq, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof CArray && seq == ((CArray) obj).seq && super.equals(obj);
  }

  @Override
  public String description() {
    return QueryText.ARRAY;
  }

  @Override
  public String toString() {
    final int el = exprs.length;
    if(seq && el == 0) return "[]";

    final TokenBuilder tb = new TokenBuilder().add(seq ? "[" : QueryText.ARRAY + " {");
    for(int e = 0; e < el; e++) {
      if(e != 0) tb.add(',');
      tb.add(' ').add(exprs[e]);
    }
    return tb.add(seq ? " ]" : " }").toString();
  }
}
