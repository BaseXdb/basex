package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

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
  /** JSON snippets. */
  private static final String[][] TOXML = {
    { "", "", "<csv/>" },
    { "X", "", "<csv><record><entry>X</entry></record></csv>" },
    { "X,Y", "", "<csv><record><entry>X</entry><entry>Y</entry></record></csv>" },
    { "X\nY", "'header':true()", "<csv><record><X>Y</X></record></csv>" },
    { "X;Y", "'separator':';'",
      "<csv><record><entry>X</entry><entry>Y</entry></record></csv>" },
  };

  /** XML snippets. */
  private static final String[][] TOCSV = {
    { "<csv/>", "", "" },

    { "<csv><record/></csv>", "", "" },
    { "<csv><record/></csv>", "'header':'yes'", "" },

    { "<csv><record><entry>A</entry></record></csv>", "", "A" },
    { "<csv><record><entry>A</entry></record></csv>", "'header':'yes'", "entryA" },
    { "<csv><record><entry>A</entry><entry>B</entry></record></csv>", "", "A,B" },
    { "<csv><record><_>A</_><_>B</_></record></csv>", "'separator':';'", "A;B" },

    { "<csv><record><A>1</A></record></csv>", "'header':'yes'", "A1" },
    { "<csv><record><A>1</A><B>2</B></record></csv>", "'header':'yes'", "A,B1,2" },
  };

  /** Test method. */
  @Test public void parse() {
    for(final String[] test : TOXML) {
      final String expected = test[2];
      final String query = _CSV_PARSE.args(test[0], " {" + test[1] + "}");
      if(test.length == 1) {
        error(query, Err.BXCS_PARSE);
      } else if(expected.startsWith("...")) {
        contains(query, expected.substring(3));
      } else {
        query(query, expected);
      }
    }
  }

  /** Test method. */
  @Test public void serialize() {
    for(final String[] test : TOCSV) {
      final String expected = test[2];
      final String query = _CSV_SERIALIZE.args(test[0], " {" + test[1] + "}");
      if(test.length == 1) {
        error(query, Err.BXCS_SERIAL);
      } else if(test[1].startsWith("...")) {
        contains(query, expected.substring(3));
      } else {
        query(query, expected);
      }
    }
  }
}
