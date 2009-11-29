package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;
import static org.basex.query.up.primitives.PrimitiveType.*;
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

    // [CG] check...
    if(data.ns.uri(nm, pre) == 0 && uri.length != 0) {
      data.ns.add(pref, uri, pre);
      data.ns(pre, data.ns.id(pref));
    }

    // passed on pre value must refer to element, attribute node or pi
    final int k = data.kind(pre);
    // [LK] update methods should consider namespace, defined in QName (name)
    if(k == Data.ELEM) {
      data.update(pre, nm);
    } else if(k == Data.ATTR) {
      data.update(pre, nm, data.attValue(pre));
    } else {
      final byte[] val = data.text(pre);
      final int i = indexOf(val, ' ');
      data.update(pre, i == -1 ? nm : concat(nm, SPACE, substring(val, i + 1)));
    }
  }

  @Override
  public PrimitiveType type() {
    return RENAME;
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    Err.or(UPMULTREN, node);
  }

  @Override
  public byte[][] addAtt() {
    return a ? new byte[][] { name.str() } : null;
  }

  @Override
  public byte[][] remAtt() {
    return a ? new byte[][] { node.nname() } : null;
  }
}
