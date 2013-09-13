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
   * @param mtc match to be added
   * @return self reference
   */
  public FTMatch add(final FTMatch mtc) {
    for(final FTStringMatch m : mtc) add(m);
    return this;
  }

  /**
   * Adds a string match.
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
   * @param mtc match to be checked
   * @return result of check
   */
  public boolean notin(final FTMatch mtc) {
    for(final FTStringMatch s : this) {
      for(final FTStringMatch m : mtc) if(!s.in(m)) return true;
    }
    return false;
  }

  /**
   * Checks if the match contains no string excludes.
   * @return result of check
   */
  boolean match() {
    for(final FTStringMatch s : this) if(s.ex) return false;
    return true;
  }

  /**
   * Sorts the matches.
   */
  public void sort() {
    Arrays.sort(match, 0, size, null);
  }

  /**
   * Creates a deep copy of this container.
   * @return copy
   */
  protected FTMatch copy() {
    final FTMatch ftm = new FTMatch();
    ftm.size = size;
    ftm.match = match.clone();
    for(int i = 0; i < ftm.match.length; i++)
      if(ftm.match[i] != null) ftm.match[i] = ftm.match[i].copy();
    return ftm;
  }

  @Override
  public Iterator<FTStringMatch> iterator() {
    return new ArrayIterator<FTStringMatch>(match, size);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.name(this));
    for(final FTStringMatch s : this) sb.append(' ').append(s);
    return sb.toString();
  }
}
