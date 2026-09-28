package org.basex.query.expr.ft;

import java.util.*;
import java.util.function.*;

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
  /** Normalized term. */
  final byte[] key;

  /** Synonyms. */
  ThesEntry[] synonyms = new ThesEntry[1];
  /** Type of relationship. */
  byte[][] relations = new byte[1][];
  /** Number of entries. */
  int size;
  /** Groups of equivalent terms (can be {@code null}). */
  ArrayList<ThesEntry[]> groups;

  /**
   * Constructor.
   * @param term term
   * @param key normalized term
   */
  ThesEntry(final byte[] term, final byte[] key) {
    this.term = term;
    this.key = key;
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

  /**
   * Adds a group of equivalent terms.
   * @param group group, including this entry
   */
  void add(final ThesEntry[] group) {
    if(groups == null) groups = new ArrayList<>(1);
    groups.add(group);
  }

  /**
   * Passes the synonyms and the other members of the groups to the specified action.
   * @param action action, receiving synonym and relationship
   */
  void forEach(final BiConsumer<ThesEntry, byte[]> action) {
    for(int s = 0; s < size; s++) action.accept(synonyms[s], relations[s]);
    if(groups != null) {
      for(final ThesEntry[] group : groups) {
        for(final ThesEntry member : group) {
          if(member != this) action.accept(member, Thesaurus.EQ);
        }
      }
    }
  }
}
