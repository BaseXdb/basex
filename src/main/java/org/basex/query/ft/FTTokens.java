package org.basex.query.ft;

import java.util.Arrays;

import org.basex.util.list.ElementList;
import org.basex.util.list.TokenList;

/**
 * This is a container for full-text tokens.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FTTokens extends ElementList {
  /** Element container. */
  private TokenList[] list;

  /**
   * Constructor.
   */
  public FTTokens() {
    list = new TokenList[1];
  }

  /**
   * Adds an element.
   * @param e element to be added
   */
  public void add(final TokenList e) {
    if(size == list.length) list = Arrays.copyOf(list, newSize());
    list[size++] = e;
  }

  /**
   * Returns the element at the specified index.
   * @param i index
   * @return element
   */
  public TokenList get(final int i) {
    return list[i];
  }

  /**
   * Returns the number of tokens of the first entry.
   * @return number of tokens
   */
  int length() {
    return list[0].size();
  }
}
