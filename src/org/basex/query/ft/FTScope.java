package org.basex.query.ft;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.ft.Tokenizer;
import org.basex.query.QueryContext;
import org.basex.query.QueryTokens;
import org.basex.query.item.FTItem;
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
  boolean filter(final QueryContext ctx, final FTItem n, final Tokenizer ft) {
    if(!same) return diff(new BoolList(), 0, n.pos, ft);

    for(int i = 0; i < n.pos[0].size; i++) {
      if(same(pos(n.pos[0].list[i], unit, ft), 1, n.pos, ft)) return true;
    }
    return false;
  }

  /**
   * Recursively checks if all words are found in the same units.
   * @param v value to be compared
   * @param n current position
   * @param ft tokenizer
   * @param pos position list
   * @return result of check
   */
  private boolean same(final int v, final int n, final IntList[] pos,
      final Tokenizer ft) {
    if(n == pos.length) return true;
    for(int i = 0; i < pos[n].size; i++) {
      if(pos(pos[n].list[i], unit, ft) == v && same(v, n + 1, pos, ft))
        return true;
    }
    return false;
  }

  /**
   * Recursively checks if all words are found in different units.
   * @param bl boolean list
   * @param n current position
   * @param pos position list
   * @param ft tokenizer
   * @return result of check
   */
  private boolean diff(final BoolList bl, final int n, final IntList[] pos,
      final Tokenizer ft) {
    if(n == pos.length) return true;
    for(int i = 0; i < pos[n].size; i++) {
      final int p = pos(pos[n].list[i], unit, ft);
      if(p < bl.size && bl.list[p]) continue;
      bl.set(true, p);
      if(diff(bl, n + 1, pos, ft)) return true;
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
