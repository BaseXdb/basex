package org.basex.query.up;

import org.basex.query.up.primitives.UpdatePrimitive;

/**
 *  Validates all update operations on the pending update list before they are
 *  applied to a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class ValidateUpdates {

  /**
   * Validates updates for a specific database.
   * @param up database primitive
   */
  public void validate(final UpdatePrimitive[] up) {
//    final Map<String, Integer> m = new HashMap<String, Integer>();
    for(int i = 0; i < up.length; i++) {
      final UpdatePrimitive p = up[i];
      if(p == null) continue;
      
//      if(p.addAtt()) changeAttributePool(m, null, true);
//      if(p.remAtt()) changeAttributePool(m, null, false);
    }
  }
  
//  private void changeAttributePool(final Map m, final String a, 
//      final boolean incr) {
//  }

  /**
   * If the updates at large would leave one database invalid, no updates are
   * applied at all.
   */
  public void status() {
    return;
  }
}
