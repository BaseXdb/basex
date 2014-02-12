package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Index Module.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Andreas Weiler
 */
public final class FNIndexTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/input.xml";

  /**
   * Initializes a test.
   * @throws BaseXException database exception
   */
  @Before
  public void initTest() throws BaseXException {
    new CreateDB(NAME, FILE).execute(context);
  }

  /**
   * Finishes the code.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    new DropDB(NAME).execute(context);
  }

  /** Test method. */
  @Test
  public void facets() {
    final String tree = _INDEX_FACETS.args(NAME);
    query(tree + "//element[@name='head']/@count/data()", 1);
    query(tree + "//element[@name='title']/text/@type/data()", "category");
    query(tree + "//element[@name='li']/text/@count/data()", 2);

    final String flat = _INDEX_FACETS.args(NAME, "flat");
    query(flat + "//element[@name='title']/@count/data()", 1);
    query(flat + "//element[@name='title']/@type/data()", "category");
    query(flat + "//element[@name='li']/@count/data()", 2);
  }

  /** Test method. */
  @Test
  public void texts() {
    // complete search
    final String entries = _INDEX_TEXTS.args(NAME);
    query("count(" + entries + ')', 5);
    query("exists(" + entries + "/self::entry)", "true");
    query(entries + "/@count = 1", "true");
    query(entries + "/@count != 1", "false");
    // prefix search
    query(COUNT.args(_INDEX_TEXTS.args(NAME, "X")), 1);
    // ascending traversal
    query(COUNT.args(_INDEX_TEXTS.args(NAME, "X", "true()")), 1);
    // descending traversal
    query(_INDEX_TEXTS.args(NAME, "B", "false()") + "/text()", "Assignments");
    // main memory traversal
    // extract single entry
    //query("let $a := copy $a := parse-xml('<a>A</a>') modify () return $a " +
    //      "return " + _INDEX_TEXTS.args("$a") + "/text()", "A");
  }

  /** Test method. */
  @Test
  public void attributes() {
    // complete search
    final String entries = _INDEX_ATTRIBUTES.args(NAME);
    query("count(" + entries + ')', 6);
    query("exists(" + entries + "/self::entry)", "true");
    query(entries + "/@count = 1", "true");
    query(entries + "/@count != 1", "false");
    // prefix search
    query(_INDEX_ATTRIBUTES.args(NAME, "1") + "/text()", "1");
    // ascending traversal
    query(_INDEX_ATTRIBUTES.args(NAME, "X", "true()") + "/text()", "right");
    // descending traversal
    query(_INDEX_ATTRIBUTES.args(NAME, "#000099", "false()") + "/text()", "#000000");
  }

  /** Test method. */
  @Test
  public void elementNames() {
    final String entries = _INDEX_ELEMENT_NAMES.args(NAME);
    query("count(" + entries + ')', 9);
    query("exists(" + entries + "/self::entry)", "true");
  }

  /** Test method. */
  @Test
  public void attributeNames() {
    final String entries = _INDEX_ATTRIBUTE_NAMES.args(NAME);
    query("count(" + entries + ')', 5);
    query("exists(" + entries + "/self::entry)", "true");
  }
}
