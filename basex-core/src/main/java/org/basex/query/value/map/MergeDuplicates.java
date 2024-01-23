package org.basex.query.value.map;

import org.basex.util.options.*;

/**
 * Duplicate handling.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public enum MergeDuplicates {
  /** Reject.    */ REJECT,
  /** Use first. */ USE_FIRST,
  /** Use last.  */ USE_LAST,
  /** Use any.   */ USE_ANY,
  /** Combine.   */ COMBINE;

  @Override
  public String toString() {
    return EnumOption.string(name());
  }
}
