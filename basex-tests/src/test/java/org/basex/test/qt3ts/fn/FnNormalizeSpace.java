package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the fn:normalize-space function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnNormalizeSpace extends QT3TestSet {

  /**
   *  A test whose essence is: `normalize-space("a string", "wrong param")`. .
   */
  @org.junit.Test
  public void kNormalizeSpaceFunc1() {
    final XQuery query = new XQuery(
      "normalize-space(\"a string\", \"wrong param\")",
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
   *  A test whose essence is: `if(false()) then normalize-space() else true()`. .
   */
  @org.junit.Test
  public void kNormalizeSpaceFunc2() {
    final XQuery query = new XQuery(
      "if(false()) then normalize-space() else true()",
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
        error("XPDY0002")
      )
    );
  }

  /**
   *  A test whose essence is: `normalize-space("foo") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeSpaceFunc3() {
    final XQuery query = new XQuery(
      "normalize-space(\"foo\") eq \"foo\"",
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
   *  A test whose essence is: `normalize-space(" foo") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeSpaceFunc4() {
    final XQuery query = new XQuery(
      "normalize-space(\" foo\") eq \"foo\"",
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
   *  A test whose essence is: `normalize-space("foo ") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeSpaceFunc5() {
    final XQuery query = new XQuery(
      "normalize-space(\"foo \") eq \"foo\"",
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
   *  A test whose essence is: `normalize-space(()) eq ""`. .
   */
  @org.junit.Test
  public void kNormalizeSpaceFunc6() {
    final XQuery query = new XQuery(
      "normalize-space(()) eq \"\"",
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
   *  A test whose essence is: `normalize-space("f o o ") eq "f o o"`. .
   */
  @org.junit.Test
  public void kNormalizeSpaceFunc7() {
    final XQuery query = new XQuery(
      "normalize-space(\"f o o \") eq \"f o o\"",
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
   *  A test whose essence is: `normalize-space(" 143 1239 fhjkls ") eq "143 1239 fhjkls"`. .
   */
  @org.junit.Test
  public void kNormalizeSpaceFunc8() {
    final XQuery query = new XQuery(
      "normalize-space(\" 143 1239 fhjkls \") eq \"143 1239 fhjkls\"",
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
   *  Invoke normalize-space on itself. Implementations supporting the static typing feature may raise XPTY0004. .
   */
  @org.junit.Test
  public void kNormalizeSpaceFunc9() {
    final XQuery query = new XQuery(
      "normalize-space(normalize-space((\"foo\", current-time())[1])) eq \"foo\"",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluation of fn-normalize-space function as per example 1 for this function from the Functions and Operators specs. .
   */
  @org.junit.Test
  public void fnNormalizeSpace1() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\" The wealthy curled darlings of our nation. \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "The wealthy curled darlings of our nation.")
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument string containing only a single space. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNormalizeSpace10() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\" \")",
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
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument string containing the zero length string. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNormalizeSpace11() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"\")",
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
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument string containing a single tab character. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNormalizeSpace12() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"\t\")",
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
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument string containing only tab characters. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNormalizeSpace13() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"\t\t\")",
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
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument string containing only a single newline character. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNormalizeSpace14() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"\n" +
      "\")",
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
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument string containing only multiple newline characters. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNormalizeSpace15() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"\r\n" +
      "\r\n" +
      "\")",
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
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument string containing only spaces and tab characters. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNormalizeSpace16() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\" \t  \t \")",
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
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument string containing only spaces and newline characters. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNormalizeSpace17() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\" \r  \n" +
      " \")",
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
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument string containing only tab and newline characters. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNormalizeSpace18() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"\t\n" +
      "\")",
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
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument string containing tabs and numerical characters. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNormalizeSpace19() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"\t12345\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12345")
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with no arguments and no context node. .
   */
  @org.junit.Test
  public void fnNormalizeSpace2() {
    final XQuery query = new XQuery(
      "fn:normalize-space()",
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
   *  Evaluation of fn-normalize-space function with argument referencing fn:string function. .
   */
  @org.junit.Test
  public void fnNormalizeSpace20() {
    final XQuery query = new XQuery(
      "fn:normalize-space(fn:string(\" ABC \"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ABC")
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument set to another fn:normalize-space function. .
   */
  @org.junit.Test
  public void fnNormalizeSpace21() {
    final XQuery query = new XQuery(
      "fn:normalize-space(fn:normalize-space(\" ABC\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ABC")
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument containing one tab character. .
   */
  @org.junit.Test
  public void fnNormalizeSpace3() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"This\ttext should contains no tabs\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This text should contains no tabs")
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument containing one newline character. .
   */
  @org.junit.Test
  public void fnNormalizeSpace4() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"This text should contains\n" +
      "no newline characters.\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This text should contains no newline characters.")
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument containing multiple tab character. .
   */
  @org.junit.Test
  public void fnNormalizeSpace5() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"This\ttext\tshould\tcontains\tno\ttab\tcharacters.\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This text should contains no tab characters.")
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument containing multiple newline characters. .
   */
  @org.junit.Test
  public void fnNormalizeSpace6() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"This\n" +
      "text\n" +
      "should\n" +
      "contains\n" +
      "no\n" +
      "newline\n" +
      "characters.\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This text should contains no newline characters.")
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument containing a tab and a newline characters. .
   */
  @org.junit.Test
  public void fnNormalizeSpace7() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"This text\tshould contains no tabs or\n" +
      "newline characters.\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This text should contains no tabs or newline characters.")
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument containing multiple tab and a newline characters. .
   */
  @org.junit.Test
  public void fnNormalizeSpace8() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"This\t text\t should\t contains\n" +
      " no tabs or newline characters.\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This text should contains no tabs or newline characters.")
    );
  }

  /**
   *  Evaluation of fn-normalize-space function with argument string containing only spaces. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNormalizeSpace9() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"    \")",
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
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   * Test normalize-space without argument .
   */
  @org.junit.Test
  public void fnNormalizeSpace0args1() {
    final XQuery query = new XQuery(
      "//doc/normalize-space(zero-or-one(a[normalize-space() = 'Hello, How are you?']))",
      ctx);
    try {
      query.context(node(file("fn/normalize-space/textWithSpaces.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Hello, How are you?")
    );
  }

  /**
   *  Evaluates The "normalize-space" function with the arguments set as follows: $arg = notNormalizedString(lower bound) .
   */
  @org.junit.Test
  public void fnNormalizeSpace1args1() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"This is a charac\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This is a charac")
    );
  }

  /**
   *  Evaluates The "normalize-space" function with the arguments set as follows: $arg = notNormalizedString(mid range) .
   */
  @org.junit.Test
  public void fnNormalizeSpace1args2() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"This is a ch\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This is a ch")
    );
  }

  /**
   *  Evaluates The "normalize-space" function with the arguments set as follows: $arg = notNormalizedString(upper bound) .
   */
  @org.junit.Test
  public void fnNormalizeSpace1args3() {
    final XQuery query = new XQuery(
      "fn:normalize-space(\"This is a charac\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This is a charac")
    );
  }

  /**
   * Test normalize-space with an empty sequence argument .
   */
  @org.junit.Test
  public void fnNormalizeSpace1args4() {
    final XQuery query = new XQuery(
      "normalize-space(())",
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
        assertEq("\"\"")
      &&
        assertType("xs:string")
      &&
        assertCount(1)
      )
    );
  }
}
