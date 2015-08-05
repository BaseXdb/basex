package org.basex.query.index;

import java.util.*;
import java.util.List;

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
 * @author BaseX Team 2005-15, BSD License
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

  /** Initializes a test. */
  @Before
  public void before() {
    context.options.set(MainOptions.MAINMEM, (Boolean) mainmem);
  }

  /** Finalizes a test. */
  @After
  public void after() {
    context.options.set(MainOptions.MAINMEM, false);
    context.options.set(MainOptions.UPDINDEX, false);
    context.options.set(MainOptions.FTINDEX, false);
    context.options.set(MainOptions.TEXTINCLUDE, "");
    context.options.set(MainOptions.ATTRINCLUDE, "");
    context.options.set(MainOptions.FTINCLUDE, "");
  }

  /**
   * Initializes the tests.
   * @throws BaseXException database exception
   */
  @BeforeClass
  public static void start() throws BaseXException {
    new CreateDB(NAME, FILE).execute(context);
  }

  /**
   * Tests the text index.
   * @throws BaseXException database exception
   */
  @Test
  public void textIndex() throws BaseXException {
    for(final Map.Entry<String, String> entry : map().entrySet()) {
      final String key = entry.getKey(), value = entry.getValue();
      context.options.set(MainOptions.TEXTINCLUDE, key);
      new CreateDB(NAME, FILE).execute(context);
      check("count(//" + key + "[text() = " + value + "])",
          Integer.toString(value.split(",").length), "exists(//ValueAccess)");
      if(!key.equals("*")) check("//X[text() = 'unknown']", "", "exists(//DBNode)");
    }
  }

  /**
   * Tests the attribute index.
   * @throws BaseXException database exception
   */
  @Test
  public void attrIndex() throws BaseXException {
    for(final Map.Entry<String, String> entry : map().entrySet()) {
      final String key = entry.getKey(), value = entry.getValue();
      context.options.set(MainOptions.ATTRINCLUDE, key);
      new CreateDB(NAME, FILE).execute(context);
      check("count(//*[@" + key + " = " + value + "])",
          Integer.toString(value.split(",").length), "exists(//ValueAccess)");
      if(!key.equals("*")) check("//*[@x = 'unknown']", "", "exists(//DBNode)");
    }
  }

  /**
   * Tests the full-text index.
   * @throws BaseXException database exception
   */
  @Test
  public void fulltextIndex() throws BaseXException {
    // not applicable in main-memory mode
    if(((Boolean) mainmem).booleanValue()) return;

    context.options.set(MainOptions.FTINDEX, true);
    for(final Map.Entry<String, String> entry : map().entrySet()) {
      final String key = entry.getKey(), value = entry.getValue();
      context.options.set(MainOptions.FTINCLUDE, key);
      new CreateDB(NAME, FILE).execute(context);
      check("count(//" + key + "[text() contains text { " + value + " }])",
          Integer.toString(value.split(",").length),
          "exists(//" + Util.className(FTIndexAccess.class) + ")");
      if(!key.equals("*")) check("//X[text() contains text 'unknown']", "", "exists(//DBNode)");
    }
  }

  /**
   * Tests the text index and update operations.
   * @throws BaseXException database exception
   */
  @Test
  public void textUpdates() throws BaseXException {
    // [CG] MAINMEM: needs to be fixed
    if(((Boolean) mainmem).booleanValue()) return;

    context.options.set(MainOptions.UPDINDEX, true);

    context.options.set(MainOptions.TEXTINCLUDE, "a");
    new CreateDB(NAME, "<x><a>text</a><b>TEXT</b></x>").execute(context);
    check("count(//a[text() = 'text'])", "1", "exists(//ValueAccess)");
    check("count(//b[text() = 'TEXT'])", "1", "empty(//ValueAccess)");

    new XQuery("replace value of node x/a with 'TEXT'").execute(context);
    check("count(//a[text() = 'TEXT'])", "1", "exists(//ValueAccess)");

    new XQuery("rename node x/a as 'b'").execute(context);
    check("//a[text() = 'TEXT']", "", "exists(//Empty)");
    check("count(//b[text() = 'TEXT'])", "2", "empty(ValueAccess)");

    new XQuery("x/b/(rename node . as 'a')").execute(context);
    check("count(//a[text() = 'TEXT'])", "2", "exists(//ValueAccess)");
    check("count(//b[text() = 'TEXT'])", "0", "empty(//ValueAccess)");

    new XQuery("x/a/(replace value of node . with 'text')").execute(context);
    check("count(//a[text() = 'text'])", "2", "exists(//ValueAccess)");

    new XQuery("delete node x/a[1]").execute(context);
    check("count(//a[text() = 'text'])", "1", "exists(//ValueAccess)");

    new XQuery("delete node x/a[1]").execute(context);
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
