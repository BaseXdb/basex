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
 * PI fragment.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CPI extends CName {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param computed computed constructor
   * @param name name
   * @param value value
   */
  public CPI(final StaticContext sc, final InputInfo info, final boolean computed, final Expr name,
      final Expr value) {
    super(sc, info, SeqType.PROCESSING_INSTRUCTION_O, computed, name, value);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    name = name.simplifyFor(Simplify.STRING, cc);
    if(name instanceof Value) {
      final byte[] nm = ncname(false, cc.qc);
      if(nm != null) {
        name = Str.get(nm);
        exprType.assign(SeqType.get(NodeType.PROCESSING_INSTRUCTION, Occ.EXACTLY_ONE,
            Test.get(NodeType.PROCESSING_INSTRUCTION, new QNm(nm))));
      }
    }
    optValue(cc);
    return this;
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
    return copyType(new CPI(sc, info, computed, name.copy(cc, vm), exprs[0].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CPI && super.equals(obj);
  }

  @Override
  public void plan(final QueryString qs) {
    if(computed) {
      plan(qs, PROCESSING_INSTRUCTION);
    } else {
      qs.concat(FPI.OPEN, ((Str) name).string(), " ",
          QueryString.toValue(((Str) exprs[0]).string()), FPI.CLOSE);
    }
  }
}
