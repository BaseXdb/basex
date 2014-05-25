package org.basex.query.util;

import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Class for building memory-based database nodes.
 *
 * @author BaseX Team 2005-14, BSD License
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
  public DataBuilder ftpos(final byte[] tag, final FTPosData pos, final int len) {
    ftbuilder = new DataFTBuilder(pos, len);
    marker = data.tagindex.index(tag, null, false);
    return this;
  }

  /**
   * Fills the data instance with the specified node.
   * @param node node
   */
  public void build(final ANode node) {
    build(new ANodeList(node));
  }

  /**
   * Fills the data instance with the specified nodes.
   * @param nodes node list
   */
  public void build(final ANodeList nodes) {
    data.meta.update();
    int ds = data.meta.size;
    for(final ANode n : nodes) ds = addNode(n, ds, -1, null);
  }

  /**
   * Adds a fragment to a database instance.
   * Document nodes are ignored.
   * @param node node to be added
   * @param pre node position
   * @param par node parent
   * @param pNode parent of node to be added
   * @return pre value of next node
   */
  private int addNode(final ANode node, final int pre, final int par, final ANode pNode) {
    switch(node.nodeType()) {
      case DOC: return addDoc(node, pre);
      case ELM: return addElem(node, pre, par);
      case TXT: return addText(node, pre, par, pNode);
      case ATT: return addAttr(node, pre, par);
      case COM: return addComm(node, pre, par);
      // will always be processing instruction
      default:  return addPI(node, pre, par);
    }
  }

  /**
   * Adds a document node.
   * @param node node to be added
   * @param pre pre reference
   * @return pre value of next node
   */
  private int addDoc(final ANode node, final int pre) {
    final int ds = data.meta.size;
    final int s = size(node, false);
    data.doc(ds, s, node.baseURI());
    data.insert(ds);
    int p = pre + 1;
    final AxisIter ai = node.children();
    for(ANode ch; (ch = ai.next()) != null;) p = addNode(ch, p, pre, null);
    if(s != p - pre) data.size(ds, Data.DOC, p - pre);
    return p;
  }

  /**
   * Adds an attribute.
   * @param node node to be added
   * @param pre pre reference
   * @param par parent reference
   * @return number of added nodes
   */
  private int addAttr(final ANode node, final int pre, final int par) {
    final int ds = data.meta.size;
    final QNm q = node.qname();
    final byte[] uri = q.uri();
    int u = 0;
    if(uri.length != 0) {
      if(par == -1) data.nspaces.add(ds, pre + 1, q.prefix(), uri, data);
      u = data.nspaces.uri(uri);
    }
    final int n = data.atnindex.index(q.string(), null, false);
    // usually, attributes don't have a namespace flag.
    // this is different here, because a stand-alone attribute has no parent element.
    data.attr(ds, pre - par, n, node.string(), u, par == -1 && u != 0);
    data.insert(ds);
    return pre + 1;
  }

  /**
   * Adds a text node.
   * @param node node to be added
   * @param pre pre reference
   * @param par parent reference
   * @param pNode parent node
   * @return pre value of next node
   */
  private int addText(final ANode node, final int pre, final int par, final ANode pNode) {
    // check full-text mode
    final int dist = pre - par;
    final TokenList tl = ftbuilder != null ? ftbuilder.build(node) : null;
    if(tl == null) return pre + addText(node.string(), dist);

    // adopt namespace from parent
    ANode p = pNode;
    while(p != null) {
      final QNm n = p.qname();
      if(n != null && !n.hasPrefix()) break;
      p = p.parent();
    }
    int u = 0;
    if(p != null) u = data.nspaces.uri(p.name(), true);

    final int ts = tl.size();
    for(int t = 0; t < ts; t++) {
      byte[] text = tl.get(t);
      final boolean elem = text == null;
      if(elem) {
        // open element
        data.elem(dist + t, marker, 1, 2, u, false);
        data.insert(data.meta.size);
        text = tl.get(++t);
      }
      addText(text, elem ? 1 : dist + t);
    }
    return pre + ts;
  }

  /**
   * Adds a text.
   * @param text text node
   * @param dist distance
   * @return number of added nodes
   */
  private int addText(final byte[] text, final int dist) {
    final int ds = data.meta.size;
    data.text(ds, dist, text, Data.TEXT);
    data.insert(ds);
    return 1;
  }

  /**
   * Adds a processing instruction.
   * @param node node to be added
   * @param pre pre reference
   * @param par parent reference
   * @return number of added nodes
   */
  private int addPI(final ANode node, final int pre, final int par) {
    final int ds = data.meta.size;
    final byte[] v = trim(concat(node.name(), SPACE, node.string()));
    data.text(ds, pre - par, v, Data.PI);
    data.insert(ds);
    return pre + 1;
  }

  /**
   * Adds a comment.
   * @param node node to be added
   * @param pre pre reference
   * @param par parent reference
   * @return number of added nodes
   */
  private int addComm(final ANode node, final int pre, final int par) {
    final int ds = data.meta.size;
    data.text(ds, pre - par, node.string(), Data.COMM);
    data.insert(ds);
    return pre + 1;
  }

  /**
   * Adds an element node.
   * @param node node to be added
   * @param pre pre reference
   * @param par parent reference
   * @return pre value of next node
   */
  private int addElem(final ANode node, final int pre, final int par) {
    final int ds = data.meta.size;

    // add new namespaces
    data.nspaces.prepare();
    final Atts ns = par == -1 ? node.nsScope() : node.namespaces();
    final int nl = ns.size();
    for(int n = 0; n < nl; n++) data.nspaces.add(ns.name(n), ns.value(n), ds);

    // analyze node name
    final QNm name = node.qname();
    final int tn = data.tagindex.index(name.string(), null, false);
    final int s = size(node, false);
    final int u = data.nspaces.uri(name.uri());

    // add element node
    data.elem(pre - par, tn, size(node, true), s, u, nl != 0);
    data.insert(ds);

    // add attributes and children
    int p = pre + 1;
    AxisIter ai = node.attributes();
    for(ANode ch; (ch = ai.next()) != null;) p = addAttr(ch, p, pre);
    ai = node.children();
    for(ANode ch; (ch = ai.next()) != null;) p = addNode(ch, p, pre, node);
    data.nspaces.close(ds);

    // update size if additional nodes have been added by the descendants
    if(s != p - pre) data.size(ds, Data.ELEM, p - pre);
    return p;
  }

  /**
   * Determines the number of descendants of a fragment.
   * @param node fragment node
   * @param att count attributes instead of elements
   * @return number of descendants + 1 or attribute size + 1
   */
  private static int size(final ANode node, final boolean att) {
    if(node instanceof DBNode) {
      final DBNode dbn = (DBNode) node;
      final int k = node.kind();
      return att ? dbn.data.attSize(dbn.pre, k) : dbn.data.size(dbn.pre, k);
    }

    int s = 1;
    AxisIter ai = node.attributes();
    while(ai.next() != null) ++s;
    if(!att) {
      ai = node.children();
      for(ANode i; (i = ai.next()) != null;) s += size(i, att);
    }
    return s;
  }

  /**
   * Returns a new node without the specified namespace.
   * @param node node to be copied
   * @param ns namespace to be stripped
   * @param ctx database context
   * @return new node
   */
  public static ANode stripNS(final ANode node, final byte[] ns, final Context ctx) {
    if(node.type != NodeType.ELM) return node;

    final MemData md = new MemData(ctx.options);
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
      final byte[] uri = md.nspaces.uri(md.uri(pre, kind));
      if(uri == null || !eq(uri, ns)) continue;

      final byte[] name = md.name(pre, kind);
      if(prefix(name).length == 0) {
        // no prefix: remove namespace from element
        if(kind == Data.ELEM) {
          md.update(pre, Data.ELEM, name, EMPTY);
          md.nsFlag(pre, false);
        }
      } else {
        // prefix: retain namespace
        del = false;
      }
    }
    if(del) md.nspaces.delete(ns);
    return new DBNode(md);
  }
}
