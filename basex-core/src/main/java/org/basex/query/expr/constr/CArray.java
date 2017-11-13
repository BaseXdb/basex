package org.basex.query.expr.constr;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.array.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Array constructor.
 *
 * @author BaseX Team 2005-17, BSD License
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
    SeqType vt = null;
    for(final Expr expr : exprs) {
      SeqType st = expr.seqType();
      if(!seq) st = st.withOcc(Occ.ONE);
      vt = vt == null ? st : vt.union(st);
    }
    if(vt != null) exprType.assign(ArrayType.get(vt));

    return allAreValues() ? cc.preEval(this) : this;
  }

  @Override
  public Array item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ArrayBuilder builder = new ArrayBuilder();
    if(seq) {
      for(final Expr expr : exprs) builder.append(qc.value(expr));
    } else {
      for(final Expr expr : exprs) {
        for(final Item it : qc.value(expr)) builder.append(it);
      }
    }
    return builder.freeze();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new CArray(info, seq, copyAll(cc, vm, exprs));
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
    final TokenBuilder tb = new TokenBuilder();
    tb.add(seq ? "[ " : "array { ");
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      if(e != 0) tb.add(", ");
      tb.addExt(exprs[e]);
    }
    return tb.add(seq ? " ]" : "}").toString();
  }
}
