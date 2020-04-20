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
    super(sc, info, SeqType.NSP_O, name, value);
  }

  @Override
  public FNSpace item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] nm = ncname(true, qc);
    if(nm.length != 0 && !XMLToken.isNCName(nm)) throw INVNSNAME_X.get(info, nm);

    final byte[] value = atomValue(qc);
    if(eq(nm, XML) ^ eq(value, XML_URI)) throw CNXML.get(info);
    if(eq(nm, XMLNS)) throw CNINV_X.get(info, nm);
    if(eq(value, XMLNS_URI) || value.length == 0) throw CNINVNS_X.get(info, value);

    return new FNSpace(nm, value);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new CNSpace(sc, info, name.copy(cc, vm), exprs[0].copy(cc, vm));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CNSpace && super.equals(obj);
  }

  @Override
  public String toString() {
    return toString(NAMESPACE);
  }
}
