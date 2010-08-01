package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryInfo;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
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
   * @param i query info
   * @param n name
   */
  protected CFrag(final QueryInfo i, final Expr... n) {
    super(i, n);
  }

  @Override
  public abstract Nod atomic(final QueryContext ctx) throws QueryException;

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
      if(contains(nm, ' ')) error(INVAL, nm);
      if(!XMLToken.isQName(nm)) error(NAMEWRONG, nm);
      name = new QNm(nm);
    }

    if(name.uri == Uri.EMPTY) name.uri = Uri.uri(ctx.ns.uri(name.pref(),
        name != i));
    return name;
  }

  @Override
  public final boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.FRG || super.uses(u, ctx);
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return SeqType.NOD;
  }

  @Override
  public String color() {
    return "FF9999";
  }

  /**
   * Returns a string info for the expression.
   * @param pre info prefix
   * @return string
   */
  protected final String info(final String pre) {
    return pre + " constructor";
  }

  /**
   * Returns a string representation of the expression.
   * @param pre expression prefix
   * @return string
   */
  protected final String toString(final String pre) {
    return pre + " { " + super.toString(", ") + " }";
  }
}
