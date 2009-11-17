package org.basex.query.up;

import java.util.Map;

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
      
//      String[] s;
//      if((s = p.addAtt()) != null) changeAttributePool(m, s, true);
//      if((s = p.remAtt()) != null) changeAttributePool(m, s, false);
    }
  }
  
  /**
   * Adds or removes attributes to the current attribute pool.
   * @param m map holding attribute pool
   * @param a attribute names to add/remove
   * @param incr true if attributes must be added
   */
  @SuppressWarnings("unused")
  private void changeAttributePool(final Map<String, Integer> m, 
      final String[] a, final boolean incr) {
  }

  /**
   * If the updates at large would leave one database invalid, no updates are
   * applied at all.
   */
  public void status() {
    return;
  }
}
