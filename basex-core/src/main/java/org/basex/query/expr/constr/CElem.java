package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Element constructor.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class CElem extends CName {
  /** Namespaces. */
  private final Atts nspaces;
  /** Computed constructor flag. */
  private final boolean comp;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param name name
   * @param nspaces namespaces or {@code null} if this is a computed constructor.
   * @param cont element contents
   */
  public CElem(final StaticContext sc, final InputInfo info, final Expr name, final Atts nspaces,
      final Expr... cont) {
    super(ELEMENT, sc, info, name, cont);
    this.nspaces = nspaces == null ? new Atts() : nspaces;
    comp = nspaces == null;
    seqType = SeqType.ELM;
  }

  @Override
  public CElem compile(final QueryContext qc, final VarScope scp) throws QueryException {
    final int s = addNS();
    super.compile(qc, scp);
    sc.ns.size(s);
    return this;
  }

  @Override
  public FElem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int s = addNS();
    try {
      // adds in-scope namespaces
      final Atts ns = new Atts();
      final int nl = nspaces.size();
      for(int i = 0; i < nl; i++) ns.add(nspaces.name(i), nspaces.value(i));

      // create and check QName
      final QNm nm = qname(qc, ii);
      final byte[] cp = nm.prefix(), cu = nm.uri();
      if(eq(cp, XML) ^ eq(cu, XML_URI)) throw CEXML.get(info, cu, cp);
      if(eq(cu, XMLNS_URI)) throw CEINV_X.get(info, cu);
      if(eq(cp, XMLNS)) throw CEINV_X.get(info, cp);
      if(!nm.hasURI() && nm.hasPrefix()) throw INVPREF_X.get(info, nm);

      // analyze element namespace unless it is "xml"
      if(!eq(cp, XML)) {
        // request namespace for the specified uri
        final byte[] uri = sc.ns.uri(cp);

        // check if element has a namespace
        if(nm.hasURI()) {
          // add to statically known namespaces
          if(!comp && (uri == null || !eq(uri, cu))) sc.ns.add(cp, cu);
          // add to in-scope namespaces
          if(!ns.contains(cp)) ns.add(cp, cu);
        } else {
          // element has no namespace: assign default uri
          nm.uri(uri);
        }
      }

      // create node
      final Constr constr = new Constr(ii, sc);
      final FElem node = new FElem(nm, ns, constr.children, constr.atts);

      // add child and attribute nodes
      constr.add(qc, exprs);
      if(constr.errAtt) throw NOATTALL.get(info);
      if(constr.errNS) throw NONSALL.get(info);
      if(constr.duplAtt != null) throw CATTDUPL_X.get(info, constr.duplAtt);
      if(constr.duplNS != null) throw DUPLNSCONS_X.get(info, constr.duplNS);

      // check namespaces
      if(constr.nspaces.contains(EMPTY) && !nm.hasURI()) throw DUPLNSCONS_X.get(info, EMPTY);

      // add namespaces from constructor
      final Atts cns = constr.nspaces;
      final int cl = cns.size();
      for(int c = 0; c < cl; c++) addNS(cns.name(c), cns.value(c), ns);

      // add namespaces
      final int al = constr.atts.size();
      for(int a = 0; a < al; a++) {
        final ANode att = constr.atts.get(a);
        final QNm qnm = att.qname();
        // skip attributes without prefixes or URIs
        if(!qnm.hasPrefix() || !qnm.hasURI()) continue;

        // skip XML namespace
        final byte[] apref = qnm.prefix();
        if(eq(apref, XML)) continue;

        final byte[] auri = qnm.uri();
        final byte[] npref = addNS(apref, auri, ns);
        if(npref != null) {
          final QNm aname = new QNm(concat(npref, COLON, qnm.local()), auri);
          constr.atts.set(a, new FAttr(aname, att.string()));
        }
      }

      // update and optimize child nodes
      for(final ANode ch : constr.children) ch.optimize();
      // return generated and optimized node
      return node.optimize();

    } finally {
      sc.ns.size(s);
    }
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new CElem(sc, info, name.copy(qc, scp, vs), comp ? null : nspaces.copy(),
        copyAll(qc, scp, vs, exprs));
  }

  /**
   * Adds the specified namespace to the namespace array.
   * If the prefix is already used for another URI, a new
   * name is generated.
   * @param pref prefix
   * @param uri uri
   * @param ns namespaces
   * @return resulting prefix
   */
  private static byte[] addNS(final byte[] pref, final byte[] uri, final Atts ns) {
    final byte[] u = ns.value(pref);
    if(u == null) {
      // add undeclared namespace
      ns.add(pref, uri);
    } else if(!eq(u, uri)) {
      // prefixes with different URIs exist; new one must be replaced
      byte[] apref = null;
      // check if one of the existing prefixes can be adopted
      final int nl = ns.size();
      for(int n = 0; n < nl; n++) {
        if(eq(ns.value(n), uri)) apref = ns.name(n);
      }
      // if negative, generate a new one that is not used yet
      if(apref == null) {
        int i = 1;
        do {
          apref = concat(pref, new byte[] { '_' }, token(i++));
        } while(ns.contains(apref));
        ns.add(apref, uri);
      }
      return apref;
    }
    return null;
  }

  /**
   * Adds namespaces to the namespace stack.
   * @return old position in namespace stack
   */
  private int addNS() {
    final NSContext ns = sc.ns;
    final int s = ns.size(), nl = nspaces.size();
    for(int n = 0; n < nl; n++) ns.add(nspaces.name(n), nspaces.value(n));
    return s;
  }
}
