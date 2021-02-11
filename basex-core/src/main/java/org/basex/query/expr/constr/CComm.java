package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Comment fragment.
 *
 * @author BaseX Team 2005-21, BSD License
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
    simplifyAll(Simplify.STRING, cc);
    return this;
  }

  @Override
  public FComm item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = exprs[0].atomIter(qc, info);

    final TokenBuilder tb = new TokenBuilder();
    boolean more = false;
    for(Item item; (item = qc.next(iter)) != null;) {
      if(more) tb.add(' ');
      tb.add(item.string(info));
      more = true;
    }
    return new FComm(FComm.parse(tb.finish(), info));
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
  public void plan(final QueryString qs) {
    if(computed) {
      plan(qs, COMMENT);
    } else {
      qs.concat("<!--", QueryString.toValue(((Str) exprs[0]).string()), "-->");
    }
  }
}
