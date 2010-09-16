package org.basex.query.expr;

import static org.basex.query.util.Err.XPATT;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.item.FDoc;
import org.basex.query.item.Type;
import org.basex.util.InputInfo;

/**
 * Document fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CDoc extends CFrag {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   */
  public CDoc(final InputInfo ii, final Expr e) {
    super(ii, e);
  }

  @Override
  public FDoc item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Constr con = new Constr(ctx, expr);
    if(con.errAtt || con.ats.size() != 0) XPATT.thrw(ii);

    final FDoc doc = new FDoc(con.children, con.base);
    for(int n = 0; n < con.children.size(); ++n)
      con.children.get(n).parent(doc);
    return doc;
  }

  @Override
  public String desc() {
    return info(QueryTokens.DOCUMENT);
  }

  @Override
  public String toString() {
    return toString(Type.DOC.name);
  }
}
