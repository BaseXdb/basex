package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the root() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnRoot extends QT3TestSet {

  /**
   *  A test whose essence is: `Root(2)`. .
   */
  @org.junit.Test
  public void kNodeRootFunc1() {
    final XQuery query = new XQuery(
      "Root(2)",
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
   *  A test whose essence is: `(1, 2, 3)[root()]`. .
   */
  @org.junit.Test
  public void kNodeRootFunc2() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[root()]",
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
   *  A test whose essence is: `root(2)`. .
   */
  @org.junit.Test
  public void kNodeRootFunc3() {
    final XQuery query = new XQuery(
      "root(2)",
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
   *  A test whose essence is: `empty(root(()))`. .
   */
  @org.junit.Test
  public void kNodeRootFunc4() {
    final XQuery query = new XQuery(
      "empty(root(()))",
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
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Apply fn:root() to a directly constructed element. .
   */
  @org.junit.Test
  public void k2NodeRootFunc1() {
    final XQuery query = new XQuery(
      "fn:root(<e/>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e/>", false)
    );
  }

  /**
   *  Apply fn:root() to an empty sequence coming from a parent axis. .
   */
  @org.junit.Test
  public void k2NodeRootFunc2() {
    final XQuery query = new XQuery(
      "empty(fn:root(<e/>/..))",
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
   *  Invoke on a comment node. .
   */
  @org.junit.Test
  public void k2NodeRootFunc3() {
    final XQuery query = new XQuery(
      "fn:root(<!-- comment -->)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!-- comment -->", false)
    );
  }

  /**
   *  Invoke on an attribute node. .
   */
  @org.junit.Test
  public void k2NodeRootFunc4() {
    final XQuery query = new XQuery(
      "<e>{fn:root(attribute name {\"value\"})}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e name=\"value\"/>", false)
    );
  }

  /**
   *  Invoke on a processing instruction node. .
   */
  @org.junit.Test
  public void k2NodeRootFunc5() {
    final XQuery query = new XQuery(
      "fn:root(<?target data?>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<?target data?>", false)
    );
  }

  /**
   *  Invoke on a processing instruction node. .
   */
  @org.junit.Test
  public void k2NodeRootFunc6() {
    final XQuery query = new XQuery(
      "fn:root(text{\"text node\"})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "text node")
    );
  }

  /**
   *  Invoke on a processing instruction node. .
   */
  @org.junit.Test
  public void k2NodeRootFunc7() {
    final XQuery query = new XQuery(
      "fn:root(text{\"text node\"})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "text node")
    );
  }

  /**
   *  Invoke on a single document node. .
   */
  @org.junit.Test
  public void k2NodeRootFunc8() {
    final XQuery query = new XQuery(
      "root(document {()}) instance of document-node()",
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
   *  Evaluation of the fn:root function with no arguments and no context node set. .
   */
  @org.junit.Test
  public void fnRoot1() {
    final XQuery query = new XQuery(
      "fn:root()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to an computed element with attribute node by setting via a variable. .
   */
  @org.junit.Test
  public void fnRoot10() {
    final XQuery query = new XQuery(
      "let $var := element anElement {attribute anAttribute {\"Attribute Value\"}} return fn:root($var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<anElement anAttribute=\"Attribute Value\"/>", false)
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to an computed element with attribute node by setting directly on argument. .
   */
  @org.junit.Test
  public void fnRoot11() {
    final XQuery query = new XQuery(
      "fn:root(element anElement {attribute anAttribute {\"Attribute Value\"}})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<anElement anAttribute=\"Attribute Value\"/>", false)
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to a document node by setting via a variable. .
   */
  @org.junit.Test
  public void fnRoot12() {
    final XQuery query = new XQuery(
      "let $var := document {<anElement><anInternalElement>element content</anInternalElement></anElement>} return fn:root($var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<anElement><anInternalElement>element content</anInternalElement></anElement>", false)
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to an computed document node by setting directly on argument. .
   */
  @org.junit.Test
  public void fnRoot13() {
    final XQuery query = new XQuery(
      "fn:root(document {<anElement><anInternalElement>element content</anInternalElement></anElement>})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<anElement><anInternalElement>element content</anInternalElement></anElement>", false)
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to a element node by quering an xml file. .
   */
  @org.junit.Test
  public void fnRoot14() {
    final XQuery query = new XQuery(
      "fn:root(fn:exactly-one(/langs[1]/para[1]))",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<langs>\n <para xml:lang=\"en\"/>\n <div xml:lang=\"en\"><para>And now, and forever!</para></div>\n <para xml:lang=\"EN\"/>\n <para xml:lang=\"en-us\"/>\n</langs>", false)
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to a attribute node by quering an xml file. .
   */
  @org.junit.Test
  public void fnRoot15() {
    final XQuery query = new XQuery(
      "fn:root(/langs[1]/para[1]/@xml:lang)",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<langs>\n <para xml:lang=\"en\"/>\n <div xml:lang=\"en\"><para>And now, and forever!</para></div>\n <para xml:lang=\"EN\"/>\n <para xml:lang=\"en-us\"/>\n</langs>", false)
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to computed text node by setting directly on argument. .
   */
  @org.junit.Test
  public void fnRoot16() {
    final XQuery query = new XQuery(
      "fn:root(text {\"A text Node\"})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "A text Node")
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to computed text node by setting argument vias a variable. .
   */
  @org.junit.Test
  public void fnRoot17() {
    final XQuery query = new XQuery(
      "let $var := text {\"a text Node\"} return fn:root($var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a text Node")
    );
  }

  /**
   *  Evaluation of the fn:root function used in conjunction with "is" operator (returns true). .
   */
  @org.junit.Test
  public void fnRoot18() {
    final XQuery query = new XQuery(
      "let $var := element anElement {\"Element Content\"} return fn:root($var) is fn:root($var)",
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
   *  Evaluation of the fn:root function used in conjunction with "is" operator (returns false). .
   */
  @org.junit.Test
  public void fnRoot19() {
    final XQuery query = new XQuery(
      "let $var := element anElement {\"Element Content\"} return fn:root($var) is fn:root($var)",
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
   *  Evaluation of the fn:root function with no arguments that uses context node, which is not a node. .
   */
  @org.junit.Test
  public void fnRoot2() {
    final XQuery query = new XQuery(
      "(1 to 100)[fn:root()]",
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
   *  Evaluation of the fn:root function used as argument to namespace-uri function (use an element). 
   *         Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnRoot20() {
    final XQuery query = new XQuery(
      "let $var := element anElement {\"Element Content\"} return fn:count(fn:namespace-uri(fn:root($var)))",
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
   *  Evaluation of the fn:root function used as argument to namespace-uri function (use a comment). 
   *         Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnRoot21() {
    final XQuery query = new XQuery(
      "let $var := <!-- An Element Node --> return fn:count(fn:namespace-uri(fn:root($var)))",
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
   *  Evaluation of the fn:root function used as argument to namespace-uri function (use a Processing Instruction). 
   *         Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnRoot22() {
    final XQuery query = new XQuery(
      "let $var := <?format role=\"output\" ?> return fn:count(fn:namespace-uri(fn:root($var)))",
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
   *  Evaluation of the fn:root function used as argument to namespace-uri function (use a text node). use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnRoot23() {
    final XQuery query = new XQuery(
      "let $var := text {\"A text node\"} return fn:count(fn:namespace-uri(fn:root($var)))",
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
   *  Evaluation of the fn:root function with argument set to "." and no context node set. .
   */
  @org.junit.Test
  public void fnRoot24() {
    final XQuery query = new XQuery(
      "fn:root(.)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to the empty sequence. Use count to avoid empty file. .
   */
  @org.junit.Test
  public void fnRoot3() {
    final XQuery query = new XQuery(
      "fn:count(fn:root(()))",
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
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to a comment node (via a variable). .
   */
  @org.junit.Test
  public void fnRoot4() {
    final XQuery query = new XQuery(
      "let $var := <!-- A Comment Node --> return fn:root($var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!-- A Comment Node -->", false)
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to a comment node set directly on argument. .
   */
  @org.junit.Test
  public void fnRoot5() {
    final XQuery query = new XQuery(
      "fn:root(<!-- A Comment Node -->)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!-- A Comment Node -->", false)
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to an elemnt node set via a variable. .
   */
  @org.junit.Test
  public void fnRoot6() {
    final XQuery query = new XQuery(
      "let $var := <anElement>An Element Content</anElement> return fn:root($var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<anElement>An Element Content</anElement>", false)
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to an direct element node by setting directly on argument. .
   */
  @org.junit.Test
  public void fnRoot7() {
    final XQuery query = new XQuery(
      "fn:root(<anElement>An Element Content</anElement>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<anElement>An Element Content</anElement>", false)
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to an processing instruction node by setting via a variable. .
   */
  @org.junit.Test
  public void fnRoot8() {
    final XQuery query = new XQuery(
      "let $var := <?format role=\"output\" ?> return fn:root($var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<?format role=\"output\" ?>", false)
    );
  }

  /**
   *  Evaluation of the fn:root function with argument set to an direct element node by setting directly on argument. .
   */
  @org.junit.Test
  public void fnRoot9() {
    final XQuery query = new XQuery(
      "fn:root(<?format role=\"output\" ?>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<?format role=\"output\" ?>", false)
    );
  }
}
