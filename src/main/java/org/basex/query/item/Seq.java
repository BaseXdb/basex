package org.basex.query.item;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;

/**
 * Sequence, containing at least two items.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Seq extends Value {
  /** Length. */
  protected final long size;

  /**
   * Constructor.
   * @param s size
   */
  protected Seq(final long s) {
    super(Type.SEQ);
    size = s;
  }

  /**
   * Returns a sequence for the specified items.
   * @param v value
   * @param s size
   * @return resulting item or sequence
   */
  public static Value get(final Item[] v, final int s) {
    return s == 0 ? Empty.SEQ : s == 1 ? v[0] : new ItemSeq(v, s);
  }

  @Override
  public final long size() {
    return size;
  }

  @Override
  public final Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    Err.or(ii, XPSEQ, this);
    return null;
  }

  @Override
  public final Item test(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return ebv(ctx, ii);
  }

  @Override
  public boolean duplicates() {
    return true;
  }
}
