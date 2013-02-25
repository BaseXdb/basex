package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * JSON Parser Test.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNJsonTest extends AdvancedQueryTest {
  /** JSON snippets. */
  private static final String[][] TOXML = {
    { "" },
    { "{" },
    { "[]", "<json arrays=\"json\"/>" },
    { "{}", "<json objects=\"json\"/>" },
    { "{ } ", "<json objects=\"json\"/>" },
    { "{ \"" },
    { "{ \"\\c\" : 0 }" },
    { "{ \"\\t\" : 0 }",
      "<json objects=\"json\" numbers=\"_0009\"><_0009>0</_0009></json>"  },
    { "{ \"a\" :0 }",
      "<json objects=\"json\" numbers=\"a\"><a>0</a></json>" },
    { "{ \"\" : 0 }",
      "<json objects=\"json\" numbers=\"_\"><_>0</_></json>" },
    { "{ \"\" : 0.0e0 }",
      "...<_>0.0e0</_>" },
    { "{ \"\" : null }",
      "...<_/>" },
    { "{ \"\" : true }",
      "...<_>true</_>" },
    { "{ \"\" : {} }",
      "... objects=\"json _\"><_/>" },
    { "{ \"\" : [] }",
      "... arrays=\"_\" objects=\"json\"><_/>" },
    { "{ \"\" : 0, \"\": 1 }",
      "... objects=\"json\" numbers=\"_\"><_>0</_><_>1</_>" },
    { "{ \"\" : [ 1 ] }",
      "... arrays=\"_\" objects=\"json\" numbers=\"value\"><_><value>1</value></_>" },
    { "{ \"A\" : [ 0,1 ] }",
      "... objects=\"json\" numbers=\"value\"><A><value>0</value><value>1</value>" },
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
    { "<json objects='json'/>", "{}" },
    { "<json type='array'/>", "[]" },
    { "<json arrays='json'/>", "[]" },
    { "<json type='number'>1</json>" }, // no text allowed in json tag
    { "<json type='array'><item type='null'/></json>", "[null]" },
    { "<json type='array'><item type='number'/></json>" }, // value needed
    { "<json type='array'><item type='boolean'/></json>" }, // value needed
    { "<json type='array'><item type='null'>x</item></json>" }, // no value
    { "<json type='array'><item type='string'/></json>", "[\"\"]" },
    { "<json type='array'><item type='string'>x</item></json>", "[\"x\"]" },
    { "<json type='array'><item type='number'>1</item></json>", "[1]" },
    { "<json numbers=\"item\" type='array'><item>1</item></json>", "[1]" },
  };

  /** Test method. */
  @Test public void parse() {
    for(final String[] f : TOXML) {
      final String qu = _JSON_PARSE.args(f[0]);
      if(f.length == 1) {
        error(qu, Err.BXJS_PARSE);
      } else if(f[1].startsWith("...")) {
        contains(qu, f[1].substring(3));
      } else {
        query(qu, f[1]);
      }
    }
  }

  /** Test method. */
  @Test public void serialize() {
    for(final String[] f : TOJSON) {
      final String qu = _JSON_SERIALIZE.args(f[0]);
      if(f.length == 1) {
        error(qu, Err.BXJS_SER);
      } else if(f[1].startsWith("...")) {
        contains(qu, f[1].substring(3));
      } else {
        query(qu, f[1]);
      }
    }
  }

  /** Tests the configuration argument of {@code json:parse(...)}. */
  @Test public void config() {
    query("json:parse('[\"foo\",{\"test\":\"asdf\"}]', map{'type':='jsonml'})",
        "<foo test=\"asdf\"/>");
    query("map:size(json:parse('[\"foo\",{\"test\":\"asdf\"}]', map{'type':='maps'}))",
        "2");
    query("json:parse('\"\\t\\u000A\"'," +
        "  map{'type':='maps','unescape':=false(),'spec':='liberal'})", "\\t\\u000A");
    query("string-to-codepoints(json:parse('\"\\t\\u000A\"'," +
        "  map{'type':='maps','unescape':=true(),'spec':='liberal'}))", "9 10");
    error("json:parse('42', map{'spec':='garbage'})", Err.BXJS_PARSE_CFG);
  }
}
