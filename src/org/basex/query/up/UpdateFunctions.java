package org.basex.query.up;

import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Namespaces;
import org.basex.data.Nodes;
import org.basex.data.PathSummary;
import org.basex.index.Names;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.NodeIter;

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
   * @param n node
   * @return new MemData instance
   * @throws QueryException query exception
   */
  public static MemData buildDB(final Nod n) throws QueryException {
    // [LK] use index refs
    final MemData m = new MemData(1, new Names(), new Names(), 
        new Namespaces(), new PathSummary(), new Prop());
    if(n instanceof DBNode) addDBNode((DBNode) n, m, 1);
//    if(n instanceof FNode) addFragment((FNode) n, m, 1);
    return m;
  }
  
//  /**
//   * Adds node to MemData instance.
//   * @param n node 
//   * @param m data reference
//   * @param dis distance
//   * @return neighbour distance from parent
//   */
//  private static int addFragment(final FNode n, final MemData m, 
//      final int dis) {
//    // [LK] add framgents to MemData instance
//    // add node
//    // add attributes
//    // recursively add childs    (for node n in child) addNode
//    return 0;
//  }
  
  /**
   * Adds node to MemData instance.
   * @param n node
   * @param m data reference
   * @param dis distance
   * @return neighbour distance from parent
   * @throws QueryException query exception
   */
  private static int addDBNode(final DBNode n, final MemData m, final int dis) 
    throws QueryException {
    int d = dis;
    final Data data = n.data;
    final int k = Nod.kind(n.type);
    // add node
    if(k == Data.TEXT || k == Data.PI || k == Data.COMM) {
      m.addText(data.text(n.pre), d++, k);
      return d;
    }
    if(k == Data.ELEM) m.addElem(
        m.tags.index(data.tag(n.pre), null, false), 
        0, d, data.attSize(n.pre, k), data.size(n.pre, k), false);
    d++;
    // add attributes
    final NodeIter nIt = n.attr();
    DBNode at = (DBNode) nIt.next();
    // local parent distance
    int ld = 1;
    while(at != null) {
      m.addAtt(m.atts.index(data.attName(at.pre), null, false), 
          0, data.attValue(at.pre), ld);
      ld++;
      d++;
      at = (DBNode) nIt.next();
    }
//    try {
//      InfoTable.table(new PrintOutput(System.out), m, 0, Integer.MAX_VALUE);
//    } catch(IOException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
    // add childs
    final NodeIter cIt = n.child();
    DBNode ch = (DBNode) cIt.next();
    while(ch != null) {
      ld = addDBNode(ch, m, ld);
      ch = (DBNode) cIt.next();
    }
    return d;
  }
}
