package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;
import static org.basex.query.up.primitives.UpdatePrimitive.Type.*;

import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;

/**
 * Represents a delete primitive.
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
  public void apply() {
    if(!(node instanceof DBNode)) return;
    final DBNode n = (DBNode) node;
    deleteDBNodes(new Nodes(new int[]{n.pre}, n.data));
  }

  @Override
  public Type type() {
    return DELETE;
  }

  @SuppressWarnings("unused")
  @Override
  public void check() throws QueryException {
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    // [LK] throw query exception: multiple deletes on same node
  }
}
