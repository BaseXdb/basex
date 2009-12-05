package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FElem;
import org.basex.query.item.FTxt;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.util.Err;
import org.basex.util.Atts;
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
   * Merges all adjacent text nodes in the given sequence.
   * @param n iterator
   * @return iterator with merged text nodes
   */
  public static NodIter mergeText(final NodIter n) {
    final NodIter s = new NodIter();
    Nod i = n.next();
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
    if(d.kind(a) != Data.TEXT || d.kind(b) != Data.TEXT) return false;
    if(d.parent(a, Data.TEXT) != d.parent(b, Data.TEXT)) return false;

    d.replace(a, Data.TEXT, concat(d.text(a, true), d.text(b, true)));
    d.delete(b);
    return true;
  }

  /**
   * Checks for namespace conflicts.
   * @param aList attribute list
   * @throws QueryException query exception
   */
  public static void checkNS(final NodIter aList) throws QueryException {
    final Atts at = new Atts();
    for(int a = 0; a < aList.size(); a++) {
      final Nod n = aList.get(a);
      if(n.type != Type.ATT) continue;
      final QNm name = n.qname();
      final byte[] an = name.pref();
      int ai = at.get(name.pref());
      if(ai == -1) {
        at.add(an, name.uri.str());
      } else if(!eq(name.uri.str(), at.val[ai])) {
        Err.or(UPNSCONFL2);
      }
    }
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

    final int ss = m.meta.size;
    for(int s = 0; s < ss; s++) {
      d.insertAttr(pre + s, par, m.name(s, false), m.text(s, false),
          m.uri(s, Data.ATTR));
    }
  }

  /**
   * Builds new data instance from iterator.
   * @param ch sequence iterator
   * @param data memory data reference
   * @return new data instance
   * @throws QueryException query exception
   */
  public static Data buildDB(final NodIter ch, final MemData data)
      throws QueryException {
    int pre = 1;
    Nod n;
    while((n = ch.next()) != null) pre = addNode(n, data, pre, 0);
    return data;
  }

  /**
   * Adds a fragment to a database instance.
   * Document nodes are ignored.
   * @param nd node to be added
   * @param md data reference
   * @param pre node position
   * @param par node parent
   * @return pre value of next node
   * @throws QueryException query exception
   */
  private static int addNode(final Nod nd, final MemData md,
      final int pre, final int par) throws QueryException {

    final int k = Nod.kind(nd.type);
    switch(k) {
      case Data.DOC:
        md.insertDoc(md.meta.size, size(nd, false), nd.base());
        int p = pre + 1;
        NodeIter ir = nd.child();
        Nod i;
        while((i = ir.next()) != null) p = addNode(i, md, p, pre);
        return p;
      case Data.ATTR:
        QNm q = nd.qname();
        byte[] uri = q.uri.str();
        int u = 0;
        if(uri.length != 0) u = Math.abs(md.ns.addURI(uri));
        final int n = md.atts.index(q.str(), null, false);
        md.insertAttr(md.meta.size, pre - par, n, nd.str(), u);
        return pre + 1;
      case Data.PI:
        final byte[] v = trim(concat(nd.nname(), SPACE, nd.str()));
        md.insertText(md.meta.size, pre - par, v, k);
        return pre + 1;
      case Data.TEXT:
      case Data.COMM:
        md.insertText(md.meta.size, pre - par, nd.str(), k);
        return pre + 1;
      default:
        q = nd.qname();
        //u = 0;
        if(par == 0) {
          final Atts ns = FElem.ns(nd);
          for(int a = 0; a < ns.size; a++) md.ns.add(ns.key[a], ns.val[a]);
        }
        final boolean ne = md.ns.open(md.meta.size);
        uri = q.uri.str();
        u = uri.length != 0 ? Math.abs(md.ns.addURI(uri)) : 0;
        final int tn = md.tags.index(q.str(), null, false);
        // [CG] missing: ne/u
        md.insertElem(md.meta.size, pre - par, tn, size(nd, true),
            size(nd, false), u, ne);
        ir = nd.attr();
        p = pre + 1;
        while((i = ir.next()) != null) p = addNode(i, md, p, pre);
        ir = nd.child();
        while((i = ir.next()) != null) p = addNode(i, md, p, pre);
        return p;
    }
  }

  /**
   * Determines the number of descendants of a fragment.
   * @param n fragment node
   * @param a count attribute size of node
   * @return number of descendants + 1 or attribute size + 1
   * @throws QueryException query exception
   */
  private static int size(final Nod n, final boolean a) throws QueryException {
    if(n instanceof DBNode) {
      final DBNode dbn = (DBNode) n;
      final int k = Nod.kind(n.type);
      return a ? dbn.data.attSize(dbn.pre, k) : dbn.data.size(dbn.pre, k);
    }

    int s = 1;
    NodeIter ch = n.attr();
    while(ch.next() != null) s++;
    if(!a) {
      ch = n.child();
      Nod i;
      while((i = ch.next()) != null) s += size(i, a);
    }
    return s;
  }
}
