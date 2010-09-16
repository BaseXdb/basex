package org.basex.query.up;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Arr;
import org.basex.query.expr.Expr;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.iter.NodIter;
import org.basex.util.InputInfo;

/**
 * Abstract update expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
abstract class Update extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e expressions
   */
  protected Update(final InputInfo ii, final Expr... e) {
    super(ii, e);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.UPD || super.uses(u);
  }

  /**
   * Checks if the new namespaces have conflicting namespaces.
   * @param list node list
   * @param targ target node
   * @param ctx query context
   * @throws QueryException query exception
   * @return specified node list
   */
  protected final NodIter checkNS(final NodIter list, final Nod targ,
      final QueryContext ctx) throws QueryException {

    for(int a = 0; a < list.size(); ++a) {
      final QNm name = list.get(a).qname();
      final byte[] pref = name.pref();
      // attributes without prefix have no namespace
      if(pref.length == 0) continue;
      // check if attribute and target have the same namespace
      final byte[] uri = targ.uri(pref, ctx);
      if(uri != null && !eq(name.uri().atom(), uri)) UPNSCONFL.thrw(input);
    }
    return list;
  }
}
