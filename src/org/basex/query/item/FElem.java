package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.iter.NodIter;
import org.basex.query.util.NSGlobal;
import org.basex.util.Atts;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;

/**
 * Element Node Fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FElem extends FNode {
  /** Namespaces. */
  private final Atts nsp;
  /** Tag name. */
  private final QNm name;
  /** Base URI. */
  private final byte[] base;

  /**
   * Constructor.
   * @param n tag name
   * @param b base uri
   * @param p parent
   */
  public FElem(final QNm n, final byte[] b, final Nod p) {
    this(n, new NodIter(), new NodIter(), b, new Atts(), p);
  }
  
  /**
   * Constructor.
   * @param n tag name
   * @param ch children
   * @param at attributes
   * @param b base uri
   * @param ns namespaces
   * @param p parent
   */
  public FElem(final QNm n, final NodIter ch, final NodIter at,
      final byte[] b, final Atts ns, final Nod p) {
    super(Type.ELM);
    name = n;
    children = ch;
    atts = at;
    base = b;
    nsp = ns;
    par = p;
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
    return nsp;
  }

  @Override
  public byte[] str() {
    final TokenBuilder tb = new TokenBuilder();
    for(int n = 0; n < children.size; n++) {
      final Nod c = children.list[n];
      if(c.type == Type.ELM || c.type == Type.TXT) tb.add(c.str());
    }
    return tb.finish();
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    final byte[] tag = name.str();
    ser.openElement(tag);
    
    // serialize top level namespace definitions
    final int s = ser.ns.size;
    final byte[] dn = ser.dn;
               
    boolean xmlns = false;
    // serialize all namespaces at top level...
    if(ser.tags.size == 1) {
      final TokenList nms = new TokenList();
      Nod node = this;
      do {
        final Atts ns = node.ns();
        for(int a = ns.size - 1; a >= 0; a--) {
          final byte[] key = ns.key[a];
          // namespace has already been serialized
          if(nms.contains(key)) continue;
          nms.add(key);
          
          final byte[] val = ns.val[a];
          if(key.length == 0) {
            xmlns = true;
            ser.dn = val;
            // reset default namespace
            if(val.length == 0) continue;
          }
          ser.namespace(key, val);
        }
        node = node.parent();
      } while(node instanceof FElem);

      // serialize default namespace if not done yet
      final Atts ns = ser.ns;
      for(int p = ns.size - 1; p >= 0 && !xmlns; p--) {
        if(ns.key[p].length != 0) continue;
        xmlns = true;
        ser.dn = ns.val[p];
        ser.namespace(EMPTY, ns.val[p]);
      }
    } else {
      for(int p = nsp.size - 1; p >= 0; p--) {
        final byte[] key = nsp.key[p];
        final int i = ser.ns.get(key);
        if(i == -1 || !Token.eq(ser.ns.val[i], name.uri.str())) {
          ser.namespace(key, nsp.val[p]);
          xmlns |= key.length == 0;
        }
      }
    }
    
    byte[] uri = name.uri.str();
    
    if(!xmlns && !name.ns() && !Token.eq(uri, ser.dn)) {
      ser.namespace(EMPTY, uri);
      ser.dn = uri;
    }
    
    // serialize attributes
    for(int n = 0; n < atts.size; n++) {
      final Nod nod = atts.list[n];
      final QNm atn = nod.qname();
      if(atn.ns()) {
        if(!NSGlobal.standard(atn.uri.str())) {
          final byte[] pre = atn.pre();
          final int i = ser.ns.get(pre);
          if(i == -1) ser.namespace(pre, atn.uri.str());
        }
      }
      ser.attribute(atn.str(), nod.str());
    }

    // serialize children
    for(int n = 0; n < children.size; n++) children.list[n].serialize(ser);
    ser.closeElement();

    // reset namespace pointer
    ser.ns.size = s;
    ser.dn = dn;
  }

  @Override
  public FElem copy() {
    final NodIter ch = new NodIter();
    final NodIter at = new NodIter();
    final FElem node = new FElem(name, ch, at, base, nsp, par);

    for(int c = 0; c < children.size; c++) {
      ch.add(children.list[c].copy());
      ch.list[c].parent(node);
    }
    for(int c = 0; c < atts.size; c++) {
      at.add(atts.list[c].copy());
      at.list[c].parent(node);
    }
    return node;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("<");
    sb.append(string(name.str()));
    if(children.size != 0) sb.append("...");
    return sb.append(">").toString();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, NAM, name.str());
  }
}
