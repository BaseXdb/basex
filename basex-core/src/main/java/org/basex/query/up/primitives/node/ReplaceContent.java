package org.basex.query.up.primitives.node;

import org.basex.data.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.list.*;
import org.basex.util.*;

/**
 * Replace element content primitive, extending insert into primitive.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Lukas Kircher
 */
public final class ReplaceContent extends InsertInto {
  /**
   * Constructor.
   * @param pre target pre value
   * @param data target data instance
   * @param info input info (can be {@code null})
   * @param nodes node copy insertion sequence
   */
  ReplaceContent(final int pre, final Data data, final InputInfo info, final ANodeList nodes) {
    super(pre, data, info, nodes);
  }

  @Override
  public void merge(final Update update) {
  }
}
