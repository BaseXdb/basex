package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the CSV Module.
 *
 * @author BaseX Team, BSD License
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
    parse("X", "", "<csv><record><entry>X</entry></record></csv>");
    parse(" '\"X\"\"Y\"'", "", "...<entry>X\"Y</entry>");
    parse(" '\"X\",Y'", "", "...<entry>X</entry><entry>Y</entry>");

    parse("X;Y", "'separator': ';'", "...<entry>X</entry><entry>Y</entry>");
    parse("X,Y", "", "...<entry>X</entry><entry>Y</entry>");

    final String header = "'header': true()", skipEmpty = "'skip-empty': true()";
    parse("X\nY", header, "<csv><record><X>Y</X></record></csv>");
    parse("A,B,C\nX,Y,Z", header, "...<A>X</A><B>Y</B><C>Z</C>");

    parse("X\nY", "'format': 'attributes', " + header, "...<entry name=\"X\">Y</entry>");

    parse("X,Y\n1,", header, "<csv><record><X>1</X><Y/></record></csv>");
    parse("X,Y\n1,", skipEmpty + ", " + header, "<csv><record><X>1</X></record></csv>");
    parse("X,Y\n,1", skipEmpty + ", " + header, "<csv><record><Y>1</Y></record></csv>");
                                       // was: "<csv/>");
    parse("X,Y\n,", skipEmpty + ", " + header, "<csv><record/></csv>");

            // was: "<csv/>");
    parse("\n", "", "<csv><record/></csv>");
              // was: "<csv/>");
    parse("\n\n", "", "<csv><record/><record/></csv>");
               // was: "<csv><record><entry>X</entry></record></csv>");
    parse("\n\nX", "", "<csv><record/><record/><record><entry>X</entry></record></csv>");
               // was:  "...<entry>X</entry></record><record><entry>Y</entry>");
    parse("X\n\nY", "", "...<entry>X</entry></record><record/><record><entry>Y</entry>");
    parse("X\n", "", "<csv><record><entry>X</entry></record></csv>");
               // was: "<csv><record><entry>X</entry></record></csv>");
    parse("X\n\n", "", "<csv><record><entry>X</entry></record><record/></csv>");

    parse(" ' \" X\"'", "'quotes': true()", "<csv><record><entry> \" X\"</entry></record></csv>");
    parse(" '\"X \" '", "'quotes': true()", "<csv><record><entry>X  </entry></record></csv>");

    parse("X\nY", "'header': false(), 'format': 'direct'", "...<record><entry>X</entry></record>");
    parse("X\nY", "'header': 'no', 'format': 'direct'", "...<record><entry>X</entry></record>");
    parse("X\nY", "'header': '0', 'format': 'direct'", "...<record><entry>X</entry></record>");
    parse("X\nY", "'header': true(), 'format': 'direct'", "<csv><record><X>Y</X></record></csv>");
    parse("X\nY", "'header': 'yes', 'format': 'direct'", "<csv><record><X>Y</X></record></csv>");
    parse("X\nY", "'header': '1', 'format': 'direct'", "<csv><record><X>Y</X></record></csv>");
    parse("X\nY", "'header': '01', 'format': 'direct'", "...<record><_01>X</_01></record>");
    parse("X\nY", "'header': '1.0', 'format': 'direct'", "...<record><_1.0>X</_1.0></record>");
    parse("X\nY", "'header': ('yes', 'no'), 'format': 'direct'", "...<yes>X</yes>");
    parse("X\nY", "'header': ' h ', 'format': 'direct'", "...<h>X</h>");

    parse("X\nY", "'header': false(), 'format': 'attributes'", "...<entry>X</entry>");
    parse("X\nY", "'header': 'no', 'format': 'attributes'", "...<record><entry>X</entry></record>");
    parse("X\nY", "'header': '0', 'format': 'attributes'", "...<record><entry>X</entry></record>");
    parse("X\nY", "'header': true(), 'format': 'attributes'", "...<entry name=\"X\">Y</entry>");
    parse("X\nY", "'header': 'yes', 'format': 'attributes'", "...<entry name=\"X\">Y</entry>");
    parse("X\nY", "'header': '1', 'format': 'attributes'", "...<entry name=\"X\">Y</entry>");
    parse("X\nY", "'header': '01', 'format': 'attributes'", "...<entry name=\"01\">Y</entry>");
    parse("X\nY", "'header': '1.0', 'format': 'attributes'", "...<entry name=\"1.0\">Y</entry>");
    parse("X\nY", "'header': ('yes', 'no'), 'format': 'attributes'", "...name=\"yes\">Y<");
    parse("X\nY", "'header': ' h ', 'format': 'attributes'", "...<entry name=\" h \">Y</entry>");

    parseError("", "'x': 'y'");
    parseError("", "'format': 'abc'");
    parseError("", "'separator': ''");
    parseError("", "'separator': 'XXX'");
  }

  /** Test method. */
  @Test public void parseXQuery() {
    parse("X\nY", "'header': false(), 'format': 'xquery'", "{\"records\":([\"X\"],[\"Y\"])}");
    parse("X\nY", "'header': 'no', 'format': 'xquery'", "{\"records\":([\"X\"],[\"Y\"])}");
    parse("X\nY", "'header': '0', 'format': 'xquery'", "{\"records\":([\"X\"],[\"Y\"])}");
    parse("X\nY", "'header': true(), 'format': 'xquery'", "...\"names\":[\"X\"]");
    parse("X\nY", "'header': 'yes', 'format': 'xquery'", "...\"names\":[\"X\"]");
    parse("X\nY", "'header': '1', 'format': 'xquery'", "...\"names\":[\"X\"]");
    parse("X\nY", "'header': '01', 'format': 'xquery'", "{\"names\":[\"01\"],"
        + "\"records\":([\"X\"],[\"Y\"])}");
    parse("X\nY", "'header': '1.0', 'format': 'xquery'", "{\"names\":[\"1.0\"],"
        + "\"records\":([\"X\"],[\"Y\"])}");
    parse("X\nY", "'header': ('yes', 'no'), 'format': 'xquery'", "{\"names\":[\"yes\",\"no\"],"
        + "\"records\":([\"X\"],[\"Y\"])}");
    parse("X\nY", "'header': ' h ' , 'format': 'xquery'", "{\"names\":[\" h \"],"
        + "\"records\":([\"X\"],[\"Y\"])}");

    parse("", "'format': 'xquery'", "{\"records\":()}");
                              // was: "{\"records\":()}");
    parse("\n", "'format': 'xquery'", "{\"records\":[]}");
                                // was: "{\"records\":()}");
    parse("\n\n", "'format': 'xquery'", "{\"records\":([],[])}");
                                 // was: "{\"records\":[\"X\"]}");
    parse("\n\nX", "'format': 'xquery'", "{\"records\":([],[],[\"X\"])}");
                                  // was: "{\"records\":([\"X\"],[\"Y\"])}");
    parse("X\n\nY", "'format': 'xquery'", "{\"records\":([\"X\"],[],[\"Y\"])}");
    parse("X\n", "'format': 'xquery'", "{\"records\":[\"X\"]}");
                                 // was: "{\"records\":[\"X\"]}");
    parse("X\n\n", "'format': 'xquery'", "{\"records\":([\"X\"],[])}");

    parse(" ' \"\"'", "'quotes': true(), 'format': 'xquery'", "{\"records\":[\" \"\"\"]}");
    parse(" ' \" X\"'", "'quotes': true(), 'format': 'xquery'", "{\"records\":[\" \"\" X\"\"\"]}");
    parse(" '\"\" '", "'quotes': true(), 'format': 'xquery'", "{\"records\":[\" \"]}");
    parse(" '\"X \" '", "'quotes': true(), 'format': 'xquery'", "{\"records\":[\"X  \"]}");
  }

  /** Test method. */
  @Test public void serializeXml() {
    final String xml = "<csv><record><entry name='X'>Y</entry></record></csv>";
    serial(xml, "'header': false()", "Y\n");
    serial(xml, "'header': 'no'", "Y\n");
    serial(xml, "'header': '0'", "Y\n");
    serial(xml, "'header': true()", "entry\nY\n");
    serial(xml, "'header': 'yes'", "entry\nY\n");
    serial(xml, "'header': '1'", "entry\nY\n");
    serial(xml, "'header': 'x'", "Y\n");

    serial(xml, "'format': 'attributes', 'header': false()", "Y\n");
    serial(xml, "'format': 'attributes', 'header': 'no'", "Y\n");
    serial(xml, "'format': 'attributes', 'header': '0'", "Y\n");
    serial(xml, "'format': 'attributes', 'header': true()", "X\nY\n");
    serial(xml, "'format': 'attributes', 'header': 'yes'", "X\nY\n");
    serial(xml, "'format': 'attributes', 'header': '1'", "X\nY\n");
    serial(xml, "'format': 'attributes', 'header': 'x'", "Y\n");

    serial("<csv><record><A__>1</A__></record></csv>", "'header': true(), 'lax': false()",
        "A_\n1\n");
    serial("<csv><record><_>1</_></record></csv>", "'header': true(), 'lax': false()", "\n1\n");
    serial("<csv><record><A_0020B>1</A_0020B></record></csv>",
        "'header': 'yes', 'lax': 'no'", "A B\n1\n");

    serial("<csv><record><_A_>1</_A_></record></csv>", "'header': true(), 'lax': true()",
        "_A_\n1\n");
    serial("<csv><record><_>1</_></record></csv>", "'header': true(), 'lax': true()", "_\n1\n");
    serial("<csv><record><__>1</__></record></csv>", "'header': true(), 'lax': true()", "__\n1\n");
    serial("<csv><record><A_0020B>1</A_0020B></record></csv>",
        "'header': 'yes', 'lax': 'yes'", "A_0020B\n1\n");

    serial("<csv/>", "", "");

    serial("<csv><record/></csv>", "", "\n");
    serial("<csv><record/></csv>", "'header': 'yes'", "\n\n");

    serial("<csv><record><entry>A</entry></record></csv>", "", "A\n");
    serial("<csv><record><entry>A</entry></record></csv>", "'header': 'yes'", "entry\nA\n");
    serial("<csv><record><entry>A</entry><entry>B</entry></record></csv>", "", "A,B\n");
    serial("<csv><record><_>A</_><_>B</_></record></csv>", "'separator': ';'", "A;B\n");
    serial("<csv><record><_>A</_><_/><_>B</_></record></csv>", "", "A,,B\n");
    serial("<csv><record><_>A</_><_><X>X</X></_></record></csv>", "", "A\n");

    serial("<csv><record><A>1</A></record><record><A>2</A></record></csv>",
        "'header': 'yes'", "A\n1\n2\n");

    serial("<csv><record><A_B>1</A_B></record></csv>", "'header': 'yes'", "A_B\n1\n");
    serial("<csv><record><A__B>1</A__B></record></csv>", "'header': true()", "A__B\n1\n");

    serial("<csv><record><A>1\n2</A></record></csv>", "'header': 'yes'", "A\n\"1\n2\"\n");
    serial("<csv><record><A>\"</A></record></csv>", "'header': 'yes'", "A\n\"\"\"\"\n");
    serial("<csv><record><A>1,2</A></record></csv>", "'header': 'yes'", "A\n\"1,2\"\n");
    serial("<csv><record><A>1</A></record></csv>", "'header': 'yes'", "A\n1\n");
    serial("<csv><record><A>1</A><B>2</B></record></csv>", "'header': 'yes'", "A,B\n1,2\n");
    serial("<csv><record><A/><A>1</A><A>1</A><A/></record></csv>",
        "'header': 'yes', 'separator': ';'", "A\n1,1\n");

    serial("<csv><record><entry name='X'>1</entry></record></csv>",
        "'format': 'attributes', 'header': true()", "X\n1\n");
    serial("<C><R><E name='X'>1</E></R></C>", "'format': 'attributes', 'header': true()", "X\n1\n");

    serialError("<csv/>", "'x': 'y'");
    serialError("<csv><record><A>1</A></record></csv>", "'separator': ''");
    serialError("<csv><record><A>1</A></record></csv>", "'separator': 'XX'");
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

    final String map = "{'names': ['A', 'B'], 'records': (['X'], ['Y'])}";
    serial(map, "'format': 'xquery', 'header': false()", "X\nY\n");
    serial(map, "'format': 'xquery', 'header': 'no'", "X\nY\n");
    serial(map, "'format': 'xquery', 'header': '0'", "X\nY\n");
    serial(map, "'format': 'xquery', 'header': true()", "A,B\nX\nY\n");
    serial(map, "'format': 'xquery', 'header': 'yes'", "A,B\nX\nY\n");
    serial(map, "'format': 'xquery', 'header': '1'", "A,B\nX\nY\n");
    serial(map, "'format': 'xquery', 'header': ('C', 'D')", "X\nY\n");
  }

  /** Test method. */
  @Test public void gh2428() {
    final Function func = _CSV_SERIALIZE;
    final String xml = " <csv xmlns='http://www.w3.org/2005/xpath-functions'><columns><column>x"
        + "</column></columns><rows><row><field column='x'>y</field></row><row><field column='$1'>z"
        + "</field></row></rows></csv>";
    query(func.args(xml.replace("$1", "x"), " { 'header': true(), 'format': 'w3-xml' }"),
        "x\ny\nz\n");
    error(func.args(xml.replace("$1", "w"), " { 'header': true(), 'format': 'w3-xml' }"),
        CSV_SERIALIZE_X_X);
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
    final String query = function.args(input, " { " + options + " }");
    error(query, INVALIDOPTION_X, CSV_PARSE_X, CSV_SERIALIZE_X);
  }
}
