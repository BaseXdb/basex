package org.basex.query.util.ft;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Match full-text container, referencing several {@link FTStringMatch} instances.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FTMatch extends ObjectList<FTStringMatch, FTMatch> {
  /**
   * Constructor.
   */
  public FTMatch() {
    this(0);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity
   */
  public FTMatch(final long capacity) {
    super(new FTStringMatch[Array.checkCapacity(capacity)]);
  }

  /**
   * Checks if the full-text match is not part of the specified match.
   * @param ftm match to be checked
   * @return result of check
   */
  public boolean notin(final FTMatch ftm) {
    for(final FTStringMatch s : this) {
      for(final FTStringMatch sm : ftm) {
        if(!s.in(sm)) return true;
      }
    }
    return false;
  }

  /**
   * Checks if the match contains no string excludes.
   * @return result of check
   */
  boolean match() {
    for(final FTStringMatch s : this) {
      if(s.exclude) return false;
    }
    return true;
  }

  /**
   * Sorts the matches by their start and end positions.
   */
  public void sort() {
    Arrays.sort(list, 0, size, null);
  }

  @Override
  protected FTStringMatch[] newArray(final int s) {
    return new FTStringMatch[s];
  }
}
