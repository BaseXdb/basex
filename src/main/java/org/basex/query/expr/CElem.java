package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.QNm;
import org.basex.util.Atts;
import org.basex.util.InputInfo;

/**
 * Element fragment.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class CElem extends CName {
  /** Namespaces. */
  private final Atts nsp;
  /** Computed constructor flag. */
  private final boolean comp;

  /**
   * Constructor.
   * @param ii input info
   * @param t tag
   * @param ns namespaces, or {@code null} if this is a computed constructor.
   * @param cont element contents
   */
  public CElem(final InputInfo ii, final Expr t, final Atts ns,
      final Expr... cont) {

    super(ELEMENT, ii, t, cont);
    nsp = ns == null ? new Atts() : ns;
    comp = ns == null;
  }

  @Override
  public CElem comp(final QueryContext ctx) throws QueryException {
    final int s = prepare(ctx);
    super.comp(ctx);
    ctx.ns.size(s);
    return this;
  }

  @Override
  public FElem item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final int s = prepare(ctx);
    try {
      // adds namespaces for the element constructor
      final Atts cns = new Atts();
      for(int i = 0; i < nsp.size(); ++i) cns.add(nsp.key(i), nsp.value(i));

      // create and check QName
      final QNm nm = qname(ctx, ii);
      final byte[] cp = nm.prefix(), cu = nm.uri();
      if(eq(cp, XML) ^ eq(cu, XMLURI)) CEXML.thrw(input, cu, cp);
      if(eq(cu, XMLNSURI)) CEINV.thrw(input, cu);
      if(eq(cp, XMLNS)) CEINV.thrw(input, cp);

      // analyze element namespace unless it is "xml"
      if(!eq(cp, XML)) {
        // request namespace for the specified uri
        final byte[] uri = ctx.ns.uri(cp);

        // check if element has a namespace
        if(nm.hasURI()) {
          // overwrite namespace declaration
          if(!comp && (uri == null || !eq(uri, cu))) {
            ctx.ns.add(cp, cu);
          }
          // add declaration if it does not already exist
          if(!cns.contains(cp)) cns.add(cp, cu);
        } else {
          // element has no namespace: assign default uri
          nm.uri(uri);
        }
      }

      // create child and attribute nodes
      final Constr c = new Constr(ii, ctx, expr);
      if(c.errAtt) NOATTALL.thrw(input);
      if(c.errNS) NONSALL.thrw(input);
      if(c.duplAtt != null) (comp ? CATTDUPL : ATTDUPL).thrw(input, c.duplAtt);
      if(c.duplNS != null) DUPLNSCONS.thrw(input, c.duplNS);

      // add computed namespaces
      for(int n = 0; n < c.ns.size(); ++n) {
        cns.add(c.ns.key(n), c.ns.value(n));
      }

      // create node and update parent references of child nodes
      final FElem node = new FElem(nm, c.children, c.atts, cns);
      for(int n = 0; n < c.children.size(); ++n) {
        c.children.get(n).parent(node);
      }

      // update attributes: set parent references, add undeclared namespaces
      for(int n = 0; n < c.atts.size(); ++n) {
        final ANode att = c.atts.get(n).parent(node);
        final QNm qnm = att.qname();
        // skip attributes without prefixes or URIs
        if(!qnm.hasPrefix() || !qnm.hasURI()) continue;
        // skip XML namespace
        byte[] apref = qnm.prefix();
        if(eq(apref, XML)) continue;

        final byte[] auri = qnm.uri();
        final int pos = cns.get(apref);
        if(pos == -1) {
          // add undeclared namespace
          cns.add(apref, auri);
        } else if(!eq(cns.value(pos), auri)) {
          // prefixes with different URIs exist; new one must be replaced
          apref = null;
          // check if one of the existing prefixes can be adopted
          for(int a = 0; a < cns.size(); a++) {
            if(eq(cns.value(a), auri)) apref = cns.key(a);
          }
          // if negative, generate a new one that is not used yet
          if(apref == null) {
            int i = 1;
            do {
              apref = concat(qnm.prefix(), new byte[] { '_' }, token(i++));
            } while(cns.contains(apref));
            cns.add(apref, auri);
          }
          // overwrite existing attribute with new one
          c.atts.item[n] = new FAttr(new QNm(concat(apref, COLON,
              qnm.local()), qnm.uri()), c.atts.get(n).string());
        }
      }
      // return generated node
      return node;

    } finally {
      ctx.ns.size(s);
    }
  }

  /**
   * Adds namespaces to the namespace stack.
   * @param ctx query context
   * @return old stack position
   */
  private int prepare(final QueryContext ctx) {
    int s = ctx.ns.size();
    for(int n = 0; n < nsp.size(); n++) {
      ctx.ns.add(nsp.key(n), nsp.value(n));
    }
    return s;
  }
}
