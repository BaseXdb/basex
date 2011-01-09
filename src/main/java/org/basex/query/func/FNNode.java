package org.basex.query.func;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Node functions.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
final class FNNode extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNNode(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    // functions have 0 or 1 arguments...
    final Item it = (expr.length != 0 ? expr[0] :
      checkCtx(ctx)).item(ctx, input);
    final boolean empty = it == null;

    switch(def) {
      case NODENAME:
        if(empty) return null;
        QNm qname = checkNode(it).qname();
        return qname != null && qname.atom().length != 0 ? qname : null;
      case DOCURI:
        if(empty) return null;
        final byte[] uri = checkNode(it).base();
        return uri.length == 0 ? null : Uri.uri(uri);
      case NILLED:
        // always false, as no schema information is given
        if(empty) return null;
        checkNode(it);
        return it.type != Type.ELM ? null : Bln.FALSE;
      case BASEURI:
        if(empty) return null;
        Nod n = checkNode(it);
        if(n.type != Type.ELM && n.type != Type.DOC && n.parent() == null)
          return null;
        Uri base = Uri.EMPTY;
        while(!base.absolute()) {
          if(n == null) {
            base = ctx.baseURI.resolve(base);
            break;
          }
          base = Uri.uri(n.base()).resolve(base);
          n = n.parent();
        }
        return base;
      case NAME:
        if(empty) return Str.ZERO;
        qname = checkNode(it).qname();
        return qname != null ? Str.get(qname.atom()) : Str.ZERO;
      case LOCNAME:
        if(empty) return Str.ZERO;
        qname = checkNode(it).qname();
        return qname != null ? Str.get(qname.ln()) : Str.ZERO;
      case NSURI:
        if(empty || it.type == Type.PI) return Uri.EMPTY;
        Nod node = checkNode(it);
        while(node != null) {
          qname = node.qname();
          if(qname == null) break;
          if(qname.hasUri()) return qname.uri();
          final Atts ns = node.nsScope();
          if(ns != null) {
            final int pos = ns.get(qname.pref());
            if(pos != -1) return Uri.uri(ns.val[pos]);
          }
          node = node.parent();
        }
        return Uri.uri(ctx.nsElem);
      case ROOT:
        if(empty) return null;
        n = checkNode(it);
        while(n.parent() != null) n = n.parent();
        return n;
      case GENID:
        return empty ? Str.ZERO : Str.get(new TokenBuilder(
            QueryTokens.ID).addLong(checkNode(it).id()).finish());
      default:
        return super.item(ctx, ii);
    }
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X11 && (def == FunDef.GENID || expr.length == 0 &&
        (def == FunDef.DOCURI || def == FunDef.NODENAME)) ||
        u == Use.CTX && expr.length == 0 || super.uses(u);
  }
}
