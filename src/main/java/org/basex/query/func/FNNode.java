package org.basex.query.func;

import static org.basex.query.util.Err.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Uri;
import org.basex.query.iter.AxisIter;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.list.TokenList;

/**
 * Node functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNNode extends FuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNNode(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    // functions have 0 or 1 arguments...
    final Item it = (expr.length != 0 ? expr[0] :
      checkCtx(ctx)).item(ctx, input);

    switch(def) {
      case NODENAME:
        if(it == null) return null;
        QNm qname = checkNode(it).qname();
        return qname != null && qname.atom().length != 0 ? qname : null;
      case DOCURI:
        if(it == null) return null;
        final byte[] uri = checkNode(it).base();
        return uri.length == 0 ? null : Uri.uri(uri);
      case NILLED:
        // always false, as no schema information is given
        if(it == null) return null;
        return checkNode(it).type != NodeType.ELM ? null : Bln.FALSE;
      case BASEURI:
        if(it == null) return null;
        ANode n = checkNode(it);
        if(n.type != NodeType.ELM && n.type != NodeType.DOC &&
            n.parent() == null) return null;
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
        if(it == null) return Str.ZERO;
        qname = checkNode(it).qname();
        return qname != null ? Str.get(qname.atom()) : Str.ZERO;
      case LOCNAME:
        if(it == null) return Str.ZERO;
        qname = checkNode(it).qname();
        return qname != null ? Str.get(qname.ln()) : Str.ZERO;
      case NSURI:
        if(it == null || it.type == NodeType.PI) return Uri.EMPTY;
        ANode node = checkNode(it);
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
        if(it == null) return null;
        n = checkNode(it);
        while(n.parent() != null) n = n.parent();
        return n;
      case GENID:
        return it == null ? Str.ZERO : Str.get(new TokenBuilder(
            QueryText.ID).addLong(checkNode(it).id).finish());
      case CHILDREN:
        return Bln.get(it != null && checkNode(it).hasChildren());
      case PATH:
        if(it == null) return null;
        return path(it);
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Performs the path function.
   * @param it item to be resolved
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private Str path(final Item it) throws QueryException {
    final TokenList tl = new TokenList();
    ANode n = checkNode(it);
    while(n.parent() != null) {
      int i = 1;
      final TokenBuilder tb = new TokenBuilder();
      if(n.type == NodeType.ATT) {
        tb.add("@\"");
        final QNm qnm = n.qname();
        if(qnm.uri().atom().length != 0) tb.add(qnm.uri().atom());
        tb.add("\":").add(qnm.ln());
      } else if(n.type == NodeType.ELM) {
        final QNm qnm = n.qname();
        final AxisIter ai = n.precSibl();
        for(ANode fs; (fs = ai.next()) != null;) if(fs.qname().eq(qnm)) i++;
        tb.addExt("\"%\":%[%]", qnm.uri().atom(), qnm.ln(), i);
      } else if(n.type == NodeType.COM || n.type == NodeType.TXT) {
        final AxisIter ai = n.precSibl();
        for(ANode fs; (fs = ai.next()) != null;) if(fs.type == n.type) i++;
        tb.addExt(n.type() + "[%]", i);
      } else if(n.type == NodeType.PI) {
        final QNm qnm = n.qname();
        final AxisIter ai = n.precSibl();
        for(ANode fs; (fs = ai.next()) != null;) {
          if(fs.type == n.type && fs.qname().eq(qnm)) i++;
        }
        tb.addExt("%(\"%\")[%]", n.type.nam(), qnm.ln(), i);
      }
      tl.add(tb.finish());
      n = n.parent();
    }
    if(n.type != NodeType.DOC) IDDOC.thrw(input);

    final TokenBuilder tb = new TokenBuilder();
    for(int i = tl.size() - 1; i >= 0; --i) tb.add('/').add(tl.get(i));
    return Str.get(tb.size() == 0 ? Token.SLASH : tb.finish());
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 && (def == Function.GENID || def == Function.PATH ||
        def == Function.CHILDREN || expr.length == 0 &&
        (def == Function.DOCURI || def == Function.NODENAME)) ||
        u == Use.CTX && expr.length == 0 || super.uses(u);
  }
}
