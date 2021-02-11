package org.basex.query.value.map;

import java.util.*;

/**
 * Duplicate handling.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum MergeDuplicates {
  /** Reject.      */ REJECT,
  /** Use first.   */ USE_FIRST,
  /** Use last.    */ USE_LAST,
  /** Combine.     */ COMBINE,
  /** Unspecified. */ UNSPECIFIED;

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH).replace('_', '-');
  }
}
