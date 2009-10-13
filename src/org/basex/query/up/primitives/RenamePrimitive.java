package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;
import static org.basex.query.up.primitives.UpdatePrimitive.Type.*;

import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;


/**
 * Represents a rename primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class RenamePrimitive extends NewValuePrimitive {
  /**
   * Constructor.
   * @param n target node
   * @param newName new name
   */
  public RenamePrimitive(final Nod n, final byte[] newName) {
    super(n, newName);
  }

  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;
    final DBNode n = (DBNode) node;
    rename(n.pre, name, n.data);
  }

  @Override
  public Type type() {
    return RENAME;
  }

  @SuppressWarnings("unused")
  @Override
  public void check() throws QueryException {
  }
}
