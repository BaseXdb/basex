package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.item.FElem;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import static org.basex.query.util.Err.*;
import org.basex.query.util.NSGlobal;
import org.basex.query.util.Var;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Element fragment.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class CElem extends CFrag {
  /** Namespaces. */
  private final Atts nsp;
  /** Tag name. */
  private Expr tag;
  /** Computed constructor. */
  private final boolean comp;

  /**
   * Constructor.
   * @param ii input info
   * @param t tag tag
   * @param c computed constructor
   * @param cont element content
   * @param ns namespaces
   */
  public CElem(final InputInfo ii, final Expr t, final Atts ns,
      final boolean c, final Expr... cont) {
    super(ii, cont);
    tag = t;
    nsp = ns;
    comp = c;
  }

  @Override
  public CElem comp(final QueryContext ctx) throws QueryException {
    final int s = ctx.ns.size();
    addNS(ctx);
    super.comp(ctx);
    tag = checkUp(tag, ctx).comp(ctx);
    ctx.ns.size(s);
    return this;
  }

  /**
   * Adds namespaces to the current context.
   * @param ctx query context
   * @throws QueryException query exception
   */
  private void addNS(final QueryContext ctx) throws QueryException {
    for(int n = nsp.size - 1; n >= 0; n--) {
      ctx.ns.add(new QNm(concat(XMLNSC, nsp.key[n]), nsp.val[n]), input);
    }
  }

  /**
   * Checks the element name for illegal prefixes or URIs.
   * @param name element name
   * @return checked name
   * @throws QueryException XQDY0096, if invalid namespace was found
   */
  private QNm checkNS(final QNm name) throws QueryException {
    final byte[] pre = name.pref(), uri = name.uri().atom();
    if(eq(pre, XMLNS) || eq(uri, XMLNSURI) || eq(pre, XML) ^ eq(uri, XMLURI))
      CEINS.thrw(input, pre, uri);
    return name;
  }

  @Override
  public FElem item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Item it = checkItem(tag, ctx);
    final int s = ctx.ns.size();
    addNS(ctx);

    // clone namespaces for context sensitive operations
    final Atts nsc = new Atts();
    for(int i = 0; i < nsp.size; ++i) nsc.add(nsp.key[i], nsp.val[i]);

    final QNm tname = checkNS(qname(ctx, it, false, ii));
    final byte[] pref = tname.pref();
    if(!eq(pref, XML)) {
      byte[] uri = ctx.ns.find(pref);
      if(uri == null) uri = NSGlobal.uri(pref);
      if(tname.hasUri()) {
        final byte[] muri = tname.uri().atom();
        if(uri == null || !eq(uri, muri)) {
          ctx.ns.add(new QNm(tname.pref(), tname.uri()), ii);
          nsc.add(pref, muri);
        } else if(!nsc.contains(pref) && !(eq(pref, EMPTY) && eq(uri, EMPTY))) {
          nsc.add(pref, uri);
        }
      } else if(uri != null) {
        tname.uri(uri);
      }
    }

    final Constr c = new Constr(ii, ctx, expr);
    if(c.errAtt) NOATTALL.thrw(input);
    if(c.duplAtt != null) (comp ? CATTDUPL : ATTDUPL).thrw(input, c.duplAtt);

    final FElem node = new FElem(tname, c.children, c.atts, nsc);
    for(int n = 0; n < c.children.size(); ++n) c.children.get(n).parent(node);
    for(int n = 0; n < c.atts.size(); ++n) {
      final ANode att = c.atts.get(n);
      final QNm name = att.qname();
      if(name.ns() && name.hasUri()) {
        final byte[] apre = name.pref(), auri = name.uri().atom();
        final int pos = nsc.get(apre);
        if(pos  == -1) {
          nsc.add(apre, auri);
        } else if(!eq(nsc.val[pos], auri)) {
          // [LK][CG] Namespaces: create new prefix
        }
      }
      att.parent(node);
    }
    ctx.ns.size(s);
    return node;
  }

  @Override
  public Expr remove(final Var v) {
    tag = tag.remove(v);
    return super.remove(v);
  }

  @Override
  public boolean uses(final Use u) {
    return tag.uses(u) || super.uses(u);
  }

  @Override
  public int count(final Var v) {
    return tag.count(v) + super.count(v);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    tag.plan(ser);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String desc() {
    return info(QueryText.ELEMENT);
  }

  @Override
  public String toString() {
    return toString(Token.string(NodeType.ELM.nam()) + " { " + tag + " }");
  }
}
