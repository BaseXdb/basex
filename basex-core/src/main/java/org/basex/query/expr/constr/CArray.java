package org.basex.query.expr.constr;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.array.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
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
  public Expr optimize(final CompileContext cc) throws QueryException {
    return allAreValues() ? cc.preEval(this) : this;
  }

  @Override
  public Array item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ArrayBuilder builder = new ArrayBuilder();
    if(create) {
      for(final Expr expr : exprs) {
        for(final Item it : qc.value(expr)) builder.append(it);
      }
    } else {
      for(final Expr expr : exprs) builder.append(qc.value(expr));
    }
    return builder.freeze();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new CArray(info, create, copyAll(cc, vm, exprs));
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof CArray && create == ((CArray) obj).create && super.equals(obj);
  }

  @Override
  public String description() {
    return QueryText.ARRAY;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder("[ ");
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      if(e != 0) tb.add(", ");
      tb.addExt(exprs[e]);
    }
    return tb.add(" ]").toString();
  }
}
