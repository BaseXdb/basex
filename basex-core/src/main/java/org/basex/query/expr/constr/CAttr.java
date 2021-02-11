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
 * @author BaseX Team 2005-21, BSD License
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
      final QNm nm = qname(true, cc.qc, null);
      if(nm != null) {
        name = nm;
        exprType.assign(SeqType.get(NodeType.ATTRIBUTE, Occ.EXACTLY_ONE,
            Test.get(NodeType.ATTRIBUTE, nm)));
      }
    }
    optValue(cc);
    return this;
  }

  @Override
  public FAttr item(final QueryContext qc, final InputInfo ii) throws QueryException {
    QNm nm = qname(false, qc, sc);
    final byte[] cp = nm.prefix();
    if(computed) {
      final byte[] cu = nm.uri();
      if(eq(cp, XML) ^ eq(cu, XML_URI)) throw CAXML.get(info);
      if(eq(cu, XMLNS_URI)) throw CAINV_.get(info, cu);
      if(eq(cp, XMLNS) || cp.length == 0 && eq(nm.string(), XMLNS))
        throw CAINV_.get(info, nm.string());

      // create new standard namespace to cover most frequent cases
      if(eq(cp, EMPTY) && !eq(cu, EMPTY))
        nm = new QNm(concat(NS0, nm.string()), cu);
    }
    if(!nm.hasURI() && nm.hasPrefix()) throw INVPREF_X.get(info, nm);

    byte[] value = atomValue(qc);
    if(eq(cp, XML) && eq(nm.local(), ID)) value = normalize(value);

    return new FAttr(nm, value);
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
  public void plan(final QueryString qs) {
    if(computed) {
      plan(qs, ATTRIBUTE);
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
