package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the namespace-uri-for-prefix() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnNamespaceUriForPrefix extends QT3TestSet {

  /**
   *  A test whose essence is: `namespace-uri-for-prefix()`. .
   */
  @org.junit.Test
  public void kNamespaceURIForPrefixFunc1() {
    final XQuery query = new XQuery(
      "namespace-uri-for-prefix()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `namespace-uri-for-prefix("string", (), "wrong param")`. .
   */
  @org.junit.Test
  public void kNamespaceURIForPrefixFunc2() {
    final XQuery query = new XQuery(
      "namespace-uri-for-prefix(\"string\", (), \"wrong param\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  Test that the correct default namespace is picked up. .
   */
  @org.junit.Test
  public void k2NamespaceURIForPrefixFunc1() {
    final XQuery query = new XQuery(
      "declare namespace p = \"http://www.example.com/B\"; namespace-uri-for-prefix(\"\", <e xmlns=\"http://www.example.com/A\" xmlns:A=\"http://www.example.com/C\"> <b xmlns:B=\"http://www.example.com/C\" xmlns=\"http://www.example.com/B\"/> </e>/p:b)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/B")
    );
  }

  /**
   *  Test that the correct default namespace is picked up(#2). .
   */
  @org.junit.Test
  public void k2NamespaceURIForPrefixFunc2() {
    final XQuery query = new XQuery(
      "namespace-uri-for-prefix(\"\", exactly-one(\n" +
      "                      <e xmlns=\"http://www.example.com/A\" xmlns:A=\"http://www.example.com/C\"> <b xmlns:B=\"http://www.example.com/C\" xmlns=\"\"/> </e>/b)\n" +
      "                      )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  Test that the correct default namespace is picked up(#3). .
   */
  @org.junit.Test
  public void k2NamespaceURIForPrefixFunc3() {
    final XQuery query = new XQuery(
      "declare namespace p = \"http://www.example.com/A\"; namespace-uri-for-prefix(\"\", <e xmlns=\"http://www.example.com/A\" xmlns:A=\"http://www.example.com/C\"> <b xmlns:B=\"http://www.example.com/C\" /> </e>/p:b)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/A")
    );
  }

  /**
   *  Test EBV of fn:namespace-uri-for-prefix .
   */
  @org.junit.Test
  public void cbclNamespaceUriForPrefix001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tfn:boolean(fn:namespace-uri-for-prefix('', <a />))\n" +
      "      ",
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
   *  Evaluation of fn:namespace-uri-for-prefix function, for which the element is defined 
   *         and namespace uri is set. Use a direct element. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix1() {
    final XQuery query = new XQuery(
      "let $var := <anElement xmlns:p1 = \"http://www.example.com/examples\">some context</anElement> \n" +
      "      return string(fn:namespace-uri-for-prefix(\"p1\",$var))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:namespace-uri-for-prefix function,with the first argument (prefix) 
   *         uses the fn:lower-case function. Element is directly constructed and given as argument. Use fn:string. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix10() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-for-prefix(fn:lower-case(\"P1\"),<anElement xmlns:p1 = \"http://www.example.com/examples\">some context</anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:namespace-uri-for-prefix function,with the first argument (prefix) 
   *         uses the fn:substring function. Element is directly constructed and given as argument. Use fn:string. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix11() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-for-prefix(fn:substring(\"abcp1\",4),<anElement xmlns:p1 = \"http://www.example.com/examples\">some context</anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:namespace-uri-for-prefix function,with the first argument (prefix) 
   *         uses the fn:concat function. Element is directly constructed and given as argument. Use fn:string. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix12() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-for-prefix(fn:concat(\"p\",\"1\"),<anElement xmlns:p1 = \"http://www.example.com/examples\">some context</anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:namespace-uri-for-prefix function,with the first argument (prefix) uses 
   *         the fn:string-join function. Element is directly constructed and given as argument. Use fn:string. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix13() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-for-prefix(fn:string-join((\"p\",\"1\"),\"\"),<anElement xmlns:p1 = \"http://www.example.com/examples\">some context</anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:namespace-uri-for-prefix function,with the first argument (prefix) 
   *         uses the fn:substring-before function. Element is directly constructed and given as argument. Use fn:string. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix14() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-for-prefix(fn:substring-before(\"p1abc\",\"abc\"),<anElement xmlns:p1 = \"http://www.example.com/examples\">some context</anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:namespace-uri-for-prefix function,with the first argument (prefix) 
   *         uses the fn:substring-after function. Element is directly constructed and given as argument. Use fn:string. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix15() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-for-prefix(fn:substring-after(\"abcp1\",\"abc\"),<anElement xmlns:p1 = \"http://www.example.com/examples\">some context</anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:namespace-uri-for-prefix function for which the element 
   *         is directly constructed and given as argument with no namespace. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix16() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/defaultnamespace\"; \n" +
      "        fn:count(fn:namespace-uri-for-prefix(\"p1\",<anElement>some context</anElement>))",
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
   *  Evaluation of fn:namespace-uri-for-prefix function for which the element is computed and given as argument with no namespace. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix17() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/defaultnamespace\"; \n" +
      "        fn:count(fn:namespace-uri-for-prefix(\"p1\",element anElement {\"some content\"}))",
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
   *  Evaluation of fn:namespace-uri-for-prefix function for which the element is computed as variable (via let) with no namespace. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix18() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/defaultnamespace\"; \n" +
      "        let $var := element anElement {\"some content\"} \n" +
      "        return fn:count(fn:namespace-uri-for-prefix(\"p1\",$var))",
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
   *  Evaluation of fn:namespace-uri-for-prefix function for which the element is computed 
   *         as variable (via for) with no namespace. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix19() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/defaultnamespace\"; \n" +
      "        for $var in (element anElement {\"some content\"}) \n" +
      "        return fn:count(fn:namespace-uri-for-prefix(\"p1\",$var))",
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
   *  Evaluation of fn:namespace-uri-for-prefix function, for which the element is 
   *         defined and namespace uri is not set. Use a direct element. Use fn:count to avoid empty sequence. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix2() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $var := <anElement xmlns:p1 = \"http://www.example.com/examples\">some context</anElement> \n" +
      "        return fn:count(fn:namespace-uri-for-prefix(\"p2\",$var))\n" +
      "      ",
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
   *  Evaluation of fn:namespace-uri-for-prefix function for which the element is computed 
   *         as variable (via declare variable) with no namespace. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix20() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/defaultnamespace\"; \n" +
      "        for $var in element anElement {\"some content\"} \n" +
      "        return fn:count(fn:namespace-uri-for-prefix(\"p1\",$var))",
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
   *  Evaluation of fn:namespace-uri-for-prefix function for which the element is directly constructed 
   *         and have multiple namespaces. Use fn:string. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix21() {
    final XQuery query = new XQuery(
      "\n" +
      "        fn:string(fn:namespace-uri-for-prefix(\"p1\",\n" +
      "                    <anElement xmlns:p1=\"http://www.example.com/examples\" xmlns:p2=\"http://www.someotherns.com/namespace\">some content\"</anElement>\n" +
      "                  ))\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  
   *            Evaluation of fn:namespace-uri-for-prefix function for which the prefix is the empty string and the element has no default namespace. 
   *            The expected behavior for this case was clarified in 3.0.  See bugs 11590, 12554.  
   *       .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix22() {
    final XQuery query = new XQuery(
      "fn:namespace-uri-for-prefix(\"\", <e/>)",
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
   *  
   *            Evaluation of fn:namespace-uri-for-prefix function for which the prefix is the empty sequence and the element has no default namespace. 
   *            The expected behavior for this case was clarified in 3.0.  See bugs 11590, 12554.  
   *       .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix23() {
    final XQuery query = new XQuery(
      "fn:namespace-uri-for-prefix((), <e/>)",
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
   *  Evaluation of fn:namespace-uri-for-prefix function,with the first argument (prefix) 
   *         set to the zero length string and element is directly constructed. Use fn:string. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix3() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/defaultspace\"; \n" +
      "        let $var := <anElement xmlns:p1 = \"http://www.example.com/examples\">some context</anElement> \n" +
      "        return fn:string(fn:namespace-uri-for-prefix(\"\",$var))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/defaultspace")
    );
  }

  /**
   *  Evaluation of fn:namespace-uri-for-prefix function,with the first argument (prefix) 
   *         set to the empty sequence and element is directly constructed. Use fn:string. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix4() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"http://www.example.com/defaultspace\"; \n" +
      "        let $var := <anElement xmlns:p1 = \"http://www.example.com/examples\">some context</anElement> \n" +
      "        return fn:string(fn:namespace-uri-for-prefix((), $var))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/defaultspace")
    );
  }

  /**
   *  Evaluation of fn:namespace-uri-for-prefix function,with the first argument (prefix) 
   *         set to the prefix of a directly constructed element given as argument. Use fn:string. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix5() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-for-prefix(\"p1\",<anElement xmlns:p1 = \"http://www.example.com/examples\">some context</anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:namespace-uri-for-prefix function,with the first argument (prefix) 
   *         set to a non existent prefix of a directly constructed element given as argument. Use fn:count to avaoid empty file. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix6() {
    final XQuery query = new XQuery(
      "fn:count(fn:namespace-uri-for-prefix(\"p2\",<anElement xmlns:p1 = \"http://www.example.com/examples\">some context</anElement>))",
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
   *  Evaluation of fn:namespace-uri-for-prefix function,with the first argument (prefix) 
   *         uses the xs:string function. Element is directly constructed and given as argument. Use fn:string. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix7() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-for-prefix(xs:string(\"p1\"),<anElement xmlns:p1 = \"http://www.example.com/examples\">some context</anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:namespace-uri-for-prefix function,with the first argument (prefix) 
   *         uses the fn:string function. Element is directly constructed and given as argument. Use fn:string. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix8() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-for-prefix(fn:string(\"p1\"),<anElement xmlns:p1 = \"http://www.example.com/examples\">some context</anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:namespace-uri-for-prefix function,with the first argument (prefix) 
   *         uses the fn:upper-case function. Element is directly constructed and given as argument. Use fn:string. .
   */
  @org.junit.Test
  public void fnNamespaceUriForPrefix9() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-for-prefix(fn:upper-case(\"p1\"),<anElement xmlns:P1 = \"http://www.example.com/examples\">some context</anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }
}
