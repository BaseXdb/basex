package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.iter.NodIter;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;

/**
 * Element Node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FElem extends FNode {
  /** Namespaces. */
  private final FAttr[] names;
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
      final byte[] b, final FAttr[] ns, final Nod p) {
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
  public FAttr[] ns() {
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

    final byte[] nm = name.str();
    ser.startElement(nm);

    final TokenList nms = new TokenList();
    final TokenList vls = new TokenList();

    // add namespace attributes
    for(final FAttr ns : names) {
      nms.add(ns.nname());
      vls.add(ns.str());
    }

    // add attributes
    for(int n = 0; n < atts.size; n++) {
      final Nod a = atts.list[n];
      final byte[] at = a.nname();
      if(level == 0 && a.qname().ns()) {
        final byte[] pref = substring(at, 0, indexOf(at, ':'));
        final byte[] atr = concat(XMLNSCOL, pref);
        boolean f = Token.eq(pref, XML);
        for(final FAttr ns : names) f |= Token.eq(ns.nname(), atr);
        if(!f) ser.attribute(atr, a.qname().uri.str());
      }
      ser.attribute(at, atts.list[n].str());
    }
    
    // add global namespaces
    if(level == 0) {
      final QNm ns = nsAnc();
      if(ns != null) {
        final byte[] p = ctx.ns.prefix(ns.uri);
        if(!Token.eq(p, XML)) {
          byte[] pre = p.length == 0 ? XMLNS : concat(XMLNSCOL, ns.pre());
          if(!nms.contains(pre)) {
            nms.add(pre);
            vls.add(ns.uri.str());
          }
        }
      } else if(ctx.nsElem != Uri.EMPTY) {
        nms.add(XMLNS);
        vls.add(ctx.nsElem.str());
      }
    }

    // serialize attributes
    for(int n = 0; n < nms.size; n++) ser.attribute(nms.list[n], vls.list[n]);
    
    // serialize children
    if(children.size == 0) {
      ser.emptyElement();
    } else {
      ser.finishElement();
      for(int n = 0; n < children.size; n++) {
        final Item child = children.list[n];
        child.serialize(ser, ctx, level + 1);
      }
      ser.closeElement(nm);
    }
  }
  
  /**
   * Returns the next ancestor with a namespace definition. 
   * @return ancestor
   */
  private QNm nsAnc() {
    FElem nm = this;
    while(nm.name.uri == Uri.EMPTY) {
      if(!(nm.par instanceof FElem)) return null;
      nm = (FElem) nm.par;
    }
    return nm.name.uri == null ? null : nm.name;
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
