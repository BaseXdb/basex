package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.QNm;
import org.basex.query.up.NamePool;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Rename node primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class RenameNode extends ValueUpdate {
  /** New name. */
  final QNm name;

  /**
   * Constructor.
   * @param p pre
   * @param d data
   * @param i input info
   * @param nm new QName
   */
  public RenameNode(final int p, final Data d, final InputInfo i,
      final QNm nm) {
    super(PrimitiveType.RENAMENODE, p, d, i);
    name = nm;
  }

  @Override
  public void apply() {
    data.rename(pre, data.kind(pre), name.atom(), name.uri().atom());
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    UPMULTREN.thrw(input, getTargetDBNode());
  }

  @Override
  public void update(final NamePool pool) {
    final DBNode node = getTargetDBNode();
    pool.add(name, node.type);
    pool.remove(node);
  }

  @Override
  public String toString() {
    return Util.name(this) + "[" + getTargetDBNode() + ", " + name + "]";
  }
}
