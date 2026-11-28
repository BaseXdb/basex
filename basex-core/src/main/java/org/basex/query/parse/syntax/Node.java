package org.basex.query.parse.syntax;

/**
 * Node of the syntax tree: a record of {@link org.basex.query.parse.gen.Nodes}, or a terminal.
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
   * @return offset of the first character, as {@code pos} of the query parsers
   */
  int begin();

  /**
   * Returns the end offset.
   * @return offset after the last character
   */
  int end();
}
