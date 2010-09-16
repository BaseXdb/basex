package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.up.NamePool;
import org.basex.util.InputInfo;

/**
 * Rename primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public final class RenamePrimitive extends NewValue {
  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param nm new name
   */
  public RenamePrimitive(final InputInfo ii, final Nod n, final QNm nm) {
    super(ii, n, nm);
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final Data data = n.data;
    final int pre = n.pre;
    data.rename(pre, data.kind(pre), name.atom(), name.uri().atom());
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    UPMULTREN.thrw(input, node);
  }

  @Override
  public void update(final NamePool pool) {
    pool.add(name, node.type);
    pool.remove(node);
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.RENAME;
  }
}
