package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class FNQName extends Fun {
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case INSCOPE:
        return inscope(ctx, (Nod) check(expr[0].atomic(ctx), Type.ELM));
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    // functions have 1 or 2 arguments...
    final Item it = expr[0].atomic(ctx);
    final Item it2 = expr.length == 2 ? expr[1].atomic(ctx) : null;

    switch(func) {
      case RESQNAME:
        if(it == null) return null;
        if(it2 == null) Err.empty(this);
        return resolve(ctx, it, it2);
      case QNAME:
        final Uri uri = Uri.uri(it == null ? EMPTY :
          check(it, Type.STR).str());
        final Item it3 = it2 == null ? Str.ZERO : check(it2, Type.STR);
        final byte[] str = it3.str();
        if(!XMLToken.isQName(str)) Err.value(Type.QNM, it3);
        QNm nm = new QNm(str, uri);
        if(nm.ns() && uri == Uri.EMPTY) Err.value(Type.URI, uri);
        return nm;
      case LOCNAMEQNAME:
        if(it == null) return null;
        return new NCN(((QNm) check(it, Type.QNM)).ln());
      case PREQNAME:
        if(it == null) return null;
        nm = (QNm) check(it, Type.QNM);
        return !nm.ns() ? null : new NCN(nm.pref());
      case NSURIPRE:
        final byte[] pre = checkStr(it);
        final Atts at = ((Nod) check(it2, Type.ELM)).ns();
        final int i = at.get(pre);
        return i != -1 ? Uri.uri(at.val[i]) : null;
      case RESURI:
        if(it == null) return null;
        final Uri rel = Uri.uri(checkStr(it));
        if(!rel.valid()) Err.or(URIINV, it);

        final Uri base = it2 == null ? ctx.baseURI : Uri.uri(checkStr(it2));
        if(!base.valid()) Err.or(URIINV, base);
        return base.resolve(rel);
      default:
        return super.atomic(ctx);
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
  private Item resolve(final QueryContext ctx, final Item q, final Item it)
      throws QueryException {

    final byte[] name = trim(checkStr(q));
    if(!XMLToken.isQName(name)) Err.value(Type.QNM, q);

    final QNm nm = new QNm(name);
    final byte[] pref = nm.pref();
    final byte[] uri = ((Nod) check(it, Type.ELM)).uri(pref, ctx);
    if(uri == null && pref.length != 0) Err.or(NSDECL, pref);
    // [CG] XQuery/QName: check if uri can be null
    nm.uri = Uri.uri(uri);
    return nm;
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
