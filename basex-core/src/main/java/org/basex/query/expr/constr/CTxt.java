package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Text constructor.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class CTxt extends CNode {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param value value
   */
  public CTxt(final InputInfo info, final Expr value) {
    super(info, SeqType.TEXT_ZO, true, value);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    exprs = simplifyAll(Simplify.STRING, cc);

    if(allAreValues(true) && !(exprs[0] instanceof Str)) {
      final byte[] value = atomValue(cc.qc, false);
      exprs[0] = value != null ? Str.get(value) : Empty.VALUE;
    }

    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.zero()) return cc.replaceWith(this, expr);

    final boolean atom = !st.mayBeArray();
    if(st.oneOrMore() && atom) exprType.assign(Occ.EXACTLY_ONE);
    return this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = atomValue(qc, false);
    return value != null ? new FTxt(qc.shared.token(value)) : Empty.VALUE;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CTxt(info, exprs[0].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CTxt && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    toString(qs, TEXT);
  }
}
