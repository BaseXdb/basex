package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Replace element content primitive.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class ReplaceElementContent extends StructuralUpdate {
  /** New value. */
  private final byte[] value;

  /**
   * Constructor.
   * @param p pre
   * @param d data
   * @param i input info
   * @param val new value
   */
  public ReplaceElementContent(final int p, final Data d, final InputInfo i,
      final byte[] val) {
    super(PrimitiveType.REPLACEELEMCONT, p, d, i);
    value = val;
  }

  @Override
  public void apply() {
    /*
     * Kind is hard coded because the target of a replace element content
     * expression can only be an element node.
     */
    final int kind = Data.ELEM;
    final int loc = pre + data.attSize(pre, kind);

    /*
     * Attributes are not affected by this expression.
     *
     * As a result of this expression all child nodes are deleted and replaced
     * by either a single text node or no node at all (depends on wheter the
     * replacing value is an empty string or not).
     */
    shifts = data.size(pre, kind) - data.attSize(pre, kind) - 1 +
      value.length == 0 ? 0 : 1;

    if(pre + data.size(pre, kind) == loc + 1 && data.kind(loc) == Data.TEXT) {
      // overwrite existing text node
      data.update(loc, Data.TEXT, value);
    } else {
      while(pre + data.size(pre, kind) > loc) data.delete(loc);
      if(value.length > 0) {
        final MemData md = new MemData(data);
        md.text(pre, loc - pre, value, Data.TEXT);
        md.insert(0);
        data.insert(loc, pre, md);
      }
    }
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    UPMULTREPV.thrw(input, targetNode());
  }

  @Override
  public boolean adjacentTexts(final int c) {
    return false;
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
