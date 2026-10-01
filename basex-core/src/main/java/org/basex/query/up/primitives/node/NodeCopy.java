package org.basex.query.up.primitives.node;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract update primitive which holds a copy of nodes to be inserted.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
abstract class NodeCopy extends NodeUpdate {
  /** Nodes to be inserted. */
  GNodeList nodes;
  /** Insertion sequence data clip (can be {@code null}; populated by {@link #prepare}). */
  DataClip insseq;

  /**
   * Constructor.
   * @param type type
   * @param pre target node PRE value
   * @param data data
   * @param info input info (can be {@code null})
   * @param nodes node copy insertion sequence
   */
  NodeCopy(final UpdateType type, final int pre, final Data data, final InputInfo info,
      final GNodeList nodes) {
    super(type, pre, data, info);
    this.nodes = nodes;
  }

  @Override
  public final void prepare(final MemData memData, final QueryContext qc) throws QueryException {
    // merge texts. after that, text nodes still need to be merged,
    // as two adjacent iterators may lead to two adjacent text nodes
    final GNodeList list = mergeNodeCacheText(nodes);
    nodes = null;
    // build main memory representation of nodes to be copied
    final int start = memData.nodes();
    new DataBuilder(memData, qc).build(list);
    insseq = new DataClip(memData, start, memData.nodes(), list.size());
    checkLimit(insseq.size());
  }

  /**
   * Adds the inserted attributes to the name pool.
   * @param pool name pool
   */
  final void add(final NamePool pool) {
    final Data d = insseq.data;
    for(int p = insseq.start; p < insseq.end; p++) {
      final byte[][] qname = d.qname(p, Data.ATTR);
      pool.add(new QNm(qname[0], qname[1]), NodeType.ATTRIBUTE);
    }
  }

  /**
   * Merges all adjacent text nodes in the given sequence.
   * @param nl iterator
   * @return iterator with merged text nodes
   */
  private static GNodeList mergeNodeCacheText(final GNodeList nl) {
    final int ns = nl.size();
    if(ns == 0) return nl;
    final GNodeList s = new GNodeList(ns);
    GNode n = nl.get(0);
    for(int c = 0; c < ns;) {
      if(n.kind() == Kind.TEXT) {
        final TokenBuilder tb = new TokenBuilder();
        while(n.kind() == Kind.TEXT) {
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
  public int size() {
    return insseq.fragments;
  }

  @Override
  public final String toString() {
    return Util.className(this) + "[], " + (insseq != null ? size() : nodes.size()) + " ops]";
  }
}
