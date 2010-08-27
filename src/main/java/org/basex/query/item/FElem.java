package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.iter.NodIter;
import org.basex.query.util.NSGlobal;
import org.basex.util.Atts;
import static org.basex.util.Token.*;
import org.basex.util.Token;
import org.basex.util.TokenMap;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * Element node fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FElem extends FNode {
  /** Namespaces. */
  private final Atts ns;
  /** Tag name. */
  private final QNm name;
  /** Base URI. */
  private final byte[] base;

  /**
   * Constructor.
   * @param n tag name
   * @param b base uri
   */
  public FElem(final QNm n, final byte[] b) {
    this(n, new NodIter(), new NodIter(), b, null);
  }

  /**
   * Constructor.
   * @param n tag name
   * @param ch children
   * @param at attributes
   * @param b base uri
   * @param nsp namespaces
   */
  public FElem(final QNm n, final NodIter ch, final NodIter at, final byte[] b,
      final Atts nsp) {
    this(n, ch, at, b, nsp, null);
  }

  /**
   * Constructor.
   * @param n tag name
   * @param ch children
   * @param p parent
   */
  public FElem(final QNm n, final NodIter ch, final Nod p) {
    this(n, ch, new NodIter(), EMPTY, new Atts(), p);
  }

  /**
   * Constructor.
   * @param n tag name
   * @param ch children
   * @param at attributes
   * @param b base uri
   * @param nsp namespaces
   * @param p parent
   */
  public FElem(final QNm n, final NodIter ch, final NodIter at, final byte[] b,
      final Atts nsp, final Nod p) {

    super(Type.ELM);
    name = n;
    children = ch;
    atts = at;
    base = b;
    ns = nsp;
    par = p;
  }

  /**
   * Constructor for DOM nodes (partial).
   * Provided by Erdal Karaca.
   * @param elem DOM node
   * @param p parent reference
   * @param nss namespaces in scope
   */
  FElem(final Element elem, final Nod p, final TokenMap nss) {
    super(Type.ELM);

    // general stuff
    final String nu = elem.getNamespaceURI();
    name = new QNm(token(elem.getNodeName()), nu == null ? EMPTY : token(nu));
    par = p;
    final String b = elem.getBaseURI();
    base = b == null ? EMPTY : token(b);

    // attributes and namespaces
    ns = new Atts();
    final NamedNodeMap at = elem.getAttributes();
    final int as = at.getLength();
    final Nod[] attArr = new Nod[as];

    int pos = 0;
    for(int i = 0; i < as; ++i) {
      final Attr att = (Attr) at.item(i);
      final byte[] nm = token(att.getName()), uri = token(att.getValue());
      if(Token.eq(nm, XMLNS)) {
        ns.add(EMPTY, uri);
      } else if(startsWith(nm, XMLNSC)) {
        ns.add(ln(nm), uri);
      } else {
        attArr[pos++] = new FAttr(att, this);
      }
    }
    atts = new NodIter(attArr, pos);

    // add all new namespaces
    for(int i = 0; i < ns.size; ++i) nss.add(ns.key[i], ns.val[i]);

    // no parent, so we have to add all namespaces in scope
    if(p == null) {
      nsScope(elem.getParentNode(), nss);
      for(final byte[] key : nss.keys()) {
        if(!ns.contains(key)) ns.add(key, nss.get(key));
      }
    }

    final byte[] pref = name.pref(), uri = name.uri().atom(),
        old = nss.get(pref);
    if(old == null || !Token.eq(uri, old)) {
      ns.add(pref, uri);
      nss.add(pref, uri);
    }

    // children
    final NodeList ch = elem.getChildNodes();
    final int s = ch.getLength();
    final Nod[] childArr = new Nod[s];
    children = new NodIter(childArr, childArr.length);

    for(int i = 0; i < ch.getLength(); ++i) {
      final Node child = ch.item(i);

      switch(child.getNodeType()) {
        case Node.TEXT_NODE:
          childArr[i] = new FTxt((Text) child, this);
          break;
        case Node.COMMENT_NODE:
          childArr[i] = new FComm((Comment) child, this);
          break;
        case Node.PROCESSING_INSTRUCTION_NODE:
          childArr[i] = new FPI((ProcessingInstruction) child, this);
          break;
        case Node.ELEMENT_NODE:
          childArr[i] = new FElem((Element) child, this, nss);
          break;
        default:
          break;
      }
    }
  }

  /**
   * Gathers all defined namespaces in the scope of the given DOM element.
   * @param elem DOM element
   * @param nss map
   */
  private static void nsScope(final Node elem, final TokenMap nss) {
    Node n = elem;
    // only elements can declare namespaces
    while(n != null && n instanceof Element) {
      final NamedNodeMap atts = n.getAttributes();
      final byte[] pre = token(n.getPrefix());
      if(nss.get(pre) != null) nss.add(pre, token(n.getNamespaceURI()));
      for(int i = 0, len = atts.getLength(); i < len; ++i) {
        final Attr a = (Attr) atts.item(i);
        final byte[] name = token(a.getName()), val = token(a.getValue());
        if(Token.eq(name, XMLNS)) {
          // default namespace
          if(nss.get(EMPTY) == null) nss.add(EMPTY, val);
        } else if(startsWith(name, XMLNS)) {
          // prefixed namespace
          final byte[] ln = ln(name);
          if(nss.get(ln) == null) nss.add(ln, val);
        }
      }
      n = n.getParentNode();
    }
  }

  @Override
  public byte[] base() {
    return base;
  }

  @Override
  public QNm qname() {
    return name;
  }

  @Override
  public byte[] nname() {
    return name.atom();
  }

  @Override
  public Atts ns() {
    return ns;
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    final byte[] tag = name.atom();
    ser.openElement(tag);

    if(name.hasUri()) ser.namespace(name.pref(), name.uri().atom());

    // serialize all namespaces at top level...
    if(ser.level() == 1) {
      final Atts nns = nsScope();
      for(int a = 0; a < nns.size; ++a) ser.namespace(nns.key[a], nns.val[a]);
    } else if(ns != null) {
      for(int p = ns.size - 1; p >= 0; p--) ser.namespace(ns.key[p], ns.val[p]);
    }

    // serialize attributes
    for(int n = 0; n < atts.size(); ++n) {
      final Nod nod = atts.get(n);
      final QNm atn = nod.qname();
      if(atn.ns() && !NSGlobal.standard(atn.uri().atom())) {
        ser.namespace(atn.pref(), atn.uri().atom());
      }
      ser.attribute(atn.atom(), nod.atom());
    }

    // serialize children
    for(int n = 0; n < children.size(); ++n) children.get(n).serialize(ser);
    ser.closeElement();
  }

  @Override
  public FElem copy() {
    final NodIter ch = new NodIter();
    final NodIter at = new NodIter();
    final FElem node = new FElem(name, ch, at, base, ns, par);

    for(int c = 0; c < children.size(); ++c) {
      ch.add(children.get(c).copy());
      ch.get(c).parent(node);
    }
    for(int c = 0; c < atts.size(); ++c) {
      at.add(atts.get(c).copy());
      at.get(c).parent(node);
    }
    return node;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, NAM, name.atom());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("<");
    sb.append(Token.string(name.atom()));
    if(atts.size() != 0 || ns != null && ns.size != 0 || children.size() != 0)
      sb.append(" ...");
    return sb.append("/>").toString();
  }
}
