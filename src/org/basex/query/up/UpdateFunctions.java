package org.basex.query.up;

import static org.basex.util.Token.*;

import java.util.HashSet;
import java.util.Set;

import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Namespaces;
import org.basex.data.PathSummary;
import org.basex.index.Names;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FNode;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeIter;
import org.basex.query.iter.SeqIter;
import org.basex.util.TokenBuilder;

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
  public static SeqIter mergeText(final Iter n) throws QueryException {
    final SeqIter s = new SeqIter();
    Item i = n.next();
    while(i != null) {
      if(i.type == Type.TXT) {
        final TokenBuilder tb = new TokenBuilder();
        while(i != null && i.type == Type.TXT) {
          tb.add(i.str());
          i = n.next();
        }
        s.add(new FTxt(tb.finish(), null));
      } else {
        s.add(i);
        i = n.next();
      }
    }
    return s;
  }
  
  /**
   * Blabooo.
   * @param at ?
   * @param r ?
   * @param m ?
   * @return ?
   * @throws QueryException ?
   */
  public static boolean checkAttNames(final Iter at, final Iter r, 
      final String m) throws QueryException {
    final Set<String> s = new HashSet<String>();
    Nod n = (Nod) at.next();
    
    while(n != null) {
      s.add(string(n.nname()));
      n = (Nod) at.next();
    }
    
    n = (Nod) r.next();
    while(n != null) {
      final String t = string(n.nname());
      final boolean b = s.add(t);
      if(!b) {
        if(m == null) return false;
        if(!t.equals(m)) return false;
      }
      n = (Nod) r.next();
    }
    return true;
  }

  /**
   * Merges two adjacent text nodes in a database. The two node arguments must
   * be ordered ascending, otherwise the text of the two nodes is concatenated
   * in the wrong order.
   * @param d data reference
   * @param a node pre value
   * @param b node pre value
   * @return true if nodes have been merged
   */
  public static boolean mergeTextNodes(final Data d, final int a, final int b) {
    // some pre value checks to prevent databases errors
    final int s = d.meta.size;
    if(a >= s || b >= s || a == b || a < 0 || b < 0) return false;
    if(!(d.kind(a) == Data.TEXT && d.kind(b) == Data.TEXT)) return false;
    if(d.parent(a, d.kind(a)) != d.parent(b, d.kind(b))) return false;
    
    d.update(a, concat(d.text(a), d.text(b)));
    d.delete(b);
    return true;
  }

  /**
   * Adds a set of attributes to a node.
   * @param pre target pre value
   * @param par parent node
   * @param d target data reference
   * @param m data instance holding attributes
   */
  public static void insertAttributes(final int pre, final int par,
      final Data d, final Data m) {
    final int ss = m.size(0, m.kind(0));
    for(int s = 1; s < ss; s++) {
      d.insert(pre + s - 1, par, m.attName(s), m.attValue(s));
    }
  }

  /**
   * Renames the specified node.
   * @param pre pre value
   * @param name new name
   * @param data data reference
   */
  public static void rename(final int pre, final QNm name, final Data data) {
    // passed on pre value must refer to element, pi or attribute node
    final int k = data.kind(pre);
    // [LK] update methods should consider namespace, defined in QName (name)
    if(k == Data.ELEM) {
      data.update(pre, name.str());
    } else if(k == Data.PI) {
      byte[] val = data.text(pre);
      final int i = indexOf(val, ' ');
      data.update(pre, i == -1 ? name.str() :
        concat(name.str(), SPACE, substring(val, i + 1)));
    } else {
      data.update(pre, name.str(), data.attValue(pre));
    }
  }

  /**
   * Builds new data instance from iterator.
   * @param ch sequence iterator
   * @param d data reference for indices
   * @return new data instance
   * @throws QueryException query exception
   */
  public static Data buildDB(final Iter ch, final Data d)
      throws QueryException {

    // [LK] usage of index refs only possible if target node is a dbnode,
    // because insert/replace etc. nodes can be mixed up (DBNode, FNode ...)
    MemData m = d == null ? new MemData(20, new Names(), new Names(),
        new Namespaces(), new PathSummary(), new Prop()) : new MemData(20, d);

    // [LK] BuildDB.. namespaces must be copied as well
    //m.ns.add(...);
    
    // determine size of sequence
    int ds = 1;
    Item n;
    while((n = ch.next()) != null) {
      if(n instanceof DBNode) {
        final DBNode dbn = (DBNode) n;
        ds += dbn.data.size(dbn.pre, Nod.kind(n.type));
      } else if(n instanceof FNode) {
        ds += fragmentSize((FNode) n, false);
      }
    }
    // add root node for data instance
    m.addDoc(EMPTY, ds);

    // add nodes as children
    int pre = 1;
    ch.reset();
    while((n = ch.next()) != null) {
      if(n instanceof DBNode) {
        pre = addDBNode((DBNode) n, m, pre, 0);
      } else if(n instanceof FNode) {
        pre = addFragment((FNode) n, m, pre, 0);
      }
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
  private static int fragmentSize(final Nod n, final boolean attr)
      throws QueryException {

    int s = 1;
    final NodeIter at = n.attr();
    while(at.next() != null) s++;
    if(attr) return s;

    final NodeIter it = n.child();
    Nod i;
    while((i = it.next()) != null) s += fragmentSize(i, false);
    return s;
  }

  /**
   * Adds a fragment to a data instance.
   * @param n node to be added
   * @param m data reference
   * @param pval node position
   * @param par node parent
   * @return pre value of next node
   * @throws QueryException query exception
   */
  private static int addFragment(final Nod n, final MemData m,
      final int pval, final int par) throws QueryException {

    final int k = Nod.kind(n.type);
    int pre = pval;
    // add node
    switch(k) {
      case Data.ELEM:
        final int as = fragmentSize(n, true);
        final int s = fragmentSize(n, false);
        m.addElem(m.tags.index(n.nname(), null, false),
            0, pre - par, as, s, false);
        pre++;
        break;
      case Data.ATTR:
        // needed to build data instance from attribute sequence
        m.addAtt(m.atts.index(n.nname(), null, false), 0, n.str(), pre - par);
        return ++pre;
      case Data.PI:
        final byte[] nm = n.nname();
        final byte[] vl = n.str();
        m.addText(vl.length == 0 ? nm : concat(nm, SPACE, vl), pre - par, k);
        return ++pre;
      case Data.TEXT:
      case Data.COMM:
        m.addText(n.str(), pre - par, k);
        return ++pre;
    }

    // add attributes and children if n is element
    NodeIter ir = n.attr();
    Nod i;
    while((i = ir.next()) != null) {
      m.addAtt(m.atts.index(i.nname(), null, false), 0, i.str(), pre++ - pval);
    }
    ir = n.child();
    while((i = ir.next()) != null) pre = addFragment(i, m, pre, pval);
    return pre;
  }

  /**
   * Adds a database node to a {@link MemData} instance.
   * @param n node to be added
   * @param m data reference
   * @param pval node position
   * @param par node parent
   * @return pre value of next node
   * @throws QueryException query exception
   */
  private static int addDBNode(final DBNode n, final MemData m,
      final int pval, final int par) throws QueryException {

    // [LK] copy nodes directly from table - see copy() method below
    final Data data = n.data;
    final int k = Nod.kind(n.type);
    int pre = pval;
    // [LK] type DOC? --- not possible --- DOC nodes are replaced by children,
    // see specification insert/replace - check though!
    switch(k) {
      case Data.ELEM:
        m.addElem(m.tags.index(n.nname(), null, false),
            0, pre - par, data.attSize(n.pre, k), data.size(n.pre, k), false);
        pre++;
        break;
      case Data.ATTR:
        // needed to build data instance from attribute sequence
        m.addAtt(m.atts.index(n.nname(), null, false), 0, n.str(), pre - par);
        return ++pre;
      case Data.PI:
        final byte[] nm = n.nname();
        final byte[] vl = n.str();
        m.addText(vl.length == 0 ? nm : concat(nm, SPACE, vl), pre - par, k);
        return ++pre;
      case Data.TEXT:
      case Data.COMM:
        m.addText(n.str(), pre - par, k);
        return ++pre;
    }

    // add attributes and children if n is element
    NodeIter ir = n.attr();
    Nod i;
    while((i = ir.next()) != null) {
      m.addAtt(m.atts.index(i.nname(), null, false), 0, i.str(), pre++ - pval);
    }
    ir = n.child();
    while((i = ir.next()) != null) pre = addDBNode((DBNode) i, m, pre, pval);
    return pre;
  }

  /**
   * Creates a memory data instance from the specified database and pre value.
   * @param data data reference
   * @param pre pre value
   * @return database instance
   */
  // [LK] need it or not? Maybe replace parts of addDBNode() with this one ...
  public static MemData copy(final Data data, final int pre) {
    // size of the data instance
    final int size = data.size(pre, data.kind(pre));
    // create temporary data instance, adopting the indexes of the source data
    final MemData tmp = new MemData(size, data.tags, data.atts, data.ns,
        data.path, data.meta.prop);

    // copy all nodes
    for(int p = pre; p < pre + size; p++) {
      final int k = data.kind(p);
      final int d = p - data.parent(p, k);
      switch(k) {
        case Data.DOC:
          tmp.addDoc(data.text(p), data.size(p, k));
          break;
        case Data.ELEM:
          tmp.addElem(data.tagID(p), data.tagNS(p), d, data.attSize(p, k),
              data.size(p, k), data.ns(p).length != 0);
          break;
        case Data.ATTR:
          tmp.addAtt(data.attNameID(p), data.attNS(p), data.attValue(p), d);
          break;
        case Data.TEXT:
        case Data.COMM:
        case Data.PI:
          tmp.addText(data.text(p), d, k);
          break;
      }
    }
    return tmp;
  }  
}
