package org.basex.query.expr.ft;

import java.util.*;

import org.basex.util.*;

/**
 * Thesaurus entry.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ThesEntry {
  /** Term. */
  final byte[] term;

  /** Synonyms. */
  ThesEntry[] synonyms = new ThesEntry[1];
  /** Type of relationship. */
  byte[][] relations = new byte[1][];
  /** Number of entries. */
  int size;

  /**
   * Constructor.
   * @param term term
   */
  ThesEntry(final byte[] term) {
    this.term = term;
  }

  /**
   * Adds a relationship to the node.
   * @param entry related node
   * @param relation type of relationship
   */
  void add(final ThesEntry entry, final byte[] relation) {
    if(size == synonyms.length) {
      final int s = Array.newCapacity(size);
      synonyms = Array.copy(synonyms, new ThesEntry[s]);
      relations = Arrays.copyOf(relations, s);
    }
    synonyms[size] = entry;
    relations[size++] = relation;
  }
}
