package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.iter.*;
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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CElem extends CName {
  /** Namespaces. */
  private final Atts nspaces;
  /** Computed constructor flag. */
  private final boolean comp;

  /**
   * Constructor.
   * @param ii input info
   * @param t tag
   * @param ns namespaces, or {@code null} if this is a computed constructor.
   * @param cont element contents
   */
  public CElem(final InputInfo ii, final Expr t, final Atts ns, final Expr... cont) {
    super(ELEMENT, ii, t, cont);
    nspaces = ns == null ? new Atts() : ns;
    comp = ns == null;
    type = SeqType.ELM;
  }

  @Override
  public CElem compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    final int s = prepare(ctx);
    super.compile(ctx, scp);
    ctx.sc.ns.size(s);
    return this;
  }

  @Override
  public FElem item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final int s = prepare(ctx);
    try {
      // adds in-scope namespaces
      final Atts ns = new Atts();
      for(int i = 0; i < nspaces.size(); ++i) {
        ns.add(nspaces.name(i), nspaces.string(i));
      }

      // create and check QName
      final QNm nm = qname(ctx, ii);
      final byte[] cp = nm.prefix(), cu = nm.uri();
      if(eq(cp, XML) ^ eq(cu, XMLURI)) CEXML.thrw(info, cu, cp);
      if(eq(cu, XMLNSURI)) CEINV.thrw(info, cu);
      if(eq(cp, XMLNS)) CEINV.thrw(info, cp);
      if(!nm.hasURI() && nm.hasPrefix()) INVPREF.thrw(info, nm);

      // analyze element namespace unless it is "xml"
      if(!eq(cp, XML)) {
        // request namespace for the specified uri
        final byte[] uri = ctx.sc.ns.uri(cp);

        // check if element has a namespace
        if(nm.hasURI()) {
          // add to statically known namespaces
          if(!comp && (uri == null || !eq(uri, cu))) ctx.sc.ns.add(cp, cu);
          // add to in-scope namespaces
          if(!ns.contains(cp)) ns.add(cp, cu);
        } else {
          // element has no namespace: assign default uri
          nm.uri(uri);
        }
      }

      // create child and attribute nodes
      final Constr constr = new Constr(ii, ctx).add(expr);
      if(constr.errAtt) NOATTALL.thrw(info);
      if(constr.errNS) NONSALL.thrw(info);
      if(constr.duplAtt != null) CATTDUPL.thrw(info, constr.duplAtt);
      if(constr.duplNS != null) DUPLNSCONS.thrw(info, constr.duplNS);

      // create node
      final FElem node = new FElem(nm, constr.children, constr.atts, ns);

      // add namespaces from constructor
      final Atts cns = constr.nspaces;
      for(int a = 0; a < cns.size(); ++a) {
        addNS(cns.name(a), cns.string(a), ns);
      }

      // add namespaces
      for(int a = 0; a < constr.atts.size(); ++a) {
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
          constr.atts.set(a, new FAttr(
              new QNm(concat(npref, COLON, qnm.local()), auri), att.string()));
        }
      }

      // add inherited namespaces
      final Atts stack = ctx.sc.ns.stack();
      for(int a = stack.size() - 1; a >= 0; a--) {
        final byte[] pref = stack.name(a);
        if(!ns.contains(pref)) ns.add(pref, stack.string(a));
      }

      // update parent references of children
      for(int c = 0; c < constr.children.size(); ++c) {
        final ANode child = constr.children.get(c);
        // add inherited and remove unused namespaces
        if(child.type == NodeType.ELM) {
          if(ctx.sc.nsInherit) inherit(child, ns);
          if(!ctx.sc.nsPreserve) noPreserve(child);
          child.optimize();
        }
      }

      // return generated and optimized node
      return node.optimize();

    } finally {
      ctx.sc.ns.size(s);
    }
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    return new CElem(info, name.copy(ctx, scp, vs), comp ? null : nspaces.copy(),
        copyAll(ctx, scp, vs, expr));
  }

  /**
   * Removes unused namespaces.
   * @param node to be modified
   */
  private static void noPreserve(final ANode node) {
    final Atts ns = node.namespaces();
    final byte[] pref = node.qname().prefix();
    for(int i = ns.size() - 1; i >= 0; i--) {
      boolean f = eq(ns.name(i), pref);
      final AxisIter atts = node.attributes();
      for(ANode it; !f && (it = atts.next()) != null;) {
        f |= eq(it.qname().prefix(), pref);
      }
      if(!f) ns.delete(i);
    }
  }

  /**
   * Inherits namespaces.
   * @param node to be modified
   * @param nsp in-scope namespaces
   */
  private static void inherit(final ANode node, final Atts nsp) {
    final Atts ns = node.namespaces();
    for(int a = nsp.size() - 1; a >= 0; a--) {
      final byte[] pref = nsp.name(a);
      if(!ns.contains(pref)) ns.add(pref, nsp.string(a));
    }
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
  private static byte[] addNS(final byte[] pref, final byte[] uri,
      final Atts ns) {

    final byte[] u = ns.string(pref);
    if(u == null) {
      // add undeclared namespace
      ns.add(pref, uri);
    } else if(!eq(u, uri)) {
      // prefixes with different URIs exist; new one must be replaced
      byte[] apref = null;
      // check if one of the existing prefixes can be adopted
      for(int c = 0; c < ns.size(); c++) {
        if(eq(ns.string(c), uri)) apref = ns.name(c);
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
   * @param ctx query context
   * @return old stack position
   */
  private int prepare(final QueryContext ctx) {
    final NSContext ns = ctx.sc.ns;
    final int s = ns.size();
    for(int n = 0; n < nspaces.size(); n++) ns.add(nspaces.name(n), nspaces.string(n));
    return s;
  }
}
