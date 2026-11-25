package org.basex.query.parse.syntax;

import java.util.*;

import org.basex.query.parse.gen.XQuerySyntaxParser.*;
import org.basex.query.parse.gen.*;

/**
 * Parses a query into a syntax tree.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class SyntaxParser {
  /** Code point that is not part of the character set of the grammar. */
  private static final int STOP = 0x110000;

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
   * Checks if the generated parser accepts an input.
   * @param input code points
   * @param prefix also accept an input that is too short
   * @param type sequence type, which is followed by the end of the input, or module
   * @return result of check
   */
  public static boolean accepts(final int[] input, final boolean prefix, final boolean type) {
    try {
      final XQuerySyntaxParser parser = new XQuerySyntaxParser(input, null);
      if(type) parser.parse_SequenceTypeEOF();
      else parser.parse_Module();
      return true;
    } catch(final ParseException ex) {
      return prefix && ex.getBegin() >= input.length;
    }
  }

  /**
   * Parses the start of a module, followed by a code point that stops the parser.
   * @param input code points of the query, with normalized line endings
   * @param length number of code points to parse
   * @return stack of the tree builder, or {@code null} if the parser fails earlier
   */
  public static SyntaxStack parseStart(final int[] input, final int length) {
    final int[] stopped = Arrays.copyOf(input, length + 1);
    stopped[length] = STOP;
    final TreeBuilder builder = new TreeBuilder(stopped);
    try {
      new XQuerySyntaxParser(stopped, builder).parse_Module();
    } catch(final ParseException ex) {
      if(ex.getBegin() == length) return builder.stack();
    }
    return null;
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
