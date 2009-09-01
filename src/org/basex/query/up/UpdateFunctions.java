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
  
  /**
   * Renames the specified node.
   * @param id node identity
   * @param name new name
   * @param data data reference
   */
  public static void rename(final int id, final byte[] name, final Data data) {
    final int p = data.pre(id);
    final int k = data.kind(p);
    if(k == Data.ELEM || k == Data.PI) data.update(data.pre(id), name);
    else if(k == Data.ATTR) {
      final byte[] v = data.attValue(p);
      data.update(p, name, v);
    }
  }
}
