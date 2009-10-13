package org.basex.query.up.primitives;

import static org.basex.query.up.primitives.UpdatePrimitive.Type.*;

import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;

/**
 * Replace value primitive.  
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class ReplaceValuePrimitive extends NewValuePrimitive {
  /**
   * Constructor.
   * @param n target node
   * @param newName new name
   */
  public ReplaceValuePrimitive(final Nod n, final byte[] newName) {
    super(n, newName);
  }

  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;
    
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int k = d.kind(n.pre);
    if(k == Data.ATTR) d.update(n.pre, d.attName(n.pre), name);
    else d.update(n.pre, name);
  }
  
  @Override
  public Type type() {
    return REPLACEVALUE;
  }
}
