package org.basex.query.func;

import org.basex.BaseX;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;

/**
 * Node functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FNNode extends Fun {
  @Override
  public Iter iter(final QueryContext ctx, final Iter[] arg)
      throws QueryException {
    final Item it = (arg.length == 0 ? checkCtx(ctx) : arg[0]).atomic();
    final boolean empty = it == null;

    switch(func) {
      case NODENAME:
        if(empty) return Iter.EMPTY;
        QNm qname = checkNode(it).qname();
        return qname != null && qname.str().length != 0 ? qname.iter() :
          Iter.EMPTY;
      case DOCURI:
        if(empty) return Iter.EMPTY;
        final byte[] uri = checkNode(it).base();
        return uri.length == 0 ? Iter.EMPTY : Uri.uri(uri).iter();
      case NILLED: // [CG] XQuery/nilled flag
        if(empty) return Iter.EMPTY;
        checkNode(it);
        return it.type != Type.ELM ? Iter.EMPTY : Bln.FALSE.iter();
      case BASEURI:
        if(empty) return Iter.EMPTY;
        Nod n = checkNode(it);
        if(n.type != Type.ELM && n.type != Type.DOC && n.parent() == null)
          return Iter.EMPTY;
        Uri base = Uri.EMPTY;
        while(!base.absolute()) {
          if(n == null) {
            base = ctx.baseURI.resolve(base);
            break;
          }
          base = Uri.uri(n.base()).resolve(base);
          n = n.parent();
        }
        return base.iter();
      case NAME:
        if(empty) return Str.ZERO.iter();
        qname = checkNode(it).qname();
        return (qname != null ? Str.get(qname.str()) : Str.ZERO).iter();
      case LOCNAME:
        if(empty) return Str.ZERO.iter();
        qname = checkNode(it).qname();
        return (qname != null ? Str.get(qname.ln()) : Str.ZERO).iter();
      case NSURI:
        if(empty || it.type == Type.PI) return Uri.EMPTY.iter();
        Nod node = checkNode(it);
        while(node != null) {
          qname = node.qname();
          if(qname == null) break;
          if(qname.uri != Uri.EMPTY) return qname.uri.iter();
          node = node.parent();
        }
        return Uri.uri(ctx.nsElem).iter();
      case ROOT:
        return empty ? Iter.EMPTY : root(checkNode(it)).iter();
      default:
        BaseX.notexpected(func); return null;

    }
  }

  /**
   * Returns the root of the specified node.
   * @param node node to be checked
   * @return root node
   */
  private static Nod root(final Nod node) {
    Nod n = node;
    while(n.parent() != null) n = n.parent();
    return n;
  }
}
