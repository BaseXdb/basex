package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the functions of the CSV Module.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNCsvTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void parse() {
    parse("", "", "<csv/>");
    parse("X", "", "<csv><record><entry>X</entry></record></csv>");
    parse(" '\"X\"\"Y\"'", "", "...<entry>X\"Y</entry>");
    parse(" '\"X\",Y'", "", "...<entry>X</entry><entry>Y</entry>");

    parse("X;Y", "'separator':';'", "...<entry>X</entry><entry>Y</entry>");
    parse("X,Y", "", "...<entry>X</entry><entry>Y</entry>");

    parse("X\nY", "'header':true()", "<csv><record><X>Y</X></record></csv>");
    parse("A,B,C\nX,Y,Z", "'header':true()", "...<A>X</A><B>Y</B><C>Z</C>");

    parse("X\nY", "'format':'attributes','header':true()", "...<entry name=\"X\">Y</entry>");

    parseError("", "'x':'y'");
    parseError("", "'format':'abc'");
    parseError("", "'separator':''");
    parseError("", "'separator':'XXX'");
  }

  /** Test method. */
  @Test
  public void serialize() {
    serial("<csv><record><A__>1</A__></record></csv>", "'header':true(),'lax':false()", "A_1");
    serial("<csv><record><_>1</_></record></csv>", "'header':true(),'lax':false()", "1");
    serial("<csv><record><A_0020B>1</A_0020B></record></csv>", "'header':'yes','lax':'no'", "A B1");
    serial("<csv><record><_A_>1</_A_></record></csv>", "'header':true(),'lax':true()", "A1");
    serial("<csv><record><_>1</_></record></csv>", "'header':true(),'lax':true()", "1");
    serial("<csv><record><A_0020B>1</A_0020B></record></csv>",
        "'header':'yes','lax':'yes'", "A 0020B1");

    serial("<csv/>", "", "");

    serial("<csv><record/></csv>", "", "");
    serial("<csv><record/></csv>", "'header':'yes'", "");

    serial("<csv><record><entry>A</entry></record></csv>", "", "A");
    serial("<csv><record><entry>A</entry></record></csv>", "'header':'yes'", "entryA");
    serial("<csv><record><entry>A</entry><entry>B</entry></record></csv>", "", "A,B");
    serial("<csv><record><_>A</_><_>B</_></record></csv>", "'separator':';'", "A;B");
    serial("<csv><record><_>A</_><_/><_>B</_></record></csv>", "", "A,,B");
    serial("<csv><record><_>A</_><_><X>X</X></_></record></csv>", "", "A");

    serial("<csv><record><A>1</A></record><record><A>2</A></record></csv>",
        "'header':'yes'", "A12");

    serial("<csv><record><A_B>1</A_B></record></csv>", "'header':'yes'", "A B1");
    serial("<csv><record><A__B>1</A__B></record></csv>", "'header':true()", "A  B1");

    serial("<csv><record><A>1\n2</A></record></csv>", "'header':'yes'", "A\"12\"");
    serial("<csv><record><A>\"</A></record></csv>", "'header':'yes'", "A\"\"\"\"");
    serial("<csv><record><A>1,2</A></record></csv>", "'header':'yes'", "A\"1,2\"");
    serial("<csv><record><A>1</A></record></csv>", "'header':'yes'", "A1");
    serial("<csv><record><A>1</A><B>2</B></record></csv>", "'header':'yes'", "A,B1,2");
    serial("<csv><record><A/><A>1</A><A>1</A><A/></record></csv>",
        "'header':'yes','separator':';'", "A1,1");

    serial("<csv><record><entry name='X'>1</entry></record></csv>",
        "'format':'attributes','header':true()", "X1");
    serial("<C><R><E name='X'>1</E></R></C>", "'format':'attributes','header':true()", "X1");

    serialError("<csv/>", "'x':'y'");
    serialError("<csv><record><A>1</A></record></csv>", "'separator':''");
    serialError("<csv><record><A>1</A></record></csv>", "'separator':'XX'");
  }

  /**
   * Runs the specified query.
   * @param input query input
   * @param options options
   * @param expected expected result
   */
  private void parse(final String input, final String options, final String expected) {
    query(input, options, expected, _CSV_PARSE);
  }

  /**
   * Runs the specified query.
   * @param input query input
   * @param options options
   * @param expected expected result
   */
  private void serial(final String input, final String options, final String expected) {
    query(input, options, expected, _CSV_SERIALIZE);
  }

  /**
   * Tests a query which yields an error.
   * @param input query input
   * @param options options
   * @param expected expected result
   * @param function function
   */
  private void query(final String input, final String options, final String expected,
      final Function function) {
    final String query = options.isEmpty() ? function.args(input) :
      function.args(input, " {" + options + "}");
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
  private void parseError(final String input, final String options) {
    error(input, options, _CSV_PARSE);
  }

  /**
   * Tests a query which yields an error.
   * @param input query input
   * @param options options
   */
  private void serialError(final String input, final String options) {
    error(input, options, _CSV_SERIALIZE);
  }

  /**
   * Tests a query which yields an error.
   * @param input query input
   * @param options options
   * @param function function
   */
  private void error(final String input, final String options, final Function function) {
    final String query = options.isEmpty() ? function.args(input) :
      function.args(input, " {" + options + "}");
    error(query, Err.INVALIDOPT);
  }
}
