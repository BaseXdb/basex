package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;
import static org.basex.query.up.UpdateFunctions.*;
import static org.basex.query.up.primitives.PrimitiveType.*;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
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
    a = Nod.kind(n.type) == Data.ATTR;
  }

  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;
    final DBNode n = (DBNode) node;
    rename(n.pre, name, n.data);
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
  public boolean addAtt() {
    return a;
  }
  
  @Override
  public boolean remAtt() {
    return a;
  }
}
