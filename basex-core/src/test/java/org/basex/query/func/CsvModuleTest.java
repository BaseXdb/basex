package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the CSV Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CsvModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void doc() {
    final Function func = _CSV_DOC;
    query(func.args(" ()"), "");
    query(func.args(" []"), "");
    query(func.args(" <_/>/text()"), "");

    final String path = "src/test/resources/input.csv";
    query(func.args(path) + "//entry[. = 'Picard'] ! string()", "Picard");
    query(func.args(path, " map { 'header': true() }") + "/descendant::Name[1] ! string()",
        "Picard");
  }

  /** Test method. */
  @Test public void parseXml() {
    parse(" ()", "", "");
    parse(" []", "", "");
    parse("", "", "<csv/>");
    parse("X", "", "<csv>\n<record>\n<entry>X</entry>\n</record>\n</csv>");
    parse(" '\"X\"\"Y\"'", "", "...<entry>X\"Y</entry>");
    parse(" '\"X\",Y'", "", "...<entry>X</entry>\n<entry>Y</entry>");

    parse("X;Y", "'separator':';'", "...<entry>X</entry>\n<entry>Y</entry>");
    parse("X,Y", "", "...<entry>X</entry>\n<entry>Y</entry>");

    parse("X\nY", "'header':true()", "<csv>\n<record>\n<X>Y</X>\n</record>\n</csv>");
    parse("A,B,C\nX,Y,Z", "'header':true()", "...<A>X</A>\n<B>Y</B>\n<C>Z</C>");

    parse("X\nY", "'format':'attributes','header':true()", "...<entry name=\"X\">Y</entry>");

    parseError("", "'x':'y'");
    parseError("", "'format':'abc'");
    parseError("", "'separator':''");
    parseError("", "'separator':'XXX'");
  }

  /** Test method. */
  @Test public void parseXQuery() {
    parse("X\nY", "'header':false(),'format':'xquery'", "...[\"X\"], [\"Y\"]");
    parse("X\nY", "'header':false(),'format':'xquery'", "...\"records\": ([\"X\"], [\"Y\"])");
    parse("X\nY", "'header':true(),'format':'xquery'", "...\"names\": [\"X\"]");
  }

  /** Test method. */
  @Test public void serializeXml() {
    serial("<csv><record><A__>1</A__></record></csv>", "'header':true(),'lax':false()", "A_\n1\n");
    serial("<csv><record><_>1</_></record></csv>", "'header':true(),'lax':false()", "\n1\n");
    serial("<csv><record><A_0020B>1</A_0020B></record></csv>",
        "'header':'yes','lax':'no'", "A B\n1\n");

    serial("<csv><record><_A_>1</_A_></record></csv>", "'header':true(),'lax':true()", "_A_\n1\n");
    serial("<csv><record><_>1</_></record></csv>", "'header':true(),'lax':true()", "_\n1\n");
    serial("<csv><record><__>1</__></record></csv>", "'header':true(),'lax':true()", "__\n1\n");
    serial("<csv><record><A_0020B>1</A_0020B></record></csv>",
        "'header':'yes','lax':'yes'", "A_0020B\n1\n");

    serial("<csv/>", "", "");

    serial("<csv><record/></csv>", "", "\n");
    serial("<csv><record/></csv>", "'header':'yes'", "\n\n");

    serial("<csv><record><entry>A</entry></record></csv>", "", "A\n");
    serial("<csv><record><entry>A</entry></record></csv>", "'header':'yes'", "entry\nA\n");
    serial("<csv><record><entry>A</entry><entry>B</entry></record></csv>", "", "A,B\n");
    serial("<csv><record><_>A</_><_>B</_></record></csv>", "'separator':';'", "A;B\n");
    serial("<csv><record><_>A</_><_/><_>B</_></record></csv>", "", "A,,B\n");
    serial("<csv><record><_>A</_><_><X>X</X></_></record></csv>", "", "A\n");

    serial("<csv><record><A>1</A></record><record><A>2</A></record></csv>",
        "'header':'yes'", "A\n1\n2\n");

    serial("<csv><record><A_B>1</A_B></record></csv>", "'header':'yes'", "A_B\n1\n");
    serial("<csv><record><A__B>1</A__B></record></csv>", "'header':true()", "A__B\n1\n");

    serial("<csv><record><A>1\n2</A></record></csv>", "'header':'yes'", "A\n\"1\n2\"\n");
    serial("<csv><record><A>\"</A></record></csv>", "'header':'yes'", "A\n\"\"\"\"\n");
    serial("<csv><record><A>1,2</A></record></csv>", "'header':'yes'", "A\n\"1,2\"\n");
    serial("<csv><record><A>1</A></record></csv>", "'header':'yes'", "A\n1\n");
    serial("<csv><record><A>1</A><B>2</B></record></csv>", "'header':'yes'", "A,B\n1,2\n");
    serial("<csv><record><A/><A>1</A><A>1</A><A/></record></csv>",
        "'header':'yes','separator':';'", "A\n1,1\n");

    serial("<csv><record><entry name='X'>1</entry></record></csv>",
        "'format':'attributes','header':true()", "X\n1\n");
    serial("<C><R><E name='X'>1</E></R></C>", "'format':'attributes','header':true()", "X\n1\n");

    serialError("<csv/>", "'x':'y'");
    serialError("<csv><record><A>1</A></record></csv>", "'separator':''");
    serialError("<csv><record><A>1</A></record></csv>", "'separator':'XX'");
  }

  /** Test method. */
  @Test public void serializeXQuery() {
    serial(" map { 'records': [ 'A', 'B' ] }",
        "'format': 'xquery'", "A,B\n");
    serial(" map { 'records': [ 'A', 'B' ] }",
        "'header': false(), 'format': 'xquery'", "A,B\n");
    serial(" map { 'names': [ 'A', 'B' ], 'records': () }",
        "'header': true(), 'format': 'xquery'", "A,B\n");
    serial(" map { 'names': [ 'A' ], 'records': [ '1' ] }",
        "'header': true(), 'format': 'xquery'", "A\n1\n");
  }

  /**
   * Runs the specified query.
   * @param input query input
   * @param options options
   * @param expected expected result
   */
  private static void parse(final String input, final String options, final String expected) {
    query(input, options, expected, _CSV_PARSE);
  }

  /**
   * Runs the specified query.
   * @param input query input
   * @param options options
   * @param expected expected result
   */
  private static void serial(final String input, final String options, final String expected) {
    query(' ' + input, options, expected, _CSV_SERIALIZE);
  }

  /**
   * Tests a query which yields an error.
   * @param input query input
   * @param options options
   * @param expected expected result
   * @param function function
   */
  private static void query(final String input, final String options, final String expected,
      final Function function) {

    final String query = options.isEmpty() ? function.args(input) :
      function.args(input, " map { " + options + " }");
    if(expected.startsWith("...")) {
      contains(query, expected.substring(3));
    } else {
      query(query, expected);
    }
  }

  /**
   * Tests a query which yields an error.
   * @param input query input
   * @param options options
   */
  private static void parseError(final String input, final String options) {
    error(input, options, _CSV_PARSE);
  }

  /**
   * Tests a query which yields an error.
   * @param input query input
   * @param options options
   */
  private static void serialError(final String input, final String options) {
    error(' ' + input, options, _CSV_SERIALIZE);
  }

  /**
   * Tests a query which yields an error.
   * @param input query input
   * @param options options
   * @param function function
   */
  private static void error(final String input, final String options, final Function function) {
    final String query = options.isEmpty() ? function.args(input) :
      function.args(input, " map { " + options + " }");
    error(query, INVALIDOPT_X);
  }
}
