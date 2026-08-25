package org.basex.query;

import static org.basex.core.Text.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.QueryInfo.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the structured query information.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryInfoTest extends SandboxTest {
  /** Enables the collection of query information. */
  @BeforeAll public static void start() {
    set(MainOptions.QUERYINFO, true);
  }

  /** Resets the options. */
  @AfterAll public static void stop() {
    set(MainOptions.QUERYINFO, false);
  }

  /** Sections of a successful query. */
  @Test public void sections() {
    final XQuery query = new XQuery("1 + 2");
    execute(query);

    final Map<String, Section> map = query.sections().sections();
    assertEquals(Arrays.asList("timing", "result", "optimized-query", "compilation", "query"),
        new ArrayList<>(map.keySet()));
    assertEquals("3", value(map, "optimized-query"));
    assertEquals("1 + 2", value(map, "query"));

    // keys are lower case, and displayed with title case
    final Entry items = map.get("result").entries().get(0);
    assertEquals("items", items.key());
    assertEquals("1", items.value());
    assertEquals(LI + "Items: 1", QueryInfo.lines(map.get("result")).get(0));
    assertEquals("Optimized Query", Strings.titleCase("optimized-query"));

    // one entry per phase, and the total time; the timing is the first section
    assertEquals(6, map.get("timing").entries().size());
    assertEquals(6, query.sections().times().size());
    assertEquals("total", map.get("timing").entries().get(5).key());
    assertEquals("parsing", map.values().iterator().next().entries().get(0).key());
    assertNull(query.message());
  }

  /** Sections and message of a failing query. */
  @Test public void error() {
    final XQuery query = new XQuery("1 + 'x'");
    query.run(context);

    assertTrue(query.message().contains("XPTY0004"), query.message());
    assertTrue(query.sections().sections().containsKey("query"));
  }

  /** The query plan is only included if it was requested. */
  @Test public void plan() {
    final XQuery query = new XQuery("1");
    execute(query);
    assertFalse(query.sections().sections().containsKey("plan"));

    set(MainOptions.XMLPLAN, true);
    try {
      final XQuery xml = new XQuery("1");
      execute(xml);
      assertTrue(value(xml.sections().sections(), "plan").startsWith("<QueryPlan"));
    } finally {
      set(MainOptions.XMLPLAN, false);
    }
  }

  /**
   * Returns the text of a section.
   * @param map sections
   * @param key key of the section
   * @return text
   */
  private static String value(final Map<String, Section> map, final String key) {
    final Section section = map.get(key);
    assertTrue(section.text(), key);
    return section.entries().get(0).value();
  }
}
