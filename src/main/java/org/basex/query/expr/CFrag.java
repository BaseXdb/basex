package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.AtomType;
import org.basex.query.item.Type;
import org.basex.util.InputInfo;
import org.basex.util.XMLToken;

/**
 * Fragment constructor.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class CFrag extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param n name
   */
  protected CFrag(final InputInfo ii, final Expr... n) {
    super(ii, n);
    type = SeqType.NOD;
  }

  @Override
  public abstract ANode item(final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  /**
   * Returns an updated name expression.
   * @param ctx query context
   * @param it item
   * @param att attribute flag
   * @param ii input info
   * @return result
   * @throws QueryException query exception
   */
  final QNm qname(final QueryContext ctx, final Item it, final boolean att,
      final InputInfo ii) throws QueryException {

    QNm n = null;
    final Type ip = it.type;
    if(ip == AtomType.QNM) {
      n = (QNm) it;
    } else {
      final byte[] nm = it.string(ii);
      if(!XMLToken.isQName(nm)) {
        (ip.isString() || ip.isUntyped() ? INVNAME : INVQNAME).thrw(input, nm);
      }
      n = new QNm(nm);
    }

    // attributes don't inherit namespaces
    if(!n.hasUri()) {
      n.uri(att && !n.ns() ? EMPTY : ctx.ns.uri(n.pref(), n != it, input));
    }
    return n;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CNS || super.uses(u);
  }

  /**
   * Returns a string info for the expression.
   * @param pref info prefix
   * @return string
   */
  protected final String info(final String pref) {
    return pref + " constructor";
  }

  @Override
  protected final String toString(final String pref) {
    final StringBuilder sb = new StringBuilder(pref).append(" { ");
    sb.append(expr.length == 0 ? "()" : super.toString(SEP));
    return sb.append(" }").toString();
  }
}
