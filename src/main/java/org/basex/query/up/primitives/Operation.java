package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Basic Operation interface.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public interface Operation {

  /**
   * Returns the total number of node operations.
   * @return number of updates
   */
  int size();

  /**
   * Returns the target data reference.
   * @return data
   */
  Data getData();

  /**
   * Input info.
   * @return input info
   */
  InputInfo getInfo();

  /**
   * Returns the target node of this operation.
   * @return target node
   */
  DBNode getTargetNode();
}
