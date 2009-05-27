package org.basex.query.ft;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryTokens;
import org.basex.util.BoolList;

/**
 * FTScope expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FTScope extends FTFilter {
  /** Same/different flag. */
  private boolean same;

  /**
   * Constructor.
   * @param u unit
   * @param s same flag
   */
  public FTScope(final FTUnit u, final boolean s) {
    unit = u;
    same = s;
  }

  @Override
  public boolean filter(final QueryContext ctx) {
    if(!same) return diff(new BoolList(), 0);

    for(int i = 0; i < sel.pos[0].size; i++) {
      if(same(pos(sel.pos[0].list[i], unit), 1)) return true;
    }
    return false;
  }

  /**
   * Recursively checks if all words are found in the same units.
   * @param v value to be compared
   * @param n current position
   * @return result of check
   */
  private boolean same(final int v, final int n) {
    if(n == sel.size) return true;
    for(int i = 0; i < sel.pos[n].size; i++) {
      if(pos(sel.pos[n].list[i], unit) == v && same(v, n + 1)) return true;
    }
    return false;
  }

  /**
   * Recursively checks if all words are found in different units.
   * @param bl boolean list
   * @param n current position
   * @return result of check
   */
  private boolean diff(final BoolList bl, final int n) {
    if(n == sel.size) return true;
    for(int i = 0; i < sel.pos[n].size; i++) {
      final int p = pos(sel.pos[n].list[i], unit);
      if(p < bl.size && bl.list[p]) continue;
      bl.set(true, p);
      if(diff(bl, n + 1)) return true;
      bl.set(false, p);
    }
    return false;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.attribute(token(same ? QueryTokens.SAME : QueryTokens.DIFFERENT),
        token(unit.toString()));
  }

  @Override
  public String toString() {
    return (same ? QueryTokens.SAME : QueryTokens.DIFFERENT) + " " + unit;
  }
}
