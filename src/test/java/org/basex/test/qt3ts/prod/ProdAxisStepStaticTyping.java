package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the AxisStep production with (pessimistic) static typing.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdAxisStepStaticTyping extends QT3TestSet {

  /**
   *  self:: axis with explicit name test called on set of nodes which do not contain any nodes of this name .
   */
  @org.junit.Test
  public void sTAxes001() {
    final XQuery query = new XQuery(
      "fn:count(//center/self::nowhere)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }

  /**
   *  Path 'self::*' from an attribute. .
   */
  @org.junit.Test
  public void sTAxes002() {
    final XQuery query = new XQuery(
      "fn:count(//center/@center-attr-2/self::*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }

  /**
   *  Path 'self::' with specified attribute name, from that attribute. .
   */
  @org.junit.Test
  public void sTAxes003() {
    final XQuery query = new XQuery(
      "fn:count(//center/@center-attr-3/self::center-attr-3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }

  /**
   *  Path 'self::' with specified attribute name that is not found, from an attribute. .
   */
  @org.junit.Test
  public void sTAxes004() {
    final XQuery query = new XQuery(
      "fn:count(//center/@center-attr-1/self::nowhere)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }

  /**
   *  self::name from a text node gets nothing, because node kind differs .
   */
  @org.junit.Test
  public void sTAxes005() {
    final XQuery query = new XQuery(
      "fn:count(//center/text()/self::*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }

  /**
   *  self::name from a text node gets nothing, because node kind differs .
   */
  @org.junit.Test
  public void sTAxes006() {
    final XQuery query = new XQuery(
      "fn:count(//center/text()/self::center)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }

  /**
   *  descendant-or-self::* from an attribute gets nothing .
   */
  @org.junit.Test
  public void sTAxes007() {
    final XQuery query = new XQuery(
      "fn:count(//center/@center-attr-1/descendant-or-self::*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }

  /**
   *  descendant-or-self::name from an attribute gets nothing .
   */
  @org.junit.Test
  public void sTAxes008() {
    final XQuery query = new XQuery(
      "fn:count(//center/@center-attr-2/descendant-or-self::far-south)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }

  /**
   *  descendant-or-self::name from an attribute gets nothing, even with attribute's name .
   */
  @org.junit.Test
  public void sTAxes009() {
    final XQuery query = new XQuery(
      "fn:count(//center/@center-attr-2/descendant-or-self::center-attr-2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }

  /**
   *  descendant-or-self::* from a text node gets nothing .
   */
  @org.junit.Test
  public void sTAxes010() {
    final XQuery query = new XQuery(
      "fn:count(//center/text()/descendant-or-self::*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }

  /**
   *  /attribute::* gets nothing because root can't have attributes .
   */
  @org.junit.Test
  public void sTAxes011() {
    final XQuery query = new XQuery(
      "fn:count(/attribute::*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }

  /**
   *  /@* gets nothing because root can't have attributes .
   */
  @org.junit.Test
  public void sTAxes012() {
    final XQuery query = new XQuery(
      "fn:count(/@*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }

  /**
   *  /parent::* gets nothing because root can't have parent .
   */
  @org.junit.Test
  public void sTAxes013() {
    final XQuery query = new XQuery(
      "fn:count(/parent::*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }

  /**
   *  /.. gets nothing because root can't have parent .
   */
  @org.junit.Test
  public void sTAxes014() {
    final XQuery query = new XQuery(
      "fn:count(/..)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }

  /**
   *  /self::* gets nothing because root is not an element node .
   */
  @org.junit.Test
  public void sTAxes015() {
    final XQuery query = new XQuery(
      "fn:count(/self::*)",
      ctx);
    query.context(node(file("prod/AxisStep/TopMany.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0005")
    );
  }
}
