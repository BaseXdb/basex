package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
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
  /** Target node is an attribute. */
  final boolean a;

  /**
   * Constructor.
   * @param n target node
   * @param newName new name
   */
  public RenamePrimitive(final Nod n, final QNm newName) {
    super(n, newName);
    a = n.type == Type.ATT;
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;

    final Data data = n.data;
    final int pre = n.pre;
    final byte[] nm = name.str();
    final byte[] uri = name.uri.str();
    final byte[] pref = pref(nm);
    final int k = data.kind(pre);

    // [CG] XQuery/Update Namespaces: check if empty uris cause troubles...
    //   should pi's be skipped here?
    if(data.ns.uri(nm, pre) == 0 && uri.length != 0) {
      data.uri(pre, k, data.ns.add(pref, uri, pre));
    }
    data.rename(pre, k, nm);
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
    return a ? new QNm[] { name } : null;
  }

  @Override
  public QNm[] remAtt() {
    return a ? new QNm[] { node.qname() } : null;
  }
}
