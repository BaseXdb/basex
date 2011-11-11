package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.AtomType;
import org.basex.query.item.Str;
import org.basex.query.item.Uri;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.XMLToken;
import org.basex.util.hash.TokenSet;

/**
 * QName functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNQName extends FuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNQName(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case IN_SCOPE_PREFIXES:
        return inscope(ctx, (ANode) checkType(expr[0].item(ctx, input),
            NodeType.ELM));
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    // functions have 1 or 2 arguments...
    final Item it = expr[0].item(ctx, input);
    final Item it2 = expr.length == 2 ? expr[1].item(ctx, input) : null;

    switch(def) {
      case RESOLVE_QNAME:
        return it == null ? null : resolve(ctx, it, checkEmpty(it2));
      case QNAME:
        final byte[] uri = checkEStr(it);
        final byte[] atm = checkEStr(it2);
        final byte[] str = !contains(atm, ':') && eq(uri, XMLURI) ?
            concat(XMLC, atm) : atm;
        if(!XMLToken.isQName(str)) Err.value(input, AtomType.QNM, atm);
        QNm nm = new QNm(str, uri);
        if(nm.ns() && uri.length == 0)
          Err.value(input, AtomType.URI, nm.uri());
        return nm;
      case LOCAL_NAME_FROM_QNAME:
        if(it == null) return null;
        nm = (QNm) checkType(it, AtomType.QNM);
        return AtomType.NCN.e(Str.get(nm.ln()), ctx, input);
      case PREFIX_FROM_QNAME:
        if(it == null) return null;
        nm = (QNm) checkType(it, AtomType.QNM);
        return !nm.ns() ? null : AtomType.NCN.e(Str.get(nm.pref()), ctx, input);
      case NAMESPACE_URI_FOR_PREFIX:
        // [LK] Namespaces: find out if inherit flag has a persistent effect
        final byte[] pre = checkEStr(it);
        final ANode an = (ANode) checkType(it2, NodeType.ELM);
        final boolean copied = ctx.copiedNods.contains(an.data());
        final Atts at = an.nsScope(!copied || ctx.nsInherit);
        final int i = at != null ? at.get(pre) : -1;
        return i != -1 ? Uri.uri(at.val[i]) : null;
      case RESOLVE_URI:
        if(it == null) return null;
        final Uri rel = Uri.uri(checkEStr(it));
        if(!rel.valid()) URIINV.thrw(input, it);
        if(rel.absolute()) return rel;
        final Uri base = it2 == null ? ctx.baseURI : Uri.uri(checkEStr(it2));
        if(!base.valid()) URIINV.thrw(input, base);
        if(!base.absolute()) URIABS.thrw(input, base);
        return base.resolve(rel);
      default:
        return super.item(ctx, ii);
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
    if(!XMLToken.isQName(name)) Err.value(input, AtomType.QNM, q);

    final QNm nm = new QNm(name);
    final byte[] pref = nm.pref();
    final byte[] uri = ((ANode) checkType(it, NodeType.ELM)).uri(pref, ctx);
    if(uri == null) NSDECL.thrw(input, pref);
    nm.uri(uri);
    return nm;
  }

  /**
   * Returns the in-scope prefixes for the specified node.
   * @param ctx query context
   * @param node node
   * @return prefix sequence
   */
  private Iter inscope(final QueryContext ctx, final ANode node) {
    final TokenSet pref = new TokenSet(XML);

    byte[] emp = null;
    ANode n = node;
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

    final ItemCache ic = new ItemCache(pref.size());
    for(final byte[] t : pref.keys()) ic.add(Str.get(t));
    return ic;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX && def == Function.IN_SCOPE_PREFIXES || super.uses(u);
  }
}
