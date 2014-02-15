package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Replace element content primitive, extending insert into primitive.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class ReplaceContent extends InsertInto {
  /**
   * Constructor.
   * @param p target pre value
   * @param d target data instance
   * @param i input info
   * @param n node copy insertion sequence
   */
  public ReplaceContent(final int p, final Data d, final InputInfo i, final ANodeList n) {
    super(p, d, i, n);
  }
}
