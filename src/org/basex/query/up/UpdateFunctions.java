package org.basex.query.up;

import java.io.IOException;

import org.basex.core.Prop;
import org.basex.core.proc.InfoTable;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Namespaces;
import org.basex.data.Nodes;
import org.basex.data.PathSummary;
import org.basex.index.Names;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FAttr;
import org.basex.query.item.FComm;
import org.basex.query.item.FElem;
import org.basex.query.item.FNode;
import org.basex.query.item.FPI;
import org.basex.query.item.FTxt;
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
    if(n instanceof FNode) addFragment((FNode) n, m, 1);
    return m;
  }
  
  /**
   * Builds new MemData instance from iterator.
   * @param n node
   * @return new MemData instance
   * @throws QueryException query exception
   */
  public static MemData buildDBFromSet(final Nod n) throws QueryException {
    // [LK] use index refs
    final MemData m = new MemData(1, new Names(), new Names(), 
        new Namespaces(), new PathSummary(), new Prop());
    if(n instanceof DBNode) addDBNode((DBNode) n, m, 1);
    if(n instanceof FNode) addFragment((FNode) n, m, 1);
    return m;
  }
  
  /**
   * Adds node to MemData instance.
   * @param n node 
   * @param m data reference
   * @param dis distance
   * @return neighbour distance from parent
   * @throws QueryException query exception 
   */
  private static int addFragment(final FNode n, final MemData m, 
      final int dis) throws QueryException {
    int d = dis;
    final int k = Nod.kind(n.type);
    // add node
    switch (k) {
      case Data.ELEM:
        final FElem e = (FElem) n;
        // [LK] check sizes
        final int as = e.attr().size();
        final int s = e.desc().size();
        m.addElem(
            m.tags.index(e.nname(), null, false), 
            0, d++, as > -1 ? as : as + 2, s > -1 ? s : s + 2, false);
        break;
      case Data.TEXT:
        m.addText(((FTxt) n).str(), d++, k);
        break;
      case Data.COMM:
        m.addText(((FComm) n).str(), d++, k);
        break;
      case Data.PI:
        m.addText(((FPI) n).str(), d++, k);
        break;
    }
    if(k != Data.ELEM) return d;
    
    final FElem e = (FElem) n;
    // add attributes
    final NodeIter nIt = e.attr();
    FAttr at = (FAttr) nIt.next();
    // local parent distance
    int ld = 1;
    while(at != null) {
      m.addAtt(m.atts.index(at.qname().str(), null, false), 
          0, at.str(), ld);
      ld++;
      d++;
      at = (FAttr) nIt.next();
    }
    // add childs
    final NodeIter cIt = n.child();
    FNode ch = (FNode) cIt.next();
    while(ch != null) {
      ld = addFragment(ch, m, ld);
      ch = (FNode) cIt.next();
    }
    return d;
  }
  
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
        0, d++, data.attSize(n.pre, k), data.size(n.pre, k), false);
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
    // add childs
    final NodeIter cIt = n.child();
    DBNode ch = (DBNode) cIt.next();
    while(ch != null) {
      ld = addDBNode(ch, m, ld);
      ch = (DBNode) cIt.next();
    }
    return d;
  }
  
  /**
   * Prints database table.
   * @param d data reference
   */
  public static void printTable(Data d) {
    try {
      InfoTable.table(new PrintOutput(System.out), d, 0, Integer.MAX_VALUE);
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
}
