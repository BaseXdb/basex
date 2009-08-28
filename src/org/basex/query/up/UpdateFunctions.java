package org.basex.query.up;

import org.basex.data.Data;
import org.basex.data.Nodes;

/**
 * XQuery Update database update functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class UpdateFunctions {
  
  /**
   * Delete nodes from database. Nodes are deleted backwards to preserve pre
   * values.
   * @param nodes nodes to delete
   */
  public static void delete(final Nodes nodes) {
    final Data data = nodes.data;
    final int size = nodes.size();
    for(int i = size - 1; i >= 0; i--) {
      final int pre = nodes.nodes[i];
      if(data.fs != null) data.fs.delete(pre);
      data.delete(pre);
    }
  }
}
