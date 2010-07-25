package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.iter.NodIter;
import org.basex.query.util.NSGlobal;
import org.basex.util.Atts;
import static org.basex.util.Token.*;
import org.basex.util.Token;
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
   */
  FElem(final Element elem, final Nod p) {
    super(Type.ELM);
 
    // general stuff
    final String nu = elem.getNamespaceURI();
    name = new QNm(token(elem.getNodeName()),
        Uri.uri(nu == null ? EMPTY : token(nu)));
    par = p;
    final String b = elem.getBaseURI();
    base = b == null ? EMPTY : token(b);
    
    // attributes and namespaces
    ns = new Atts();
    final NamedNodeMap at = elem.getAttributes();
    final int as = at.getLength();
    final Nod[] attArr = new Nod[as];
    
    int pos = 0;
    for(int i = 0; i < as; i++) {
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
    
    // no parent, so we have to add all namespaces in scope
    if(p == null) {
      final Atts nss = nsScope(elem.getParentNode());
      for(int i = 0; i < nss.size; i++) {
        if(!ns.contains(nss.key[i])) {
          ns.add(nss.key[i], nss.val[i]);
        }
      }
    }

    // children
    final NodeList ch = elem.getChildNodes();
    final int s = ch.getLength();
    final Nod[] childArr = new Nod[s];
    children = new NodIter(childArr, childArr.length);

    for(int i = 0; i < ch.getLength(); i++) {
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
          childArr[i] = new FElem((Element) child, this);
          break;
        default:
          break;
      }
    }
  }
  
  /**
   * Gathers all defined namespaces in the scope of the given DOM element.
   * @param elem DOM element
   * @return namespaces
   */
  private static Atts nsScope(final Node elem) {
    final Atts ns = new Atts();
    Node n = elem;
    // only elements can declare namespaces
    while(n != null && n instanceof Element) {
      final NamedNodeMap atts = n.getAttributes();
      for(int i = 0, len = atts.getLength(); i < len; i++) {
        final Attr a = (Attr) atts.item(i);
        final byte[] name = token(a.getName()), uri = token(a.getValue());
        if(Token.eq(name, XMLNS)) {
          // default namespace
          if(!ns.contains(EMPTY)) ns.add(EMPTY, uri);
        } else if(startsWith(name, XMLNS)) {
          // prefixed namespace
          final byte[] ln = ln(name);
          if(!ns.contains(ln)) ns.add(ln, uri);
        }
      }
      n = n.getParentNode();
    }
    return ns;
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
    return name.str();
  }

  @Override
  public Atts ns() {
    return ns;
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    final byte[] tag = name.str();
    final byte[] uri = name.uri.str();
    ser.openElement(tag);

    // remember top level namespace
    final byte[] dn = ser.dn;
    boolean xmlns = false;

    // serialize all namespaces at top level...
    if(ser.level() == 1) {
      final Atts nns = nsScope();
      for(int a = 0; a < nns.size; a++) {
        if(nns.key[a].length == 0) {
          xmlns = true;
          if(Token.eq(ser.dn, nns.val[a])) continue;
          // reset default namespace
          ser.dn = nns.val[a];
        }
        ser.namespace(nns.key[a], nns.val[a]);
      }

      // serialize default namespace if not done yet
      for(int p = ser.ns.size - 1; p >= 0 && !xmlns; p--) {
        if(ser.ns.key[p].length != 0) continue;
        xmlns = true;
        ser.dn = ser.ns.val[p];
        ser.namespace(EMPTY, ser.ns.val[p]);
      }
    } else {
      if(ns != null) {
        for(int p = ns.size - 1; p >= 0; p--) {
          final byte[] key = ns.key[p];
          final int i = ser.ns.get(key);
          if(i == -1 || !Token.eq(ser.ns.val[i], uri)) {
            ser.namespace(key, ns.val[p]);
            xmlns |= key.length == 0;
          }
        }
      }
    }

    if(!xmlns && !name.ns() && !Token.eq(uri, ser.dn))
      ser.namespace(EMPTY, uri);
    ser.dn = uri;

    // serialize attributes
    for(int n = 0; n < atts.size(); n++) {
      final Nod nod = atts.get(n);
      final QNm atn = nod.qname();
      if(atn.ns()) {
        if(!NSGlobal.standard(atn.uri.str())) {
          final byte[] pre = atn.pref();
          final int i = ser.ns.get(pre);
          if(i == -1) ser.namespace(pre, atn.uri.str());
        }
      }
      ser.attribute(atn.str(), nod.str());
    }

    // serialize children
    for(int n = 0; n < children.size(); n++) children.get(n).serialize(ser);
    ser.closeElement();

    // reset top level namespace
    ser.dn = dn;
  }

  @Override
  public FElem copy() {
    final NodIter ch = new NodIter();
    final NodIter at = new NodIter();
    final FElem node = new FElem(name, ch, at, base, ns, par);

    for(int c = 0; c < children.size(); c++) {
      ch.add(children.get(c).copy());
      ch.get(c).parent(node);
    }
    for(int c = 0; c < atts.size(); c++) {
      at.add(atts.get(c).copy());
      at.get(c).parent(node);
    }
    return node;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, NAM, name.str());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("<");
    sb.append(string(name.str()));
    if(atts.size() != 0 || ns != null && ns.size != 0 || children.size() != 0)
      sb.append(" ...");
    return sb.append("/>").toString();
  }
}
