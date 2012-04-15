package org.basex.test.qt3ts.app;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the UseCaseSEQ.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class AppUseCaseSEQ extends QT3TestSet {

  /**
.
   */
  @org.junit.Test
  public void seqQueriesResultsQ1() {
    final XQuery query = new XQuery(
      "for $s in //section[section.title = \"Procedure\"] return ($s//incision)[2]/instrument",
      ctx);
    query.context(node(file("docs/report1.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<instrument>electrocautery</instrument>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void seqQueriesResultsQ2() {
    final XQuery query = new XQuery(
      "for $s in //section[section.title = \"Procedure\"] return ($s//instrument)[position()<=2]",
      ctx);
    query.context(node(file("docs/report1.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<instrument>using electrocautery.</instrument><instrument>electrocautery</instrument>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void seqQueriesResultsQ3() {
    final XQuery query = new XQuery(
      "let $i2 := (//incision)[2] for $a in (//action)[. >> $i2][position()<=2] return $a//instrument",
      ctx);
    query.context(node(file("docs/report1.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<instrument>Hasson trocar</instrument><instrument>trocar</instrument>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void seqQueriesResultsQ4() {
    final XQuery query = new XQuery(
      "for $p in //section[section.title = \"Procedure\"] where not(some $a in $p//anesthesia satisfies $a << ($p//incision)[1] ) return $p",
      ctx);
    query.context(node(file("docs/report1.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void seqQueriesResultsQ5() {
    final XQuery query = new XQuery(
      "<critical_sequence> { let $proc := //section[section.title=\"Procedure\"][1], $i1 := ($proc//incision)[1], $i2 := ($proc//incision)[2] for $n in $proc//node() except $i1//node() where $n >> $i1 and $n << $i2 return $n } </critical_sequence>",
      ctx);
    query.context(node(file("docs/report1.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<critical_sequence>\n      The fascia was identified and\n      <action>#2 0 Maxon stay sutures were placed on each side of the midline.\n      </action>#2 0 Maxon stay sutures were placed on each side of the midline.\n      \n      </critical_sequence>", false)
    );
  }
}
