package org.basex.test.qt3ts.map;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the map:contains function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MapContains extends QT3TestSet {

  /**
   * Integer key, data is present.
   */
  @org.junit.Test
  public void mapContains001() {
    final XQuery query = new XQuery(
      "map:contains(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",4:=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, 4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Integer key, data is absent.
   */
  @org.junit.Test
  public void mapContains002() {
    final XQuery query = new XQuery(
      "map:contains(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",4:=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, 23)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Empty map, data is absent.
   */
  @org.junit.Test
  public void mapContains003() {
    final XQuery query = new XQuery(
      "map:contains(map{}, 23)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Singleton map, data is absent.
   */
  @org.junit.Test
  public void mapContains004() {
    final XQuery query = new XQuery(
      "map:contains(map:entry(\"foo\", \"bar\"), \"baz\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Singleton map, data is present.
   */
  @org.junit.Test
  public void mapContains005() {
    final XQuery query = new XQuery(
      "map:contains(map:entry(\"foo\", \"bar\"), \"foo\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Singleton map, untyped atomic search key, data is present.
   */
  @org.junit.Test
  public void mapContains006() {
    final XQuery query = new XQuery(
      "map:contains(map:entry(\"foo\", \"bar\"), xs:untypedAtomic(\"foo\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Singleton map, untyped atomic data, data is present.
   */
  @org.junit.Test
  public void mapContains007() {
    final XQuery query = new XQuery(
      "map:contains(map:entry(xs:untypedAtomic(\"foo\"), \"bar\"), \"foo\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Singleton map, untyped atomic data, does not match numeric key.
   */
  @org.junit.Test
  public void mapContains008() {
    final XQuery query = new XQuery(
      "map:contains(map:entry(xs:untypedAtomic(\"12\"), \"bar\"), 12)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Singleton map, untyped atomic search key, does not match numeric data.
   */
  @org.junit.Test
  public void mapContains009() {
    final XQuery query = new XQuery(
      "map:contains(map:entry(12, \"bar\"), xs:untypedAtomic(\"12\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Integer key in data, double in search key.
   */
  @org.junit.Test
  public void mapContains010() {
    final XQuery query = new XQuery(
      "map:contains(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",4:=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, 4.0e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Double key in data, integer in search key.
   */
  @org.junit.Test
  public void mapContains011() {
    final XQuery query = new XQuery(
      "map:contains(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",4.0e0:=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, 4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * String key in data, uri in search key.
   */
  @org.junit.Test
  public void mapContains012() {
    final XQuery query = new XQuery(
      "map:contains(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",\"urn:weds\":=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, xs:anyURI(\"urn:weds\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * URI key in data, string in search key.
   */
  @org.junit.Test
  public void mapContains013() {
    final XQuery query = new XQuery(
      "map:contains(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",xs:anyURI(\"urn:weds\"):=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, \"urn:weds\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * NaN as search key.
   */
  @org.junit.Test
  public void mapContains014() {
    final XQuery query = new XQuery(
      "map:contains(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",xs:anyURI(\"urn:weds\"):=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, number('NaN'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * NaN in data and as search key.
   */
  @org.junit.Test
  public void mapContains015() {
    final XQuery query = new XQuery(
      "map:contains(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",number('NaN'):=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, number('NaN'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Contains in a largish map.
   */
  @org.junit.Test
  public void mapContains016() {
    final XQuery query = new XQuery(
      "map:contains(\n" +
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
      assertBoolean(true)
    );
  }

  /**
   * duration as search key.
   */
  @org.junit.Test
  public void mapContains017() {
    final XQuery query = new XQuery(
      "map:contains(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",xs:duration('P1Y'):=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, xs:yearMonthDuration('P12M'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * QName as search key.
   */
  @org.junit.Test
  public void mapContains018() {
    final XQuery query = new XQuery(
      "map:contains(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",function-name(abs#1):=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, xs:QName('fn:abs'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * contains() returns true even if the value is empty.
   */
  @org.junit.Test
  public void mapContains019() {
    final XQuery query = new XQuery(
      "map:contains(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",4:=(),5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, 4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * default collation is not case-blind.
   */
  @org.junit.Test
  public void mapContains020() {
    final XQuery query = new XQuery(
      "map:contains(map{\"su\":=\"Sunday\",\"mo\":=\"Monday\",\"tu\":=\"Tuesday\",\"we\":=\"Wednesday\",\"th\":=\"Thursday\",\"fr\":=\"Friday\",\"sa\":=\"Saturday\"}, \"TH\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * codepoint collation is not case-blind.
   */
  @org.junit.Test
  public void mapContains021() {
    final XQuery query = new XQuery(
      "map:contains(map:new(map{\"su\":=\"Sunday\",\"mo\":=\"Monday\",\"tu\":=\"Tuesday\",\"we\":=\"Wednesday\",\"th\":=\"Thursday\",\"fr\":=\"Friday\",\"sa\":=\"Saturday\"}, \n" +
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
      assertBoolean(false)
    );
  }

  /**
   * Empty sequence as search key.
   */
  @org.junit.Test
  public void mapContains901() {
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
  public void mapContains902() {
    final XQuery query = new XQuery(
      "map:contains(map{1:=\"Sunday\",2:=\"Monday\",3:=\"Tuesday\",number('NaN'):=\"Wednesday\",5:=\"Thursday\",6:=\"Friday\",7:=\"Saturday\"}, (1 to 5)[. mod 2 = 0])",
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
  public void mapContains903() {
    final XQuery query = new XQuery(
      "map:contains((\"a\", \"b\", \"c\"), \"a\")",
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
  public void mapContains904() {
    final XQuery query = new XQuery(
      "map:contains((), \"a\")",
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
  public void mapContains905() {
    final XQuery query = new XQuery(
      "map:contains(abs#1, \"a\")",
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
  public void mapContains906() {
    final XQuery query = new XQuery(
      "map:contains((map{}, map{\"a\":=\"b\"}), \"a\")",
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
