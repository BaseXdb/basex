package org.basex.query.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple map expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class SimpleMap extends Arr {
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
      if(expr.has(Flag.FCS)) return new SimpleMap(info, exprs);
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
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value cv = qc.value;
    final long cp = qc.pos, cs = qc.size;
    try {
      ValueBuilder result = new ValueBuilder().add(qc.value(exprs[0]));
      final int el = exprs.length;
      for(int e = 1; e < el; e++) {
        qc.pos = 0;
        qc.size = result.size();
        final ValueBuilder vb = new ValueBuilder((int) result.size());
        for(final Item it : result) {
          qc.pos++;
          qc.value = it;
          vb.add(qc.value(exprs[e]));
        }
        result = vb;
      }
      return result;
    } finally {
      qc.value = cv;
      qc.size = cs;
      qc.pos = cp;
    }
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX || super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    visitor.enterFocus();
    if(!visitAll(visitor, exprs)) return false;
    visitor.exitFocus();
    return true;
  }

  @Override
  public SimpleMap copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new SimpleMap(info, Arr.copyAll(qc, scp, vs, exprs)));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder().append('(');
    for(final Expr s : exprs) {
      if(sb.length() != 1) sb.append(" ! ");
      sb.append(s);
    }
    return sb.append(')').toString();
  }
}
