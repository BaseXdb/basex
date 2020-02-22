package org.basex.util.list;

import static org.basex.util.Token.*;

import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Resizable-array implementation for tokens (byte arrays).
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class TokenList extends ObjectList<byte[], TokenList> {
  /**
   * Default constructor.
   */
  public TokenList() {
    this(Array.CAPACITY);
  }

  /**
   * Constructor, specifying an initial internal array size
   * (default is {@link Array#CAPACITY}).
   * @param capacity initial array capacity
   */
  public TokenList(final int capacity) {
    super(new byte[capacity][]);
  }

  /**
   * Constructor, specifying a resize factor. Smaller values are more memory-saving,
   * while larger will provide better performance.
   * @param resize resize factor
   */
  public TokenList(final double resize) {
    this();
    factor = resize;
  }

  /**
   * Constructor, adopting the elements from the specified set.
   * @param set set to be added
   */
  public TokenList(final TokenSet set) {
    this(set.size());
    for(final byte[] e : set) add(e);
  }

  /**
   * Lightweight constructor, assigning the specified array.
   * @param elements initial array
   */
  public TokenList(final byte[]... elements) {
    super(elements);
    size = elements.length;
  }

  /**
   * Adds a long value.
   * @param element element to be added
   * @return self reference
   */
  public TokenList add(final long element) {
    add(token(element));
    return this;
  }

  /**
   * Adds a string.
   * @param element element to be added
   * @return self reference
   */
  public TokenList add(final String element) {
    add(token(element));
    return this;
  }

  @Override
  public boolean equals(final byte[] element1, final byte[] element2) {
    return Token.eq(element1, element2);
  }

  /**
   * Sorts the elements.
   * @return self reference
   */
  public TokenList sort() {
    return sort(true);
  }

  /**
   * Sorts the elements.
   * @param cs respect case sensitivity
   * @return self reference
   */
  public TokenList sort(final boolean cs) {
    return sort(cs, true);
  }

  /**
   * Sorts the elements.
   * @param cs respect case sensitivity
   * @param ascending ascending/descending order
   * @return self reference
   */
  public TokenList sort(final boolean cs, final boolean ascending) {
    return sort(cs ? COMP : LC_COMP, ascending);
  }

  @Override
  protected byte[][] newList(final int s) {
    return new byte[s][];
  }
}
