package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Namespace constructor.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class CNSpace extends CName {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param name name
   * @param value value
   */
  public CNSpace(final StaticContext sc, final InputInfo info, final Expr name, final Expr value) {
    super(NAMESPACE, sc, info, SeqType.NSP_O, name, value);
  }

  @Override
  public FNSpace item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] cp = trim(toZeroToken(name, qc));
    if(cp.length != 0 && !XMLToken.isNCName(cp)) throw INVNSNAME_X.get(info, cp);

    final byte[] cu = atomValue(qc);
    if(eq(cp, XML) ^ eq(cu, XML_URI)) throw CNXML.get(info);
    if(eq(cp, XMLNS)) throw CNINV_X.get(info, cp);
    if(eq(cu, XMLNS_URI) || cu.length == 0) throw CNINVNS_X.get(info, cu);

    return new FNSpace(cp, cu);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new CNSpace(sc, info, name.copy(cc, vm), exprs[0].copy(cc, vm));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CNSpace && super.equals(obj);
  }
}
