package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Uri;
import org.basex.query.iter.NodIter;

/**
 * Insert attribute primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class InsertAttribute extends NodeCopy {
  /**
   * Constructor.
   * @param n target node
   * @param copy insertion nods
   */
  public InsertAttribute(final Nod n, final NodIter copy) {
    super(n, copy);
  }

  @Override
  public void apply(final int add) {
    if(md == null) return;
    final DBNode n = (DBNode) node;
    n.data.insertAttr(n.pre + 1, n.pre, md);
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    c.add(((NodeCopy) p).c.getFirst());
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.INSERTATTR;
  }

  @Override
  public QNm[] addAtt() {
    final QNm[] at = new QNm[md.meta.size];
    for(int pre = 0; pre < md.meta.size; pre++) {
      final int u = md.uri(pre, Data.ATTR);
      at[pre] = new QNm(md.name(pre, Data.ATTR));
      if(u != 0) at[pre].uri = Uri.uri(md.ns.uri(u));
    }
    return at;
  }
}
