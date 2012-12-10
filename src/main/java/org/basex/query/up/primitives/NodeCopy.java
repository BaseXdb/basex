package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
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
  private int size;
  /** Insertion sequence data clip. */
  DataClip insseq;

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
   * Prepares this update primitive before execution. This includes e.g. the
   * preparation of insertion sequences.
   * @param tmp temporary database
   */
  public final void prepare(final MemData tmp) {
    // build main memory representation of nodes to be copied
    // text nodes still need to be merged. two adjacent iterators may lead to two
    // adjacent text nodes

    // works...
    final MemData md = new MemData(tmp);
    new DataBuilder(md).build(mergeNodeCacheText(insert));
    insseq = new DataClip(md);

    /* [LK] XQuery Update with single database: work in progress...
    final int s = tmp.meta.size;
    new DataBuilder(tmp).build(mergeNodeCacheText(insert));
    insseq = new DataBox(tmp, s, tmp.meta.size);
    */

    size += insert.size();
    insert = null;
  }

  /**
   * Adds top entries from the temporary data instance to the name pool,
   * which is used for finding duplicate attributes and namespace conflicts.
   * @param pool name pool
   */
  final void add(final NamePool pool) {
    final Data d = insseq.data;
    final int ps = insseq.start;
    for(int p = ps; p < insseq.end; ++p) {
      final int k = d.kind(p);
      if(k != Data.ATTR && k != Data.ELEM || d.parent(p, k) >= ps) continue;
      final int u = d.uri(p, k);
      final QNm qnm = new QNm(d.name(p, k));
      if(u != 0) qnm.uri(d.nspaces.uri(u));
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
    if(ns == 0) return nl;
    final ANodeList s = new ANodeList(ns);
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