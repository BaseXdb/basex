package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;
import static org.basex.query.up.primitives.PrimitiveType.*;

import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.util.Token;

/**
 * Delete primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class DeletePrimitive extends UpdatePrimitive {
  /** Target node is an attribute. */
  final boolean a;

  /**
   * Constructor.
   * @param n expression target node
   */
  public DeletePrimitive(final Nod n) {
    super(n);
    a = Nod.kind(n.type) == Data.ATTR;
  }

  @Override
  public void prepare() { }

  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int p = n.pre + add;
    d.delete(p);
    mergeTextNodes(d, p - 1, p);
  }

  @Override
  public PrimitiveType type() {
    return DELETE;
  }

  @Override
  public void merge(final UpdatePrimitive p) {
  }

  @Override
  public String[] addAtt() {
    return null;
  }

  @Override
  public String[] remAtt() {
    return a ? new String[] { Token.string(node.nname()) } : null;
  }
}
