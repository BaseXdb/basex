package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Attribute constructor.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class CAttr extends CName {
  /** Generated namespace. */
  private static final byte[] NS0 = token("ns0:");

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param name name
   * @param computed computed construction flag
   * @param value attribute value
   */
  public CAttr(final StaticContext sc, final InputInfo info, final boolean computed,
      final Expr name, final Expr... value) {
    super(sc, info, SeqType.ATTRIBUTE_O, computed, name, value);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    name = name.simplifyFor(Simplify.STRING, cc);
    if(name instanceof Value) {
      final QNm nm = qname(false, cc.qc);
      name = nm;
      exprType.assign(SeqType.get(NodeType.ATTRIBUTE, Occ.EXACTLY_ONE,
          Test.get(NodeType.ATTRIBUTE, nm, null)));
    }
    optValue(cc);
    return this;
  }

  @Override
  public FAttr item(final QueryContext qc, final InputInfo ii) throws QueryException {
    QNm nm = qname(false, qc);
    final byte[] nmPrefix = nm.prefix(), nmUri = nm.uri();
    if(computed) {
      if(eq(nmPrefix, XML) ^ eq(nmUri, XML_URI)) throw CAXML.get(info);
      if(eq(nmUri, XMLNS_URI)) throw CAINV_.get(info, nmUri);
      if(eq(nmPrefix, XMLNS) || nmPrefix.length == 0 && eq(nm.string(), XMLNS))
        throw CAINV_.get(info, nm.string());

      // create new standard namespace to cover most frequent cases
      if(eq(nmPrefix, EMPTY) && !eq(nmUri, EMPTY))
        nm = qc.pool.qnm(concat(NS0, nm.string()), nmUri);
    }
    if(!nm.hasURI() && nm.hasPrefix()) throw NOQNNAMENS_X.get(info, nmPrefix);

    byte[] value = atomValue(qc, true);
    if(eq(nmPrefix, XML) && eq(nm.local(), ID)) value = normalize(value);

    return new FAttr(nm, qc.pool.token(value));
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CAttr(sc, info, computed, name.copy(cc, vm), copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CAttr && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    if(computed) {
      toString(qs, ATTRIBUTE);
    } else {
      qs.token(((QNm) name).string()).token('=');
      if(exprs.length == 1 && exprs[0] instanceof Str) {
        qs.quoted(((Str) exprs[0]).string());
      } else {
        qs.token("\"{").tokens(exprs, SEP).token("}\"");
      }
    }
  }
}
