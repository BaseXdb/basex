package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;
import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;

/**
 * Delete primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class DeletePrimitive extends UpdatePrimitive {
  /**
   * Constructor.
   * @param n expression target node
   */
  public DeletePrimitive(final Nod n) {
    super(n);
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int p = n.pre + add;
    d.delete(p);
    mergeTextNodes(d, p - 1, p);
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.DELETE;
  }

  @Override
  public QNm[] remAtt() {
    return node.type == Type.ATT ? new QNm[] { node.qname() } : null;
  }
}
