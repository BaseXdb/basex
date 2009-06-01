package org.basex.query.ft;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryTokens;
import org.basex.query.item.FTNode;
import org.basex.util.BoolList;
import org.basex.util.IntList;

/**
 * FTScope expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FTScope extends FTFilter {
  /** Same/different flag. */
  private final boolean same;

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
  boolean filter(final QueryContext ctx, final FTNode node) {
    if(!same) return diff(new BoolList(), 0, node.pos);

    for(int i = 0; i < node.pos[0].size; i++) {
      if(same(pos(node.pos[0].list[i], unit), 1, node.pos)) return true;
    }
    return false;
  }

  /**
   * Recursively checks if all words are found in the same units.
   * @param v value to be compared
   * @param n current position
   * @param pos position list
   * @return result of check
   */
  private boolean same(final int v, final int n, final IntList[] pos) {
    if(n == pos.length) return true;
    for(int i = 0; i < pos[n].size; i++) {
      if(pos(pos[n].list[i], unit) == v && same(v, n + 1, pos)) return true;
    }
    return false;
  }

  /**
   * Recursively checks if all words are found in different units.
   * @param bl boolean list
   * @param n current position
   * @param pos position list
   * @return result of check
   */
  private boolean diff(final BoolList bl, final int n, final IntList[] pos) {
    if(n == pos.length) return true;
    for(int i = 0; i < pos[n].size; i++) {
      final int p = pos(pos[n].list[i], unit);
      if(p < bl.size && bl.list[p]) continue;
      bl.set(true, p);
      if(diff(bl, n + 1, pos)) return true;
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
