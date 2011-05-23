package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.ANode;
import org.basex.query.item.QNm;
import org.basex.query.up.NamePool;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Rename primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class RenameNode extends Primitive {
  /** New name. */
  final QNm name;

  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param nm new name
   */
  public RenameNode(final InputInfo ii, final ANode n, final QNm nm) {
    super(PrimitiveType.RENAMENODE, ii, n);
    name = nm;
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final Data data = n.data;
    final int pre = n.pre;
    data.rename(pre, data.kind(pre), name.atom(), name.uri().atom());
  }

  @Override
  public void merge(final Primitive p) throws QueryException {
    UPMULTREN.thrw(input, node);
  }

  @Override
  public void update(final NamePool pool) {
    pool.add(name, node.type);
    pool.remove(node);
  }

  @Override
  public String toString() {
    return Util.name(this) + "[" + node + ", " + name + "]";
  }
}
