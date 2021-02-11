package org.basex.query.func.fn;

import java.util.*;

import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpV.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnDistinctValues extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Collation coll = toCollation(1, qc);
    final Expr expr = exprs[0];
    final Iter iter = expr.atomIter(qc, info);

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
    return iter(qc).value(qc, this);
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    exprs[0] = exprs[0].simplifyFor(Simplify.DATA, cc).simplifyFor(Simplify.DISTINCT, cc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.zero()) return expr;

    final AtomType type = st.type.atomic();
    if(type != null) {
      // assign atomic type of argument
      exprType.assign(type);

      if(exprs.length == 1) {
        // distinct-values(1 to 10)  ->  1 to 10
        if(expr instanceof Range || expr instanceof RangeSeq) return expr;
        // distinct-values($string)  ->  $string
        // distinct-values($node)  ->  data($node)
        if(st.zeroOrOne()) return type == st.type ? expr : cc.function(Function.DATA, info, exprs);
      }
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
    final AxisPath path = (AxisPath) expr;
    final ArrayList<PathNode> nodes = path.pathNodes(path.root);
    if(nodes == null) return this;

    // loop through all nodes
    final ValueBuilder vb = new ValueBuilder(cc.qc);
    final HashItemSet set = new HashItemSet(false);
    for(PathNode node : nodes) {
      // retrieve text child if addressed node is an element
      if(node.kind == Data.ELEM) {
        if(!node.stats.isLeaf()) return this;
        for(final PathNode nd : node.children) {
          if(nd.kind == Data.TEXT) node = nd;
        }
      }
      // skip nodes others than texts and attributes
      if(node.kind != Data.TEXT && node.kind != Data.ATTR) return this;
      // check if distinct values are available
      if(!StatsType.isCategory(node.stats.type)) return this;
      // if yes, add them to the item set
      for(final byte[] c : node.stats.values) {
        final Atm item = new Atm(c);
        if(set.add(item, info)) vb.add(item);
      }
    }
    return vb.value(this);
  }

  /**
   * Rewrites the function call to a duplicate check.
   * @param op comparison operator
   * @param cc compilation context
   * @return new function or {@code null}
   * @throws QueryException query context
   */
  public Expr duplicates(final OpV op, final CompileContext cc) throws QueryException {
    if(op == OpV.LT) return Bln.FALSE;
    if(op == OpV.GE) return Bln.TRUE;

    final Expr dupl = cc.function(Function._UTIL_DUPLICATES, info, exprs);
    return cc.function(op == OpV.LE || op == OpV.EQ ? Function.EMPTY : Function.EXISTS, info, dupl);
  }
}
