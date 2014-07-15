package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
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
 * @author BaseX Team 2005-14, BSD License
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
    type = SeqType.TXT_ZO;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optPre(oneIsEmpty() ? null : this, qc);
  }

  @Override
  public FTxt item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = qc.iter(exprs[0]);
    Item it = iter.next();
    if(it == null) return null;

    final TokenBuilder tb = new TokenBuilder();
    boolean more = false;
    do {
      if(more) tb.add(' ');
      tb.add(it.string(ii));
      more = true;
    } while((it = iter.next()) != null);

    return new FTxt(tb.finish());
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
