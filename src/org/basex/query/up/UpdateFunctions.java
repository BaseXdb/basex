package org.basex.query.up;

import static org.basex.util.Token.*;

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
import org.basex.query.iter.SeqIter;

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
   * Merges all adjacent text nodes in the given sequence. The given iterator
   * must contain 
   * @param n iterator
   * @return iterator with merged text nodes
   * @throws QueryException query exception
   */
  public static SeqIter mergeTextNodes(final Iter n) throws QueryException {
    final SeqIter s = new SeqIter();
    Nod i = (Nod) n.next();
    while(i != null) {
      if(Nod.kind(i.type) == Data.TEXT) {
        byte[] t = EMPTY;
        while(i != null && Nod.kind(i.type) == Data.TEXT) {
          t = concat(t, i.str());
          i = (Nod) n.next();
        }
        s.add(new FTxt(t, null));
        continue;
      }
      s.add(i);
      i = (Nod) n.next();
    }
    return s;
  }
  
  /**
   * Merges two adjacent text nodes in a database.
   * @param d data reference
   * @param a node
   * @param b node
   * @return true if nodes have been merged
   */
  public static boolean mergeTextNodes(final Data d, final int a, final int b) {
    if(!(d.kind(a) == Data.TEXT && d.kind(b) == Data.TEXT)) return false;
    d.update(a, concat(d.text(a), d.text(b)));
    d.delete(b);
    return true;
  }
  
  /**
   * Finds the left sibling of the given node.
   * @param d data reference
   * @param pre value of node for which to find sibling
   * @return pre value of left sibling or -1 if non exists
   */
  public static int leftSibling(final Data d, final int pre) {
    // [CG] We already have something like that?
    if(pre < 1 || pre >= d.meta.size) return -1;
    final int par = d.parent(pre, d.kind(pre));
    int s = -1;
    int p = par + 1;
    while(p <= pre) {
      p = p + d.size(p, d.kind(p));
      if(p == pre) return s;
      s = p;
    }
    return s;
  }

  /**
   * Deletes nodes from database. Nodes are deleted backwards to preserve pre
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
   * Adds a set of attributes to a node.
   * @param pre target pre value
   * @param par parent node
   * @param d target data reference
   * @param m {@link MemData} instance holding attributes
   */
  public static void insertAttributes(final int pre, final int par, 
      final Data d, final MemData m) {
    final int ss = m.size(0, m.kind(0));
//    printTable(m);
    for(int s = 1; s < ss; s++) {
      final int p = pre + s - 1;
      d.insert(p, par, m.attName(s), m.attValue(s));
    }
//    printTable(d);
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
      data.update(pre, name, v);
    }
  }

  /**
   * Builds new {@link MemData} instance from iterator.
   * @param ch sequence iterator
   * @param d data reference for indices
   * @return new data instance
   * @throws QueryException query exception
   */
  public static MemData buildDB(final Iter ch, final Data d)
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
        ds += fragmentSize(fn, false);
      }
      n = (Nod) i.next();
    }
    // add root node for data instance
    m.addDoc(EMPTY, ds);

    // add nodes as children
    int pre = 1;
    i.reset();
    n = (Nod) i.next();
    while(n != null) {
      if(n instanceof DBNode) {
        final DBNode dn = (DBNode) n;
        pre = addDBNode(dn, m, pre, 0);
      } else if(n instanceof FNode) {
        final FNode fn = (FNode) n;
        pre = addFragment(fn, m, pre, 0);
      }
      n = (Nod) i.next();
    }
    return m;
  }

  /**
   * Determines the number of descendants of a fragment.
   * @param n fragment node
   * @param attr count attribute size of node
   * @return number of descendants + 1 or attribute size + 1
   * @throws QueryException query exception
   */
  public static int fragmentSize(final FNode n, final boolean attr)
  throws QueryException {
    int s = 1;
    final NodeIter at = n.attr();
    Nod fat = at.next();
    while(fat != null) {
      s++;
      fat = at.next();
    }
    if(attr) return s;
    final NodeIter it = n.child();
    Nod i = it.next();
    while(i != null) {
      s += fragmentSize((FNode) i, false);
      i = it.next();
    }
    return s;
  }

  /**
   * Adds a fragment to a {@link MemData} instance.
   * @param n node to be added
   * @param m data reference
   * @param preVal node position
   * @param par node parent
   * @return pre value of next node
   * @throws QueryException query exception
   */
  private static int addFragment(final FNode n, final MemData m,
      final int preVal, final int par) throws QueryException {
    final int k = Nod.kind(n.type);
    int pre = preVal;
    // add node
    switch (k) {
      case Data.ELEM:
        final FElem e = (FElem) n;
        final int as = fragmentSize(e, true);
        final int s = fragmentSize(e, false);
        m.addElem(
            m.tags.index(e.nname(), null, false),
            0, pre - par, as == -1 ? 1 : as, s == -1 ? 1 : s, false);
        pre++;
        break;
      case Data.ATTR:
        // needed to build data instance from attribute sequence
        final FAttr fat = (FAttr) n;
        m.addAtt(m.atts.index(fat.qname().str(), null, false),
            0, fat.str(), pre - par);
        return ++pre;
      case Data.TEXT:
        m.addText(((FTxt) n).str(), pre - par, k);
        return ++pre;
      case Data.PI:
        m.addText(((FPI) n).str(), pre - par, k);
        return ++pre;
      case Data.COMM:
        m.addText(((FComm) n).str(), pre - par, k);
        return ++pre;
    }

    // add attributes
    final NodeIter attIt = n.attr();
    FAttr at = (FAttr) attIt.next();
    while(at != null) {
      m.addAtt(m.atts.index(at.qname().str(), null, false),
          0, at.str(), pre - preVal);
      pre++;
      at = (FAttr) attIt.next();
    }
    final NodeIter chIt = n.child();
    Nod fn = chIt.next();
    while(fn != null) {
      pre = addFragment((FNode) fn, m, pre, preVal);
      fn = chIt.next();
    }
    return pre;
  }

  /**
   * Adds a database node to a {@link MemData} instance.
   * @param n node to be added
   * @param m data reference
   * @param preVal node position
   * @param par node parent
   * @return pre value of next node
   * @throws QueryException query exception
   */
  private static int addDBNode(final DBNode n, final MemData m,
      final int preVal, final int par) throws QueryException {
    // [LK] copy nodes directly from table instead of using iterators
    final Data data = n.data;
    final int k = Nod.kind(n.type);
    int pre = preVal;
    // [LK] type DOC? --- not possible --- DOC nodes are replaced by children,
    // see specification insert/replace - check though!
    switch(k) {
      case Data.ELEM:
        m.addElem(m.tags.index(data.tag(n.pre), null, false),
            0, pre - par, data.attSize(n.pre, k), data.size(n.pre, k), false);
        pre++;
        break;
      case Data.ATTR:
        // needed to build data instance from attribute sequence
        m.addAtt(m.atts.index(data.attName(n.pre), null, false),
            0, data.attValue(n.pre), pre - par);
        return ++pre;
      case Data.TEXT:
        m.addText(data.text(n.pre), pre - par, k);
        return ++pre;
      case Data.PI:
        m.addText(data.text(n.pre), pre - par, k);
        return ++pre;
      case Data.COMM:
        m.addText(data.text(n.pre), pre - par, k);
        return ++pre;
    }
    // add attributes and child nodes if n is element
    final NodeIter atts = n.attr();
    DBNode at = (DBNode) atts.next();
    while(at != null) {
      m.addAtt(m.atts.index(data.attName(at.pre), null, false),
          0, data.attValue(at.pre), pre - preVal);
      pre++;
      at = (DBNode) atts.next();
    }
    final NodeIter chIt = n.child();
    Nod dn = chIt.next();
    while(dn != null) {
      pre = addDBNode((DBNode) dn, m, pre, preVal);
      dn = chIt.next();
    }
    return pre;
  }

  /**
   * Prints database table.
   * @param d data reference
   */
  public static void printTable(final Data d) {
    try {
      InfoTable.table(new PrintOutput(System.out), d, 0, Integer.MAX_VALUE);
    } catch(final IOException e) {
      e.printStackTrace();
    }
  }
}
