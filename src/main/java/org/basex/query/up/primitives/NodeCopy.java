package org.basex.query.up.primitives;

import java.util.LinkedList;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.iter.NodIter;
import org.basex.query.up.NamePool;
import org.basex.query.util.DataBuilder;
import org.basex.util.InputInfo;

/**
 * Abstract update primitive which holds a copy of nodes to be inserted.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public abstract class NodeCopy extends UpdatePrimitive {
  /** Insertion nodes. */
  protected final LinkedList<NodIter> c = new LinkedList<NodIter>();
  /** Final copy of insertion nodes. */
  public MemData md;

  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param ni nodes to be inserted
   */
  protected NodeCopy(final InputInfo ii, final Nod n, final NodIter ni) {
    super(ii, n);
    c.add(ni);
  }

  @Override
  public void prepare() throws QueryException {
    if(c.size() == 0) return;
    final NodIter seq = new NodIter();
    for(final NodIter ni : c) {
      Nod i;
      while((i = ni.next()) != null) seq.add(i);
    }
    // ignore fragment nodes
    if(!(node instanceof DBNode)) return;

    // text nodes still need to be merged. two adjacent iterators in c may
    // lead to two adjacent text nodes
    md = new MemData(((DBNode) node).data);
    new DataBuilder(md).build(mergeText(seq));
  }

  /**
   * Adds top entries from the temporary data instance to the name pool,
   * which is used for finding duplicate attributes and namespace conflicts.
   * @param pool name pool
   */
  protected final void add(final NamePool pool) {
    for(int pre = 0; pre < md.meta.size; ++pre) {
      final int k = md.kind(pre);
      if(k != Data.ATTR && k != Data.ELEM || md.parent(pre, k) > -1) continue;
      final int u = md.uri(pre, k);
      final QNm qnm = new QNm(md.name(pre, k));
      if(u != 0) qnm.uri(md.ns.uri(u));
      pool.add(qnm, Nod.type(k));
    }
  }
}
