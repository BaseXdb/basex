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
   * @param pre target pre value
   * @param data target data instance
   * @param ii input info
   * @param nodes node copy insertion sequence
   */
  public ReplaceContent(final int pre, final Data data, final InputInfo ii, final ANodeList nodes) {
    super(pre, data, ii, nodes);
  }
}
