package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the functions of the CSV Module.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNCsvTest extends AdvancedQueryTest {
  /** JSON snippets. */
  private static final String[][] TOXML = {
    { "", "<csv/>" },
    { "X", "<csv><record><entry>X</entry></record></csv>" },
    { "X,Y", "<csv><record><entry>X</entry><entry>Y</entry></record></csv>" },
    { "X\nY", " { 'header':true() }",
      "<csv><record><X>Y</X></record></csv>" },
    { "X;Y", " { 'separator':';' }",
        "<csv><record><entry>X</entry><entry>Y</entry></record></csv>" },
  };

  /** Test method. */
  @Test public void parse() {
    for(final String[] test : TOXML) {
      final String expected = test[test.length == 3 ? 2 : 1];
      final String query = test.length == 3 ? _CSV_PARSE.args(test[0], test[1]) :
        _CSV_PARSE.args(test[0]);
      if(expected.startsWith("...")) {
        contains(query, expected.substring(3));
      } else {
        query(query, expected);
      }
    }
  }

  /** Test method. */
  @Test public void serialize() {
  }
}
