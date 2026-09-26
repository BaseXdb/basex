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
 * @author BaseX Team, BSD License
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
    super(info, Types.PROCESSING_INSTRUCTION_O, computed, name, value);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    name = name.simplifyFor(Simplify.STRING, cc);
    if(name instanceof Value) {
      final byte[] nm = ncname(false, cc.qc);
      name = Str.get(nm);
      exprType.assign(NodeType.get(NameTest.get(new QNm(nm), Kind.PROCESSING_INSTRUCTION)));
    }
    optValue(cc);
    return this;
  }

  @Override
  public FPI value(final QueryContext qc) throws QueryException {
    final byte[] nm = ncname(false, qc);
    if(eq(lc(nm), XML)) throw CPIXML_X.get(info, nm);
    if(!XMLToken.isNCName(nm)) throw CPIINVAL_X.get(info, nm);

    final byte[] value = FPI.content(atomValue(qc, true), info);
    return new FPI(qc.shared.qName(nm), qc.shared.token(value));
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyNode(new CPI(info, computed, name.copy(cc, vm), exprs[0].copy(cc, vm)));
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
      qs.pi(((Str) name).string(), ((Str) exprs[0]).string());
    }
  }
}
