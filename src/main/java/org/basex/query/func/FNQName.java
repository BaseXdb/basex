package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.NCN;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemIter;
import org.basex.query.util.Err;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.TokenSet;
import org.basex.util.XMLToken;

/**
 * QName functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNQName extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNQName(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case INSCOPE: return inscope(ctx,
          (Nod) checkType(expr[0].atomic(ctx, input), Type.ELM));
      default:      return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    // functions have 1 or 2 arguments...
    final Item it = expr[0].atomic(ctx, input);
    final Item it2 = expr.length == 2 ? expr[1].atomic(ctx, input) : null;

    switch(def) {
      case RESQNAME:
        return it == null ? null : resolve(ctx, it, checkEmpty(it2));
      case QNAME:
        final byte[] uri = it == null ? EMPTY :
          checkType(it, Type.STR).atom();
        final Item it3 = it2 == null ? Str.ZERO : checkType(it2, Type.STR);
        final byte[] atm = it3.atom();
        final byte[] str = !contains(atm, ':') && eq(uri, XMLURI)
            ? concat(XMLC, atm) : atm;
        if(!XMLToken.isQName(str)) Err.value(input, Type.QNM, it3);
        QNm nm = new QNm(str, uri);
        if(nm.ns() && uri.length == 0) Err.value(input, Type.URI, nm.uri());
        return nm;
      case LOCNAMEQNAME:
        if(it == null) return null;
        return new NCN(((QNm) checkType(it, Type.QNM)).ln(), input);
      case PREQNAME:
        if(it == null) return null;
        nm = (QNm) checkType(it, Type.QNM);
        return !nm.ns() ? null : new NCN(nm.pref(), input);
      case NSURIPRE:
        final byte[] pre = checkEStr(it);
        final Atts at = ((Nod) checkType(it2, Type.ELM)).nsScope();
        final int i = at != null ? at.get(pre) : -1;
        return i != -1 ? Uri.uri(at.val[i]) : null;
      case RESURI:
        if(it == null) return null;
        final Uri rel = Uri.uri(checkEStr(it));
        if(!rel.valid()) Err.or(input, URIINV, it);

        final Uri base = it2 == null ? ctx.baseURI : Uri.uri(checkEStr(it2));
        if(!base.valid()) Err.or(input, URIINV, base);
        return base.resolve(rel);
      default:
        return super.atomic(ctx, ii);
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

    final byte[] name = trim(checkEStr(q));
    if(!XMLToken.isQName(name)) Err.value(input, Type.QNM, q);

    final QNm nm = new QNm(name);
    final byte[] pref = nm.pref();
    final byte[] uri = ((Nod) checkType(it, Type.ELM)).uri(pref, ctx);
    if(uri == null && pref.length != 0) Err.or(input, NSDECL, pref);
    nm.uri(uri);
    return nm;
  }

  /**
   * Returns the in-scope prefixes for the specified node.
   * @param ctx query context
   * @param node node
   * @return prefix sequence
   */
  private Iter inscope(final QueryContext ctx, final Nod node) {
    final TokenSet pref = new TokenSet();
    pref.add(XML);

    byte[] emp = null;
    Nod n = node;
    do {
      final Atts at = n.ns();
      if(at == null) break;
      if(n != node || ctx.nsPreserve) {
        for(int a = 0; a < at.size; ++a) {
          final byte[] pre = at.key[a];
          if(pre.length == 0) {
            if(emp == null) emp = at.val[a];
          } else pref.add(pre);
        }
      }
      if(emp == null) {
        final QNm nm = n.qname();
        if(!nm.ns()) emp = nm.uri().atom();
      }
      n = n.parent();
    } while(n != null && ctx.nsInherit);
    
    if(emp == null) emp = ctx.nsElem;
    if(emp.length != 0) pref.add(EMPTY);

    final ItemIter ir = new ItemIter(pref.size());
    for(final byte[] t : pref.keys()) ir.add(Str.get(t));
    return ir;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX && def == FunDef.INSCOPE || super.uses(u);
  }
}
