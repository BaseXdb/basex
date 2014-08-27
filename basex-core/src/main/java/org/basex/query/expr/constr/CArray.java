package org.basex.query.expr.constr;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Array constructor.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class CArray extends Arr {
  /** Create array members. */
  private final boolean create;

  /**
   * Constructor.
   * @param info input info
   * @param create create members
   * @param exprs array expressions
   */
  public CArray(final InputInfo info, final boolean create, final Expr... exprs) {
    super(info, exprs);
    this.create = create;
    seqType = SeqType.ARRAY_O;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return allAreValues() ? preEval(qc) : this;
  }

  @Override
  public Array item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ValueList vl;
    if(create) {
      final ValueBuilder vb = new ValueBuilder(exprs.length);
      for(final Expr expr : exprs) vb.add(qc.value(expr));
      vl = new ValueList((int) vb.size());
      for(final Item it : vb) vl.add(it);
    } else {
      vl = new ValueList(exprs.length);
      for(final Expr expr : exprs) vl.add(qc.value(expr));
    }
    return vl.array();
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new CArray(info, create, copyAll(qc, scp, vs, exprs));
  }

  @Override
  public String description() {
    return QueryText.ARRAYSTR;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder("[");
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      if(e != 0) tb.add(", ");
      tb.add('(').addExt(exprs[e]).add(')');
    }
    return tb.add("]").toString();
  }
}
