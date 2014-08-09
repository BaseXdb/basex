package org.basex.query.func;

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
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Sequence functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNSeq extends StandardFunc {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNSeq(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case HEAD: return head(qc);
      default:   return super.item(qc, ii);
    }
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case INDEX_OF:        return indexOf(qc);
      case DISTINCT_VALUES: return distinctValues(qc);
      case INSERT_BEFORE:   return insertBefore(qc);
      case REVERSE:         return reverse(qc);
      case REMOVE:          return remove(qc);
      case SUBSEQUENCE:     return subseqIter(qc);
      case TAIL:            return tail(qc);
      case OUTERMOST:       return most(qc, true);
      case INNERMOST:       return most(qc, false);
      default:              return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case SUBSEQUENCE: return subseqValue(qc);
      case TAIL:        final Value seq = qc.value(exprs[0]);
                        return SubSeq.get(seq, 1, seq.size() - 1);
      default:          return super.value(qc);
    }
  }

  /**
   * Returns the outermost/innermost nodes of a node sequence, i.e. a node is
   * only contained, if none of its ancestors/descendants are.
   * @param qc query context
   * @param outer outermost flag
   * @return outermost/innermost nodes
   * @throws QueryException exception
   */
  private Iter most(final QueryContext qc, final boolean outer) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
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
  protected Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    // static typing:
    // index-of will create integers, insert-before might add new types
    if(func == Function.INDEX_OF || func == Function.INSERT_BEFORE) return this;

    // pre-evaluate distinct values
    final SeqType st = exprs[0].seqType();
    final Type t = st.type;
    if(func == Function.DISTINCT_VALUES && exprs.length == 1) {
      seqType = t instanceof NodeType ? SeqType.get(AtomType.ATM, st.occ) : st;
      return cmpDist(qc);
    }

    // all other types will return existing types
    Occ o = Occ.ZERO_MORE;
    // at most one returned item
    if(func == Function.SUBSEQUENCE && st.one()) o = Occ.ZERO_ONE;
    // head will return at most one item
    else if(func == Function.HEAD) o = Occ.ZERO_ONE;
    // zero items
    else if(func == Function.TAIL && st.one()) o = Occ.ZERO;
    seqType = SeqType.get(t, o);

    return this;
  }

  /**
   * Pre-evaluates distinct-values() function, utilizing database statistics.
   * @param qc query context
   * @return original or optimized expression
   * @throws QueryException query exception
   */
  private Expr cmpDist(final QueryContext qc) throws QueryException {
    // can only be performed on axis paths
    if(!(exprs[0] instanceof AxisPath)) return this;
    // try to get statistics for resulting nodes
    final ArrayList<PathNode> nodes = ((AxisPath) exprs[0]).pathNodes(qc);
    if(nodes == null) return this;
    // loop through all nodes
    final HashItemSet is = new HashItemSet();
    for(PathNode pn : nodes) {
      // retrieve text child if addressed node is an element
      if(pn.kind == Data.ELEM) {
        if(!pn.stats.isLeaf()) return this;
        for(final PathNode n : pn.children) if(n.kind == Data.TEXT) pn = n;
      }
      // skip nodes others than texts and attributes
      if(pn.kind != Data.TEXT && pn.kind != Data.ATTR) return this;
      // check if distinct values are available
      if(pn.stats.type != StatsType.CATEGORY) return this;
      // if yes, add them to the item set
      for(final byte[] c : pn.stats.cats) is.put(new Atm(c), info);
    }
    // return resulting sequence
    final ValueBuilder vb = new ValueBuilder(is.size());
    for(final Item i : is) vb.add(i);
    return vb.value();
  }

  /**
   * Returns the first item in a sequence.
   * @param qc query context
   * @return first item
   * @throws QueryException query exception
   */
  private Item head(final QueryContext qc) throws QueryException {
    final Expr e = exprs[0];
    return e.seqType().zeroOrOne() ? e.item(qc, info) : e.iter(qc).next();
  }

  /**
   * Returns all but the first item in a sequence.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter tail(final QueryContext qc) throws QueryException {
    final Expr e = exprs[0];
    if(e.seqType().zeroOrOne()) return Empty.ITER;

    final Iter ir = e.iter(qc);
    if(ir instanceof ValueIter) {
      final Value val = ir.value();
      return SubSeq.get(val, 1, val.size() - 1).iter();
    }
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
   * @param qc query context
   * @return position(s) of item
   * @throws QueryException query exception
   */
  private Iter indexOf(final QueryContext qc) throws QueryException {
    final Item it = checkItem(exprs[1], qc);
    final Collation coll = checkColl(2, qc);

    return new Iter() {
      final Iter ir = exprs[0].iter(qc);
      int c;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item i = ir.next();
          if(i == null) return null;
          ++c;
          if(i.comparable(it) && OpV.EQ.eval(i, it, coll, info)) return Int.get(c);
        }
      }
    };
  }

  /**
   * Returns all distinct values of a sequence.
   * @param qc query context
   * @return distinct iterator
   * @throws QueryException query exception
   */
  private Iter distinctValues(final QueryContext qc) throws QueryException {
    final Collation coll = checkColl(1, qc);
    if(exprs[0] instanceof RangeSeq) return exprs[0].iter(qc);

    return new Iter() {
      final ItemSet set = coll == null ? new HashItemSet() : new CollationItemSet(coll);
      final Iter ir = exprs[0].iter(qc);
      Iter ir2;

      @Override
      public Item next() throws QueryException {
        while(true) {
          qc.checkStop();
          while(ir2 != null) {
            final Item it = ir2.next();
            if(it == null) {
              ir2 = null;
            } else if(set.add(it, info)) {
              return it;
            }
          }
          final Item it = ir.next();
          if(it == null) return null;
          ir2 = it.atomValue(info).iter();
        }
      }
    };
  }

  /**
   * Inserts items before the specified position.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter insertBefore(final QueryContext qc) throws QueryException {
    return new Iter() {
      final long pos = Math.max(1, checkItr(exprs[1], qc));
      final Iter iter = exprs[0].iter(qc);
      final Iter ins = exprs[2].iter(qc);
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
   * @param qc query context
   * @return iterator without item
   * @throws QueryException query exception
   */
  private Iter remove(final QueryContext qc) throws QueryException {
    return new Iter() {
      final long pos = checkItr(exprs[1], qc);
      final Iter iter = exprs[0].iter(qc);
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
   * @param qc query context
   * @return subsequence
   * @throws QueryException query exception
   */
  private Iter subseqIter(final QueryContext qc) throws QueryException {
    final double ds = checkDbl(exprs[1], qc);
    if(Double.isNaN(ds)) return Empty.ITER;
    final long s = StrictMath.round(ds);
    final boolean si = s == Long.MIN_VALUE;

    long l = Long.MAX_VALUE;
    if(exprs.length > 2) {
      final double dl = checkDbl(exprs[2], qc);
      if(Double.isNaN(dl)) return Empty.ITER;
      if(si && dl == Double.POSITIVE_INFINITY) return Empty.ITER;
      l = StrictMath.round(dl);
    }
    final boolean li = l == Long.MAX_VALUE;
    if(si) return li ? exprs[0].iter(qc) : Empty.ITER;

    final Iter iter = qc.iter(exprs[0]);

    // optimization: return subsequence
    if(iter instanceof ValueIter) {
      final Value val = iter.value();
      final long rs = val.size();
      final long from = Math.max(1, s) - 1;
      final long len = Math.min(rs - from, l + Math.min(0, s - 1));
      return SubSeq.get(val, from, len).iter();
    }
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
   * Evaluates the {@code subsequence} function strictly.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Value subseqValue(final QueryContext qc) throws QueryException {
    final double dstart = checkDbl(exprs[1], qc);
    if(Double.isNaN(dstart)) return Empty.SEQ;
    final long start = StrictMath.round(dstart);
    final boolean sinf = start == Long.MIN_VALUE;

    long length = Long.MAX_VALUE;
    if(exprs.length > 2) {
      final double dlength = checkDbl(exprs[2], qc);
      if(Double.isNaN(dlength)) return Empty.SEQ;
      if(sinf && dlength == Double.POSITIVE_INFINITY) return Empty.SEQ;
      length = StrictMath.round(dlength);
    }
    final boolean linf = length == Long.MAX_VALUE;
    if(sinf) return linf ? exprs[0].value(qc) : Empty.SEQ;

    final Iter iter = qc.iter(exprs[0]);

    // optimization: return subsequence
    if(iter instanceof ValueIter) {
      final Value val = iter.value();
      final long rs = val.size();
      final long from = Math.max(1, start) - 1;
      final long len = Math.min(rs - from, length + Math.min(0, start - 1));
      return SubSeq.get(val, from, len);
    }

    // fast route if the size is known
    final long max = iter.size();
    if(max >= 0) {
      final long from = Math.max(1, start) - 1;
      final long len = Math.min(max - from, length + Math.min(0, start - 1));
      if(from >= max || len <= 0) return Empty.SEQ;
      final ValueBuilder vb = new ValueBuilder(Math.max((int) len, 1));
      for(long i = 0; i < len; i++) vb.add(iter.get(from + i));
      return vb.value();
    }

    final long e = linf ? length : start + length;
    final ValueBuilder build = new ValueBuilder();
    Item i;
    for(int c = 1; (i = iter.next()) != null; c++) {
      if(c >= e) {
        iter.reset();
        break;
      }
      if(c >= start) build.add(i);
    }
    return build.value();
  }

  /**
   * Reverses a sequence.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter reverse(final QueryContext qc) throws QueryException {
    // optimization: reverse sequence
    if(exprs[0] instanceof Seq) return ((Seq) exprs[0]).reverse().iter();

    // materialize value if number of results is unknown
    final Iter iter = qc.iter(exprs[0]);
    final long s = iter.size();
    if(s == -1) {
      // estimate result size (could be known in the original expression)
      final ValueBuilder vb = new ValueBuilder(Math.max((int) exprs[0].size(), 1));
      for(Item it; (it = iter.next()) != null;) vb.add(it);
      Array.reverse(vb.items(), 0, (int) vb.size());
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
}
