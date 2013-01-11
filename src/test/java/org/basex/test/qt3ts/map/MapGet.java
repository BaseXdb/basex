package org.basex.test.qt3ts.map;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the map:get function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MapGet extends QT3TestSet {

  /**
   * Integer key, data is present.
   */
  @org.junit.Test
  public void mapGet001() {
    final XQuery query = new XQuery(
      "map:get(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",4:=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, 4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Wednesday")
    );
  }

  /**
   * Integer key, data is absent.
   */
  @org.junit.Test
  public void mapGet002() {
    final XQuery query = new XQuery(
      "map:get(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",4:=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, 23)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Empty map, data is absent.
   */
  @org.junit.Test
  public void mapGet003() {
    final XQuery query = new XQuery(
      "map:get(map{}, 23)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Singleton map, data is absent.
   */
  @org.junit.Test
  public void mapGet004() {
    final XQuery query = new XQuery(
      "map:get(map:entry(\"foo\", \"bar\"), \"baz\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Singleton map, data is present.
   */
  @org.junit.Test
  public void mapGet005() {
    final XQuery query = new XQuery(
      "map:get(map:entry(\"foo\", \"bar\"), \"foo\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "bar")
    );
  }

  /**
   * Singleton map, untyped atomic search key, data is present.
   */
  @org.junit.Test
  public void mapGet006() {
    final XQuery query = new XQuery(
      "map:get(map:entry(\"foo\", \"bar\"), xs:untypedAtomic(\"foo\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "bar")
    );
  }

  /**
   * Singleton map, untyped atomic data, data is present.
   */
  @org.junit.Test
  public void mapGet007() {
    final XQuery query = new XQuery(
      "map:get(map:entry(xs:untypedAtomic(\"foo\"), \"bar\"), \"foo\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "bar")
    );
  }

  /**
   * Singleton map, untyped atomic data, does not match numeric key.
   */
  @org.junit.Test
  public void mapGet008() {
    final XQuery query = new XQuery(
      "map:get(map:entry(xs:untypedAtomic(\"12\"), \"bar\"), 12)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Singleton map, untyped atomic search key, does not match numeric data.
   */
  @org.junit.Test
  public void mapGet009() {
    final XQuery query = new XQuery(
      "map:get(map:entry(12, \"bar\"), xs:untypedAtomic(\"12\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Integer key in data, double in search key.
   */
  @org.junit.Test
  public void mapGet010() {
    final XQuery query = new XQuery(
      "map:get(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",4:=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, 4.0e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Wednesday")
    );
  }

  /**
   * Double key in data, integer in search key.
   */
  @org.junit.Test
  public void mapGet011() {
    final XQuery query = new XQuery(
      "map:get(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",4.0e0:=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, 4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Wednesday")
    );
  }

  /**
   * String key in data, uri in search key.
   */
  @org.junit.Test
  public void mapGet012() {
    final XQuery query = new XQuery(
      "map:get(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",\"urn:weds\":=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, xs:anyURI(\"urn:weds\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Wednesday")
    );
  }

  /**
   * URI key in data, string in search key.
   */
  @org.junit.Test
  public void mapGet013() {
    final XQuery query = new XQuery(
      "map:get(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",xs:anyURI(\"urn:weds\"):=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, \"urn:weds\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Wednesday")
    );
  }

  /**
   * NaN as search key.
   */
  @org.junit.Test
  public void mapGet014() {
    final XQuery query = new XQuery(
      "map:get(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",xs:anyURI(\"urn:weds\"):=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, number('NaN'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * NaN in data and as search key.
   */
  @org.junit.Test
  public void mapGet015() {
    final XQuery query = new XQuery(
      "map:get(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",number('NaN'):=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, number('NaN'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Contains in a largish map.
   */
  @org.junit.Test
  public void mapGet016() {
    final XQuery query = new XQuery(
      "map:get(\n" +
      "                map:new(((for $i in 1 to 1000 return map:entry($i, $i*$i)),\n" +
      "                         (for $i in 2000 to 3000 return map:entry($i, $i+30)),\n" +
      "                         (for $i in 2500 to 3500 return map:entry($i, $i+30)))),\n" +
      "                         3260)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3290")
    );
  }

  /**
   * duration as search key.
   */
  @org.junit.Test
  public void mapGet017() {
    final XQuery query = new XQuery(
      "map:get(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",xs:duration('P1Y'):=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, xs:yearMonthDuration('P12M'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Wednesday")
    );
  }

  /**
   * QName as search key.
   */
  @org.junit.Test
  public void mapGet018() {
    final XQuery query = new XQuery(
      "map:get(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",function-name(abs#1):=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, xs:QName('fn:abs'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Wednesday")
    );
  }

  /**
   * get() returns () when the value is empty.
   */
  @org.junit.Test
  public void mapGet019() {
    final XQuery query = new XQuery(
      "map:get(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",4:=(),5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, 4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * default collation is not case-blind.
   */
  @org.junit.Test
  public void mapGet020() {
    final XQuery query = new XQuery(
      "map:get(map{\"su\":=\"Sunday\",\"mo\":=\"Monday\",\"tu\":=\"Tuesday\",\"we\":=\"Wednesday\",\"th\":=\"Thursday\",\"fr\":=\"Friday\",\"sa\":=\"Saturday\"}, \"TH\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * codepoint collation is not case-blind.
   */
  @org.junit.Test
  public void mapGet021() {
    final XQuery query = new XQuery(
      "map:get(map:new(map{\"su\":=\"Sunday\",\"mo\":=\"Monday\",\"tu\":=\"Tuesday\",\"we\":=\"Wednesday\",\"th\":=\"Thursday\",\"fr\":=\"Friday\",\"sa\":=\"Saturday\"}, \n" +
      "            \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"), \"TH\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * use map as a function item.
   */
  @org.junit.Test
  public void mapGet100() {
    final XQuery query = new XQuery(
      "fn:map(\n" +
      "            map{\"su\":=\"Sunday\",\"mo\":=\"Monday\",\"tu\":=\"Tuesday\",\"we\":=\"Wednesday\",\"th\":=\"Thursday\",\"fr\":=\"Friday\",\"sa\":=\"Saturday\"},\n" +
      "            (\"we\", \"th\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertCount(2)
      &&
        assertQuery("$result[1] eq \"Wednesday\"")
      &&
        assertQuery("$result[2] eq \"Thursday\"")
      )
    );
  }

  /**
   * Empty sequence as search key.
   */
  @org.junit.Test
  public void mapGet901() {
    final XQuery query = new XQuery(
      "map:contains(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",number('NaN'):=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, (1 to 5)[10])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Non singleton sequence as search key.
   */
  @org.junit.Test
  public void mapGet902() {
    final XQuery query = new XQuery(
      "map:get(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",number('NaN'):=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, (1 to 5)[. mod 2 = 0])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * First argument is not a map.
   */
  @org.junit.Test
  public void mapGet903() {
    final XQuery query = new XQuery(
      "map:get((\"a\", \"b\", \"c\"), \"a\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * First argument is not a map.
   */
  @org.junit.Test
  public void mapGet904() {
    final XQuery query = new XQuery(
      "map:get((), \"a\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * First argument is not a map.
   */
  @org.junit.Test
  public void mapGet905() {
    final XQuery query = new XQuery(
      "map:get(abs#1, \"a\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * First argument is a sequence of maps.
   */
  @org.junit.Test
  public void mapGet906() {
    final XQuery query = new XQuery(
      "map:get((map{}, map{\"a\":=\"b\"}), \"a\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }
}
