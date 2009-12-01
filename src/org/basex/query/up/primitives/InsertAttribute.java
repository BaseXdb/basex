package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;
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
    insertAttributes(n.pre + 1, n.pre, n.data, md);
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
    // [CG] namespace check still buggy (see {@link ReplacePrimitive}...
    final QNm[] at = new QNm[md.meta.size];
    for(int i = 0; i < md.meta.size; i++) {
      final byte[] nm = md.attName(i);
      final int j = md.ns.uri(nm);
      at[i] = new QNm(md.attName(i));
      if(j != 0) at[i].uri = Uri.uri(md.ns.key(j));
    }
    return at;
  }
}
