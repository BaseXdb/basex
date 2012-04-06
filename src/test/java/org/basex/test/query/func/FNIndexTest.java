package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery index functions prefixed with "ix".
 *
 * @author BaseX Team 2005-12, BSD License
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

  /**
   * Test method for the facets() function.
   */
  @Test
  public void indexFacets() {
    check(_INDEX_FACETS);

    final String tree = _INDEX_FACETS.args(NAME);
    query(tree + "//element[@name='head']/@count/data()", 1);
    query(tree + "//element[@name='title']/text/@type/data()", "category");
    query(tree + "//element[@name='li']/text/@count/data()", 2);

    final String flat = _INDEX_FACETS.args(NAME, "flat");
    query(flat + "//element[@name='title']/@count/data()", 1);
    query(flat + "//element[@name='title']/@type/data()", "category");
    query(flat + "//element[@name='li']/@count/data()", 2);
  }

  /**
   * Test method for the texts() function.
   */
  @Test
  public void indexTexts() {
    check(_INDEX_TEXTS);

    String entries = _INDEX_TEXTS.args(NAME);
    query("count(" + entries + ')', 5);
    query("exists(" + entries + "/self::value)", "true");
    query(entries + "/@count = 1", "true");
    query(entries + "/@count != 1", "false");

    entries = _INDEX_TEXTS.args(NAME, "X");
    query("count(" + entries + ')', 1);
  }

  /**
   * Test method for the attributes() function.
   */
  @Test
  public void indexAttributes() {
    check(_INDEX_ATTRIBUTES);

    String entries = _INDEX_ATTRIBUTES.args(NAME);
    query("count(" + entries + ')', 6);
    query("exists(" + entries + "/self::value)", "true");
    query(entries + "/@count = 1", "true");
    query(entries + "/@count != 1", "false");

    entries = _INDEX_ATTRIBUTES.args(NAME, "1");
    query("count(" + entries + ')', 1);
  }

  /**
   * Test method for the element-names() function.
   */
  @Test
  public void indexElementNames() {
    check(_INDEX_ELEMENT_NAMES);

    final String entries = _INDEX_ELEMENT_NAMES.args(NAME);
    query("count(" + entries + ')', 9);
    query("exists(" + entries + "/self::value)", "true");
  }

  /**
   * Test method for the attribute-names() function.
   */
  @Test
  public void indexAttributeNames() {
    check(_INDEX_ATTRIBUTE_NAMES);

    final String entries = _INDEX_ATTRIBUTE_NAMES.args(NAME);
    query("count(" + entries + ')', 5);
    query("exists(" + entries + "/self::value)", "true");
  }
}
