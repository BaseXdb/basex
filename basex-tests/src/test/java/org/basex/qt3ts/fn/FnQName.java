package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the QName() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnQName extends QT3TestSet {

  /**
   *  Test function fn:QName. Simple use case from functions and operators spec .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc001() {
    final XQuery query = new XQuery(
      "element {fn:QName(\"http://www.example.com/example\", \"person\")}{ \"test\" }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<person xmlns=\"http://www.example.com/example\">test</person>", false)
    );
  }

  /**
   *  Test function fn:QName. Simple use case from functions and operators spec .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc002() {
    final XQuery query = new XQuery(
      "element {fn:QName(\"http://www.example.com/example\", \"ht:person\")}{ \"test\" }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<ht:person xmlns:ht=\"http://www.example.com/example\">test</ht:person>", false)
    );
  }

  /**
   *  Test function fn:QName. Simple use case for 'no namespace' QName .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc003() {
    final XQuery query = new XQuery(
      "element {fn:QName(\"\", \"person\")}{ \"test\" }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<person>test</person>", false)
    );
  }

  /**
   *  Test function fn:QName. Simple use case for 'no namespace' QName .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc004() {
    final XQuery query = new XQuery(
      "element {fn:QName((), \"person\")}{ \"test\" }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<person>test</person>", false)
    );
  }

  /**
   *  Test function fn:QName. Error case - local name contains a prefix, but no namespace URI is specified .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc005() {
    final XQuery query = new XQuery(
      "fn:QName(\"\", \"ht:person\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  Test function fn:QName. Error case - local name contains a prefix, but no namespace URI is specified .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc006() {
    final XQuery query = new XQuery(
      "fn:QName((), \"ht:person\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  Test function fn:QName. Error case - invalid lexical representation for the local-name part .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc007() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"1person\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  Test function fn:QName. Error case - invalid lexical representation for the local-name part .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc008() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"@person\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  Test function fn:QName. Error case - invalid lexical representation for the local-name part .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc009() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"-person\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  Test function fn:QName. Error case - invalid lexical representation for the local-name part .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc010() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"<person>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  Test function fn:QName. Error case - invalid lexical representation for the local-name part .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc011() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \":person\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  Test function fn:QName. Error case - invalid lexical representation for the local-name part .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc012() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"person:\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  Test function fn:QName. Error case - wrong number of input parameters .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc013() {
    final XQuery query = new XQuery(
      "fn:QName(\"person\")",
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
   *  Test function fn:QName. Error case - wrong number of input parameters .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc014() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\")",
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
   *  Test function fn:QName. Error case - invalid input type for parameters (integer) .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc015() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", xs:integer(\"100\"))",
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
   *  Test function fn:QName. Error case - invalid input type for parameters (integer) .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc016() {
    final XQuery query = new XQuery(
      "fn:QName( xs:integer(\"100\"), \"person\" )",
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
   *  Test function fn:QName. Select local-name part from source document .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc017() {
    final XQuery query = new XQuery(
      "element {fn:QName( \"http://www.example.com/example\", string((//FolderName)[2]) )}{ \"test\" }",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<Folder00000000001 xmlns=\"http://www.example.com/example\">test</Folder00000000001>", false)
    );
  }

  /**
   *  Test function fn:QName. Select namespace-URI part from source document .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc018() {
    final XQuery query = new XQuery(
      "element {fn:QName( concat('http://www.example.com/', string((//FolderName)[2])), \"people\" )}{ \"test\" }",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<people xmlns=\"http://www.example.com/Folder00000000001\">test</people>", false)
    );
  }

  /**
   *  Test function fn:QName. Error case - zero length string for local-name .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc019() {
    final XQuery query = new XQuery(
      "element {fn:QName( \"http://www.example.com/example\", \"\" )}{ \"test\" }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  Test function fn:QName. Local-name references an already defined namespace prefix which is assigned to a different URI .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc020() {
    final XQuery query = new XQuery(
      "declare namespace ht=\"http://www.example.com/example\"; element {fn:QName( \"http://www.example.com/another-example\", \"ht:person\" )}{ \"test\" }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<ht:person xmlns:ht=\"http://www.example.com/another-example\">test</ht:person>", false)
    );
  }

  /**
   *  Test function fn:QName. URI exists and is linked to a different namespace prefix .
   */
  @org.junit.Test
  public void expandedQNameConstructFunc021() {
    final XQuery query = new XQuery(
      "declare namespace ht=\"http://www.example.com/example\"; element {fn:QName( \"http://www.example.com/example\", \"ht2:person\" )}{ \"test\" }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<ht2:person xmlns:ht2=\"http://www.example.com/example\">test</ht2:person>", false)
    );
  }

  /**
   *  A test whose essence is: `QName()`. .
   */
  @org.junit.Test
  public void kExpandedQNameConstructFunc1() {
    final XQuery query = new XQuery(
      "QName()",
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
   *  A test whose essence is: `QName((), "local") eq xs:QName("local")`. .
   */
  @org.junit.Test
  public void kExpandedQNameConstructFunc10() {
    final XQuery query = new XQuery(
      "QName((), \"local\") eq xs:QName(\"local\")",
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
   *  A test whose essence is: `QName("http://www.example.com/")`. .
   */
  @org.junit.Test
  public void kExpandedQNameConstructFunc2() {
    final XQuery query = new XQuery(
      "QName(\"http://www.example.com/\")",
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
   *  A test whose essence is: `QName("http://www.example.com/", "ncname", "error")`. .
   */
  @org.junit.Test
  public void kExpandedQNameConstructFunc3() {
    final XQuery query = new XQuery(
      "QName(\"http://www.example.com/\", \"ncname\", \"error\")",
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
   *  A test whose essence is: `QName("http://www.w3.org/2005/xpath-functions", "prefix:local") eq xs:QName("fn:local")`. .
   */
  @org.junit.Test
  public void kExpandedQNameConstructFunc4() {
    final XQuery query = new XQuery(
      "QName(\"http://www.w3.org/2005/xpath-functions\", \"prefix:local\") eq xs:QName(\"fn:local\")",
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
   *  A QName cannot start with a digit. .
   */
  @org.junit.Test
  public void kExpandedQNameConstructFunc5() {
    final XQuery query = new XQuery(
      "QName(\"http://www.example.com/\", \"1asd:error\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  A test whose essence is: `QName("", "error:ncname")`. .
   */
  @org.junit.Test
  public void kExpandedQNameConstructFunc6() {
    final XQuery query = new XQuery(
      "QName(\"\", \"error:ncname\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  A test whose essence is: `QName((), "error:ncname")`. .
   */
  @org.junit.Test
  public void kExpandedQNameConstructFunc7() {
    final XQuery query = new XQuery(
      "QName((), \"error:ncname\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  URI/QName arguments appearing in wrong order, leading to an invalid QName. .
   */
  @org.junit.Test
  public void kExpandedQNameConstructFunc8() {
    final XQuery query = new XQuery(
      "QName(\"my:qName\", \"http://example.com/MyErrorNS\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  A test whose essence is: `QName("", "local") eq xs:QName("local")`. .
   */
  @org.junit.Test
  public void kExpandedQNameConstructFunc9() {
    final XQuery query = new XQuery(
      "QName(\"\", \"local\") eq xs:QName(\"local\")",
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
   *  The last argument must be a string. .
   */
  @org.junit.Test
  public void k2ExpandedQNameConstructFunc1() {
    final XQuery query = new XQuery(
      "fn:QName((), ())",
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
   *  Test fn:QName for FOCA0002 on invalid input. .
   */
  @org.junit.Test
  public void cbclQname001() {
    final XQuery query = new XQuery(
      "fn:QName('', ' ')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  Evaluation of constructor function xs:QName for which the argument is not a literal. .
   */
  @org.junit.Test
  public void qName1() {
    final XQuery query = new XQuery(
      "xs:QName(20)",
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
