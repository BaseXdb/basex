package org.basex.data;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Match full-text container, referencing several {@link FTStringMatch} instances.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTMatch extends ElementList implements Iterable<FTStringMatch> {
  /** String matches. */
  FTStringMatch[] match;

  /**
   * Constructor.
   */
  public FTMatch() {
    this(0);
  }

  /**
   * Constructor, specifying an initial internal array size.
   * @param capacity initial array capacity
   */
  public FTMatch(final int capacity) {
    match = new FTStringMatch[capacity];
  }

  /**
   * Adds a all matches of a full-text match.
   * @param m match to be added
   * @return self reference
   */
  public FTMatch add(final FTMatch m) {
    for(final FTStringMatch sm : m) add(sm);
    return this;
  }

  /**
   * Adds a single string match.
   * @param m match to be added
   * @return self reference
   */
  public FTMatch add(final FTStringMatch m) {
    if(size == match.length) match = Array.copy(match, new FTStringMatch[newSize()]);
    match[size++] = m;
    return this;
  }

  /**
   * Checks if the full-text match is not part of the specified match.
   * @param m match to be checked
   * @return result of check
   */
  public boolean notin(final FTMatch m) {
    for(final FTStringMatch s : this) {
      for(final FTStringMatch sm : m) if(!s.in(sm)) return true;
    }
    return false;
  }

  /**
   * Checks if the match contains no string excludes.
   * @return result of check
   */
  boolean match() {
    for(final FTStringMatch s : this) if(s.exclude) return false;
    return true;
  }

  /**
   * Sorts the matches by their start and end positions.
   */
  public void sort() {
    Arrays.sort(match, 0, size, null);
  }

  @Override
  public Iterator<FTStringMatch> iterator() {
    return new ArrayIterator<FTStringMatch>(match, size);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final FTStringMatch s : this) {
      sb.append(sb.length() != 0 ? ", " : "").append(s);
    }
    return Util.className(this) + ' ' + sb;
  }
}
