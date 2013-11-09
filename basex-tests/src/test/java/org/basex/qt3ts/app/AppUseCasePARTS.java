package org.basex.qt3ts.app;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the UseCasePARTS.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class AppUseCasePARTS extends QT3TestSet {

  /**
.
   */
  @org.junit.Test
  public void partsQueriesResultsQ1() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $input-context := .;\n" +
      "        declare function local:one_level($p as element()) as element() { \n" +
      "            <part partid=\"{ $p/@partid }\" name=\"{ $p/@name }\" > { \n" +
      "                for $s in ($input-context//part)\n" +
      "                where $s/@partof = $p/@partid \n" +
      "                return local:one_level($s) } </part> }; \n" +
      "        <parttree> { for $p in //part[empty(@partof)] \n" +
      "                     return local:one_level($p) } </parttree>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/partlist.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<parttree><part partid=\"0\" name=\"car\"><part partid=\"1\" name=\"engine\"><part partid=\"3\" name=\"piston\"/></part><part partid=\"2\" name=\"door\"><part partid=\"4\" name=\"window\"/><part partid=\"5\" name=\"lock\"/></part></part><part partid=\"10\" name=\"skateboard\"><part partid=\"11\" name=\"board\"/><part partid=\"12\" name=\"wheel\"/></part><part partid=\"20\" name=\"canoe\"/></parttree>", false)
      ||
        error("XPTY0004")
      )
    );
  }
}
