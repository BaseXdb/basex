package org.basex.query.expr.ft;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is a container for full-text tokens.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTTokens extends ElementList {
  /** Element container. */
  private TokenList[] list = new TokenList[1];

  /**
   * Adds an element.
   * @param e element to be added
   */
  public void add(final TokenList e) {
    if(size == list.length) list = Array.copy(list, new TokenList[newSize()]);
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
