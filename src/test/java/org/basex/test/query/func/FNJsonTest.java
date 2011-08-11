package org.basex.test.query.func;

import org.basex.query.func.Function;
import org.basex.query.util.Err;
import org.basex.test.query.AdvancedQueryTest;
import org.junit.Test;

/**
 * JSON Parser Test.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNJsonTest extends AdvancedQueryTest {
  /** JSON snippets. */
  private static final String[][] TOXML = {
    { "{" },
    { "{}", "<json type=\"object\"/>" },
    { "  { } ", "<json type=\"object\"/>" },
    { "{ \"" },
    { "{ \"\\c\" : 0 }" },
    { "{ \"\\t\" : 0 }",
      "...<pair name=\"&#x09;\" type=\"number\">0</pair>"  },
    { "{ \"a\" :0 }",
      "...<pair name=\"a\" type=\"number\">0</pair>" },
    { "{ \"\" : 0 }",
      "...<pair name=\"\" type=\"number\">0</pair>" },
    { "{ \"\" : 0.0e0 }",
      "...<pair name=\"\" type=\"number\">0.0e0</pair>" },
    { "{ \"\" : null }",
      "...<pair name=\"\" type=\"null\"/>" },
    { "{ \"\" : true }",
      "...<pair name=\"\" type=\"boolean\">true</pair>" },
    { "{ \"\" : {} }",
      "...<pair name=\"\" type=\"object\"/>" },
    { "{ \"\" : [] }",
      "...<pair name=\"\" type=\"array\"/>" },
    { "{ \"\" : 0, \"\": 1 }",
      "... type=\"number\">0</pair><pair name=\"\" type=\"number\">1</pair>" },
    { "{ \"\" : [ 1 ] }",
      "...<pair name=\"\" type=\"array\"><item type=\"number\">1</item>" },
    { "{ \"\" : [ 0,1 ] }",
    "... type=\"number\">0</item><item type=\"number\">1</item>" },
    { "{ \"\" : 00 }" },
    { "{ \"\" : 0. }" },
    { "{ \"\" : 0.0 }", "...0.0" },
    { "{ \"\" : 0e }" },
    { "{ \"\" : 0.1. }" },
    { "{ \"\" : 0.1e }" },
    { "{ \"a\" : 0, }" },
    { "{ \"a\" : 0 }}" },
  };

  /** XML snippets. */
  private static final String[][] TOJSON = {
    { "<a/>" }, // invalid tag
    { "<json/>" }, // no type specified
    { "<json type='o'/>" }, // invalid type
    { "<json type='object'/>", "{}" },
    { "<json type='array'/>", "[]" },
    { "<json type='number'>1</json>" }, // no text allowed in json tag
    { "<json type='array'><item type='null'/></json>", "[null]" },
    { "<json type='array'><item type='number'/></json>" }, // value needed
    { "<json type='array'><item type='boolean'/></json>" }, // value needed
    { "<json type='array'><item type='null'>x</item></json>" }, // no value
    { "<json type='array'><item type='string'/></json>", "[\"\"]" },
    { "<json type='array'><item type='string'>x</item></json>", "[\"x\"]" },
    { "<json type='array'><item type='number'>1</item></json>", "[1]" },
  };

  /**
   * Test method for the json:parse() function.
   */
  @Test
  public void jsonParse() {
    final String fun = check(Function.JPARSE);
    for(final String[] f : TOXML) {
      final String qu = fun + "('" + f[0] + "')";
      if(f.length == 1) {
        error(qu, Err.JSONPARSE);
      } else if(f[1].startsWith("...")) {
        contains(qu, f[1].substring(3));
      } else {
        query(qu, f[1]);
      }
    }
  }

  /**
   * Test method for the json:serialize() function.
   */
  @Test
  public void jsonSerialize() {
    final String fun = check(Function.JSERIALIZE);
    for(final String[] f : TOJSON) {
      final String qu = fun + "(" + f[0] + ")";
      if(f.length == 1) {
        error(qu, Err.JSONSER);
      } else if(f[1].startsWith("...")) {
        contains(qu, f[1].substring(3));
      } else {
        query(qu, f[1]);
      }
    }
  }
}
