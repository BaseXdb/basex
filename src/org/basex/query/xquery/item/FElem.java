package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.iter.NodIter;
import org.basex.util.TokenBuilder;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * Element Node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FElem extends FNode {
  /** Namespaces. */
  private final QNm[] names;
  /** Tag name. */
  private final QNm name;
  /** Base URI. */
  private final byte[] base;

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
      final byte[] b, final QNm[] ns, final Nod p) {
    super(Type.ELM);
    name = n;
    children = ch;
    atts = at;
    base = b;
    names = ns;
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
  public QNm[] ns() {
    return names;
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
  public void serialize(final Serializer ser,  final XQContext ctx,
      final int level) throws IOException {

    final byte[] tag = name.str();
    ser.startElement(tag);

    // serialize attributes
    for(int n = 0; n < atts.size; n++) {
      ser.attribute(atts.list[n].nname(), atts.list[n].str());
    }
    
    // serialize namespace definitions
    if(level == 0) {
      // top level...
      final TokenList nms = new TokenList();

      Nod elm = this;
      boolean xmlns = false;
      do {
        for(final QNm ns : ((FElem) elm).names) {
          final byte[] key = ns.str();
          final Uri val = ns.uri;
          
          if(nms.contains(key)) continue;
          nms.add(key);
          if(Token.eq(key, XMLNS)) {
            xmlns = true;
            if(val == Uri.EMPTY) continue;
          }
          ser.attribute(key, val.str());
        }
        elm = elm.parent();
      } while(elm instanceof FElem);

      final QNm[] qn = ctx.ns.ns();
      for(int p = 0; p < qn.length; p++) {
        byte[] key = qn[p].str();
        key = key.length == 0 ? XMLNS : concat(XMLNSC, key);
        final byte[] val = qn[p].uri.str();
        
        if(nms.contains(key)) continue;
        nms.add(key);
        if(Token.eq(key, XMLNS)) {
          xmlns = true;
          if(val.length == 0) continue;
        }
        ser.attribute(key, val);
      }
      
      if(ctx.nsElem != Uri.EMPTY && !xmlns) {
        ser.attribute(XMLNS, ctx.nsElem.str());
      }
    } else {
      for(final QNm ns : names) ser.attribute(ns.str(), ns.uri.str());
    }

    /* add attributes
    for(int n = 0; n < atts.size; n++) {
      final Nod a = atts.list[n];
      if(level == 0 && a.qname().ns()) {
        final byte[] at = a.nname();
        final byte[] pref = substring(at, 0, indexOf(at, ':'));
        final byte[] atr = concat(XMLNSC, pref);
        boolean f = Token.eq(pref, XML);
        for(final FAttr ns : names) f |= Token.eq(ns.nname(), atr);
        if(!f) ser.attribute(atr, a.qname().uri.str());
      }
    }
    
    // add global namespaces
    if(level == 0) {
      final QNm ns = nsAnc();
      if(ns != null) {
        final byte[] p = ctx.ns.prefix(ns.uri);
        if(!Token.eq(p, XML)) {
          byte[] pre = p.length == 0 ? XMLNS : concat(XMLNSC, ns.pre());
          if(!nms.contains(pre)) {
            nms.add(pre);
            vls.add(ns.uri.str());
          }
        }
      } else if(ctx.nsElem != Uri.EMPTY) {
        nms.add(XMLNS);
        vls.add(ctx.nsElem.str());
      }
    }*/

    // serialize children
    if(children.size == 0) {
      ser.emptyElement();
    } else {
      ser.finishElement();
      for(int n = 0; n < children.size; n++) {
        children.list[n].serialize(ser, ctx, level + 1);
      }
      ser.closeElement(tag);
    }
  }

  @Override
  public FElem copy() {
    final NodIter ch = new NodIter();
    final NodIter at = new NodIter();
    final FElem node = new FElem(name, ch, at, base, names, par);

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
  public void plan(final Serializer ser) throws Exception {
    ser.emptyElement(this, NAM, name.str());
  }
}
