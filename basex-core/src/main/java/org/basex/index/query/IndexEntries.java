package org.basex.index.query;

import org.basex.index.*;

/**
 * This class contains information for returning index entries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IndexEntries implements IndexSearch {
  /** Index type. */
  private final IndexType type;
  /** Text. */
  private final byte[] text;
  /** Ascending/descending traversal. */
  public boolean descending;
  /** Prefix/traversal flag. */
  public boolean prefix;
  /** Number of allowed errors for fuzzy traversal ({@code -1}: no fuzzy traversal). */
  public int errors = -1;

  /**
   * Private constructor.
   * @param type index type
   * @param text token
   */
  private IndexEntries(final IndexType type, final byte[] text) {
    this.type = type;
    this.text = text;
  }

  /**
   * Constructor for prefix search.
   * @param text token
   * @param type index type
   */
  public IndexEntries(final byte[] text, final IndexType type) {
    this(type, text);
    prefix = true;
  }

  /**
   * Constructor for traversing entries.
   * @param prefix token to start with
   * @param asc return results in ascending order
   * @param type index type
   */
  public IndexEntries(final byte[] prefix, final boolean asc, final IndexType type) {
    this(type, prefix);
    descending = !asc;
  }

  /**
   * Constructor for fuzzy search. Only supported by the full-text index.
   * @param text token
   * @param errors number of allowed errors (dynamic calculation if the value is {@code 0})
   * @param type index type
   */
  public IndexEntries(final byte[] text, final int errors, final IndexType type) {
    this(type, text);
    this.errors = errors;
  }

  @Override
  public IndexType type() {
    return type;
  }

  @Override
  public byte[] token() {
    return text;
  }
}
