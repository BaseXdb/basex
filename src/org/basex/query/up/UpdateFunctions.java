package org.basex.query.up;

import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Namespaces;
import org.basex.data.Nodes;
import org.basex.data.PathSummary;
import org.basex.index.Names;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;

/**
 * XQuery Update update functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class UpdateFunctions {
  
  /**
   * Constructor.
   */
  private UpdateFunctions() { }
  
  /**
   * Delete nodes from database. Nodes are deleted backwards to preserve pre
   * values. All given nodes are part of the same Data instance.
   * @param nodes nodes to delete
   */
  public static void deleteDBNodes(final Nodes nodes) {
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
   * @param pre pre value
   * @param name new name
   * @param data data reference
   */
  public static void rename(final int pre, final byte[] name, final Data data) {
    final int k = data.kind(pre);
    if(k == Data.ELEM || k == Data.PI) data.update(pre, name);
    else if(k == Data.ATTR) {
      final byte[] v = data.attValue(pre);
      // [LK] proc.Update
      data.update(pre, name, v);
    }
  }
  
  /**
   * Builds new MemData instance from iterator.
   * @param node node
   * @return new MemData instance
   */
  public static MemData buildDB(final Nod node) {
    final MemData m = new MemData(20, new Names(), new Names(), 
        new Namespaces(), new PathSummary(), new Prop());
    int dis = 1;
    if(node instanceof DBNode) {
      DBNode n = (DBNode) node; 
      final Data d = n.data;
      final int k = Nod.kind(n.type); 
      if(k == Data.ELEM) m.addElem(
          m.tags.index(d.tag(n.pre), null, false), 
          0, dis, n.attr().size() + 1, d.size(n.pre, k), false);

    } else {
//      FNode n = (FNode) node;
    }
    return m;
  }
}
