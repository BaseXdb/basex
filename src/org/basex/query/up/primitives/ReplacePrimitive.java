package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;
import static org.basex.query.up.UpdateFunctions.*;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.iter.NodIter;
import org.basex.query.util.Err;

/**
 * Replace primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class ReplacePrimitive extends NodeCopy {
  /**
   * Constructor.
   * @param n target node
   * @param replace replace nodes
   */
  public ReplacePrimitive(final Nod n, final NodIter replace) {
    super(n, replace);
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final int pre = n.pre + add;
    final Data d = n.data;
    // source nodes may be empty, thus the replace results in deleting the
    // target node
    if(md == null) {
      d.delete(pre);
      return;
    }
    final int par = d.parent(pre, Nod.kind(n.type));
    if(n.type == Type.ATT) {
      d.insertAttr(pre, par, md);
    } else {
      d.insert(pre, par, md);
    }
    d.delete(pre + md.meta.size);
    mergeTexts(d, pre, pre + 1);
    mergeTexts(d, pre - 1, pre);
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.REPLACENODE;
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    Err.or(UPMULTREPL, node.qname());
  }

  @Override
  public QNm[] addAtt() {
    if(node.type != Type.ATT) return null;
    final QNm[] at = new QNm[md.meta.size];
    for(int pre = 0; pre < md.meta.size; pre++) {
      final int u = md.uri(pre, Data.ATTR);
      at[pre] = new QNm(md.name(pre, Data.ATTR));
      if(u != 0) at[pre].uri = Uri.uri(md.ns.uri(u));
    }
    return at;
  }

  @Override
  public QNm[] remAtt() {
    return node.type != Type.ATT ? new QNm[] { node.qname() } : null;
  }
}
