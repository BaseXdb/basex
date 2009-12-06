package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.util.Err;

/**
 * Rename primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class RenamePrimitive extends NewValue {
  /**
   * Constructor.
   * @param n target node
   * @param nm new name
   */
  public RenamePrimitive(final Nod n, final QNm nm) {
    super(n, nm);
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final Data data = n.data;
    final int pre = n.pre;
    data.rename(pre, data.kind(pre), name.str(), name.uri.str());
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.RENAME;
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    Err.or(UPMULTREN, node);
  }

  @Override
  public QNm[] addAtt() {
    return node.type == Type.ATT ? new QNm[] { name } : null;
  }

  @Override
  public QNm[] remAtt() {
    return node.type == Type.ATT ? new QNm[] { node.qname() } : null;
  }
}
