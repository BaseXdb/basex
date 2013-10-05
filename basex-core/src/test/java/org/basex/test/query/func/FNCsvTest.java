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
    { "", "'x':'y'" },
    { "X", "", "<csv><record><entry>X</entry></record></csv>" },
    { " '\"X\"\"Y\"'", "", "...<entry>X\"Y</entry>" },
    { " '\"X\",Y'", "", "...<entry>X</entry><entry>Y</entry>" },
    { "X\nY", "'separator':''" },
    { "X\nY", "'separator':'XXX'" },
    { "X,Y", "", "...<entry>X</entry><entry>Y</entry>" },
    { "X\nY", "'header':true()", "<csv><record><X>Y</X></record></csv>" },
    { "A,B,C\nX,Y,Z", "'header':true()", "...<A>X</A><B>Y</B><C>Z</C>" },
    { "X;Y", "'separator':';'", "...<entry>X</entry><entry>Y</entry>" },
  };

  /** XML snippets. */
  private static final String[][] TOCSV = {
    { "<csv/>", "'x':'y'" },

    { "<csv><record><A__>1</A__></record></csv>",
      "'header':true(),'lax':false()", "A_1" },
    { "<csv><record><_>1</_></record></csv>",
      "'header':true(),'lax':false()", "1" },
    { "<csv><record><A_0020B>1</A_0020B></record></csv>",
      "'header':'yes','lax':'no'", "A B1" },

    { "<csv><record><A_>1</A_></record></csv>",
      "'header':true(),'lax':true()", "A 1" },
    { "<csv><record><_>1</_></record></csv>",
      "'header':true(),'lax':true()", " 1" },
    { "<csv><record><A_0020B>1</A_0020B></record></csv>",
      "'header':'yes','lax':'yes'", "A 0020B1" },

    { "<csv/>", "", "" },

    { "<csv><record/></csv>", "", "" },
    { "<csv><record/></csv>", "'header':'yes'", "" },

    { "<csv><record><entry>A</entry></record></csv>", "", "A" },
    { "<csv><record><entry>A</entry></record></csv>", "'header':'yes'", "entryA" },
    { "<csv><record><entry>A</entry><entry>B</entry></record></csv>", "", "A,B" },
    { "<csv><record><_>A</_><_>B</_></record></csv>", "'separator':';'", "A;B" },
    { "<csv><record><_>A</_><_/><_>B</_></record></csv>", "", "A,,B" },
    { "<csv><record><_>A</_><_><X>X</X></_></record></csv>", "", "A" },

    { "<csv><record><A>1</A></record><record><A>2</A></record></csv>",
      "'header':'yes'", "A12" },

    { "<csv><record><A_B>1</A_B></record></csv>", "'header':'yes'", "A B1" },
    { "<csv><record><A__B>1</A__B></record></csv>", "'header':true()", "A  B1" },

    { "<csv><record><A>1\n2</A></record></csv>", "'header':'yes'", "A\"12\"" },
    { "<csv><record><A>\"</A></record></csv>", "'header':'yes'", "A\"\"\"\"" },
    { "<csv><record><A>1,2</A></record></csv>", "'header':'yes'", "A\"1,2\"" },
    { "<csv><record><A>1</A></record></csv>", "'header':'yes'", "A1" },
    { "<csv><record><A>1</A><B>2</B></record></csv>", "'header':'yes'", "A,B1,2" },
    { "<csv><record><A/><A>1</A><A>1</A><A/></record></csv>",
      "'header':'yes','separator':';'", "A1,1" },

    { "<csv><record><A>1</A></record></csv>", "'separator':''" },
    { "<csv><record><A>1</A></record></csv>", "'separator':'XX'" },
  };

  /** Test method. */
  @Test public void parse() {
    for(final String[] test : TOXML) {
      final String query = test[1].isEmpty() ? _CSV_PARSE.args(test[0]) :
        _CSV_PARSE.args(test[0], " {" + test[1] + "}");
      if(test.length == 2) {
        error(query, Err.BXCS_PARSE, Err.ELMOPTION);
      } else if(test[2].startsWith("...")) {
        contains(query, test[2].substring(3));
      } else {
        query(query, test[2]);
      }
    }
  }

  /** Test method. */
  @Test public void serialize() {
    for(final String[] test : TOCSV) {
      final String query = test[1].isEmpty() ? _CSV_SERIALIZE.args(test[0]) :
        _CSV_SERIALIZE.args(test[0], " {" + test[1] + "}");
      if(test.length == 2) {
        error(query, Err.BXCS_CONFSEP, Err.ELMOPTION);
      } else {
        query(query, test[2]);
      }
    }
  }
}
