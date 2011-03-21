package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.ANode;
import org.basex.query.iter.NodeCache;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Insert into as last primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class InsertInto extends NodeCopy {
  /** Insert into or insert into as last. */
  private final boolean last;
  /** Index of most recently added 'insert into' nodes. */
  private int i;

  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param copy copy of nodes to be inserted
   * @param l as last flag
   */
  public InsertInto(final InputInfo ii, final ANode n, final NodeCache copy,
      final boolean l) {
    super(ii, n, copy);
    last = l;
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int pre = n.pre + d.size(n.pre, ANode.kind(node.ndType())) + add;
    d.insert(pre, n.pre, md);
    if(!mergeTexts(d, pre - 1, pre)) {
      final int s = md.meta.size;
      mergeTexts(d, pre + s - 1, pre + s);
    }
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    if(((InsertInto) p).last) insert.add(((NodeCopy) p).insert.get(0));
    else insert.add(i++, ((NodeCopy) p).insert.get(0));
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.INSERTINTO;
  }

  @Override
  public String toString() {
    return Util.name(this) + "[" + node + ", " + insert + "]";
  }
}
