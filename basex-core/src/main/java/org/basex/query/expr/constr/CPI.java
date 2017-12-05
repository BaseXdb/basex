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
 * @author BaseX Team 2005-17, BSD License
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
    super(PI, sc, info, SeqType.PI_O, name, value);
  }

  @Override
  public FPI item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = checkNoEmpty(name.atomItem(qc, info), AtomType.QNM);
    final Type type = item.type;
    final QNm qnm;
    if(type == AtomType.QNM) {
      qnm = (QNm) item;
    } else {
      if(!type.isStringOrUntyped() || type == AtomType.URI)
        throw CPIWRONG_X_X.get(info, type, item);

      final byte[] nm = trim(item.string(info));
      if(eq(lc(nm), XML)) throw CPIXML_X.get(info, nm);
      if(!XMLToken.isNCName(nm)) throw CPIINVAL_X.get(info, nm);
      qnm = new QNm(nm);
    }

    byte[] v = atomValue(qc);
    int i = -1;
    final int vl = v.length;
    while(++i < vl && v[i] >= 0 && v[i] <= ' ');
    v = substring(v, i);

    return new FPI(qnm, FPI.parse(v, info));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CPI && super.equals(obj);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new CPI(sc, info, name.copy(cc, vm), exprs[0].copy(cc, vm));
  }
}
