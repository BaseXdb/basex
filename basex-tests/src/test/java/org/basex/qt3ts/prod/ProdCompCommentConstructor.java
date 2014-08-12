package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the CompCommentConstructor (computed comment constructor) production .
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdCompCommentConstructor extends QT3TestSet {

  /**
   *  dash as comment .
   */
  @org.junit.Test
  public void constrCompcommentDash1() {
    final XQuery query = new XQuery(
      "comment {'-'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0072")
    );
  }

  /**
   *  comment ends with a dash .
   */
  @org.junit.Test
  public void constrCompcommentDash2() {
    final XQuery query = new XQuery(
      "comment {'comment-'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0072")
    );
  }

  /**
   *  character ref as dash .
   */
  @org.junit.Test
  public void constrCompcommentDash3() {
    final XQuery query = new XQuery(
      "comment {'comment&#x2D;'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0072")
    );
  }

  /**
   * dash at the end of content -.
   */
  @org.junit.Test
  public void constrCompcommentDash4() {
    final XQuery query = new XQuery(
      "comment {//*:test-case[@name=\"Constr-compcomment-dash-4\"]/*:description}",
      ctx);
    try {
      query.context(node(file("prod/CompCommentConstructor.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0072")
    );
  }

  /**
   *  typed value of computed comment node .
   */
  @org.junit.Test
  public void constrCompcommentData1() {
    final XQuery query = new XQuery(
      "fn:data(comment {'a', element a {}, 'b'})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a  b")
    );
  }

  /**
   *  strip document nodes .
   */
  @org.junit.Test
  public void constrCompcommentDoc1() {
    final XQuery query = new XQuery(
      "comment {., .}",
      ctx);
    try {
      query.context(node(file("prod/CompAttrConstructor/DupNode.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!--texttext texttext-->", false)
    );
  }

  /**
   *  double dash as comment .
   */
  @org.junit.Test
  public void constrCompcommentDoubledash1() {
    final XQuery query = new XQuery(
      "comment {'--'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0072")
    );
  }

  /**
   *  comment contains double dash .
   */
  @org.junit.Test
  public void constrCompcommentDoubledash2() {
    final XQuery query = new XQuery(
      "comment {'com--ment'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0072")
    );
  }

  /**
   *  character ref as double dash .
   */
  @org.junit.Test
  public void constrCompcommentDoubledash3() {
    final XQuery query = new XQuery(
      "comment {'com&#x2D;&#x2D;ment'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0072")
    );
  }

  /**
   *  double dash -- in content .
   */
  @org.junit.Test
  public void constrCompcommentDoubledash4() {
    final XQuery query = new XQuery(
      "comment {//*:test-case[@name=\"Constr-compcomment-doubledash-4\"]/*:description}",
      ctx);
    try {
      query.context(node(file("prod/CompCommentConstructor.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0072")
    );
  }

  /**
   *  adjacent dashes in seperate expressions .
   */
  @org.junit.Test
  public void constrCompcommentDoubledash5() {
    final XQuery query = new XQuery(
      "comment {'com','-','-','ment'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!--com - - ment-->", false)
    );
  }

  /**
   *  enclosed expression in computed comment node - atomic values .
   */
  @org.junit.Test
  public void constrCompcommentEnclexpr1() {
    final XQuery query = new XQuery(
      "comment {1,'string',3.14,xs:float('1.2345e-2'),xs:dateTime('2002-04-02T12:00:00-01:00')}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!--1 string 3.14 0.012345 2002-04-02T12:00:00-01:00-->", false)
    );
  }

  /**
   *  enclosed expression in computed comment node - nodes .
   */
  @org.junit.Test
  public void constrCompcommentEnclexpr2() {
    final XQuery query = new XQuery(
      "comment {<elem>123</elem>, (<elem attr='456'/>)/@attr, (<elem>789</elem>)/text()}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!--123 456 789-->", false)
    );
  }

  /**
   *  enclosed expression in computed comment node - empty string .
   */
  @org.junit.Test
  public void constrCompcommentEnclexpr3() {
    final XQuery query = new XQuery(
      "comment {1,'',2}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!--1  2-->", false)
    );
  }

  /**
   *  enclosed expression in computed comment node - empty node .
   */
  @org.junit.Test
  public void constrCompcommentEnclexpr4() {
    final XQuery query = new XQuery(
      "comment {1,<a/>,2}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!--1  2-->", false)
    );
  }

  /**
   *  enclosed expression in computed comment node - nodes .
   */
  @org.junit.Test
  public void constrCompcommentEnclexpr5() {
    final XQuery query = new XQuery(
      "comment {/root}",
      ctx);
    try {
      query.context(node(file("prod/CompAttrConstructor/DupNode.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!--texttext-->", false)
    );
  }

  /**
   *  nested computed comment node constructor .
   */
  @org.junit.Test
  public void constrCompcommentNested1() {
    final XQuery query = new XQuery(
      "comment {comment {'one', comment {'two'}}, 'three', comment {'four'}}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!--one two three four-->", false)
    );
  }

  /**
   *  nested computed comment nodes in element constructor .
   */
  @org.junit.Test
  public void constrCompcommentNested2() {
    final XQuery query = new XQuery(
      "<elem>{comment {'one'}}<a>{comment {'two'}}</a>{comment {'three'}}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem><!--one--><a><!--two--></a><!--three--></elem>", false)
    );
  }

  /**
   *  nested computed comment nodes in element constructor .
   */
  @org.junit.Test
  public void constrCompcommentNested3() {
    final XQuery query = new XQuery(
      "document {comment {'one'}, <a/>, comment {'two'}, <b/>, comment {'three'}}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!--one--><a/><!--two--><b/><!--three-->", false)
    );
  }

  /**
   *  empty parent .
   */
  @org.junit.Test
  public void constrCompcommentParent1() {
    final XQuery query = new XQuery(
      "count((comment {'comment'})/..)",
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
   *  string value of computed comment node .
   */
  @org.junit.Test
  public void constrCompcommentString1() {
    final XQuery query = new XQuery(
      "fn:string(comment {'a', element a {}, 'b'})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a  b")
    );
  }

  /**
   *  Comment constructors cannot specify a name. .
   */
  @org.junit.Test
  public void k2ComputeConComment1() {
    final XQuery query = new XQuery(
      "comment {\"name\"} {\"content\"}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Comment constructors cannot specify a name. .
   */
  @org.junit.Test
  public void k2ComputeConComment2() {
    final XQuery query = new XQuery(
      "comment name {\"content\"}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  A computed comment constructor having an empty sequence as input. .
   */
  @org.junit.Test
  public void k2ComputeConComment3() {
    final XQuery query = new XQuery(
      "comment {()}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!---->", false)
    );
  }

  /**
   *  The enclosed expression isn't optional. .
   */
  @org.junit.Test
  public void k2ComputeConComment4() {
    final XQuery query = new XQuery(
      "comment{}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Test the atomized type. .
   */
  @org.junit.Test
  public void k2ComputeConComment5() {
    final XQuery query = new XQuery(
      "data(comment {\"content\"}) instance of xs:string",
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
   *  test detection of '--' in computed comment .
   */
  @org.junit.Test
  public void cbclConstrCompcomment001() {
    final XQuery query = new XQuery(
      "<element> { comment { '-', '-' } } </element>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0072")
    );
  }

  /**
   *  test detection of '--' in computed comment .
   */
  @org.junit.Test
  public void cbclConstrCompcomment002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:tag($arg) as element() { element { 'tag' } { $arg } }; \n" +
      "      \t<element> { comment { 'comment', local:tag('--') } } </element>\n" +
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
      error("XQDY0072")
    );
  }
}
