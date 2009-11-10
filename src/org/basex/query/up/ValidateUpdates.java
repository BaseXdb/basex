package org.basex.query.up;

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
   * @param dbp database primitive
   */
  public void validate(final DBPrimitives dbp) {
    final int[] nodes = dbp.getNodes();
    for(int i = 0; i < nodes.length; i++) {
      
    }
  }

  /**
   * If the updates at large would leave one database invalid, no updates are
   * applied at all.
   */
  public void status() {
    return;
  }
}
