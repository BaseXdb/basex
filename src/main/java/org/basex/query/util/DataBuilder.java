package org.basex.query.util;

import static org.basex.util.Token.*;
import org.basex.data.Data;
import org.basex.data.FTPos;
import org.basex.data.FTPosData;
import org.basex.data.MemData;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.iter.ItemIter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.util.Atts;
import org.basex.util.TokenBuilder;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTSpan;

/**
 * Class for building memory-based database nodes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DataBuilder {
  /** Target data instance. */
  private final MemData data;
  /** Full-text position data. */
  private FTPosData ftpos;
  /** Marker tag reference. */
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
   * @return self reference
   */
  public DataBuilder ftpos(final byte[] tag, final FTPosData pos) {
    marker = data.tags.index(tag, null, false);
    ftpos = pos;
    return this;
  }

  /**
   * Marks the full-text terms in the specified node and returns the new nodes.
   * @param nod input node
   * @param tag tag name
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  public static ItemIter mark(final Nod nod, final byte[] tag,
      final QueryContext ctx) throws QueryException {

    // copy node to main memory data instance
    final MemData md = new MemData(ctx.resource.context.prop);
    new DataBuilder(md).ftpos(tag, ctx.ftpos).build(nod);
    final ItemIter ir = new ItemIter();
    for(int p = 0; p < md.meta.size; p += md.size(p, md.kind(p))) {
      ir.add(new DBNode(md, p));
    }
    return ir;
  }

  /**
   * Fills the data instance with the specified node.
   * @param n node
   * @throws QueryException query exception
   */
  public void build(final Nod n) throws QueryException {
    build(new NodIter(new Nod[] { n }, 1));
  }

  /**
   * Fills the data instance with the specified nodes.
   * @param ni node iterator
   * @throws QueryException query exception
   */
  public void build(final NodIter ni) throws QueryException {
    int pre = 1;
    Nod n;
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
   * @throws QueryException query exception
   */
  private int addNode(final Nod nd, final int pre,
      final int par, final Nod ndPar) throws QueryException {

    switch(nd.type) {
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
   * @throws QueryException query exception
   */
  private int addDoc(final Nod nd, final int pre) throws QueryException {
    final int ms = data.meta.size;
    data.doc(ms, size(nd, false), nd.base());
    data.insert(ms);
    int p = pre + 1;
    final NodeIter ir = nd.child();
    Nod ch;
    while((ch = ir.next()) != null) p = addNode(ch, p, pre, null);
    return p;
  }

  /**
   * Adds an attribute.
   * @param nd node to be added
   * @param pre pre reference
   * @param par parent reference
   * @return number of added nodes
   */
  private int addAttr(final Nod nd, final int pre, final int par) {
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
  private int addText(final Nod nd, final int pre, final int par,
      final Nod ndPar) {

    final byte[] val = nd.atom();
    int dist = pre - par;

    // check full-text mode
    if(ftpos == null || !(nd instanceof DBNode)) return addText(val, dist);

    // check if full-text data exists for the current node
    final FTPos ftp = ftpos.get(((DBNode) nd).pre);
    if(ftp == null) return addText(val, dist);

    // adopt namespace from parent
    final int u = ndPar != null ? data.ns.uri(ndPar.nname(), true) : 0;

    int ins = 0;
    boolean marked = false;
    final TokenBuilder tb = new TokenBuilder();
    final FTLexer lex = new FTLexer().sc().init(val);
    while(lex.hasNext()) {
      final FTSpan span = lex.next();
      // check if one of the conditions is true
      if(ftp.contains(span.pos) ^ marked ^ (marked && span.special)) {
        if(tb.size() != 0) {
          // write current text node
          ins += addText(tb.finish(), marked ? 1 : dist);
          tb.reset();
          dist++;
        }
        if(!marked) {
          // open element
          data.elem(dist++, marker, 1, 2, u, false);
          data.insert(data.meta.size);
          ins++;
        }
        marked ^= true;
      }
      // collect data
      tb.add(span.text);
    }
    if(tb.size() != 0) {
      // write last text node
      ins += addText(tb.finish(), marked ? 1 : dist);
    } else {
      // check if this can happen at all
      System.out.println("???????");
    }
    return ins;
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
  private int addPI(final Nod nd, final int pre, final int par) {
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
  private int addComm(final Nod nd, final int pre, final int par) {
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
   * @throws QueryException query exception
   */
  private int addElem(final Nod nd, final int pre, final int par,
      final Nod ndPar) throws QueryException {

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

    // remove duplicate namespace bindings
    if(ns != null) {
      if(ns.size > 0 && ndPar != null && preserve) {
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
    }
    // add new namespaces
    for(int a = 0; ne && a < ns.size; ++a)
      data.ns.add(ns.key[a], ns.val[a], ms);

    final byte[] uri = q.uri().atom();
    final int u = uri.length != 0 ? data.ns.addURI(uri) : 0;
    final int tn = data.tags.index(q.atom(), null, false);
    final int s = size(nd, false);

    // add element node
    data.elem(pre - par, tn, size(nd, true), s, u, ne);
    data.insert(ms);

    final int pp = pre;
    int p = pre + 1;
    Nod ch;

    // add attributes
    NodeIter ir = nd.attr();
    while((ch = ir.next()) != null) p = addNode(ch, p, pre, nd);

    // add children
    ir = nd.child();
    while((ch = ir.next()) != null) p = addNode(ch, p, pre, nd);
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
    while(ch.next() != null) ++s;
    if(!a) {
      ch = n.child();
      Nod i;
      while((i = ch.next()) != null) s += size(i, a);
    }
    return s;
  }
}
