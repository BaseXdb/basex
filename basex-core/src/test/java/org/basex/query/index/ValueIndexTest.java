package org.basex.query.index;

import java.util.*;
import java.util.List;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.ast.*;
import org.basex.query.expr.ft.*;
import org.basex.util.*;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;

/**
 * This class tests if value indexes will be used.
 *
 * @author BaseX Team 2005-16, BSD License
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
  @Before
  public void before() {
    set(MainOptions.MAINMEM, mainmem);
  }

  /**
   * Finalizes a test.
   */
  @After
  public void after() {
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
  @BeforeClass
  public static void start() {
    execute(new CreateDB(NAME, FILE));
  }

  /**
   * Tests the text index.
   */
  @Test
  public void textIndex() {
    for(final Entry<String, String> entry : map().entrySet()) {
      final String key = entry.getKey(), value = entry.getValue();
      set(MainOptions.TEXTINCLUDE, key);
      execute(new CreateDB(NAME, FILE));
      check("count(//" + key + "[text() = " + value + "])",
          Integer.toString(value.split(",").length), "exists(//ValueAccess)");
      if(!key.equals("*")) check("//X[text() = 'unknown']", "", "exists(//DBNode)");
    }
  }

  /**
   * Tests the attribute index.
   */
  @Test
  public void attrIndex() {
    for(final Entry<String, String> entry : map().entrySet()) {
      final String key = entry.getKey(), value = entry.getValue();
      set(MainOptions.ATTRINCLUDE, key);
      execute(new CreateDB(NAME, FILE));
      check("count(//*[@" + key + " = " + value + "])",
          Integer.toString(value.split(",").length), "exists(//ValueAccess)");
      if(!key.equals("*")) check("//*[@x = 'unknown']", "", "exists(//DBNode)");
    }
  }

  /**
   * Tests the full-text index.
   */
  @Test
  public void fulltextIndex() {
    // not applicable in main-memory mode
    if((Boolean) mainmem) return;

    set(MainOptions.FTINDEX, true);
    for(final Entry<String, String> entry : map().entrySet()) {
      final String key = entry.getKey(), value = entry.getValue();
      set(MainOptions.FTINCLUDE, key);
      execute(new CreateDB(NAME, FILE));
      check("count(//" + key + "[text() contains text { " + value + " }])",
          Integer.toString(value.split(",").length),
          "exists(//" + Util.className(FTIndexAccess.class) + ")");
      if(!key.equals("*")) check("//X[text() contains text 'unknown']", "", "exists(//DBNode)");
    }
  }

  /**
   * Tests the text index and update operations.
   */
  @Test
  public void textUpdates() {
    set(MainOptions.UPDINDEX, true);

    set(MainOptions.TEXTINCLUDE, "a");
    execute(new CreateDB(NAME, "<x><a>text</a><b>TEXT</b></x>"));
    check("count(//a[text() = 'text'])", "1", "exists(//ValueAccess)");
    check("count(//b[text() = 'TEXT'])", "1", "empty(//ValueAccess)");

    query("replace value of node x/a with 'TEXT'");
    check("count(//a[text() = 'TEXT'])", "1", "exists(//ValueAccess)");

    query("rename node x/a as 'b'");
    check("//a[text() = 'TEXT']", "", "exists(//Empty)");
    check("count(//b[text() = 'TEXT'])", "2", "empty(ValueAccess)");

    query("x/b/(rename node . as 'a')");
    check("count(//a[text() = 'TEXT'])", "2", "exists(//ValueAccess)");
    check("count(//b[text() = 'TEXT'])", "0", "empty(//ValueAccess)");

    query("x/a/(replace value of node . with 'text')");
    check("count(//a[text() = 'text'])", "2", "exists(//ValueAccess)");

    query("delete node x/a[1]");
    check("count(//a[text() = 'text'])", "1", "exists(//ValueAccess)");

    query("delete node x/a[1]");
    check("//a[text() = 'text']", "", "exists(//Empty)");
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
