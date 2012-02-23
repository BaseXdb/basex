package org.basex.query.up.primitives;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Abstract update primitive which holds a copy of nodes to be inserted.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public abstract class NodeCopy extends StructuralUpdate {
  /** Nodes to be inserted. */
  ArrayList<NodeCache> insert = new ArrayList<NodeCache>(1);
  /** Final copy of insertion nodes. */
  MemData md;
  /** Number of insert operations (initialized by {@link #prepare}). */
  int size;

  /**
   * Constructor.
   * @param t type
   * @param p pre
   * @param d data
   * @param i input info
   * @param nc node copy
   */
  NodeCopy(final PrimitiveType t, final int p, final Data d,
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
    for(int i = 0; i < insert.size(); i++) {
      final NodeCache nc = insert.get(i);
      for(ANode n; (n = nc.next()) != null;) {
        seq.add(n);
        size++;
      }
      // clear entries to recover memory
      insert.set(i, null);
    }
    insert = null;

    // text nodes still need to be merged. two adjacent iterators may
    // lead to two adjacent text nodes
    new DataBuilder(md).build(mergeNodeCacheText(seq));
  }

  /**
   * Adds top entries from the temporary data instance to the name pool,
   * which is used for finding duplicate attributes and namespace conflicts.
   * @param pool name pool
   */
  final void add(final NamePool pool) {
    for(int p = 0; p < md.meta.size; ++p) {
      final int k = md.kind(p);
      if(k != Data.ATTR && k != Data.ELEM || md.parent(p, k) > -1) continue;
      final int u = md.uri(p, k);
      final QNm qnm = new QNm(md.name(p, k));
      if(u != 0) qnm.uri(md.nspaces.uri(u));
      pool.add(qnm, ANode.type(k));
    }
  }

  /**
   * Merges all adjacent text nodes in the given sequence.
   * @param n iterator
   * @return iterator with merged text nodes
   */
  private static NodeCache mergeNodeCacheText(final NodeCache n) {
    final NodeCache s = new NodeCache();
    ANode i = n.next();
    while(i != null) {
      if(i.type == NodeType.TXT) {
        final TokenBuilder tb = new TokenBuilder();
        while(i != null && i.type == NodeType.TXT) {
          tb.add(i.string());
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
  public final int size() {
    return size;
  }

  @Override
  public String toString() {
    return Util.name(this) + '[' + targetNode() + ", " + size() + " ops]";
  }
}