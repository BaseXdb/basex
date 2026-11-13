package org.basex.query.parse.syntax;

import org.basex.query.parse.gen.XQuerySyntaxParser.*;
import org.basex.query.parse.gen.*;

/**
 * Parses a query into a syntax tree.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class SyntaxParser {
  /** Private constructor. */
  private SyntaxParser() { }

  /**
   * Parses a module.
   * @param query query string, which will be normalized in the same way as in {@code InputParser}
   * @return syntax tree
   * @throws ParseException if the query is not accepted by the grammar
   */
  public static SyntaxTree parse(final String query) throws ParseException {
    return parse(query.replaceAll("\r\n?", "\n").codePoints().toArray());
  }

  /**
   * Parses a module. The offsets of the syntax tree are positions in the array. The array is not
   * copied: the caller must not modify it as long as the syntax tree is in use.
   * @param input code points of the query, with normalized line endings
   * @return syntax tree
   * @throws ParseException if the query is not accepted by the grammar
   */
  public static SyntaxTree parse(final int[] input) throws ParseException {
    final TreeBuilder builder = new TreeBuilder(input);
    new XQuerySyntaxParser(input, builder).parse_Module();
    return builder.finish();
  }

  /**
   * Parses a sequence type, which is followed by the end of the input.
   * @param input code points of the type, with normalized line endings
   * @return syntax tree, whose root is the sequence type
   * @throws ParseException if the type is not accepted by the grammar
   */
  public static SyntaxTree parseSequenceType(final int[] input) throws ParseException {
    final TreeBuilder builder = new TreeBuilder(input);
    new XQuerySyntaxParser(input, builder).parse_SequenceTypeEOF();
    return builder.finish();
  }
}
