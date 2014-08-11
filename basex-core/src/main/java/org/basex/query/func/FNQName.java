package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * QName functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNQName extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case IN_SCOPE_PREFIXES: return inscope(qc);
      default:                return super.iter(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case RESOLVE_QNAME:            return resolveQName(qc);
      case QNAME:                    return qName(qc);
      case LOCAL_NAME_FROM_QNAME:    return localNameFromQName(qc);
      case PREFIX_FROM_QNAME:        return prefixFromQName(qc);
      case NAMESPACE_URI_FOR_PREFIX: return namespaceUriForPrefix(qc);
      case RESOLVE_URI:              return resolveUri(qc);
      default:                       return super.item(qc, ii);
    }
  }

  /**
   * Returns the in-scope prefixes of the specified node.
   * @param qc query context
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Iter inscope(final QueryContext qc) throws QueryException {
    final Atts ns = toElem(exprs[0], qc).nsScope().add(XML, XMLURI);
    final int as = ns.size();
    final ValueBuilder vb = new ValueBuilder(as);
    for(int a = 0; a < as; ++a) {
      final byte[] key = ns.name(a);
      if(key.length + ns.value(a).length != 0) vb.add(Str.get(key));
    }
    return vb;
  }

  /**
   * Resolves a QName.
   * @param qc query context
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Item qName(final QueryContext qc) throws QueryException {
    final byte[] uri = toToken(exprs[0], qc, true);
    final byte[] name = toToken(exprs[1], qc, false);
    final byte[] str = !contains(name, ':') && eq(uri, XMLURI) ? concat(XMLC, name) : name;
    if(!XMLToken.isQName(str)) throw valueError(info, AtomType.QNM, name);
    final QNm nm = new QNm(str, uri);
    if(nm.hasPrefix() && uri.length == 0) throw valueError(info, AtomType.URI, nm.uri());
    return nm;
  }

  /**
   * Returns the local name of a QName.
   * @param qc query context
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Item localNameFromQName(final QueryContext qc) throws QueryException {
    final QNm nm = toQNm(exprs[0], qc, sc, true);
    return nm == null ? null : AtomType.NCN.cast(Str.get(nm.local()), qc, sc, info);
  }

  /**
   * Returns the local name of a QName.
   * @param qc query context
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Item prefixFromQName(final QueryContext qc) throws QueryException {
    final QNm nm = toQNm(exprs[0], qc, sc, true);
    return nm == null ? null : nm.hasPrefix() ?
      AtomType.NCN.cast(Str.get(nm.prefix()), qc, sc, info) : null;
  }

  /**
   * Returns a new QName.
   * @param qc query context
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Item resolveQName(final QueryContext qc) throws QueryException {
    final Item it = exprs[0].atomItem(qc, info);
    if(it == null) return null;
    final ANode base = toElem(exprs[1], qc);

    final byte[] name = toToken(it);
    if(!XMLToken.isQName(name)) throw valueError(info, AtomType.QNM, name);

    final QNm nm = new QNm(name);
    final byte[] pref = nm.prefix();
    final byte[] uri = base.uri(pref);
    if(uri == null) throw NSDECL_X.get(info, pref);
    nm.uri(uri);
    return nm;
  }

  /**
   * Returns the namespace URI for a prefix.
   * @param qc query context
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Item namespaceUriForPrefix(final QueryContext qc) throws QueryException {
    final byte[] pref = toToken(exprs[0], qc, true);
    final ANode an = toElem(exprs[1], qc);
    if(eq(pref, XML)) return Uri.uri(XMLURI, false);
    final Atts at = an.nsScope();
    final byte[] s = at.value(pref);
    return s == null || s.length == 0 ? null : Uri.uri(s, false);
  }

  /**
   * Resolves a URI.
   * @param qc query context
   * @return prefix sequence
   * @throws QueryException query exception
   */
  private Item resolveUri(final QueryContext qc) throws QueryException {
    final Item it = exprs[0].atomItem(qc, info);
    final byte[] bs = exprs.length > 1 ? toToken(exprs[1], qc, false) : null;
    if(it == null) return null;

    // check relative uri
    final Uri rel = Uri.uri(toToken(it));
    if(!rel.isValid()) throw URIARG_X.get(info, rel);
    if(rel.isAbsolute()) return rel;

    // check base uri
    final Uri base = bs == null ? sc.baseURI() : Uri.uri(bs);
    if(!base.isAbsolute()) throw URINOTABS_X.get(info, base);
    if(!base.isValid() || contains(base.string(), '#') || !contains(base.string(), '/'))
      throw URIARG_X.get(info, base);

    return base.resolve(rel, info);
  }
}
