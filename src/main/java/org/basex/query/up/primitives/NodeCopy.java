package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.FTxt;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.iter.NodeCache;
import org.basex.query.up.NamePool;
import org.basex.query.util.DataBuilder;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.list.ObjList;

/**
 * Abstract update primitive which holds a copy of nodes to be inserted.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public abstract class NodeCopy extends StructuralUpdate {
  /** Nodes to be inserted. */
  protected final ObjList<NodeCache> insert = new ObjList<NodeCache>(1);
  /** Final copy of insertion nodes. */
  protected MemData md;

  /**
   * Constructor.
   * @param t type
   * @param p pre
   * @param d data
   * @param i input info
   * @param nc node copy
   */
  protected NodeCopy(final PrimitiveType t, final int p, final Data d,
      final InputInfo i, final NodeCache nc) {
    super(t, p, d, i);
    insert.add(nc);
  }

  /**
   * Prepares this update primitive before execution. This includes i.e. the
   * preparation of insertion sequences.
   * @throws QueryException exception during preparation of data
   */
  @SuppressWarnings("unused")
  public void prepare() throws QueryException {
    // build main memory representation of nodes to be copied
    md = new MemData(data);
    final NodeCache seq = new NodeCache();
    for(final NodeCache nc : insert) {
      for(ANode i; (i = nc.next()) != null;) seq.add(i);
    }
    // text nodes still need to be merged. two adjacent iterators may
    // lead to two adjacent text nodes
    new DataBuilder(md).build(mergeNodeCacheText(seq));
  }

  /**
   * Adds top entries from the temporary data instance to the name pool,
   * which is used for finding duplicate attributes and namespace conflicts.
   * @param pool name pool
   */
  protected final void add(final NamePool pool) {
    for(int p = 0; p < md.meta.size; ++p) {
      final int k = md.kind(p);
      if(k != Data.ATTR && k != Data.ELEM || md.parent(p, k) > -1) continue;
      final int u = md.uri(p, k);
      final QNm qnm = new QNm(md.name(p, k));
      if(u != 0) qnm.uri(md.ns.uri(u));
      pool.add(qnm, ANode.type(k));
    }
  }

  /**
   * Merges all adjacent text nodes in the given sequence.
   * @param n iterator
   * @return iterator with merged text nodes
   */
  protected static NodeCache mergeNodeCacheText(final NodeCache n) {
    final NodeCache s = new NodeCache();
    ANode i = n.next();
    while(i != null) {
      if(i.type == NodeType.TXT) {
        final TokenBuilder tb = new TokenBuilder();
        while(i != null && i.type == NodeType.TXT) {
          tb.add(i.atom());
          i = n.next();
        }
        s.add(new FTxt(tb.finish()));
      } else {
        s.add(i);
        i = n.next();
      }
    }
    return s;
  }

  @Override
  public int size() {
    return insert.size();
  }
}