package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Simple map operator.
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
    super(info, SeqType.ITEM_ZM, exprs);
  }

  /**
   * Returns a map instance.
   * @param info input info
   * @param exprs expressions
   * @return instance
   */
  public static Expr get(final InputInfo info, final Expr... exprs) {
    if(exprs.length == 1) return exprs[0];
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
  public Expr optimize(final CompileContext cc) throws QueryException {
    // rewrite path with empty steps
    for(final Expr expr : exprs) {
      if(expr == Empty.SEQ) return cc.emptySeq(this);
    }

    // compute result size
    final ExprList list = new ExprList(exprs.length);
    long min = 1, max = 1;
    boolean item = true;
    for(final Expr expr : exprs) {
      if(max == 0) break;
      list.add(expr);
      final long s = expr.size();
      if(s == 0) {
        min = 0;
        max = 0;
      } else if(s > 0) {
        min *= s;
        if(max != -1) max *= s;
        if(s > 1) item = false;
      } else {
        final Occ o = expr.seqType().occ;
        if(o.min == 0) min = 0;
        if(o.max > 1) {
          max = -1;
          item = false;
        }
      }
    }
    final int es = list.size();
    if(exprs.length != es) {
      if(es == 1) return cc.replaceWith(this, list.get(0));
      cc.info(OPTSIMPLE_X, this);
      exprs = list.finish();
    }
    seqType(exprs[exprs.length - 1], new long[] { min, max });

    // single items: use item mapper; only values: pre-evaluate
    return item ? copyType(new ItemMap(info, exprs)).optimize(cc) : allAreValues() ?
      cc.preEval(this) : this;
  }

  @Override
  public final boolean has(final Flag... flags) {
    /* Context dependency: Only check first expression.
     * Examples: . ! abc */
    if(Flag.CTX.in(flags) && exprs[0].has(Flag.CTX)) return true;
    /* Positional access: only check root node (steps will refer to result of root node).
     * Example: position()/a */
    if(Flag.POS.in(flags) && exprs[0].has(Flag.POS)) return true;
    // check remaining flags
    final Flag[] flgs = Flag.POS.remove(Flag.CTX.remove(flags));
    return flgs.length != 0 && super.has(flgs);
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
