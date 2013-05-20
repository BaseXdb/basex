package org.basex.query.func;

import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.util.*;

/**
 * Sequence functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNSeq extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNSeq(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case HEAD: return head(ctx);
      default:   return super.item(ctx, ii);
    }
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case INDEX_OF:        return indexOf(ctx);
      case DISTINCT_VALUES: return distinctValues(ctx);
      case INSERT_BEFORE:   return insertBefore(ctx);
      case REVERSE:         return reverse(ctx);
      case REMOVE:          return remove(ctx);
      case SUBSEQUENCE:     return subsequence(ctx);
      case TAIL:            return tail(ctx);
      case OUTERMOST:       return most(ctx, true);
      case INNERMOST:       return most(ctx, false);
      default:              return super.iter(ctx);
    }
  }

  /**
   * Returns the outermost/innermost nodes of a node sequence, i.e. a node is
   * only contained, if none of its ancestors/descendants are.
   * @param ctx query context
   * @param outer outermost flag
   * @return outermost/innermost nodes
   * @throws QueryException exception
   */
  private Iter most(final QueryContext ctx, final boolean outer) throws QueryException {
    final Iter iter = expr[0].iter(ctx);
    final NodeSeqBuilder nc = new NodeSeqBuilder().check();
    for(Item it; (it = iter.next()) != null;) nc.add(checkNode(it));
    final int len = (int) nc.size();

    // only go further if there are at least two nodes
    if(len < 2) return nc;

    // after this, the iterator is sorted and duplicate free
    if(nc.dbnodes()) {
      // nodes are sorted, so ancestors always come before their descendants
      // the first/last node is thus always included in the output
      final DBNode fst = (DBNode) nc.get(outer ? 0 : len - 1);
      final Data data = fst.data;
      final ANode[] nodes = nc.nodes.clone();

      if(outer) {
        // skip the subtree of the last added node
        nc.size(0);
        final DBNode dummy = new DBNode(fst.data);
        final NodeSeqBuilder src = new NodeSeqBuilder(nodes, len);
        for(int next = 0, p; next < len; next = p < 0 ? -p - 1 : p) {
          final DBNode nd = (DBNode) nodes[next];
          dummy.pre = nd.pre + data.size(nd.pre, data.kind(nd.pre));
          p = src.binarySearch(dummy, next + 1, len - next - 1);
          nc.add(nd);
        }
      } else {
        // skip ancestors of the last added node
        nc.nodes[0] = fst;
        nc.size(1);
        int before = fst.pre;
        for(int i = len - 1; i-- != 0;) {
          final DBNode nd = (DBNode) nodes[i];
          if(nd.pre + data.size(nd.pre, data.kind(nd.pre)) <= before) {
            nc.add(nd);
            before = nd.pre;
          }
        }

        // nodes were added in reverse order, correct that
        Array.reverse(nc.nodes, 0, (int) nc.size());
      }

      return nc;
    }

    // multiple documents and/or constructed fragments
    final NodeSeqBuilder out = new NodeSeqBuilder(new ANode[len], 0);
    OUTER: for(int i = 0; i < len; i++) {
      final ANode nd = nc.nodes[i];
      final AxisIter ax = outer ? nd.ancestor() : nd.descendant();
      for(ANode a; (a = ax.next()) != null;)
        if(nc.indexOf(a, false) != -1) continue OUTER;
      out.add(nc.nodes[i]);
    }

    return out;
  }

  @Override
  protected Expr opt(final QueryContext ctx) throws QueryException {
    // static typing:
    // index-of will create integers, insert-before might add new types
    if(sig == Function.INDEX_OF || sig == Function.INSERT_BEFORE) return this;

    // pre-evaluate distinct values
    final SeqType st = expr[0].type();
    final Type t = st.type;
    if(sig == Function.DISTINCT_VALUES) {
      type = t.isNode() ? SeqType.get(AtomType.ATM, st.occ) : st;
      return cmpDist(ctx);
    }

    // all other types will return existing types
    Occ o = Occ.ZERO_MORE;
    // at most one returned item
    if(sig == Function.SUBSEQUENCE && st.one()) o = Occ.ZERO_ONE;

    // head will return at most one item
    else if(sig == Function.HEAD) o = Occ.ZERO_ONE;
    type = SeqType.get(t, o);

    return this;
  }

  /**
   * Pre-evaluates distinct-values() function, utilizing database statistics.
   * @param ctx query context
   * @return original or optimized expression
   * @throws QueryException query exception
   */
  private Expr cmpDist(final QueryContext ctx) throws QueryException {
    // can only be performed on axis paths
    if(!(expr[0] instanceof AxisPath)) return this;
    // try to get statistics for resulting nodes
    final ArrayList<PathNode> nodes = ((AxisPath) expr[0]).nodes(ctx);
    if(nodes == null) return this;
    // loop through all nodes
    final ItemSet is = new ItemSet();
    for(PathNode pn : nodes) {
      // retrieve text child if addressed node is an element
      if(pn.kind == Data.ELEM) {
        if(!pn.stats.isLeaf()) return this;
        for(final PathNode n : pn.ch) if(n.kind == Data.TEXT) pn = n;
      }
      // skip nodes others than texts and attributes
      if(pn.kind != Data.TEXT && pn.kind != Data.ATTR) return this;
      // check if distinct values are available
      if(pn.stats.type != StatsType.CATEGORY) return this;
      // if yes, add them to the item set
      for(final byte[] c : pn.stats.cats) is.add(new Atm(c), info);
    }
    // return resulting sequence
    final ValueBuilder vb = new ValueBuilder(is.size());
    for(final Item i : is) vb.add(i);
    return vb.value();
  }

  /**
   * Returns the first item in a sequence.
   * @param ctx query context
   * @return first item
   * @throws QueryException query exception
   */
  private Item head(final QueryContext ctx) throws QueryException {
    final Expr e = expr[0];
    return e.type().zeroOrOne() ? e.item(ctx, info) : e.iter(ctx).next();
  }

  /**
   * Returns all but the first item in a sequence.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter tail(final QueryContext ctx) throws QueryException {
    final Expr e = expr[0];
    if(e instanceof Seq) return ((Seq) e).sub(1, e.size() - 1).iter();

    if(e.type().zeroOrOne()) return Empty.ITER;

    final Iter ir = e.iter(ctx);
    if(ir.next() == null) return Empty.ITER;

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        return ir.next();
      }
    };
  }

  /**
   * Returns the indexes of an item in a sequence.
   * @param ctx query context
   * @return position(s) of item
   * @throws QueryException query exception
   */
  private Iter indexOf(final QueryContext ctx) throws QueryException {
    final Item it = checkItem(expr[1], ctx);
    if(expr.length == 3) checkColl(expr[2], ctx);

    return new Iter() {
      final Iter ir = expr[0].iter(ctx);
      int c;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item i = ir.next();
          if(i == null) return null;
          ++c;
          if(i.comparable(it) && OpV.EQ.eval(info, i, it)) return Int.get(c);
        }
      }
    };
  }

  /**
   * Returns all distinct values of a sequence.
   * @param ctx query context
   * @return distinct iterator
   * @throws QueryException query exception
   */
  private Iter distinctValues(final QueryContext ctx) throws QueryException {
    if(expr.length == 2) checkColl(expr[1], ctx);
    if(expr[0] instanceof RangeSeq) return expr[0].iter(ctx);

    return new Iter() {
      final ItemSet map = new ItemSet();
      final Iter ir = expr[0].iter(ctx);

      @Override
      public Item next() throws QueryException {
        while(true) {
          Item i = ir.next();
          if(i == null) return null;
          ctx.checkStop();
          i = atom(i, info);
          if(map.add(i, info) >= 0) return i;
        }
      }
    };
  }

  /**
   * Inserts items before the specified position.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter insertBefore(final QueryContext ctx) throws QueryException {
    return new Iter() {
      final long pos = Math.max(1, checkItr(expr[1], ctx));
      final Iter iter = expr[0].iter(ctx);
      final Iter ins = expr[2].iter(ctx);
      long p = pos;
      boolean last;

      @Override
      public Item next() throws QueryException {
        if(last) return p > 0 ? ins.next() : null;
        final boolean sub = p == 0 || --p == 0;
        final Item i = (sub ? ins : iter).next();
        if(i != null) return i;
        if(sub) --p;
        else last = true;
        return next();
      }
    };
  }

  /**
   * Removes an item at a specified position in a sequence.
   * @param ctx query context
   * @return iterator without item
   * @throws QueryException query exception
   */
  private Iter remove(final QueryContext ctx) throws QueryException {
    return new Iter() {
      final long pos = checkItr(expr[1], ctx);
      final Iter iter = expr[0].iter(ctx);
      long c;

      @Override
      public Item next() throws QueryException {
        return ++c != pos || iter.next() != null ? iter.next() : null;
      }
    };
  }

  /**
   * Creates a subsequence out of a sequence, starting with start and
   * ending with end.
   * @param ctx query context
   * @return subsequence
   * @throws QueryException query exception
   */
  private Iter subsequence(final QueryContext ctx) throws QueryException {
    final double ds = checkDbl(expr[1], ctx);
    if(Double.isNaN(ds)) return Empty.ITER;
    final long s = StrictMath.round(ds);
    final boolean si = s == Long.MIN_VALUE;

    long l = Long.MAX_VALUE;
    if(expr.length > 2) {
      final double dl = checkDbl(expr[2], ctx);
      if(Double.isNaN(dl)) return Empty.ITER;
      if(si && dl == Double.POSITIVE_INFINITY) return Empty.ITER;
      l = StrictMath.round(dl);
    }
    final boolean li = l == Long.MAX_VALUE;
    if(si) return li ? expr[0].iter(ctx) : Empty.ITER;

    // optimization: return subsequence
    if(expr[0] instanceof Seq) {
      final Seq seq = (Seq) expr[0];
      final long rs = seq.size();
      final long from = Math.max(1, s) - 1;
      final long len = Math.min(rs - from, l + Math.min(0, s - 1));
      return from >= rs || len <= 0 ? Empty.ITER : seq.sub(from, len).iter();
    }

    final Iter iter = ctx.iter(expr[0]);
    final long max = iter.size();
    final long e = li ? l : s + l;

    // return iterator with all supported functions if number of returned values is known
    if(max != -1) return new Iter() {
      // directly access specified items
      final long m = Math.min(e, max + 1);
      long c = Math.max(1, s);

      @Override
      public Item next() throws QueryException {
        return c < m ? iter.get(c++ - 1) : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        return iter.get(c + i - 1);
      }
      @Override
      public long size() {
        return Math.max(0, m - c);
      }
      @Override
      public boolean reset() {
        c = Math.max(1, s);
        return true;
      }
    };

    // return simple iterator if number of returned values is unknown
    return new Iter() {
      long c;
      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item i = iter.next();
          if(i == null || ++c >= e) return null;
          if(c >= s) return i;
        }
      }
    };
  }

  /**
   * Reverses a sequence.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter reverse(final QueryContext ctx) throws QueryException {
    // optimization: reverse sequence
    if(expr[0] instanceof Seq) return ((Seq) expr[0]).reverse().iter();

    // materialize value if number of results is unknown
    final Iter iter = ctx.iter(expr[0]);
    final long s = iter.size();
    if(s == -1) {
      // estimate result size (could be known in the original expression)
      final ValueBuilder vb = new ValueBuilder(Math.max((int) expr[0].size(), 1));
      for(Item it; (it = iter.next()) != null;) vb.add(it);
      Array.reverse(vb.item, 0, (int) vb.size());
      return vb;
    }

    // return iterator if only a single result will be returned
    return s == 0 ? Empty.ITER : s == 1 ? iter : new Iter() {
      long c = s;

      @Override
      public Item next() throws QueryException {
        return --c >= 0 ? iter.get(c) : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        return iter.get(s - i - 1);
      }
      @Override
      public long size() {
        return s;
      }
      @Override
      public boolean reset() {
        c = s;
        return iter.reset();
      }
    };
  }

  @Override
  public boolean xquery3() {
    return oneOf(sig, HEAD, TAIL);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 && xquery3() || super.uses(u);
  }
}
