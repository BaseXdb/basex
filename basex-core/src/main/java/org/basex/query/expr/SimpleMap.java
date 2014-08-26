package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Simple map expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class SimpleMap extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  SimpleMap(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  /**
   * Returns a map instance.
   * @param info input info
   * @param exprs expressions
   * @return instance
   */
  public static SimpleMap get(final InputInfo info, final Expr... exprs) {
    for(final Expr expr : exprs) {
      if(expr.has(Flag.FCS)) return new CachedMap(info, exprs);
    }
    return new IterMap(info, exprs);
  }

  @Override
  public final void checkUp() throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e < el - 1; e++) checkNoUp(exprs[e]);
    exprs[el - 1].checkUp();
  }

  @Override
  public final Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    final Value cv = qc.value;
    try {
      final int el = exprs.length;
      for(int e = 0; e < el; e++) {
        final Expr ex = exprs[e].compile(qc, scp);
        if(ex.isEmpty()) return optPre(qc);
        exprs[e] = ex;
        qc.value = null;
      }
    } finally {
      qc.value = cv;
    }
    return optimize(qc, scp);
  }

  @Override
  public final Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    seqType = SeqType.get(exprs[exprs.length - 1].seqType().type, Occ.ZERO_MORE);
    return allAreValues() ? optPre(value(qc), qc) : this;
  }

  @Override
  public final boolean has(final Flag flag) {
    return flag == Flag.CTX || super.has(flag);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    visitor.enterFocus();
    if(!visitAll(visitor, exprs)) return false;
    visitor.exitFocus();
    return true;
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder().append('(');
    for(final Expr s : exprs) {
      if(sb.length() != 1) sb.append(" ! ");
      sb.append(s);
    }
    return sb.append(')').toString();
  }
}
