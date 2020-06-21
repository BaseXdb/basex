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
  /** Computed constructor. */
  final boolean computed;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param computed computed constructor
   * @param seqType sequence type
   * @param exprs expressions
   */
  CNode(final StaticContext sc, final InputInfo info, final SeqType seqType, final boolean computed,
      final Expr... exprs) {
    super(info, seqType, exprs);
    this.sc = sc;
    this.computed = computed;
  }

  @Override
  public abstract Item item(QueryContext qc, InputInfo ii) throws QueryException;

  @Override
  public boolean has(final Flag... flags) {
    return Flag.CNS.in(flags) || super.has(flags);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof CNode && computed == ((CNode) obj).computed && super.equals(obj);
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
