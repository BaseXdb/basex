package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the CompPIConstructor (Computed Processing Instruction Constructor) production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdCompPIConstructor extends QT3TestSet {

  /**
   *  empty computed name .
   */
  @org.junit.Test
  public void constrComppiCompname1() {
    final XQuery query = new XQuery(
      "processing-instruction {()} {'text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  string as name .
   */
  @org.junit.Test
  public void constrComppiCompname10() {
    final XQuery query = new XQuery(
      "processing-instruction {'pi'} {'text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi text?>", false)
    );
  }

  /**
   *  string as name .
   */
  @org.junit.Test
  public void constrComppiCompname11() {
    final XQuery query = new XQuery(
      "processing-instruction {'pi', ()} {'text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi text?>", false)
    );
  }

  /**
   *  string as name .
   */
  @org.junit.Test
  public void constrComppiCompname12() {
    final XQuery query = new XQuery(
      "processing-instruction {(), 'pi'} {'text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi text?>", false)
    );
  }

  /**
   *  string with prefix as name .
   */
  @org.junit.Test
  public void constrComppiCompname13() {
    final XQuery query = new XQuery(
      "<elem xmlns:foo=\"http://www.example.com/foo\">{processing-instruction {'foo:attr'} {}}</elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0041")
    );
  }

  /**
   *  untyped atomic as name .
   */
  @org.junit.Test
  public void constrComppiCompname15() {
    final XQuery query = new XQuery(
      "processing-instruction {xs:untypedAtomic('pi')} {'text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi text?>", false)
    );
  }

  /**
   *  untyped atomic with prefix as name .
   */
  @org.junit.Test
  public void constrComppiCompname16() {
    final XQuery query = new XQuery(
      "<elem xmlns:foo=\"http://www.example.com/foo\">{processing-instruction {xs:untypedAtomic('foo:pi')} {'text'}}</elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0041")
    );
  }

  /**
   *  non-ncname string as name .
   */
  @org.junit.Test
  public void constrComppiCompname18() {
    final XQuery query = new XQuery(
      "processing-instruction {'p i'} {'text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0041")
    );
  }

  /**
   *  non-ncname untyped atomic as name .
   */
  @org.junit.Test
  public void constrComppiCompname19() {
    final XQuery query = new XQuery(
      "processing-instruction {xs:untypedAtomic('p i')} {'text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0041")
    );
  }

  /**
   *  two strings as name .
   */
  @org.junit.Test
  public void constrComppiCompname2() {
    final XQuery query = new XQuery(
      "processing-instruction {'one', 'two'} {'text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  two untypedAtomic values as name .
   */
  @org.junit.Test
  public void constrComppiCompname3() {
    final XQuery query = new XQuery(
      "processing-instruction {xs:untypedAtomic('one'), xs:untypedAtomic('two')} {'text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  content of two nodes as name .
   */
  @org.junit.Test
  public void constrComppiCompname4() {
    final XQuery query = new XQuery(
      "processing-instruction {//a} {'text'}",
      ctx);
    query.context(node(file("prod/CompAttrConstructor/DupNode.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  two numeric values as name .
   */
  @org.junit.Test
  public void constrComppiCompname5() {
    final XQuery query = new XQuery(
      "processing-instruction {1,2} {'text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  numeric value as name .
   */
  @org.junit.Test
  public void constrComppiCompname6() {
    final XQuery query = new XQuery(
      "processing-instruction {123} {'text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  dataTime value as name .
   */
  @org.junit.Test
  public void constrComppiCompname7() {
    final XQuery query = new XQuery(
      "processing-instruction {xs:dateTime(\"1999-05-31T13:20:00\")} {'text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  typed value of PI .
   */
  @org.junit.Test
  public void constrComppiData1() {
    final XQuery query = new XQuery(
      "fn:data(processing-instruction pi {'a', element a {}, 'b'})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "a  b")
    );
  }

  /**
   *  strip document nodes .
   */
  @org.junit.Test
  public void constrComppiDoc1() {
    final XQuery query = new XQuery(
      "processing-instruction pi {., .}",
      ctx);
    query.context(node(file("prod/CompAttrConstructor/DupNode.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi texttext texttext?>", false)
    );
  }

  /**
   *  Empty PI node .
   */
  @org.junit.Test
  public void constrComppiEmpty1() {
    final XQuery query = new XQuery(
      "processing-instruction pi {()}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi ?>", false)
    );
  }

  /**
   *  Empty PI node .
   */
  @org.junit.Test
  public void constrComppiEmpty2() {
    final XQuery query = new XQuery(
      "processing-instruction pi {''}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi ?>", false)
    );
  }

  /**
   *  enclosed expression in computed processing-instruction node - atomic values .
   */
  @org.junit.Test
  public void constrComppiEnclexpr1() {
    final XQuery query = new XQuery(
      "processing-instruction pi {1,'string',3.14,xs:float('1.2345e-2'),xs:dateTime('2002-04-02T12:00:00-01:00')}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi 1 string 3.14 0.012345 2002-04-02T12:00:00-01:00?>", false)
    );
  }

  /**
   *  enclosed expression in computed processing-instruction node - nodes .
   */
  @org.junit.Test
  public void constrComppiEnclexpr2() {
    final XQuery query = new XQuery(
      "processing-instruction pi {<elem>123</elem>, (<elem attr='456'/>)/@attr, (<elem>789</elem>)/text()}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi 123 456 789?>", false)
    );
  }

  /**
   *  enclosed expression in computed processing-instruction node - empty string .
   */
  @org.junit.Test
  public void constrComppiEnclexpr3() {
    final XQuery query = new XQuery(
      "processing-instruction pi {1,'',2}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi 1  2?>", false)
    );
  }

  /**
   *  enclosed expression in computed processing-instruction node - empty node .
   */
  @org.junit.Test
  public void constrComppiEnclexpr4() {
    final XQuery query = new XQuery(
      "processing-instruction pi {1,<a/>,2}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi 1  2?>", false)
    );
  }

  /**
   *  enclosed expression in computed processing-instruction node - nodes .
   */
  @org.junit.Test
  public void constrComppiEnclexpr5() {
    final XQuery query = new XQuery(
      "processing-instruction pi {/root}",
      ctx);
    query.context(node(file("prod/CompAttrConstructor/DupNode.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi texttext?>", false)
    );
  }

  /**
   *  invalid PI content .
   */
  @org.junit.Test
  public void constrComppiInvalid1() {
    final XQuery query = new XQuery(
      "processing-instruction pi {'?>'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0026")
    );
  }

  /**
   *  invalid PI content .
   */
  @org.junit.Test
  public void constrComppiInvalid2() {
    final XQuery query = new XQuery(
      "processing-instruction pi {'?>text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0026")
    );
  }

  /**
   *  invalid PI content .
   */
  @org.junit.Test
  public void constrComppiInvalid3() {
    final XQuery query = new XQuery(
      "processing-instruction pi {'text?>text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0026")
    );
  }

  /**
   *  NCName for computed PI constructor .
   */
  @org.junit.Test
  public void constrComppiName1() {
    final XQuery query = new XQuery(
      "processing-instruction pi {'text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi text?>", false)
    );
  }

  /**
   *  QName for computed PI constructor .
   */
  @org.junit.Test
  public void constrComppiName2() {
    final XQuery query = new XQuery(
      "<elem xmlns:foo=\"http://www.example.com\">{processing-instruction foo:pi {'text'}}</elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  invalid PI target xml .
   */
  @org.junit.Test
  public void constrComppiNamexml1() {
    final XQuery query = new XQuery(
      "processing-instruction xml {'pi'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0064")
    );
  }

  /**
   *  invalid PI target xml .
   */
  @org.junit.Test
  public void constrComppiNamexml2() {
    final XQuery query = new XQuery(
      "processing-instruction XmL {'pi'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0064")
    );
  }

  /**
   *  invalid PI target xml .
   */
  @org.junit.Test
  public void constrComppiNamexml3() {
    final XQuery query = new XQuery(
      "processing-instruction {'xml'} {'pi'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0064")
    );
  }

  /**
   *  invalid PI target xml .
   */
  @org.junit.Test
  public void constrComppiNamexml4() {
    final XQuery query = new XQuery(
      "processing-instruction {'XmL'} {'pi'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0064")
    );
  }

  /**
   *  empty parent .
   */
  @org.junit.Test
  public void constrComppiParent1() {
    final XQuery query = new XQuery(
      "count((processing-instruction pi {()})/..)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  leading whitespace in PI content .
   */
  @org.junit.Test
  public void constrComppiSpace1() {
    final XQuery query = new XQuery(
      "processing-instruction pi {' text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi text?>", false)
    );
  }

  /**
   *  leading whitespace in PI content .
   */
  @org.junit.Test
  public void constrComppiSpace2() {
    final XQuery query = new XQuery(
      "processing-instruction pi {'&#x20;&#x0A;&#x0D;&#x09;text'}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?pi text?>", false)
    );
  }

  /**
   *  leading whitespace in PI content .
   */
  @org.junit.Test
  public void constrComppiSpace3() {
    final XQuery query = new XQuery(
      "string-to-codepoints(processing-instruction pi {' text'})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "116 101 120 116")
    );
  }

  /**
   *  leading whitespace in PI content .
   */
  @org.junit.Test
  public void constrComppiSpace4() {
    final XQuery query = new XQuery(
      "string-to-codepoints(processing-instruction pi {'&#x20;&#x0A;&#x0D;&#x09;text'})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "116 101 120 116")
    );
  }

  /**
   *  string value of PI .
   */
  @org.junit.Test
  public void constrComppiString1() {
    final XQuery query = new XQuery(
      "fn:string(processing-instruction pi {'a', element a {}, 'b'})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "a  b")
    );
  }

  /**
   *  The name can't be specified as a string literal. .
   */
  @org.junit.Test
  public void k2ComputeConPI1() {
    final XQuery query = new XQuery(
      "processing-instruction \"name\" {\"content\"}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Data that only is whitespace. .
   */
  @org.junit.Test
  public void k2ComputeConPI10() {
    final XQuery query = new XQuery(
      "processing-instruction name {\" \"} eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test the atomized type. .
   */
  @org.junit.Test
  public void k2ComputeConPI11() {
    final XQuery query = new XQuery(
      "data(processing-instruction name {\"content\"}) instance of xs:string",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An invalid name for a processing-instruction. .
   */
  @org.junit.Test
  public void k2ComputeConPI2() {
    final XQuery query = new XQuery(
      "processing-instruction {\"xml\"} {\"content\"}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0064")
    );
  }

  /**
   *  An invalid name for a processing-instruction. .
   */
  @org.junit.Test
  public void k2ComputeConPI3() {
    final XQuery query = new XQuery(
      "processing-instruction {\" xmL \"} {\"content\"}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0064")
    );
  }

  /**
   *  Extract the name from a computed PI. .
   */
  @org.junit.Test
  public void k2ComputeConPI4() {
    final XQuery query = new XQuery(
      "local-name(processing-instruction {\" name \"} {\"content\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "name")
    );
  }

  /**
   *  Extract the name from a computed PI. .
   */
  @org.junit.Test
  public void k2ComputeConPI5() {
    final XQuery query = new XQuery(
      "local-name(processing-instruction {\" XmLnaMe \"} {\"content\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "XmLnaMe")
    );
  }

  /**
   *  Invalid target name. .
   */
  @org.junit.Test
  public void k2ComputeConPI6() {
    final XQuery query = new XQuery(
      "processing-instruction {\"1.das \"} {\"content\"}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0041")
    );
  }

  /**
   *  PI data containing '?>'. .
   */
  @org.junit.Test
  public void k2ComputeConPI7() {
    final XQuery query = new XQuery(
      "processing-instruction {\"thename\"} {\"asdas?>\"}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0026")
    );
  }

  /**
   *  Extract the data from a computed PI. .
   */
  @org.junit.Test
  public void k2ComputeConPI8() {
    final XQuery query = new XQuery(
      "string(processing-instruction {\"thename\"} {\"asdas? >\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "asdas? >")
    );
  }

  /**
   *  Extract (possible parser-confusing) PI data. .
   */
  @org.junit.Test
  public void k2ComputeConPI9() {
    final XQuery query = new XQuery(
      "string(processing-instruction {\"thename\"} {\"content {1+ } {\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "content {1+ } {")
    );
  }
}
