package org.basex.query.func.fn;

import java.util.*;

import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FnDistinctValues extends StandardFunc {
  /** Item evaluation flag. */
  private boolean simple;

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Collation coll = toCollation(1, qc);
    final Expr expr = exprs[0];
    final Iter iter = expr.atomIter(qc, info);
    if(simple || expr instanceof RangeSeq || expr instanceof Range) return iter;

    final ItemSet set = coll == null ? new HashItemSet(false) : new CollationItemSet(coll);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        for(Item item; (item = qc.next(iter)) != null;) {
          if(set.add(item, info)) return item;
        }
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Collation coll = toCollation(1, qc);
    final Expr expr = exprs[0];
    if(simple || expr instanceof RangeSeq || expr instanceof Range)
      return expr.atomValue(qc, info);

    final ItemSet set = coll == null ? new HashItemSet(false) : new CollationItemSet(coll);
    final Iter iter = expr.atomIter(qc, info);
    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(set.add(item, info)) vb.add(item);
    }
    return vb.value();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    Expr expr = exprs[0];
    if(expr instanceof RangeSeq) return expr;
    if(expr instanceof SingletonSeq) {
      expr = ((SingletonSeq) expr).value;
      exprs[0] = expr;
    }

    final SeqType st = expr.seqType();
    final AtomType type = st.type.atomic();
    if(type != null) {
      exprType.assign(type);
      simple = st.zeroOrOne();
    }
    return optStats(expr, cc);
  }

  /**
   * Tries to evaluate distinct values based on the database statistics.
   * @param expr expression
   * @param cc compilation context
   * @return original expression or sequence of distinct values
   * @throws QueryException query exception
   */
  private Expr optStats(final Expr expr, final CompileContext cc) throws QueryException {
    // can only be performed without collation and on axis paths
    if(exprs.length > 1 || !(expr instanceof AxisPath)) return this;

    // try to get statistics for resulting nodes
    final ArrayList<PathNode> nodes = ((AxisPath) expr).pathNodes(cc);
    if(nodes == null) return this;

    // loop through all nodes
    final ValueBuilder vb = new ValueBuilder(cc.qc);
    final HashItemSet set = new HashItemSet(false);
    for(PathNode pn : nodes) {
      // retrieve text child if addressed node is an element
      if(pn.kind == Data.ELEM) {
        if(!pn.stats.isLeaf()) return this;
        for(final PathNode n : pn.children) {
          if(n.kind == Data.TEXT) pn = n;
        }
      }
      // skip nodes others than texts and attributes
      if(pn.kind != Data.TEXT && pn.kind != Data.ATTR) return this;
      // check if distinct values are available
      if(!StatsType.isCategory(pn.stats.type)) return this;
      // if yes, add them to the item set
      for(final byte[] c : pn.stats.values) {
        final Atm it = new Atm(c);
        if(set.add(it, info)) vb.add(it);
      }
    }
    return vb.value();
  }
}
