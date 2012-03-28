package org.basex.query.func;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.expr.*;
import org.basex.query.item.*;
import org.basex.query.item.SeqType.Occ;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
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
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
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
  private Iter most(final QueryContext ctx, final boolean outer)
      throws QueryException {
    final Iter iter = expr[0].iter(ctx);
    final NodeCache nc = new NodeCache().random();
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
      final ANode[] nodes = nc.item.clone();

      if(outer) {
        // skip the subtree of the last added node
        nc.size(0);
        final DBNode dummy = new DBNode(fst.data);
        final NodeCache src = new NodeCache(nodes, len);
        for(int next = 0, p; next < len; next = p < 0 ? -p - 1 : p) {
          final DBNode nd = (DBNode) nodes[next];
          dummy.pre = nd.pre + data.size(nd.pre, data.kind(nd.pre));
          p = src.binarySearch(dummy, next + 1, len - next - 1);
          nc.add(nd);
        }
      } else {
        // skip ancestors of the last added node
        nc.item[0] = fst;
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
        Array.reverse(nc.item, 0, (int) nc.size());
      }

      return nc;
    }

    // multiple documents and/or constructed fragments
    final NodeCache out = new NodeCache(new ANode[len], 0);
    OUTER: for(int i = 0; i < len; i++) {
      final ANode nd = nc.item[i];
      final AxisIter ax = outer ? nd.ancestor() : nd.descendant();
      for(ANode a; (a = ax.next()) != null;)
        if(nc.indexOf(a, false) != -1) continue OUTER;
      out.add(nc.item[i]);
    }

    return out;
  }

  @Override
  public Expr cmp(final QueryContext ctx) throws QueryException {
    // static typing:
    // index-of will create integers, insert-before might add new types
    if(sig == Function.INDEX_OF ||
       sig == Function.INSERT_BEFORE) return this;

    // all other types will return existing types
    final Type t = expr[0].type().type;
    Occ o = Occ.ZERO_MORE;
    // at most one returned item
    if(sig == Function.SUBSEQUENCE && expr[0].type().one()) o = Occ.ZERO_ONE;

    // head will return at most one item
    else if(sig == Function.HEAD) o = Occ.ZERO_ONE;
    type = SeqType.get(t, o);

    // pre-evaluate distinct values
    if(sig == Function.DISTINCT_VALUES) return cmpDist(ctx);

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
      for(final byte[] c : pn.stats.cats) is.index(input, new Atm(c));
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
    return e.type().zeroOrOne() ? e.item(ctx, input) : e.iter(ctx).next();
  }

  /**
   * Returns all but the first item in a sequence.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter tail(final QueryContext ctx) throws QueryException {
    final Expr e = expr[0];
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
          if(i.comparable(it) && OpV.EQ.eval(input, i, it)) return Int.get(c);
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

    return new Iter() {
      final ItemSet map = new ItemSet();
      final Iter ir = expr[0].iter(ctx);

      @Override
      public Item next() throws QueryException {
        while(true) {
          Item i = ir.next();
          if(i == null) return null;
          ctx.checkStop();
          i = atom(i, input);
          if(map.index(input, i)) return i;
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

    long l = Long.MAX_VALUE;
    if(expr.length > 2) {
      final double dl = checkDbl(expr[2], ctx);
      if(Double.isNaN(dl)) return Empty.ITER;
      l = s + StrictMath.round(dl);
    }
    final long e = l;

    final Iter iter = ctx.iter(expr[0]);
    final long max = iter.size();
    return max != -1 ? new Iter() {
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
    } : new Iter() {
      // run through all items
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
  private ValueIter reverse(final QueryContext ctx) throws QueryException {
    // has to be strictly evaluated
    final Value val = ctx.value(expr[0]);
    final ValueIter iter = val.iter();
    // if only one item found: no reversion necessary
    return val.size() == 1 ? iter : new ValueIter() {
      final long s = iter.size();
      long c = s;

      @Override
      public Item next() {
        return --c >= 0 ? iter.get(c) : null;
      }
      @Override
      public Item get(final long i) {
        return iter.get(s - i - 1);
      }
      @Override
      public long size() {
        return s;
      }
      @Override
      public boolean reset() {
        c = s;
        return true;
      }
      @Override
      public Value value() {
        final Item[] arr = new Item[(int) val.size()];
        final int written = val.writeTo(arr, 0);
        Array.reverse(arr, 0, written);
        return Seq.get(arr, written);
      }
    };
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 && (sig == Function.HEAD || sig == Function.TAIL) ||
      super.uses(u);
  }
}
