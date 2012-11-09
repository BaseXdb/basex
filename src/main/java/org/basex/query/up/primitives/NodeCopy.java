package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract update primitive which holds a copy of nodes to be inserted.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public abstract class NodeCopy extends UpdatePrimitive {
  /** Nodes to be inserted. */
  public ANodeList insert;
  /** Number of insert operations (initialized by {@link #prepare}). */
  int size;
  /** Insertion sequence data instance. */
  MemData insseq;

  /**
   * Constructor.
   * @param t type
   * @param p target node pre value
   * @param d data
   * @param i input info
   * @param n node copy
   */
  NodeCopy(final PrimitiveType t, final int p, final Data d, final InputInfo i,
      final ANodeList n) {
    super(t, p, d, i);
    insert = n;
  }

  /**
   * Prepares this update primitive before execution. This includes i.e. the
   * preparation of insertion sequences.
   * @throws QueryException exception during preparation of data
   */
  @SuppressWarnings("unused")
  public final void prepare() throws QueryException {
    // build main memory representation of nodes to be copied
    insseq = new MemData(data);

    // text nodes still need to be merged. two adjacent iterators may lead to two
    // adjacent text nodes
    new DataBuilder(insseq).build(mergeNodeCacheText(insert));
    size += insert.size();
    insert = null;
  }

  /**
   * Adds top entries from the temporary data instance to the name pool,
   * which is used for finding duplicate attributes and namespace conflicts.
   * @param pool name pool
   */
  final void add(final NamePool pool) {
    for(int p = 0; p < insseq.meta.size; ++p) {
      final int k = insseq.kind(p);
      if(k != Data.ATTR && k != Data.ELEM || insseq.parent(p, k) > -1) continue;
      final int u = insseq.uri(p, k);
      final QNm qnm = new QNm(insseq.name(p, k));
      if(u != 0) qnm.uri(insseq.nspaces.uri(u));
      pool.add(qnm, ANode.type(k));
    }
  }

  /**
   * Merges all adjacent text nodes in the given sequence.
   * @param nl iterator
   * @return iterator with merged text nodes
   */
  private static ANodeList mergeNodeCacheText(final ANodeList nl) {
    final int ns = nl.size();
    final ANodeList s = new ANodeList(ns);
    if(ns == 0) return s;

    ANode n = nl.get(0);
    for(int c = 0; c < ns;) {
      if(n.type == NodeType.TXT) {
        final TokenBuilder tb = new TokenBuilder();
        while(n.type == NodeType.TXT) {
          tb.add(n.string());
          if(++c == ns) break;
          n = nl.get(c);
        }
        s.add(new FTxt(tb.finish()));
      } else {
        s.add(n);
        if(++c < ns) n = nl.get(c);
      }
    }
    return s;
  }

  @Override
  public final int size() {
    return size;
  }

  @Override
  public final String toString() {
    return Util.name(this) + '[' + getTargetNode() + ", " + size() + " ops]";
  }
}