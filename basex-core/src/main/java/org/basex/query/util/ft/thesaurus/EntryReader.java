package org.basex.query.util.ft.thesaurus;

import static org.basex.util.Token.*;

import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * Reader for thesauri in the W3 format: each term is a concept, and synonyms are related terms.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class EntryReader {
  /** Element name: entry. */
  private static final byte[] ENTRY = token("entry");
  /** Element name: relationship. */
  private static final byte[] RELATIONSHIP = token("relationship");
  /** Element name: synonym. */
  private static final byte[] SYNONYM = token("synonym");
  /** Element name: term. */
  private static final byte[] TERM = token("term");

  /** Builder for the thesaurus structure. */
  private final ThesBuilder builder;
  /** Concepts of terms, indexed by labels ({@code -1}: no concept). */
  private final IntList concepts = new IntList();

  /**
   * Constructor.
   * @param builder builder for the thesaurus structure
   */
  EntryReader(final ThesBuilder builder) {
    this.builder = builder;
  }

  /**
   * Checks if an element is an entry.
   * @param element element
   * @return result of check
   */
  static boolean root(final GNode element) {
    return element.qname().eqLocal(ENTRY);
  }

  /**
   * Adds an entry and its synonyms.
   * @param entry entry
   */
  void entry(final GNode entry) {
    synonyms(entry, term(entry));
  }

  /**
   * Adds the synonyms of an entry or synonym.
   * @param node entry or synonym
   * @param term concept of the term of the node, or {@code -1} if the term has no tokens
   */
  private void synonyms(final GNode node, final int term) {
    for(final GNode synonym : elements(node, SYNONYM)) {
      final int syn = term(synonym);
      if(term != -1 && syn != -1) builder.relate(term, value(synonym, RELATIONSHIP), syn);
      synonyms(synonym, syn);
    }
  }

  /**
   * Returns the concept of the term of an entry or synonym.
   * @param node entry or synonym
   * @return id of the concept, or {@code -1} if the term has no tokens
   */
  private int term(final GNode node) {
    final int label = builder.label(value(node, TERM));
    if(label == -1) return -1;
    while(concepts.size() <= label) concepts.add(-1);
    int concept = concepts.get(label);
    if(concept == -1) {
      concept = builder.concept();
      builder.link(concept, label);
      concepts.set(label, concept);
    }
    return concept;
  }

  /**
   * Returns child elements with the specified local name.
   * @param node node to start from
   * @param name local name of elements to find
   * @return resulting elements
   */
  private static GNodeList elements(final GNode node, final byte[] name) {
    final GNodeList list = new GNodeList();
    for(final GNode element : node.childIter()) {
      if(element.kind() == Kind.ELEMENT && element.qname().eqLocal(name)) list.add(element);
    }
    return list;
  }

  /**
   * Returns the string value of a child element with the specified name.
   * @param node node to start from
   * @param name name of child element
   * @return value or empty string
   */
  private static byte[] value(final GNode node, final byte[] name) {
    final GNodeList elements = elements(node, name);
    return elements.isEmpty() ? EMPTY : elements.peek().string();
  }
}
