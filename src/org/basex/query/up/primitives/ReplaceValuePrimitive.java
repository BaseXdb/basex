package org.basex.query.up.primitives;

import org.basex.query.item.Nod;

/**
 * Replace value primitive.  
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class ReplaceValuePrimitive extends NewValuePrimitive {

  /**
   * Constructor.
   * @param n target node
   * @param newName new name
   */
  public ReplaceValuePrimitive(final Nod n, final byte[] newName) {
    super(n, newName);
  }

  @Override
  public void apply() { 
  }
}
