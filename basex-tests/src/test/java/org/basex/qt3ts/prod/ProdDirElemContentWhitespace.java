package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the DirElemContent.whitespace production.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdDirElemContentWhitespace extends QT3TestSet {

  /**
   *  preserve space adjacent to cdata section .
   */
  @org.junit.Test
  public void constrWsAdjcdata1() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-adjcdata-1.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>  </elem>", false)
    );
  }

  /**
   *  preserve line feed adjacent to cdata section .
   */
  @org.junit.Test
  public void constrWsAdjcdata2() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-adjcdata-2.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("deep-equal(string-to-codepoints(string($result)), (10, 10))")
    );
  }

  /**
   *  preserve tab adjacent to cdata section .
   */
  @org.junit.Test
  public void constrWsAdjcdata3() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-adjcdata-3.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("deep-equal(string-to-codepoints(string($result)), (9, 9))")
    );
  }

  /**
   *  preserve space adjacent to character reference .
   */
  @org.junit.Test
  public void constrWsAdjchref1() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem> &#x30; </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem> 0 </elem>", false)
    );
  }

  /**
   *  preserve line feed adjacent to character reference .
   */
  @org.junit.Test
  public void constrWsAdjchref2() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-adjchref-2.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("deep-equal(string-to-codepoints(string($result)), (10, 48, 10))")
    );
  }

  /**
   *  preserve tab adjacent to character reference .
   */
  @org.junit.Test
  public void constrWsAdjchref3() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-adjchref-3.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("deep-equal(string-to-codepoints(string($result)), (9, 48, 9))")
    );
  }

  /**
   *  strip whitespace space between open tag and enclosed expression .
   */
  @org.junit.Test
  public void constrWsEnclexpr1() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem> {1}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1</elem>", false)
    );
  }

  /**
   *  strip whitespace space between child elements .
   */
  @org.junit.Test
  public void constrWsEnclexpr10() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem> <a/> <b/> </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem><a/><b/></elem>", false)
    );
  }

  /**
   *  strip whitespace line feed between child elements .
   */
  @org.junit.Test
  public void constrWsEnclexpr11() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem> <a/> <b/> </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem><a/><b/></elem>", false)
    );
  }

  /**
   *  strip whitespace tab between child elements .
   */
  @org.junit.Test
  public void constrWsEnclexpr12() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem> <a/> <b/> </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem><a/><b/></elem>", false)
    );
  }

  /**
   *  preserve whitespace space between open tag and enclosed expression .
   */
  @org.junit.Test
  public void constrWsEnclexpr13() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; <elem>   {1}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>   1</elem>", false)
    );
  }

  /**
   *  preserve whitespace line feed between open tag and enclosed expression .
   */
  @org.junit.Test
  public void constrWsEnclexpr14() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; <elem>\n" +
      "\n" +
      "\n" +
      "{1}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>\n\n\n1</elem>", false)
    );
  }

  /**
   *  preserve whitespace tab between open tag and enclosed expression .
   */
  @org.junit.Test
  public void constrWsEnclexpr15() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-enclexpr-15.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>\t\t\t1</elem>", false)
    );
  }

  /**
   *  preserve whitespace space between enclosed expressions .
   */
  @org.junit.Test
  public void constrWsEnclexpr16() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-enclexpr-16.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1   2</elem>", false)
    );
  }

  /**
   *  preserve whitespace line feed between enclosed expressions .
   */
  @org.junit.Test
  public void constrWsEnclexpr17() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-enclexpr-17.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1\r\n\r\n\r\n2</elem>", false)
    );
  }

  /**
   *  preserve whitespace tab between enclosed expressions .
   */
  @org.junit.Test
  public void constrWsEnclexpr18() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-enclexpr-18.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1\t\t\t2</elem>", false)
    );
  }

  /**
   *  preserve whitespace space between enclosed expression and close tag .
   */
  @org.junit.Test
  public void constrWsEnclexpr19() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-enclexpr-19.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1   </elem>", false)
    );
  }

  /**
   *  strip whitespace line feed between open tag and enclosed expression .
   */
  @org.junit.Test
  public void constrWsEnclexpr2() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem> {1}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1</elem>", false)
    );
  }

  /**
   *  preserve whitespace line feed between enclosed expression and close tag .
   */
  @org.junit.Test
  public void constrWsEnclexpr20() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-enclexpr-20.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1\r\n\r\n\r\n</elem>", false)
    );
  }

  /**
   *  preserve whitespace tab between enclosed expression and close tag .
   */
  @org.junit.Test
  public void constrWsEnclexpr21() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-enclexpr-21.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1\t\t\t</elem>", false)
    );
  }

  /**
   *  preserve whitespace space between child elements .
   */
  @org.junit.Test
  public void constrWsEnclexpr22() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-enclexpr-22.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>   <a/>   <b/>   </elem>", false)
    );
  }

  /**
   *  preserve whitespace line feed between child elements .
   */
  @org.junit.Test
  public void constrWsEnclexpr23() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-enclexpr-23.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>\r\n\r\n\r\n<a/>\r\n\r\n\r\n<b/>\r\n\r\n\r\n</elem>", false)
    );
  }

  /**
   *  preserve whitespace tab between child elements .
   */
  @org.junit.Test
  public void constrWsEnclexpr24() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-enclexpr-24.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>\t\t\t<a/>\t\t\t<b/>\t\t\t</elem>", false)
    );
  }

  /**
   *  strip whitespace tab between open tag and enclosed expression .
   */
  @org.junit.Test
  public void constrWsEnclexpr3() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem> {1}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1</elem>", false)
    );
  }

  /**
   *  strip whitespace space between enclosed expressions .
   */
  @org.junit.Test
  public void constrWsEnclexpr4() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem>{1} {2}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>12</elem>", false)
    );
  }

  /**
   *  strip whitespace line feed between enclosed expressions .
   */
  @org.junit.Test
  public void constrWsEnclexpr5() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem>{1} {2}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>12</elem>", false)
    );
  }

  /**
   *  strip whitespace tab between enclosed expressions .
   */
  @org.junit.Test
  public void constrWsEnclexpr6() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem>{1} {2}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>12</elem>", false)
    );
  }

  /**
   *  strip whitespace space between enclosed expression and close tag .
   */
  @org.junit.Test
  public void constrWsEnclexpr7() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem>{1} </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1</elem>", false)
    );
  }

  /**
   *  strip whitespace line feed between enclosed expression and close tag .
   */
  @org.junit.Test
  public void constrWsEnclexpr8() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem>{1} </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1</elem>", false)
    );
  }

  /**
   *  strip whitespace tab between enclosed expression and close tag .
   */
  @org.junit.Test
  public void constrWsEnclexpr9() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem>{1} </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1</elem>", false)
    );
  }

  /**
   *  preserve cdata space .
   */
  @org.junit.Test
  public void constrWsGencdata1() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem><![CDATA[ ]]></elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem> </elem>", false)
    );
  }

  /**
   *  preserve cdata tab .
   */
  @org.junit.Test
  public void constrWsGencdata3() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-gencdata-3.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("deep-equal(string-to-codepoints(string($result)), (9))")
    );
  }

  /**
   *  preserve character reference x20 .
   */
  @org.junit.Test
  public void constrWsGenchref1() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem>&#x20;</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem> </elem>", false)
    );
  }

  /**
   *  preserve character reference xA .
   */
  @org.junit.Test
  public void constrWsGenchref2() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem>&#xA;</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("deep-equal(string-to-codepoints(string($result)), (10))")
    );
  }

  /**
   *  preserve character reference xD .
   */
  @org.junit.Test
  public void constrWsGenchref3() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem>&#xD;</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>&#xD;</elem>", false)
    );
  }

  /**
   *  preserve character reference x9 .
   */
  @org.junit.Test
  public void constrWsGenchref4() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem>&#x9;</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>\t</elem>", false)
    );
  }

  /**
   *  preserve enclosed exp space .
   */
  @org.junit.Test
  public void constrWsGenenclexpr1() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem>{\" \"}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem> </elem>", false)
    );
  }

  /**
   *  preserve enclosed exp line feed .
   */
  @org.junit.Test
  public void constrWsGenenclexpr2() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-genenclexpr-2.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("deep-equal(string-to-codepoints(string($result)), (10))")
    );
  }

  /**
   *  preserve enclosed exp tab .
   */
  @org.junit.Test
  public void constrWsGenenclexpr3() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-genenclexpr-3.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("deep-equal(string-to-codepoints(string($result)), (9))")
    );
  }

  /**
   *  preserve leading space .
   */
  @org.junit.Test
  public void constrWsNobound1() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem> x</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem> x</elem>", false)
    );
  }

  /**
   *  preserve leading CRLF (as newline).
   */
  @org.junit.Test
  public void constrWsNobound2() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-nobound-2.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("string-to-codepoints(string($result))[1] = 10")
    );
  }

  /**
   *  preserve leading tab .
   */
  @org.junit.Test
  public void constrWsNobound3() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-nobound-3.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("string-to-codepoints(string($result))[1] = 9")
    );
  }

  /**
   *  preserve trailing space .
   */
  @org.junit.Test
  public void constrWsNobound4() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem>x </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>x </elem>", false)
    );
  }

  /**
   *  preserve trailing CRLF (as newline) .
   */
  @org.junit.Test
  public void constrWsNobound5() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-nobound-5.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("deep-equal(string-to-codepoints(string($result)), (120, 10))")
    );
  }

  /**
   *  preserve trailing tab .
   */
  @org.junit.Test
  public void constrWsNobound6() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirElemContent.whitespace/Constr-ws-nobound-6.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("deep-equal(string-to-codepoints(string($result)), (120, 9))")
    );
  }

  /**
   *  strip whitespace space .
   */
  @org.junit.Test
  public void constrWsTag1() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem> </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem/>", false)
    );
  }

  /**
   *  count text nodes when preserving whitespace .
   */
  @org.junit.Test
  public void constrWsTag10() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; fn:count((<elem> <a> </a> <a> </a> <a> <b> </b> </a> </elem>)//text())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("9")
    );
  }

  /**
   *  strip whitespace line feed .
   */
  @org.junit.Test
  public void constrWsTag2() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem> </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem/>", false)
    );
  }

  /**
   *  strip whitespace tab .
   */
  @org.junit.Test
  public void constrWsTag3() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem> </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem/>", false)
    );
  }

  /**
   *  strip mixed whitespace .
   */
  @org.junit.Test
  public void constrWsTag4() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem> <a> </a> <a> </a> <a> <b> </b> </a> </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem><a/><a/><a><b/></a></elem>", false)
    );
  }

  /**
   *  count text nodes when stripping whitespace .
   */
  @org.junit.Test
  public void constrWsTag5() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; fn:count((<elem> <a> </a> <a> </a> <a> <b> </b> </a> </elem>)//text())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  preserve whitespace space .
   */
  @org.junit.Test
  public void constrWsTag6() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; <elem>   </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>   </elem>", false)
    );
  }

  /**
   *  preserve whitespace line feed .
   */
  @org.junit.Test
  public void constrWsTag7() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; <elem>\n" +
      "\n" +
      "\n" +
      "</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>\n\n\n</elem>", false)
    );
  }

  /**
   *  preserve whitespace tab .
   */
  @org.junit.Test
  public void constrWsTag8() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; <elem>\t\t\t</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>\t\t\t</elem>", false)
    );
  }

  /**
   *  preserve mixed whitespace .
   */
  @org.junit.Test
  public void constrWsTag9() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; <elem>   \t\n" +
      "      \t    <a>          \t         \n" +
      "\t\t\n" +
      "\t\n" +
      "        </a>\t\n" +
      "<a>        \t     </a>             <a>\t  <b>\n" +
      "\n" +
      "         \t </b>\n" +
      "\n" +
      "  </a>\t\n" +
      "\n" +
      "\t\t\n" +
      "      </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>   \t\n      \t    <a>          \t         \n\t\t\n\t\n        </a>\t\n<a>        \t     </a>             <a>\t  <b>\n\n         \t </b>\n\n  </a>\t\n\n\t\t\n      </elem>", false)
    );
  }

  /**
   *  ignore xml:space attribute preserve .
   */
  @org.junit.Test
  public void constrWsXmlspace1() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <elem xml:space=\"preserve\"> </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xml:space=\"preserve\"/>", false)
    );
  }

  /**
   *  ignore xml:space attribute strip .
   */
  @org.junit.Test
  public void constrWsXmlspace2() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; <elem xml:space=\"strip\"> </elem>",
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
        assertSerialization("<elem xml:space=\"strip\"> </elem>", false)
      ||
        error("XQDY0092")
      )
    );
  }

  /**
   *  Apply fn:string() on a direct element constructor with preserved whitespace. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace1() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; string(<e> <b/> </e>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "  ")
    );
  }

  /**
   *  Ensure non-boundary characters, a character reference, are handled properly. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace10() {
    final XQuery query = new XQuery(
      "string(<e> &#32; </e>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "   ")
    );
  }

  /**
   *  Ensure non-boundary characters, CDATA, are handled properly. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace11() {
    final XQuery query = new XQuery(
      "string(<e> <![CDATA[ ]]> </e>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "   ")
    );
  }

  /**
   *  Ensure extracting the string value from mixed content involving elements is properly done. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace12() {
    final XQuery query = new XQuery(
      "string(<e>123<b>XX</b>abc</e>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "123XXabc")
    );
  }

  /**
   *  Ensure extracting the string value from mixed content involving comments is properly done. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace13() {
    final XQuery query = new XQuery(
      "string(<e>123<!-- a comment -->ab<!-- another comment -->c</e>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "123abc")
    );
  }

  /**
   *  Ensure extracting the string value from mixed content involving processing instructions is properly done. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace14() {
    final XQuery query = new XQuery(
      "string(<e>123<?target content ?>ab<?target2 content?>c</e>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "123abc")
    );
  }

  /**
   *  Ensure only one text node is constructed for contiguous data. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace15() {
    final XQuery query = new XQuery(
      "count(<elem>str{\"\"}asdas{\"asd\", \"asd\", \"''\", \"\"}{''}asd{''}{''}</elem>/text())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Test empty element content result in an element with no children. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace16() {
    final XQuery query = new XQuery(
      "count(<a></a>/node())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Test empty element content result in an element with no children(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace17() {
    final XQuery query = new XQuery(
      "count(<a/>/node())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Empty CDATA sections nevertheless causes whitespace to be preserved. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace18() {
    final XQuery query = new XQuery(
      "string(<elem> <![CDATA[]]> </elem>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "  ")
    );
  }

  /**
   *  Extract the string value of nested elements. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace19() {
    final XQuery query = new XQuery(
      "string(<e>e<b>ddd</b></e>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "eddd")
    );
  }

  /**
   *  Element constructor with no content. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace2() {
    final XQuery query = new XQuery(
      "<elem>{\"\"}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem/>", false)
    );
  }

  /**
   *  Extract the string value of nested elements with adjacent enclosed expressions. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace20() {
    final XQuery query = new XQuery(
      "<e>{1}{1}{1}<e/></e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e>111<e/></e>", false)
    );
  }

  /**
   *  Extract the string value of nested elements with adjacent enclosed expressions(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace21() {
    final XQuery query = new XQuery(
      "<e><e/>{1}{1}{1}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e><e/>111</e>", false)
    );
  }

  /**
   *  It is valid to put ']]>' in element content. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace22() {
    final XQuery query = new XQuery(
      "string(<e>]]></e>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "]]>")
    );
  }

  /**
   *  Extract the string value of mixed content. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace23() {
    final XQuery query = new XQuery(
      "string(<elem><![CDATA[cdat]]><!-- a comment --><?target content?></elem>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "cdat")
    );
  }

  /**
   *  Extract the string value of mixed content(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace24() {
    final XQuery query = new XQuery(
      "string(<elem> content <![CDATA[ content ]]> content </elem>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, " content  content  content ")
    );
  }

  /**
   *  Extract the string value of mixed content(#3). .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace25() {
    final XQuery query = new XQuery(
      "string(<elem><![CDATA[cdata&<>'\"< ]]>asda <?target content?>asdad</elem>) eq \"cdata&amp;<>'\"\"&lt;&#x20;asda asdad\"",
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
   *  Extract the string value of a couple of nested elements. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace26() {
    final XQuery query = new XQuery(
      "string(<a> {1} <b> {1} </b> </a>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "11")
    );
  }

  /**
   *  Apply fn:string to an element constructor with two text nodes. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace3() {
    final XQuery query = new XQuery(
      "string(<a>aaaa<b/>aaaa</a>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "aaaaaaaa")
    );
  }

  /**
   *  Serialize a direct element constructor with preserved whitespace. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace4() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; <e> <b/>  </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e> <b/>  </e>", false)
    );
  }

  /**
   *  xml:space declarations are ignored. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace5() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; string(<e xml:space=\"preserve\"> </e>) eq \"\"",
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
   *  xml:space declarations are ignored(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace6() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <e xml:space=\"preserve\"> </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xml:space=\"preserve\"/>", false)
    );
  }

  /**
   *  xml:space declarations are ignored(#3). .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace7() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; string(<e xml:space=\"default\"> </e>) eq \" \"",
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
   *  xml:space declarations are ignored(#4). .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace8() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; <e xml:space=\"preserve\"> </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xml:space=\"preserve\"> </e>", false)
    );
  }

  /**
   *  No escaping mechanism applies for quotes/apostrophes inside element content. .
   */
  @org.junit.Test
  public void k2DirectConElemWhitespace9() {
    final XQuery query = new XQuery(
      "string(<e>'a''a'''a\"a\"\"a\"\"\"a\"</e>) eq \"'a''a'''a\"\"a\"\"\"\"a\"\"\"\"\"\"a\"\"\"",
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
}
