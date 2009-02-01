package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.BaseX;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.NCN;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;
import org.basex.util.Atts;
import org.basex.util.TokenList;
import org.basex.util.XMLToken;

/**
 * QName functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNQName extends Fun {
  @Override
  public Iter iter(final QueryContext ctx, final Iter[] arg)
      throws QueryException {
    switch(func) {
      case RESQNAME:
        Item it = arg[0].atomic();
        if(it == null) return Iter.EMPTY;
        Item re = arg[1].atomic();
        if(re == null) Err.empty(this);
        return resolve(ctx, it, re);
      case QNAME:
        it = arg[0].atomic();
        final Uri uri = Uri.uri(it == null ? EMPTY :
          check(it, Type.STR).str());
        it = arg[1].atomic();
        it = it == null ? Str.ZERO : check(it, Type.STR);
        final byte[] str = it.str();
        if(!XMLToken.isQName(str)) Err.value(Type.QNM, it);
        QNm nm = new QNm(str, uri);
        if(nm.ns() && uri == Uri.EMPTY) Err.value(Type.URI, uri);
        return nm.iter();
      case LOCNAMEQNAME:
        it = arg[0].atomic();
        if(it == null) return Iter.EMPTY;
        return new NCN(((QNm) check(it, Type.QNM)).ln()).iter();
      case PREQNAME:
        it = arg[0].atomic();
        if(it == null) return Iter.EMPTY;
        nm = (QNm) check(it, Type.QNM);
        return !nm.ns() ? Iter.EMPTY : new NCN(nm.pre()).iter();
      case NSURIPRE:
        it = check(arg[1].atomic(), Type.ELM);
        final byte[] pre = checkStr(arg[0]);
        if(pre.length == 0) return Uri.uri(ctx.nsElem).iter();
        final Atts at = ((Nod) it).ns();
        for(int a = 0; a < at.size; a++) {
          if(eq(pre, at.key[a])) return Uri.uri(at.val[a]).iter();
        }
        return Iter.EMPTY;
      case INSCOPE:
        return inscope(ctx, (Nod) check(arg[0].atomic(), Type.ELM));
      case RESURI:
        it = arg[0].atomic();
        if(it == null) return Iter.EMPTY;
        final Uri rel = Uri.uri(checkStr(it));
        if(!rel.valid()) Err.or(URIINV, it);

        final Uri base = arg.length == 1 ? ctx.baseURI :
          Uri.uri(checkStr(arg[1].atomic()));
        if(!base.valid()) Err.or(URIINV, base);

        return base.resolve(rel).iter();
      default:
        BaseX.notexpected(func); return null;
    }
  }

  /**
   * Resolves a QName.
   * @param ctx query context
   * @param q qname
   * @param it item
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Iter resolve(final QueryContext ctx, final Item q, final Item it)
      throws QueryException {

    final byte[] name = trim(checkStr(q));
    if(!XMLToken.isQName(name)) Err.value(Type.QNM, q);
    final QNm nm = new QNm(name);
    
    final byte[] pre = nm.pre();
    Nod n = (Nod) check(it, Type.ELM);
    nm.uri = n.qname().uri;
    if(nm.uri != Uri.EMPTY) return nm.iter();

    while(n != null) {
      final Atts at = n.ns();
      if(at == null) break;
      final int i = at.get(pre);
      if(i != -1) {
        nm.uri = Uri.uri(at.val[i]);
        return nm.iter();
      }
      n = n.parent();
    }
    if(pre.length != 0) Err.or(NSDECL, pre);
    nm.uri = Uri.uri(ctx.nsElem);
    return nm.iter();
  }

  /**
   * Returns the in-scope prefixes for the specified node.
   * @param ctx query context
   * @param node node
   * @return prefix sequence
   */
  private Iter inscope(final QueryContext ctx, final Nod node) {
    final TokenList tl = new TokenList();
    tl.add(XML);
    if(ctx.nsElem.length != 0) tl.add(EMPTY);

    Nod n = node;
    do {
      final Atts at = n.ns();
      if(at == null) break;
      if(n != node || ctx.nsPreserve) {
        for(int a = 0; a < at.size; a++) {
          if(!tl.contains(at.key[a])) tl.add(at.key[a]);
        }
      }
      n = n.parent();
    } while(n != null && ctx.nsInherit);
    
    final SeqIter seq = new SeqIter();
    for(final byte[] t : tl) seq.add(Str.get(t));
    return seq;
  }
}
