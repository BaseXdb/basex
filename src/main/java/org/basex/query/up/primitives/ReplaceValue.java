package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.expr.ParseExpr;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;

/**
 * Replace value primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public final class ReplaceValue extends NewValue {
  /**
   * Constructor.
   * @param u updating expression
   * @param n target node
   * @param newName new name
   */
  public ReplaceValue(final ParseExpr u, final Nod n, final QNm newName) {
    super(u, n, newName);
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int k = d.kind(n.pre);
    final byte[] nn = name.atom();

    if(k == Data.TEXT && nn.length == 0) {
      d.delete(n.pre);
    } else {
      d.replace(n.pre, k, nn);
    }
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    parent.error(UPMULTREPV, node);
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.REPLACEVALUE;
  }
}
