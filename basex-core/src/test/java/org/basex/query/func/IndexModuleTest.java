package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the functions of the Index Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Andreas Weiler
 */
public final class IndexModuleTest extends SandboxTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/input.xml";

  /** Initializes a test. */
  @BeforeEach public void initTest() {
    execute(new CreateDB(NAME, FILE));
  }

  /** Finishes a test. */
  @AfterAll public static void finish() {
    execute(new DropDB(NAME));
  }

  /** Test method. */
  @Test public void attributeNames() {
    final Function func = _INDEX_ATTRIBUTE_NAMES;
    // queries
    final String entries = func.args(NAME);
    query("count(" + entries + ')', 5);
    query("exists(" + entries + "/self::entry)", true);
  }

  /** Test method. */
  @Test public void attributes() {
    final Function func = _INDEX_ATTRIBUTES;
    // complete search
    final String entries = func.args(NAME);
    query("count(" + entries + ')', 6);
    query("exists(" + entries + "/self::entry)", true);
    query(entries + "/@count = 1", true);
    query(entries + "/@count != 1", false);
    // prefix search
    query(func.args(NAME, "1") + "/text()", 1);
    // ascending traversal
    query(func.args(NAME, "X", true) + "/text()", "right");
    // descending traversal
    query(func.args(NAME, "#000099", false) + "/text()", "#000000");
  }

  /** Test method. */
  @Test public void elementNames() {
    final Function func = _INDEX_ELEMENT_NAMES;
    // queries
    final String entries = func.args(NAME);
    query("count(" + entries + ')', 9);
    query("exists(" + entries + "/self::entry)", true);
  }

  /** Test method. */
  @Test public void facets() {
    final Function func = _INDEX_FACETS;
    // queries
    final String tree = func.args(NAME);
    query(tree + "//element[@name='head']/@count/data()", 1);
    query(tree + "//element[@name='title']/text/@type/data()", "string");
    query(tree + "//element[@name='title']/text/entry/data()", "XML");
    query(tree + "//element[@name='title']/text/entry/@count/data()", 1);
    query(tree + "//element[@name='li']/text/@count/data()", 2);

    final String flat = func.args(NAME, "flat");
    query(flat + "//element[@name='title']/@count/data()", 1);
    query(flat + "//element[@name='title']/@type/data()", "string");
    query(tree + "//element[@name='title']/text/entry/data()", "XML");
    query(tree + "//element[@name='title']/text/entry/@count/data()", 1);
    query(flat + "//element[@name='li']/@count/data()", 2);
  }

  /** Test method. */
  @Test public void texts() {
    final Function func = _INDEX_TEXTS;
    // complete search
    final String entries = func.args(NAME);
    query("count(" + entries + ')', 5);
    query("exists(" + entries + "/self::entry)", true);
    query(entries + "/@count = 1", true);
    query(entries + "/@count != 1", false);
    // prefix search
    query(COUNT.args(func.args(NAME, "X")), 1);
    // ascending traversal
    query(COUNT.args(func.args(NAME, "X", true)), 1);
    // descending traversal
    query(func.args(NAME, "B", false) + "/text()", "Assignments");
  }
}
