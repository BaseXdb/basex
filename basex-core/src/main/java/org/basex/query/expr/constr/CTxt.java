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
 * @author BaseX Team 2005-16, BSD License
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
    size = -1;
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) {
    return optPre(oneIsEmpty() ? null : this, qc);
  }

  @Override
  public FTxt item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    boolean more = false;

    final Iter iter = qc.iter(exprs[0]);
    for(Item it; (it = iter.next()) != null;) {
      if(more) tb.add(' ');
      tb.add(it.string(ii));
      more = true;
    }
    return more ? new FTxt(tb.finish()) : null;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new CTxt(sc, info, exprs[0].copy(qc, scp, vs));
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
