package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
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
 * Processing instruction constructor.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class CPI extends CName {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param computed computed constructor
   * @param name name
   * @param value value
   */
  public CPI(final InputInfo info, final boolean computed, final Expr name, final Expr value) {
    super(info, SeqType.PROCESSING_INSTRUCTION_O, computed, name, value);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    name = name.simplifyFor(Simplify.STRING, cc);
    if(name instanceof Value) {
      final byte[] nm = ncname(false, cc.qc);
      name = Str.get(nm);
      exprType.assign(SeqType.get(NodeType.PROCESSING_INSTRUCTION, Occ.EXACTLY_ONE,
          Test.get(NodeType.PROCESSING_INSTRUCTION, new QNm(nm), null)));
    }
    optValue(cc);
    return this;
  }

  @Override
  public FPI item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] nm = ncname(false, qc);
    if(eq(lc(nm), XML)) throw CPIXML_X.get(info, nm);
    if(!XMLToken.isNCName(nm)) throw CPIINVAL_X.get(info, nm);

    byte[] value = atomValue(qc, true);
    int i = -1;
    final int vl = value.length;
    while(++i < vl && ws(value[i]));
    value = substring(value, i);

    return new FPI(qc.shared.qName(nm), qc.shared.token(FPI.parse(value, info)));
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CPI(info, computed, name.copy(cc, vm), exprs[0].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CPI && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    if(computed) {
      toString(qs, QueryText.PROCESSING_INSTRUCTION);
    } else {
      qs.concat(FPI.OPEN, ((Str) name).string(), " ",
          QueryString.toValue(((Str) exprs[0]).string()), FPI.CLOSE);
    }
  }
}
