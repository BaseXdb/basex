package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.item.FElem;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.util.Atts;

/**
 * Element fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CElem extends CFrag {
  /** Namespaces. */
  private final Atts nsp;
  /** Tag name. */
  private Expr tag;

  /**
   * Constructor.
   * @param t tag tag
   * @param cont element content
   * @param ns namespaces
   */
  public CElem(final Expr t, final Expr[] cont, final Atts ns) {
    super(cont);
    tag = t;
    nsp = ns;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
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
      ctx.ns.add(new QNm(concat(XMLNSC, nsp.key[n]), Uri.uri(nsp.val[n])));
    }
  }

  @Override
  public FElem atomic(final QueryContext ctx) throws QueryException {
    final Item it = tag.atomic(ctx);
    if(it == null) Err.empty(CElem.this);
    final int s = ctx.ns.size();
    addNS(ctx);

    final QNm tname = CAttr.name(ctx, it);
    final byte[] uri = tname.uri.str();
    if(uri.length != 0) {
      final byte[] key = tname.pref();
      if(!eq(key, XML)) {
        final int i = nsp.get(key);
        if(i == -1 || !eq(nsp.val[i], uri)) nsp.add(key, uri);
      }
    }

    final Constr c = new Constr(ctx, expr);
    if(c.errAtt) Err.or(NOATTALL);
    if(c.duplAtt != null) Err.or(ATTDUPL, c.duplAtt);

    final FElem node = new FElem(tname, c.children, c.ats, c.base, nsp, null);
    for(int n = 0; n < c.children.size(); n++) c.children.get(n).parent(node);
    for(int n = 0; n < c.ats.size(); n++) c.ats.get(n).parent(node);
    ctx.ns.size(s);
    return node;
  }

  @Override
  public Expr remove(final Var v) {
    tag = tag.remove(v);
    return super.remove(v);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    tag.plan(ser);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String info() {
    return info(QueryTokens.ELEMENT);
  }

  @Override
  public String toString() {
    return toString(Type.ELM.name + " { " + tag + " }");
  }
}
