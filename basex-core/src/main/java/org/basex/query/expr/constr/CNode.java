package org.basex.query.expr.constr;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Node constructor.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public abstract class CNode extends Arr {
  /** Static context. */
  final StaticContext sc;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param seqType sequence type
   * @param exprs expressions
   */
  CNode(final StaticContext sc, final InputInfo info, final SeqType seqType, final Expr... exprs) {
    super(info, seqType, exprs);
    this.sc = sc;
  }

  @Override
  public abstract Item item(QueryContext qc, InputInfo ii) throws QueryException;

  @Override
  public boolean has(final Flag... flags) {
    return Flag.CNS.in(flags) || super.has(flags);
  }

  @Override
  public final String description() {
    return Token.string(((NodeType) seqType().type).name) + " constructor";
  }

  @Override
  protected String toString(final String kind) {
    final TokenBuilder tb = new TokenBuilder().add(kind).add(" { ");
    if(exprs.length > 0 && exprs[0] != Empty.VALUE) tb.add(super.toString(null)).add(' ');
    return tb.add('}').toString();
  }
}
