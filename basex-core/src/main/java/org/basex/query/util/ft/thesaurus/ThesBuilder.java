package org.basex.query.util.ft.thesaurus;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Builder for the thesaurus structure.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ThesBuilder {
  /** Inverse relationships. */
  private static final TokenObjectMap<byte[]> INVERSE = new TokenObjectMap<>();

  static {
    INVERSE.put(token("NT"), token("BT"));
    INVERSE.put(token("BT"), token("NT"));
    INVERSE.put(token("BTG"), token("NTG"));
    INVERSE.put(token("NTG"), token("BTG"));
    INVERSE.put(token("BTP"), token("NTP"));
    INVERSE.put(token("NTP"), token("BTP"));
    INVERSE.put(token("USE"), token("UF"));
    INVERSE.put(token("UF"), token("USE"));
    INVERSE.put(token("RT"), token("RT"));
  }

  /** Normalized labels. */
  final TokenSet keys = new TokenSet();
  /** Original labels. */
  final TokenList terms = new TokenList();
  /** Names of relationships. */
  final TokenSet relations = new TokenSet();
  /** Labels of the concepts. */
  final ArrayList<IntList> labels = new ArrayList<>();
  /** Relationships of the concepts: pairs of related concept and relationship. */
  final ArrayList<IntList> related = new ArrayList<>();
  /** Lexer for normalizing terms. */
  private final FTLexer lexer;

  /**
   * Constructor.
   * @param lexer lexer for normalizing terms
   */
  ThesBuilder(final FTLexer lexer) {
    this.lexer = lexer;
  }

  /**
   * Returns the id of a label.
   * @param term original label
   * @return id, or {@code -1} if the label has no tokens
   */
  int label(final byte[] term) {
    final byte[] key = Thesaurus.normalize(term, lexer);
    if(key.length == 0) return -1;
    final int label = keys.put(key) - 1;
    if(label == terms.size()) terms.add(term);
    return label;
  }

  /**
   * Adds a label to a concept.
   * @param concept id of the concept
   * @param term label
   */
  void label(final int concept, final byte[] term) {
    final int label = label(term);
    if(label != -1) link(concept, label);
  }

  /**
   * Adds a label to a concept unless it exists.
   * @param concept id of the concept
   * @param label id of the label
   */
  void link(final int concept, final int label) {
    labels.get(concept).addUnique(label);
  }

  /**
   * Creates a concept.
   * @return id of the concept
   */
  int concept() {
    labels.add(new IntList(1));
    related.add(new IntList(2));
    return labels.size() - 1;
  }

  /**
   * Creates concepts with consecutive ids.
   * @param count number of concepts
   * @return id of the first concept
   */
  int concepts(final int count) {
    final int first = labels.size();
    for(int c = 0; c < count; c++) concept();
    return first;
  }

  /**
   * Adds a relationship and its inverse relationship.
   * @param source source concept
   * @param name name of the relationship
   * @param target target concept
   */
  void relate(final int source, final byte[] name, final int target) {
    final byte[] rel = Thesaurus.relationship(name), inverse = INVERSE.get(rel);
    add(source, target, relations.put(rel));
    if(inverse != null) add(target, source, relations.put(inverse));
  }

  /**
   * Adds a relationship unless it exists.
   * @param source source concept
   * @param target target concept
   * @param rel id of the relationship
   */
  private void add(final int source, final int target, final int rel) {
    final IntList list = related.get(source);
    final int size = list.size();
    for(int l = 0; l < size; l += 2) {
      if(list.get(l) == target && list.get(l + 1) == rel) return;
    }
    list.add(target, rel);
  }
}
