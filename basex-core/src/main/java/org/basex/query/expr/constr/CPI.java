package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * PI fragment.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class CPI extends CName {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param name name
   * @param value value
   */
  public CPI(final StaticContext sc, final InputInfo info, final Expr name, final Expr value) {
    super(sc, info, SeqType.PI_O, name, value);
  }

  @Override
  public FPI item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] nm = ncname(false, qc);
    if(eq(lc(nm), XML)) throw CPIXML_X.get(info, nm);
    if(!XMLToken.isNCName(nm)) throw CPIINVAL_X.get(info, nm);

    byte[] value = atomValue(qc);
    int i = -1;
    final int vl = value.length;
    while(++i < vl && value[i] >= 0 && value[i] <= ' ');
    value = substring(value, i);

    return new FPI(new QNm(nm), FPI.parse(value, info));
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new CPI(sc, info, name.copy(cc, vm), exprs[0].copy(cc, vm));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CPI && super.equals(obj);
  }

  @Override
  public String toString() {
    return toString(PROCESSING_INSTRUCTION);
  }
}
