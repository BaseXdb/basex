package org.basex.util.ft;

import java.util.Collection;

/**
 * Japanese Full-text tokenizer.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Toshio HIRAI
 */
class JapaneseTokenizer extends Tokenizer {
  /** Reference to Japanese lexer. */
  private IgoLexer lexer;
  /** Sensitivity flag. */
  private final FTOpt opt;

  /**
   * Checks if the library is available.
   * @return result of check
   */
  static boolean available() {
    return IgoLexer.isAvailable;
  }

  /**
   * Constructor.
   * @param f (optional) full-text options
   */
  JapaneseTokenizer(final FTOpt f) {
    opt = f;
  }

  @Override
  Tokenizer get(final FTOpt f) {
    return new JapaneseTokenizer(f);
  }

  @Override
  public JapaneseTokenizer init(final byte[] txt) {
    lexer = new IgoLexer(txt, opt);
    return this;
  }

  @Override
  public boolean hasNext() {
    return lexer.hasNext();
  }

  @Override
  public FTSpan next() {
    return lexer.next();
  }

  @Override
  public byte[] nextToken() {
    return lexer.nextToken();
  }

  @Override
  protected byte prec() {
    return 20;
  }

  @Override
  Collection<Language> languages() {
    return collection("ja");
  }

  @Override
  public String toString() {
    return lexer.toString();
  }

}
