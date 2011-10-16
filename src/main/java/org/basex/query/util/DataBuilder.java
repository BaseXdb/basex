package org.basex.query.util;

import static org.basex.util.Token.*;

import org.basex.data.Data;
import org.basex.data.FTPosData;
import org.basex.data.MemData;
import org.basex.query.QueryContext;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.NodeCache;
import org.basex.util.Atts;
import org.basex.util.list.TokenList;

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

  /**
   * Constructor.
   * @param md target data
   */
  public DataBuilder(final MemData md) {
    data = md;
  }

  /**
   * Attaches full-text position data.
   * @param tag name of marker tag
   * @param pos full-text position data
   * @param len length of extract
   * @return self reference
   */
  public DataBuilder ftpos(final byte[] tag, final FTPosData pos,
      final int len) {
    ftbuilder = new DataFTBuilder(pos, len);
    marker = data.tagindex.index(tag, null, false);
    return this;
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
   * @param nc node iterator
   */
  public void build(final NodeCache nc) {
    int pre = 1;
    for(ANode n; (n = nc.next()) != null;) pre = addNode(n, pre, 0, null);
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

    switch(nd.nodeType()) {
      case DOC: return addDoc(nd, pre);
      case ELM: return addElem(nd, pre, par);
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
    data.doc(size(nd, false), nd.baseURI());
    data.insert(ms);
    int p = pre + 1;
    final AxisIter ai = nd.children();
    for(ANode ch; (ch = ai.next()) != null;) p = addNode(ch, p, pre, null);
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
    final byte[] uri = q.uri();
    int u = 0;
    final boolean ne = uri.length != 0;
    if(ne) {
      if(par == 0) data.ns.add(ms, pre - par, q.prefix(), uri);
      u = data.ns.addURI(uri);
    }
    final int n = data.atnindex.index(q.string(), null, false);
    // attribute namespace flag is only set in main memory instance
    data.attr(pre - par, n, nd.string(), u, ne);
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
    if(tl == null) return addText(nd.string(), dist);

    // adopt namespace from parent
    final int u = ndPar != null ? data.ns.uri(ndPar.name(), true) : 0;

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
    data.text(dist, txt, Data.TEXT);
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
    final byte[] v = trim(concat(nd.name(), SPACE, nd.string()));
    data.text(pre - par, v, Data.PI);
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
    data.text(pre - par, nd.string(), Data.COMM);
    data.insert(ms);
    return 1;
  }

  /**
   * Adds an element node.
   * @param nd node to be added
   * @param pre pre reference
   * @param par parent reference
   * @return number of added nodes
   */
  private int addElem(final ANode nd, final int pre, final int par) {
    final int ms = data.meta.size;
    data.ns.open();

    // add new namespaces
    final Atts ns = nd.nsScope();
    final boolean ne = ns.size() > 0;
    for(int a = 0, as = ns.size(); a < as; a++)
      data.ns.add(ns.name(a), ns.string(a), ms);

    final QNm q = nd.qname();
    final byte[] uri = q.uri();
    final int u = uri.length != 0 ? data.ns.addURI(uri) : 0;
    final int tn = data.tagindex.index(q.string(), null, false);
    final int s = size(nd, false);

    // add element node
    data.elem(pre - par, tn, size(nd, true), s, u, ne);
    data.insert(ms);

    final int pp = pre;
    int p = pre + 1;

    // add attributes
    AxisIter ai = nd.attributes();
    for(ANode ch; (ch = ai.next()) != null;) p = addNode(ch, p, pre, nd);

    // add children
    ai = nd.children();
    for(ANode ch; (ch = ai.next()) != null;) p = addNode(ch, p, pre, nd);
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
      final int k = n.kind();
      return a ? dbn.data.attSize(dbn.pre, k) : dbn.data.size(dbn.pre, k);
    }

    int s = 1;
    AxisIter ai = n.attributes();
    while(ai.next() != null) ++s;
    if(!a) {
      ai = n.children();
      for(ANode i; (i = ai.next()) != null;) s += size(i, a);
    }
    return s;
  }

  /**
   * Returns a new node without the specified namespace.
   * @param node node to be copied
   * @param ns namespace to be stripped
   * @param ctx query context
   * @return new node
   */
  public static ANode stripNS(final ANode node, final byte[] ns,
      final QueryContext ctx) {
    if(node.type != NodeType.ELM) return node;

    final MemData md = new MemData(ctx.context.prop);
    final DataBuilder db = new DataBuilder(md);
    db.build(node);

    // flag indicating if namespace should be completely removed
    boolean del = true;
    // loop through all nodes
    for(int pre = 0; pre < md.meta.size; pre++) {
      // only check elements and attributes
      final int kind = md.kind(pre);
      if(kind != Data.ELEM && kind != Data.ATTR) continue;
      // check if namespace is referenced
      final byte[] uri = md.ns.uri(md.uri(pre, kind));
      if(uri == null || !eq(uri, ns)) continue;

      final byte[] name = md.name(pre, kind);
      if(prefix(name).length == 0) {
        // no prefix: remove namespace from element
        if(kind == Data.ELEM) {
          md.update(pre, kind, name, EMPTY);
          md.nsFlag(pre, false);
        }
      } else {
        // prefix: retain namespace
        del = false;
      }
    }
    if(del) md.ns.delete(ns);
    return new DBNode(md, 0);
  }
}
