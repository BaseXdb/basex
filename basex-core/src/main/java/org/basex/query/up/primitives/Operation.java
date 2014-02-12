package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Abstract class for update operations.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public abstract class Operation {
  /** Input info. */
  final InputInfo info;
  /** Target data reference. */
  public Data data;

  /**
   * Constructor.
   * @param d target data reference
   * @param ii input info
   */
  Operation(final Data d, final InputInfo ii) {
    data = d;
    info = ii;
  }

  /**
   * Returns the total number of node operations.
   * @return number of updates
   */
  public abstract int size();

  /**
   * Returns the target data reference.
   * @return data
   */
  public abstract Data getData();

  /**
   * Input info.
   * @return input info
   */
  public abstract InputInfo getInfo();

  /**
   * Returns the target node of this operation.
   * @return target node
   */
  public abstract DBNode getTargetNode();
}
