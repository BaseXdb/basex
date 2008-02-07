package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQTokens.*;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.iter.NodIter;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

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
      final byte[] b, final FAttr[] ns, final Node p) {
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
      final Node c = children.list[n];
      if(c.type == Type.ELM || c.type == Type.TXT) tb.add(c.str());
    }
    return tb.finish();
  }

  @Override
  public void serialize(final Serializer ser,  final XQContext ctx,
      final int level) throws Exception {

    final byte[] nm = name.str();
    ser.startElement(nm);

    for(final FAttr ns : names) ser.attribute(ns.nname(), ns.str());

    if(level == 0) {
      byte[] pre = name.pre();
      final Uri uri = name.uri;

      if(uri != Uri.EMPTY) {
        final byte[] p = ctx.ns.prefix(uri);
        if(!Token.eq(p, XML)) {
          pre = p.length == 0 ? XMLNS : Token.concat(XMLNSCOL, pre);
          ser.attribute(pre, uri.str());
        }
      } else if(ctx.nsElem != Uri.EMPTY) {
        ser.attribute(XMLNS, ctx.nsElem.str());
      }
    }

    for(int n = 0; n < atts.size; n++) {
      ser.attribute(atts.list[n].nname(), atts.list[n].str());
    }
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

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("<");
    sb.append(Token.string(name.str()));
    if(children.size != 0) sb.append("...");
    return sb.append(">").toString();
  }

  @Override
  public FElem copy() throws XQException {
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
  public void plan(final Serializer ser) throws Exception {
    ser.emptyElement(this, NAM, name.str());
  }
}
