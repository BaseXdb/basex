package org.basex.query.xquery.func;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.FAttr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.NCN;
import org.basex.query.xquery.item.Node;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.item.Uri;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;

/**
 * QName functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNQName extends Fun {
  @Override
  public Iter iter(final XQContext ctx, final Iter[] arg) throws XQException {
    switch(func) {
      case RESQNAME:
        Item it = arg[0].atomic(this, true);
        if(it == null) return Iter.EMPTY;
        QNm name = new QNm(Token.trim(checkStr(it)), ctx);
        byte[] pre = name.pre();
        it = arg[1].atomic(this, false);
        final Node n = (Node) check(it, Type.ELM);
        name.uri = n.qname().uri;
        if(name.uri != Uri.EMPTY) return name.iter();

        Item i;
        final Iter iter = inscope(ctx, n);
        while((i = iter.next()) != null) {
          final byte[] ns = i.str();
          if(ns.length == 0) continue;
          if(Token.eq(pre, ns)) {
            name.uri = ctx.ns.uri(ns);
            return name.iter();
          }
        }

        if(pre.length != 0) Err.or(NSDECL, pre);
        name.uri = ctx.nsElem;
        return name.iter();
      case QNAME:
        it = arg[0].atomic(this, true);
        Uri uri = Uri.uri(it == null ? Token.EMPTY :
          check(it, Type.STR).str());
        it = arg[1].atomic(this, true);
        it = it == null ? Str.ZERO : check(it, Type.STR);
        final byte[] str = it.str();
        if(!Type.isQName(str)) Err.value(Type.QNM, it);
        name = new QNm(str, uri);

        if(name.ns()) {
          if(uri == Uri.EMPTY || name.pre().length == 0)
            Err.value(Type.QNM, name);
          ctx.ns.index(name);
        }
        return name.iter();
      case LOCNAMEQNAME:
        it = arg[0].atomic(this, true);
        if(it == null) return Iter.EMPTY;
        return new NCN(((QNm) check(it, Type.QNM)).ln()).iter();
      case PREQNAME:
        it = arg[0].atomic(this, true);
        if(it == null) return Iter.EMPTY;
        name = (QNm) check(it, Type.QNM);
        return !name.ns() ? Iter.EMPTY : new NCN(name.pre()).iter();
      case NSURIPRE:
        it = arg[1].atomic(this, false);
        check(it, Type.ELM);
        try {
          pre = checkStr(arg[0]);
          return (pre.length == 0 ? ctx.nsElem : ctx.ns.uri(pre)).iter();
        } catch(final XQException e) {
          return Iter.EMPTY;
        }
      case INSCOPE:
        it = arg[0].atomic(this, false);
        return inscope(ctx, (Node) check(it, Type.ELM));
      case RESURI:
        it = arg[0].atomic(this, true);
        if(it == null) return Iter.EMPTY;
        final Uri rel = Uri.uri(checkStr(it));
        if(!rel.valid()) Err.or(URIINV, it);
        
        final Uri base = arg.length == 1 ? ctx.baseURI :
          Uri.uri(checkStr(arg[1].atomic(this, false)));
        if(!base.valid()) Err.or(URIINV, base);

        return base.resolve(rel).iter();
      default:
        throw new RuntimeException("Not defined: " + func);
    }
  }

  /**
   * Returns the in-scope prefixes for the specified node.
   * @param ctx query context
   * @param node node
   * @return prefix sequence
   * @throws XQException xquery exception
   */
  private Iter inscope(final XQContext ctx, final Node node)
      throws XQException {

    final SeqIter seq = new SeqIter();
    seq.add(new NCN(XML).iter());
    if(ctx.nsElem != Uri.EMPTY) seq.add(Str.ZERO);

    // <CG> XQuery/inscope; nested namespace handling
    Node n = node;
    while(n != null) {
      final FAttr[] at = n.ns();
      if(at == null) break;
      for(final FAttr ns : at) {
        final QNm name = ns.qname();
        if(name.ns()) seq.add(Str.get(name.ln()).iter());
      }
      n = n.parent();
    }
    return seq;
  }
}
