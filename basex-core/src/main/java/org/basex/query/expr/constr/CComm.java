package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Comment constructor.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class CComm extends CNode {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param computed computed constructor
   * @param comment comment
   */
  public CComm(final StaticContext sc, final InputInfo info, final boolean computed,
      final Expr comment) {
    super(sc, info, SeqType.COMMENT_O, computed, comment);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    exprs = simplifyAll(Simplify.STRING, cc);
    optValue(cc);
    return this;
  }

  @Override
  public FComm item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return new FComm(qc.shared.token(FComm.parse(atomValue(qc, true), info)));
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CComm(sc, info, computed, exprs[0].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CComm && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    if(computed) {
      toString(qs, COMMENT);
    } else {
      qs.concat("<!--", QueryString.toValue(((Str) exprs[0]).string()), "-->");
    }
  }
}
