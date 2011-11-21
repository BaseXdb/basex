package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.item.ANode;
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.Uri;
import org.basex.query.util.Var;
import org.basex.util.Atts;
import org.basex.util.InputInfo;

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
   * @param cont element contents
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

  @Override
  public FElem item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Item it = checkItem(tag, ctx);
    final int s = ctx.ns.size();
    try {
      addNS(ctx);

      // clone namespaces
      final Atts nns = new Atts();
      for(int i = 0; i < nsp.size; ++i) nns.add(nsp.key[i], nsp.val[i]);

      // create QName and set namespaces
      final QNm nnm = checkNS(qname(ctx, it, false, ii));
      final byte[] p = nnm.pref();
      if(!eq(p, XML)) {
        final byte[] uri = ctx.ns.uri(p);
        if(nnm.hasUri()) {
          // uri assigned: add to namespace declarations
          final Uri u = nnm.uri();
          if(uri == null || !eq(uri, u.string())) {
            ctx.ns.add(new QNm(p, u), ii);
            nns.add(p, u.string());
          } else if(!nns.contains(p) && !(eq(p, EMPTY) && eq(uri, EMPTY))) {
            nns.add(p, uri);
          }
        } else if(uri != null) {
          // no uri: assign default uri
          nnm.uri(uri);
        }
      }

      // create child and attribute nodes
      final Constr c = new Constr(ii, ctx, expr);
      if(c.errAtt) NOATTALL.thrw(input);
      if(c.duplAtt != null) (comp ? CATTDUPL : ATTDUPL).thrw(input, c.duplAtt);

      // update parent and namespace references
      final FElem node = new FElem(nnm, c.children, c.atts, nns);
      for(int n = 0; n < c.children.size(); ++n) {
        c.children.get(n).parent(node);
      }
      for(int n = 0; n < c.atts.size(); ++n) {
        final ANode att = c.atts.get(n).parent(node);
        final QNm name = att.qname();
        if(name.ns() && name.hasUri()) {
          byte[] apref = name.pref();
          final byte[] auri = name.uri().string();
          final int pos = nns.get(apref);
          if(pos == -1) {
            nns.add(apref, auri);
          } else if(!eq(nns.val[pos], auri)) {
            // same prefixes with different URIs exist
            apref = null;
            // check if existing prefix can be assigned
            for(int a = 0; a < nns.size; a++) {
              if(eq(nns.val[a], auri)) apref = nns.key[a];
            }
            // if not, generate new one
            if(apref == null) {
              int i = 1;
              do {
                apref = concat(name.pref(), token(i++));
              } while(nns.contains(apref));
              nns.add(apref, auri);
            }
            // create new attribute node
            c.atts.item[n] = new FAttr(
                new QNm(concat(apref, COLON, name.ln()), name.uri()),
                c.atts.get(n).string());
          }
        }
      }
      return node;
    } finally {
      ctx.ns.size(s);
    }
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
    final byte[] pre = name.pref(), uri = name.uri().string();
    if(eq(pre, XMLNS) || eq(uri, XMLNSURI) || eq(pre, XML) ^ eq(uri, XMLURI))
      CEINS.thrw(input, pre, uri);
    return name;
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
    return toString(string(NodeType.ELM.string()) + " { " + tag + " }");
  }
}
