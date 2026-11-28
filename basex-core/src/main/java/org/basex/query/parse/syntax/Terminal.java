package org.basex.query.parse.syntax;

import org.basex.query.parse.gen.*;

/**
 * Terminal of the syntax tree: a token that carries a value, such as a name or a literal, or
 * a keyword or operator that tells alternatives apart.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class Terminal implements Node {
  /** Symbol. */
  public final int sym;
  /** Start offset. */
  public final int begin;
  /** End offset (can be extended while consecutive characters are merged). */
  int end;

  /**
   * Constructor.
   * @param sym symbol
   * @param begin start offset
   * @param end end offset
   */
  Terminal(final int sym, final int begin, final int end) {
    this.sym = sym;
    this.begin = begin;
    this.end = end;
  }

  @Override
  public int sym() {
    return sym;
  }

  @Override
  public int begin() {
    return begin;
  }

  @Override
  public int end() {
    return end;
  }

  /**
   * Returns the text.
   * @param input input
   * @return text
   */
  public String text(final int[] input) {
    return new String(input, begin, end - begin);
  }

  @Override
  public String toString() {
    return Sym.symbol(sym) + '[' + begin + ',' + end + ')';
  }
}
