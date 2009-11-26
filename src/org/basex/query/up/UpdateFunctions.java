package org.basex.query.up;

import static org.basex.util.Token.*;
import java.util.HashSet;
import java.util.Set;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FTxt;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
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
   * Blabooo.
   * @param at ?
   * @param r ?
   * @param m ?
   * @return ?
   */
  public static boolean checkAttNames(final NodIter at, final NodIter r,
      final String m) {
    final Set<String> s = new HashSet<String>();
    Nod n = at.next();

    while(n != null) {
      s.add(string(n.nname()));
      n = at.next();
    }

    n = r.next();
    while(n != null) {
      final String t = string(n.nname());
      final boolean b = s.add(t);
      if(!b) {
        if(m == null) return false;
        if(!t.equals(m)) return false;
      }
      n = r.next();
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
    final int ss = m.meta.size;
    for(int s = 0; s < ss; s++) {
      d.insert(pre + s, par, m.attName(s), m.attValue(s));
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
      final byte[] val = data.text(pre);
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
  public static Data buildDB(final NodIter ch, final Data d)
      throws QueryException {

    // [LK] usage of index refs only possible if target node is a dbnode,
    // because insert/replace etc. nodes can be mixed up (DBNode, FNode ...)
    final MemData md = d == null ? new MemData(new Prop(false)) :
      new MemData(16, d);

    int pre = 1;
    Nod n;
    while((n = ch.next()) != null) pre = addFragment(n, md, pre, 0);
    return md;
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

  /**
   * Adds a fragment to a data instance.
   * @param n node to be added
   * @param m data reference
   * @param pre node position
   * @param par node parent
   * @return pre value of next node
   * @throws QueryException query exception
   */
  private static int addFragment(final Nod n, final MemData m,
      final int pre, final int par) throws QueryException {

    final int k = Nod.kind(n.type);
    switch(k) {
      case Data.ATTR:
        m.addAtt(m.atts.index(n.nname(), null, false), 0, n.str(), pre - par);
        return pre + 1;
      case Data.PI:
        final byte[] nm = n.nname();
        final byte[] vl = n.str();
        m.addText(vl.length == 0 ? nm : concat(nm, SPACE, vl), pre - par, k);
        return pre + 1;
      case Data.TEXT:
      case Data.COMM:
        m.addText(n.str(), pre - par, k);
        return pre + 1;
      default:
        // no document nodes will occur at this point..
        m.addElem(m.tags.index(n.nname(), null, false),
            0, pre - par, size(n, true), size(n, false), false);
        NodeIter ir = n.attr();
        Nod i;
        int p = pre + 1;
        while((i = ir.next()) != null) p = addFragment(i, m, p, pre);
        ir = n.child();
        while((i = ir.next()) != null) p = addFragment(i, m, p, pre);
        return p;
    }
  }
}
