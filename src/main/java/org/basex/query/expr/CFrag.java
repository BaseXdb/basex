package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.XMLToken;

/**
 * Fragment constructor.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  public abstract Nod atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  /**
   * Returns an updated name expression.
   * @param ctx query context
   * @param i item
   * @return result
   * @throws QueryException query exception
   */
  final QNm qname(final QueryContext ctx, final Item i) throws QueryException {
    QNm name = null;
    if(i.type == Type.QNM) {
      name = (QNm) i;
    } else {
      final byte[] nm = i.atom();
      if(contains(nm, ' ')) Err.or(input, INVAL, nm);
      if(!XMLToken.isQName(nm)) Err.or(input, NAMEWRONG, nm);
      name = new QNm(nm);
    }

    if(name.uri == Uri.EMPTY) {
      name.uri = Uri.uri(ctx.ns.uri(name.pref(), name != i, input));
    }
    return name;
  }

  @Override
  public final boolean uses(final Use u) {
    return u == Use.FRG || super.uses(u);
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
    return pref + " { " + super.toString(", ") + " }";
  }
}
