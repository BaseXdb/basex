package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * PI fragment.
 *
 * @author BaseX Team 2005-14, BSD License
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
    super(PI, sc, info, name, value);
    seqType = SeqType.PI;
  }

  @Override
  public FPI item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = checkItem(name, qc);
    final Type ip = it.type;

    final QNm qnm;
    if(ip == AtomType.QNM) {
      qnm = (QNm) it;
    } else {
      if(!ip.isStringOrUntyped() || ip == AtomType.URI) throw CPIWRONG.get(info, ip, it);

      final byte[] nm = trim(it.string(ii));
      if(eq(lc(nm), XML)) throw CPIXML.get(info, nm);
      if(!XMLToken.isNCName(nm)) throw CPIINVAL.get(info, nm);
      qnm = new QNm(nm);
    }

    byte[] v = value(qc, ii);
    int i = -1;
    final int vl = v.length;
    while(++i < vl && v[i] >= 0 && v[i] <= ' ');
    v = substring(v, i);
    return new FPI(qnm, FPI.parse(v, info));
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new CPI(sc, info, name.copy(qc, scp, vs), exprs[0].copy(qc, scp, vs));
  }
}
