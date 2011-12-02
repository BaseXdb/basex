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
      case IN_SCOPE_PREFIXES: return inscope(ctx);
      default:                return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    // functions have 1 or 2 arguments...
    final Item it = expr[0].item(ctx, input);
    final Item it2 = expr.length == 2 ? expr[1].item(ctx, input) : null;
    switch(def) {
      case RESOLVE_QNAME:            return resolveQName(ctx, it, it2);
      case QNAME:                    return qName(it, it2);
      case LOCAL_NAME_FROM_QNAME:    return lnFromQName(ctx, it);
      case PREFIX_FROM_QNAME:        return prefixFromQName(ctx, it);
      case NAMESPACE_URI_FOR_PREFIX: return nsUriForPrefix(it, it2);
      case RESOLVE_URI:              return resolveURI(ctx, it, it2);
      default:                       return super.item(ctx, ii);
    }
  }

  /**
   * Returns the in-scope prefixes of the specified node.
   * @param ctx query context
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Iter inscope(final QueryContext ctx) throws QueryException {
    final ANode node = (ANode) checkType(expr[0].item(ctx, input),
        NodeType.ELM);

    final Atts ns = node.nsScope().add(XML, XMLURI);
    final int as = ns.size();
    final ItemCache ic = new ItemCache(as);
    for(int a = 0; a < as; ++a) {
      final byte[] key = ns.key(a);
      if(key.length + ns.value(a).length != 0) ic.add(Str.get(key));
    }
    return ic;
  }

  /**
   * Resolves a QName.
   * @param it qname
   * @param it2 item
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Item qName(final Item it, final Item it2) throws QueryException {
    final byte[] uri = checkEStr(it);
    final byte[] atm = checkEStr(it2);
    final byte[] str = !contains(atm, ':') && eq(uri, XMLURI) ?
        concat(XMLC, atm) : atm;
    if(!XMLToken.isQName(str)) Err.value(input, AtomType.QNM, Str.get(atm));
    final QNm nm = new QNm(str, uri);
    if(nm.hasPrefix() && uri.length == 0)
      Err.value(input, AtomType.URI, Str.get(nm.uri()));
    return nm;
  }

  /**
   * Returns the local name of a QName.
   * @param ctx query context
   * @param it qname
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Item lnFromQName(final QueryContext ctx, final Item it)
      throws QueryException {

    if(it == null) return null;
    final QNm nm = (QNm) checkType(it, AtomType.QNM);
    return AtomType.NCN.e(Str.get(nm.local()), ctx, input);
  }

  /**
   * Returns the local name of a QName.
   * @param ctx query context
   * @param it qname
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Item prefixFromQName(final QueryContext ctx, final Item it)
      throws QueryException {

    if(it == null) return null;
    final QNm nm = (QNm) checkType(it, AtomType.QNM);
    return nm.hasPrefix() ?
        AtomType.NCN.e(Str.get(nm.prefix()), ctx, input) : null;
  }

  /**
   * Returns a new QName.
   * @param ctx query context
   * @param it qname
   * @param it2 item
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Item resolveQName(final QueryContext ctx, final Item it,
      final Item it2) throws QueryException {

    final ANode base = (ANode) checkType(it2, NodeType.ELM);
    if(it == null) return null;

    final byte[] name = trim(checkEStr(it));
    if(!XMLToken.isQName(name)) Err.value(input, AtomType.QNM, it);

    final QNm nm = new QNm(name);
    final byte[] pref = nm.prefix();
    final byte[] uri = base.uri(pref, ctx);
    if(uri == null) NSDECL.thrw(input, pref);
    nm.uri(uri);
    return nm;
  }

  /**
   * Returns the namespace URI for a prefix.
   * @param it qname
   * @param it2 item
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Item nsUriForPrefix(final Item it, final Item it2)
      throws QueryException {

    final byte[] pre = checkEStr(it);
    final ANode an = (ANode) checkType(it2, NodeType.ELM);
    if(eq(pre, XML)) return Uri.uri(XMLURI);
    final Atts at = an.nsScope();
    final int i = at != null ? at.get(pre) : -1;
    return i == -1 || at.value(i).length == 0 ? null : Uri.uri(at.value(i));
  }

  /**
   * Resolves a URI.
   * @param ctx query context
   * @param it item
   * @param it2 second item
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Item resolveURI(final QueryContext ctx, final Item it, final Item it2)
      throws QueryException {
    if(it == null) return null;
    final Uri rel = Uri.uri(checkEStr(it));
    if(!rel.isValid()) URIINV.thrw(input, it);
    if(rel.isAbsolute()) return rel;
    final Uri base = it2 == null ? ctx.baseURI() : Uri.uri(checkEStr(it2));
    if(!base.isValid()) URIINV.thrw(input, base);
    if(!base.isAbsolute()) URIABS.thrw(input, base);
    return base.resolve(rel);
  }
}
