package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import java.util.*;

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
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
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

    // X => sort() => distinct-values()  ->  X => distinct-values() => sort()
    if(SORT.is(expr) && (expr.args().length == 1 ||
        expr.arg(0).seqType().type.instanceOf(AtomType.ANY_ATOMIC_TYPE))) {
      final ExprList list = new ExprList().add(expr.args());
      list.set(0, cc.function(DISTINCT_VALUES, info, expr.arg(0)));
      return cc.function(SORT, info, list.finish());
    }

    if(exprs.length == 1 && exprs[0] instanceof Path) {
      final ArrayList<Stats> list = ((Path) exprs[0]).pathStats(false);
      if(list != null) {
        final ValueBuilder vb = new ValueBuilder(cc.qc);
        final HashItemSet set = new HashItemSet(false);
        for(final Stats stats : list) {
          for(final byte[] value : stats.values) {
            final Atm item = Atm.get(value);
            if(set.add(item, info)) vb.add(item);
          }
        }
        return vb.value(this);
      }
    }

    final AtomType type = st.type.atomic();
    if(type != null) {
      // assign atomic type of argument
      exprType.assign(type);

      if(exprs.length == 1) {
        // distinct-values(1 to 10)  ->  1 to 10
        if(expr instanceof Range || expr instanceof RangeSeq) return expr;
        // distinct-values($string)  ->  $string
        // distinct-values($node)  ->  data($node)
        if(st.zeroOrOne() && !st.mayBeArray())
          return type == st.type ? expr : cc.function(Function.DATA, info, exprs);
      }
    }
    return this;
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
