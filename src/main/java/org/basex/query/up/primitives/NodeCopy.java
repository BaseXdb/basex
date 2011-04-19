package org.basex.query.up.primitives;

import java.util.ArrayList;
import java.util.List;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.ANode;
import org.basex.query.item.QNm;
import org.basex.query.iter.NodeCache;
import org.basex.query.up.NamePool;
import org.basex.query.util.DataBuilder;
import org.basex.util.InputInfo;

/**
 * Abstract update primitive which holds a copy of nodes to be inserted.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public abstract class NodeCopy extends Primitive {
  /** Nodes to be inserted. */
  protected final List<NodeCache> insert = new ArrayList<NodeCache>(1);
  /** Final copy of insertion nodes. */
  public MemData md;

  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param nc nodes to be inserted
   */
  protected NodeCopy(final InputInfo ii, final ANode n, final NodeCache nc) {
    super(ii, n);
    insert.add(nc);
  }

  @Override
  public void prepare() throws QueryException {
    if(insert.get(0).get(0) == null) return;
    // ignore fragment nodes
    if(!(node instanceof DBNode)) return;

    final NodeCache seq = new NodeCache();
    for(final NodeCache nc : insert) {
      for(ANode i; (i = nc.next()) != null;) seq.add(i);
    }
    if(node instanceof DBNode) {
      // text nodes still need to be merged. two adjacent iterators may
      // lead to two adjacent text nodes
      md = new MemData(((DBNode) node).data);
      new DataBuilder(md).build(mergeText(seq));
    }
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
      pool.add(qnm, ANode.type(k));
    }
  }
}
