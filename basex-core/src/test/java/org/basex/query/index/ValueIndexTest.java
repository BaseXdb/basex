package org.basex.query.index;

import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.ast.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.index.*;
import org.basex.query.value.node.*;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;

/**
 * This class tests if value indexes will be used.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
@RunWith(Parameterized.class)
public final class ValueIndexTest extends QueryPlanTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/selective.xml";

  /** Main memory flag. */
  @Parameter
  public Object mainmem;

  /**
   * Mainmem parameters.
   * @return parameters
   */
  @Parameters
  public static Collection<Object[]> params() {
    final List<Object[]> params = new ArrayList<>();
    params.add(new Object[] { false });
    params.add(new Object[] { true });
    return params;
  }

  /**
   * Initializes a test.
   */
  @Before public void before() {
    set(MainOptions.MAINMEM, mainmem);
  }

  /**
   * Finalizes a test.
   */
  @After public void after() {
    set(MainOptions.MAINMEM, false);
    set(MainOptions.UPDINDEX, false);
    set(MainOptions.FTINDEX, false);
    set(MainOptions.TEXTINCLUDE, "");
    set(MainOptions.ATTRINCLUDE, "");
    set(MainOptions.TOKENINCLUDE, "");
    set(MainOptions.FTINCLUDE, "");
  }

  /**
   * Initializes the tests.
   */
  @BeforeClass public static void start() {
    execute(new CreateDB(NAME, FILE));
  }

  /**
   * Tests the text index.
   */
  @Test public void textIndex() {
    map().forEach((key, value) -> {
      set(MainOptions.TEXTINCLUDE, key);
      execute(new CreateDB(NAME, FILE));
      check("count(//" + key + "[text() = " + value + "])",
          value.split(",").length, exists(ValueAccess.class));
      if(!key.equals("*")) check("//X[text() = 'unknown']", "", exists(DBNode.class));
    });
  }

  /**
   * Tests the attribute index.
   */
  @Test public void attrIndex() {
    map().forEach((key, value) -> {
      set(MainOptions.ATTRINCLUDE, key);
      execute(new CreateDB(NAME, FILE));
      check("count(//*[@" + key + " = " + value + "])",
          value.split(",").length, exists(ValueAccess.class));
      if(!key.equals("*")) check("//*[@x = 'unknown']", "", exists(DBNode.class));
    });
  }

  /**
   * Tests the full-text index.
   */
  @Test public void fulltextIndex() {
    // not applicable in main-memory mode
    if((Boolean) mainmem) return;

    set(MainOptions.FTINDEX, true);
    map().forEach((key, value) -> {
      set(MainOptions.FTINCLUDE, key);
      execute(new CreateDB(NAME, FILE));
      check("count(//" + key + "[text() contains text { " + value + " }])",
          value.split(",").length,
          exists(FTIndexAccess.class));
      if(!key.equals("*")) check("//X[text() contains text 'unknown']", "", exists(DBNode.class));
    });
  }

  /**
   * Tests the text index and update operations.
   */
  @Test public void textUpdates() {
    set(MainOptions.UPDINDEX, true);

    set(MainOptions.TEXTINCLUDE, "a");
    execute(new CreateDB(NAME, "<x><a>text</a><b>TEXT</b></x>"));
    check("count(//a[text() = 'text'])", 1, exists(ValueAccess.class));
    check("count(//b[text() = 'TEXT'])", 1, empty(ValueAccess.class));

    query("replace value of node x/a with 'TEXT'");
    check("count(//a[text() = 'TEXT'])", 1, exists(ValueAccess.class));

    query("rename node x/a as 'b'");
    check("//a[text() = 'TEXT']", "", empty());
    check("count(//b[text() = 'TEXT'])", 2, empty(ValueAccess.class));

    query("x/b/(rename node . as 'a')");
    check("count(//a[text() = 'TEXT'])", 2, exists(ValueAccess.class));
    check("count(//b[text() = 'TEXT'])", 0, empty(ValueAccess.class));

    query("x/a/(replace value of node . with 'text')");
    check("count(//a[text() = 'text'])", 2, exists(ValueAccess.class));

    query("delete node x/a[1]");
    check("count(//a[text() = 'text'])", 1, exists(ValueAccess.class));

    query("delete node x/a[1]");
    check("//a[text() = 'text']", "", empty());
  }

  /**
   * Returns a map with name tests.
   * @return map
   */
  private static HashMap<String, String> map() {
    final LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("*", "'A'");
    map.put("a", "'A'");
    map.put("*:c", "('C','PC')");
    map.put("Q{ns}*", "('PC','PD')");
    map.put("Q{ns}c", "'PC'");
    return map;
  }
}
