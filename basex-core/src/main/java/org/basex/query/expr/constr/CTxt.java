package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Text fragment.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class CTxt extends CNode {
  /** Item evaluation flag. */
  private boolean simple;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param text text
   */
  public CTxt(final StaticContext sc, final InputInfo info, final Expr text) {
    super(sc, info, SeqType.TXT_ZO, text);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    simplifyAll(Simplify.ATOM, cc);

    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.zero()) return cc.replaceWith(this, expr);
    final boolean atom = !st.mayBeArray();
    if(st.oneOrMore() && atom) exprType.assign(Occ.ONE);
    simple = st.zeroOrOne() && atom;
    return this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // if possible, retrieve single item
    final Expr expr = exprs[0];
    if(simple) {
      final Item item = expr.item(qc, info);
      return new FTxt(item == Empty.VALUE ? Token.EMPTY : item.string(info));
    }

    final TokenBuilder tb = new TokenBuilder();
    boolean more = false;
    final Iter iter = expr.atomIter(qc, info);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(more) tb.add(' ');
      tb.add(item.string(info));
      more = true;
    }
    return more ? new FTxt(tb.finish()) : Empty.VALUE;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final CTxt ctxt = copyType(new CTxt(sc, info, exprs[0].copy(cc, vm)));
    ctxt.simple = simple;
    return ctxt;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CTxt && super.equals(obj);
  }

  @Override
  public String toString() {
    return toString(TEXT);
  }
}
