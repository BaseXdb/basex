package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Simple map expression.
 *
 * @author BaseX Team 2005-17, BSD License
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
      if(expr.has(Flag.POS)) return new CachedMap(info, exprs);
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
  public final Expr compile(final CompileContext cc) throws QueryException {
    final QueryFocus focus = cc.qc.focus;
    final Value cv = focus.value;
    try {
      final int el = exprs.length;
      for(int e = 0; e < el; e++) {
        try {
          exprs[e] = exprs[e].compile(cc);
        } catch(final QueryException ex) {
          // replace original expression with error
          exprs[e] = cc.error(ex, this);
        }
        focus.value = null;
      }
    } finally {
      focus.value = cv;
    }
    return optimize(cc);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    seqType = exprs[exprs.length - 1].seqType().withOcc(Occ.ZERO_MORE);

    // rewrite path with empty steps
    for(final Expr expr : exprs) {
      if(expr.isEmpty()) return cc.emptySeq(this);
    }
    // pre-evaluate map with statically known values
    return allAreValues() ? cc.preEval(this) : this;
  }

  @Override
  public final boolean has(final Flag flag) {
    return flag == Flag.CTX ? exprs[0].has(flag) : super.has(flag);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    visitor.enterFocus();
    if(!visitAll(visitor, exprs)) return false;
    visitor.exitFocus();
    return true;
  }

  @Override
  public final VarUsage count(final Var var) {
    VarUsage all = VarUsage.NEVER;
    final int el = exprs.length;
    for(int e = 1; e < el; e++) {
      all = all.plus(exprs[e].count(var));
      if(all == VarUsage.MORE_THAN_ONCE) break;
    }
    return all == VarUsage.NEVER ? exprs[0].count(var) : VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public final boolean removable(final Var var) {
    if(!exprs[0].removable(var)) return false;
    final int el = exprs.length;
    for(int e = 1; e < el; e++) if(exprs[e].uses(var)) return false;
    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof SimpleMap && super.equals(obj);
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
