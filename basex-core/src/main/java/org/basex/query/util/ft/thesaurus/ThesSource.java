package org.basex.query.util.ft.thesaurus;

/**
 * Thesaurus source: concepts with labels and relationships.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public interface ThesSource {
  /**
   * Returns the id of a label.
   * @param key normalized label
   * @return id, or {@code -1} if the label is not found
   */
  int label(byte[] key);

  /**
   * Returns the original label with the specified id.
   * @param label id of the label
   * @return label
   */
  byte[] term(int label);

  /**
   * Returns the concepts of a label.
   * @param label id of the label
   * @return ids of the concepts
   */
  int[] concepts(int label);

  /**
   * Returns the labels of a concept.
   * @param concept id of the concept
   * @return ids of the labels
   */
  int[] labels(int concept);

  /**
   * Returns the relationships of a concept.
   * @param concept id of the concept
   * @return pairs of ids: related concept and relationship
   */
  int[] relations(int concept);

  /**
   * Returns the id of a relationship, resolving alternative names.
   * @param name name of the relationship
   * @return id, or {@code 0} if the relationship is not found
   */
  int relation(byte[] name);
}
