package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Text fragment.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class CTxt extends CNode {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param text text
   */
  public CTxt(final StaticContext sc, final InputInfo info, final Expr text) {
    super(sc, info, text);
    seqType = SeqType.TXT_ZO;
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    final Expr e = exprs[0];
    if(e == Str.ZERO || e.isEmpty()) return cc.emptySeq(this);
    // non-empty string: result cannot be empty
    if(e instanceof Str) {
      seqType = SeqType.TXT;
      size = 1;
    }
    return this;
  }

  @Override
  public FTxt item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    boolean more = false;

    final Iter iter = qc.iter(exprs[0]);
    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();
      if(more) tb.add(' ');
      tb.add(it.string(info));
      more = true;
    }
    return more ? new FTxt(tb.finish()) : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new CTxt(sc, info, exprs[0].copy(cc, vm));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CTxt && super.equals(obj);
  }

  @Override
  public String description() {
    return info(TEXT);
  }

  @Override
  public String toString() {
    return toString(TEXT);
  }
}
