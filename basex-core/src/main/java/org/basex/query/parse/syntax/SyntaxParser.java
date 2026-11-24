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
   * Parses a module, with offsets that are positions in the array.
   * @param input code points of the query, with normalized line endings (not copied, and not to
   *   be modified as long as the syntax tree is in use)
   * @return syntax tree
   * @throws ParseException if the query is not accepted by the grammar
   * @throws TagMismatch if an element with different start and end tags was parsed before
   */
  public static SyntaxTree parse(final int[] input) throws ParseException {
    final TreeBuilder builder = new TreeBuilder(input);
    try {
      new XQuerySyntaxParser(input, builder).parse_Module();
    } catch(final ParseException ex) {
      // different tags are reported first
      final Nodes.DirElemConstructor mismatch = builder.mismatch();
      if(mismatch != null) throw new TagMismatch(mismatch);
      throw ex;
    }
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
