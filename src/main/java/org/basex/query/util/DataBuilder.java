package org.basex.query.util;

import static org.basex.util.Token.*;
import org.basex.data.Data;
import org.basex.data.FTPosData;
import org.basex.data.MemData;
import org.basex.query.QueryContext;
import org.basex.query.item.DBNode;
import org.basex.query.item.ANode;
import org.basex.query.item.QNm;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.AxisIter;
import org.basex.util.Atts;
import org.basex.util.TokenList;

/**
 * Class for building memory-based database nodes.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DataBuilder {
  /** Target data instance. */
  private final MemData data;
  /** Full-text position data. */
  private DataFTBuilder ftbuilder;
  /** Index reference of marker tag. */
  private int marker;
  /** Preserve flag. */
  private boolean preserve = true;
  /** Inherit flag. */
  private final boolean inherit = true;

  /**
   * Constructor.
   * @param md target data
   */
  public DataBuilder(final MemData md) {
    data = md;
  }

  /**
   * Attaches flags of the query context.
   * @param ctx query context
   * @return self reference
   */
  public DataBuilder context(final QueryContext ctx) {
    preserve = ctx.nsPreserve;
    //inherit = ctx.nsInherit;
    return this;
  }

  /**
   * Attaches full-text position data.
   * @param tag name of marker tag
   * @param pos full-text position data
   * @param len length of extract
   * @return self reference
   */
  private DataBuilder ftpos(final byte[] tag, final FTPosData pos,
      final int len) {
    ftbuilder = new DataFTBuilder(pos, len);
    marker = data.tags.index(tag, null, false);
    return this;
  }

  /**
   * Marks the full-text terms in the specified node and returns the new nodes.
   * @param node input node
   * @param tag tag name
   * @param len length of extract
   * @param ctx query context
   * @return resulting value
   */
  public static ItemCache mark(final ANode node, final byte[] tag,
      final int len, final QueryContext ctx) {

    // copy node to main memory data instance
    final MemData md = new MemData(ctx.context.prop);
    new DataBuilder(md).ftpos(tag, ctx.ftpos, len).build(node);
    final ItemCache ir = new ItemCache();
    for(int p = 0; p < md.meta.size; p += md.size(p, md.kind(p))) {
      ir.add(new DBNode(md, p));
    }
    return ir;
  }

  /**
   * Fills the data instance with the specified node.
   * @param n node
   */
  public void build(final ANode n) {
    build(new NodeCache(new ANode[] { n }, 1));
  }

  /**
   * Fills the data instance with the specified nodes.
   * @param ni node iterator
   */
  public void build(final NodeCache ni) {
    int pre = 1;
    ANode n;
    while((n = ni.next()) != null) pre = addNode(n, pre, 0, null);
  }

  /**
   * Adds a fragment to a database instance.
   * Document nodes are ignored.
   * @param nd node to be added
   * @param pre node position
   * @param par node parent
   * @return pre value of next node
   * @param ndPar parent of node to be added
   */
  private int addNode(final ANode nd, final int pre, final int par,
      final ANode ndPar) {

    switch(nd.ndType()) {
      case DOC: return addDoc(nd, pre);
      case ELM: return addElem(nd, pre, par, ndPar);
      case TXT: return pre + addText(nd, pre, par, ndPar);
      case ATT: return pre + addAttr(nd, pre, par);
      case COM: return pre + addComm(nd, pre, par);
      // will always be processing instruction
      default:  return pre + addPI(nd, pre, par);
    }
  }

  /**
   * Adds a document node.
   * @param nd node to be added
   * @param pre pre reference
   * @return number of added nodes
   */
  private int addDoc(final ANode nd, final int pre) {
    final int ms = data.meta.size;
    data.doc(ms, size(nd, false), nd.base());
    data.insert(ms);
    int p = pre + 1;
    final AxisIter ai = nd.children();
    ANode ch;
    while((ch = ai.next()) != null) p = addNode(ch, p, pre, null);
    return p;
  }

  /**
   * Adds an attribute.
   * @param nd node to be added
   * @param pre pre reference
   * @param par parent reference
   * @return number of added nodes
   */
  private int addAttr(final ANode nd, final int pre, final int par) {
    final int ms = data.meta.size;
    final QNm q = nd.qname();
    final byte[] uri = q.uri().atom();
    int u = 0;
    final boolean ne = uri.length != 0;
    if(ne) {
      if(par == 0) data.ns.add(ms, pre - par, q.pref(), uri);
      u = data.ns.addURI(uri);
    }
    final int n = data.atts.index(q.atom(), null, false);
    // attribute namespace flag is only set in main memory instance
    data.attr(ms, pre - par, n, nd.atom(), u, ne);
    data.insert(ms);
    return 1;
  }

  /**
   * Adds a text node.
   * @param nd node to be added
   * @param pre pre reference
   * @param par parent reference
   * @param ndPar parent node
   * @return number of added nodes
   */
  private int addText(final ANode nd, final int pre, final int par,
      final ANode ndPar) {

    // check full-text mode
    final int dist = pre - par;
    final TokenList tl = ftbuilder != null ? ftbuilder.build(nd) : null;
    if(tl == null) return addText(nd.atom(), dist);

    // adopt namespace from parent
    final int u = ndPar != null ? data.ns.uri(ndPar.nname(), true) : 0;

    for(int i = 0; i < tl.size(); i++) {
      byte[] text = tl.get(i);
      final boolean elem = text == null;
      if(elem) {
        // open element
        data.elem(dist + i, marker, 1, 2, u, false);
        data.insert(data.meta.size);
        text = tl.get(++i);
      }
      addText(text, elem ? 1 : dist + i);
    }
    return tl.size();
  }

  /**
   * Adds a text.
   * @param txt text node
   * @param dist distance
   * @return number of added nodes
   */
  private int addText(final byte[] txt, final int dist) {
    final int ms = data.meta.size;
    data.text(ms, dist, txt, Data.TEXT);
    data.insert(ms);
    return 1;
  }

  /**
   * Adds a processing instruction.
   * @param nd node to be added
   * @param pre pre reference
   * @param par parent reference
   * @return number of added nodes
   */
  private int addPI(final ANode nd, final int pre, final int par) {
    final int ms = data.meta.size;
    final byte[] v = trim(concat(nd.nname(), SPACE, nd.atom()));
    data.text(ms, pre - par, v, Data.PI);
    data.insert(ms);
    return 1;
  }

  /**
   * Adds a comment.
   * @param nd node to be added
   * @param pre pre reference
   * @param par parent reference
   * @return number of added nodes
   */
  private int addComm(final ANode nd, final int pre, final int par) {
    final int ms = data.meta.size;
    data.text(ms, pre - par, nd.atom(), Data.COMM);
    data.insert(ms);
    return 1;
  }

  /**
   * Adds an element node.
   * @param nd node to be added
   * @param pre pre reference
   * @param par parent reference
   * @param ndPar parent node
   * @return number of added nodes
   */
  private int addElem(final ANode nd, final int pre, final int par,
      final ANode ndPar) {

    final int ms = data.meta.size;
    final QNm q = nd.qname();
    data.ns.open();
    boolean ne = false;

    // [LK] copy-namespaces no-preserve
    // best way to determine necessary descendant ns bindings?
    Atts ns = null;
    if(!preserve) {
      ns = nd.ns();
      // detect default namespace bindings on ancestor axis of nd and
      // remove them to avoid duplicates
      final Atts ns2 = nd.nsScope(inherit);
      int uid;
      if((uid = ns2.get(EMPTY)) != -1) ns.add(ns2.key[uid], ns2.val[uid]);
    } else {
      ns = par == 0 ? nd.nsScope(inherit) : nd.ns();
    }

    if(ns != null) {
      if(ns.size > 0 && ndPar != null && preserve) {
        // remove duplicate namespace bindings
        final Atts nsPar = ndPar.nsScope(inherit);
        for(int j = 0; j < nsPar.size; ++j) {
          final byte[] key = nsPar.key[j];
          final int ki = ns.get(key);
          // check if prefix (or empty prefix) is already indexed and if so
          // check for different URIs. If the URIs are different the
          // prefix must be added to the index
          if(ki > -1 && eq(nsPar.val[j], ns.val[ki])) ns.delete(ki);
        }
      }
      ne = ns.size > 0;

      // add new namespaces
      for(int a = 0; ne && a < ns.size; ++a)
        data.ns.add(ns.key[a], ns.val[a], ms);
    }

    final byte[] uri = q.uri().atom();
    final int u = uri.length != 0 ? data.ns.addURI(uri) : 0;
    final int tn = data.tags.index(q.atom(), null, false);
    final int s = size(nd, false);

    // add element node
    data.elem(pre - par, tn, size(nd, true), s, u, ne);
    data.insert(ms);

    final int pp = pre;
    int p = pre + 1;
    ANode ch;

    // add attributes
    AxisIter ai = nd.atts();
    while((ch = ai.next()) != null) p = addNode(ch, p, pre, nd);

    // add children
    ai = nd.children();
    while((ch = ai.next()) != null) p = addNode(ch, p, pre, nd);
    data.ns.close(ms);

    // update size if additional nodes have been added by the descendants
    if(s != p - pp) data.size(ms, Data.ELEM, p - pp);
    return p;
  }

  /**
   * Determines the number of descendants of a fragment.
   * @param n fragment node
   * @param a count attribute size of node
   * @return number of descendants + 1 or attribute size + 1
   */
  private static int size(final ANode n, final boolean a) {
    if(n instanceof DBNode) {
      final DBNode dbn = (DBNode) n;
      final int k = ANode.kind(n.ndType());
      return a ? dbn.data.attSize(dbn.pre, k) : dbn.data.size(dbn.pre, k);
    }

    int s = 1;
    AxisIter ai = n.atts();
    while(ai.next() != null) ++s;
    if(!a) {
      ai = n.children();
      ANode i;
      while((i = ai.next()) != null) s += size(i, a);
    }
    return s;
  }
}
