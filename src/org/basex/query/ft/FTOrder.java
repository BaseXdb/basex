package org.basex.query.ft;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryTokens;
import org.basex.util.IntList;

/**
 * FTOrder expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FTOrder extends FTFilter {
  @Override
  public boolean filter(final QueryContext ctx) {
    if(sel.size == 1) return true;

    final IntList[] il = sortPositions();
    final IntList p = il[0];
    final IntList pp = il[1];
    int i = 0;
    int lp;
    while(i < p.size && pp.list[i] != 0) i++;
    lp = i;
    i++;
    while(i < p.size) {
      if(pp.list[i] < pp.list[lp] || pp.list[i] == pp.list[lp] + 1) lp = i;
      if(pp.list[lp] == sel.size - 1) return true;
      i++;
    }
    return false;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.attribute(token(QueryTokens.ORDERED), TRUE);
  }

  @Override
  public String toString() {
    return QueryTokens.ORDERED;
  }
}
