package org.basex.query.xquery.func;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Node;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.item.Uri;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Token;

/**
 * Node functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FNNode extends Fun {
  @Override
  public Iter iter(final XQContext ctx, final Iter[] arg) throws XQException {
    final Iter iter = arg.length == 0 ? check(ctx) : arg[0];
    final Item it = iter.atomic(this, true);
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
        return uri == Token.EMPTY ? Iter.EMPTY : Uri.uri(uri).iter();
      case NILLED: // [CG] XQuery/nilled flag
        if(empty) return Iter.EMPTY;
        checkNode(it);
        return it.type != Type.ELM ? Iter.EMPTY : Bln.FALSE.iter();
      case BASEURI:
        if(empty) return Iter.EMPTY;
        Node n = checkNode(it);
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
        return qname != null ? Str.iter(qname.str()) : Str.ZERO.iter();
      case LOCNAME:
        if(empty) return Str.ZERO.iter();
        qname = checkNode(it).qname();
        return qname != null ? Str.iter(qname.ln()) : Str.ZERO.iter();
      case NSURI:
        if(empty || it.type == Type.PI) return Uri.EMPTY.iter();
        Node node = checkNode(it);
        while(node != null) {
          qname = node.qname();
          if(qname == null) break;
          if(qname.uri != Uri.EMPTY) return qname.uri.iter();
          node = node.parent();
        }
        return ctx.nsElem.iter();
      case ROOT:
        return empty ? Iter.EMPTY : root(checkNode(it)).iter();
      default:
        throw new RuntimeException("Not defined: " + func);

    }
  }

  @Override
  public Expr comp(final XQContext ctx, final Expr[] arg) {
    return this;
  }

  /**
   * Returns the root of the specified node.
   * @param node node to be checked
   * @return root node
   */
  private static Node root(final Node node) {
    Node n = node;
    while(n.parent() != null) n = n.parent();
    return n;
  }
}
