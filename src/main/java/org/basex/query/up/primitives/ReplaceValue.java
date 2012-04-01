package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Replace value primitive.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class ReplaceValue extends ValueUpdate {
  /** New value. */
  private final byte[] value;

  /**
   * Constructor.
   * @param p pre
   * @param d data
   * @param i input info
   * @param v new value
   */
  public ReplaceValue(final int p, final Data d, final InputInfo i,
      final byte[] v) {
    super(PrimitiveType.REPLACEVALUE, p, d, i);
    value = v;
  }

  @Override
  public void apply() {
    final int kind = data.kind(pre);
    if(kind == Data.TEXT && value.length == 0) {
      // empty text nodes must be removed
      data.delete(pre);
    } else {
      data.update(pre, kind, value);
    }
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    UPMULTREPV.thrw(info, targetNode());
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public String toString() {
    return Util.info("%[%, %]", Util.name(this), targetNode(), value);
  }
}
