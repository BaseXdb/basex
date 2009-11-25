package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;
import static org.basex.query.up.UpdateFunctions.*;
import static org.basex.query.up.primitives.PrimitiveType.*;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.NodIter;
import org.basex.query.util.Err;
import org.basex.util.Token;

/**
 * Replace primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class ReplacePrimitive extends NodeCopy {
  /** Target node is an attribute. */
  private final boolean a;

  /**
   * Constructor.
   * @param n target node
   * @param replace replace nodes
   * @param attr replacing nodes are attributes
   */
  public ReplacePrimitive(final Nod n, final NodIter replace,
      final boolean attr) {
    super(n, replace);
    a = attr;
  }

  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;

    final DBNode n = (DBNode) node;
    final int p = n.pre + add;
    final Data d = n.data;
    // source nodes may be empty, thus the replace results in deleting the
    // target node
    if(m == null) {
      d.delete(p);
      return;
    }
    final int k = Nod.kind(n.type);
    final int par = d.parent(p, k);
    if(a) insertAttributes(p, par, d, m);
    else d.insert(p, par , m);
    d.delete(p + m.meta.size);
    mergeTextNodes(d, p, p + 1);
    mergeTextNodes(d, p - 1, p);
  }

  @Override
  public PrimitiveType type() {
    return REPLACENODE;
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    Err.or(UPMULTREPL, node.qname());
  }

  @Override
  public String[] addAtt() {
    if(!a) return null;
    final String[] at = new String[m.meta.size];
    for(int i = 0; i < m.meta.size; i++) at[i] = Token.string(m.attName(i));
    return at;
  }

  @Override
  public String[] remAtt() {
    return a ? new String[] { Token.string(node.nname()) } : null;
  }
}