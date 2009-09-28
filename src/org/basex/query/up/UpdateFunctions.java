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
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeIter;
import org.basex.util.Token;

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
   * @param root root node  
   * @param ch sequence iterator
   * @param d data reference for indices
   * @return new MemData instance
   * @throws QueryException query exception
   */
  public static MemData buildDB(final Nod root, final Iter ch, final Data d) 
  throws QueryException {
    MemData m = null;
    // [LK] usage of index refs only possible if target node is a dbnode,
    // because insert/replace etc. nodes can be mixed up (DBNode, FNode ...)
    if(d == null) m = new MemData(20, new Names(), new Names(), 
        new Namespaces(), new PathSummary(), new Prop());
    else m = new MemData(20, d.tags, d.atts, d.ns, d.path, d.meta.prop);
    // add parent node
    final Iter i = ch;
    Nod n = (Nod) i.next();
    // determine size of sequence
    int ds = 1;
    while(n != null) {
      if(n instanceof DBNode) {
        final DBNode dbn = (DBNode) n;
        ds += dbn.data.size(dbn.pre, Nod.kind(dbn.type));
      } else if(n instanceof FNode) {
        final FNode fn = (FNode) n;
        ds += fn.descOrSelf().size();
      }
      n = (Nod) i.next();
    }
    // add doc root node
    if(root == null) m.addDoc(Token.EMPTY, ds);
    // [LK] alternative root
    else return null;
    
    // add nodes as childs
    int dis = 1;
    i.reset();
    n = (Nod) i.next();
    while(n != null) {
      if(n instanceof DBNode) {
        final NodeIter desc = ((DBNode) n).descOrSelf();
        DBNode dn = (DBNode) desc.next();
        while(dn != null) {
          addDBNode(dn, m, dis);
          dn = (DBNode) desc.next();
        }
      } else if(n instanceof FNode) {
        final NodeIter desc = ((FNode) n).descOrSelf();
        FNode fn = (FNode) desc.next();
        while(fn != null) {
          addFragment(fn, m, dis);
          fn = (FNode) desc.next();
        }
      }
      n = (Nod) i.next();
    }
    printTable(m);
    return m;
  }
  
  /**
   * Adds node to MemData instance.
   * @param n node 
   * @param m data reference
   * @param dis distance
   * @throws QueryException query exception 
   */
  private static void addFragment(final FNode n, final MemData m, 
      final int dis) throws QueryException {
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
            0, dis, as == -1 ? 1 : as, s == -1 ? 1 : s, false);
        break;
      case Data.TEXT:
        m.addText(((FTxt) n).str(), dis, k);
        return;
      case Data.PI:
        m.addText(((FPI) n).str(), dis, k);
        return;
      case Data.COMM:
        m.addText(((FComm) n).str(), dis, k);
        return;
    }
    
    // add attributes
    final NodeIter nIt = n.attr();
    FAttr at = (FAttr) nIt.next();
    // local parent distance
    while(at != null) {
      m.addAtt(m.atts.index(at.qname().str(), null, false), 
          0, at.str(), dis);
      at = (FAttr) nIt.next();
    }
  }
  
  /**
   * Adds node to MemData instance.
   * @param n node
   * @param m data reference
   * @param dis distance
   * @throws QueryException query exception
   */
  private static void addDBNode(final DBNode n, final MemData m, final int dis) 
    throws QueryException {
    final Data data = n.data;
    final int k = Nod.kind(n.type);
    // [LK] type DOC?
    switch(k) {
      case Data.ELEM:
        m.addElem(m.tags.index(data.tag(n.pre), null, false), 
            0, dis, data.attSize(n.pre, k), data.size(n.pre, k), false);
        break;
      case Data.TEXT:
        m.addText(data.text(n.pre), dis, k);
        return;
      case Data.PI:
        m.addText(data.text(n.pre), dis, k);
        return;
      case Data.COMM:
        m.addText(data.text(n.pre), dis, k);
        return;
    }
    // add attributes
    final NodeIter atts = n.attr();
    DBNode at = (DBNode) atts.next();
    while(at != null) {
      m.addAtt(m.atts.index(at.data.attName(at.pre), null, false), 
          0, at.data.attValue(at.pre), dis);
      at = (DBNode) atts.next();
    }
  }
  
  /**
   * Prints database table.
   * @param d data reference
   */
  public static void printTable(final Data d) {
    try {
      InfoTable.table(new PrintOutput(System.out), d, 0, Integer.MAX_VALUE);
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
}
