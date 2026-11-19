package org.basex.query.parse.syntax;

/**
 * Node of the syntax tree. Nonterminals are represented by the records of
 * {@link org.basex.query.parse.gen.Nodes}, terminals by {@link Terminal}. Offsets are
 * positions in the input, which is an array of code points, so they are the same as the
 * {@code pos} of the query parsers.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public interface Node {
  /**
   * Returns the symbol: the nonterminal of a record, or the terminal.
   * @return symbol
   */
  int sym();

  /**
   * Returns the start offset.
   * @return offset of the first character
   */
  int begin();

  /**
   * Returns the end offset.
   * @return offset after the last character
   */
  int end();
}
