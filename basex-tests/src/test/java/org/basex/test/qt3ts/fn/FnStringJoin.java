package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the string-join() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnStringJoin extends QT3TestSet {

  /**
   *  A test whose essence is: `string-join("a string")`. .
   */
  @org.junit.Test
  public void kStringJoinFunc1() {
    xquery10();
    final XQuery query = new XQuery(
      "string-join(\"a string\")",
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
   *  A test whose essence is: `string-join("a string")`. Allowed in 3.0.
   */
  @org.junit.Test
  public void kStringJoinFunc1a() {
    final XQuery query = new XQuery(
      "string-join(\"a string\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a string")
    );
  }

  /**
   *  A test whose essence is: `string-join("a string", "a string", "wrong param")`. .
   */
  @org.junit.Test
  public void kStringJoinFunc2() {
    final XQuery query = new XQuery(
      "string-join(\"a string\", \"a string\", \"wrong param\")",
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
   *  A test whose essence is: `string-join(('Now', 'is', 'the', 'time', '...'), ' ') eq "Now is the time ..."`. .
   */
  @org.junit.Test
  public void kStringJoinFunc3() {
    final XQuery query = new XQuery(
      "string-join(('Now', 'is', 'the', 'time', '...'), ' ') eq \"Now is the time ...\"",
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
   *  A test whose essence is: `string-join(("abc", "def"), "") eq "abcdef"`. .
   */
  @org.junit.Test
  public void kStringJoinFunc4() {
    final XQuery query = new XQuery(
      "string-join((\"abc\", \"def\"), \"\") eq \"abcdef\"",
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
   *  A test whose essence is: `string-join(('Blow, ', 'blow, ', 'thou ', 'winter ', 'wind!'), '') eq "Blow, blow, thou winter wind!"`. .
   */
  @org.junit.Test
  public void kStringJoinFunc5() {
    final XQuery query = new XQuery(
      "string-join(('Blow, ', 'blow, ', 'thou ', 'winter ', 'wind!'), '') eq \"Blow, blow, thou winter wind!\"",
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
   *  A test whose essence is: `string-join((), 'separator') eq ""`. .
   */
  @org.junit.Test
  public void kStringJoinFunc6() {
    final XQuery query = new XQuery(
      "string-join((), 'separator') eq \"\"",
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
   *  A test whose essence is: `string-join("a string", ())`. .
   */
  @org.junit.Test
  public void kStringJoinFunc7() {
    final XQuery query = new XQuery(
      "string-join(\"a string\", ())",
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
   *  Test boolean fn:string-join on various cases .
   */
  @org.junit.Test
  public void cbclFnStringJoin001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:repeat($count as xs:integer, $arg as xs:string) as xs:string* { if ($count le 0) then \"\" else for $x in 1 to $count return $arg };\n" +
      "        string-join( for $x in 0 to 4 return local:repeat($x, 'a') , ' ') and string-join( for $x in 0 to 4 return local:repeat($x, 'a') , '')",
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
   *  Evaluates The "string-join" function as per example 1 for this frunction in F&O sepecs. .
   */
  @org.junit.Test
  public void fnStringJoin1() {
    final XQuery query = new XQuery(
      "fn:string-join(('Now', 'is', 'the', 'time', '...'), ' ')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Now is the time ...")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = (" ") $arg2 = " AAAAABBBBB". Use of count to avoid empty file .
   */
  @org.junit.Test
  public void fnStringJoin10() {
    final XQuery query = new XQuery(
      "fn:count(fn:string-join((\" \"),\"AAAAABBBBB\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "string-join" function using it as a argument of a fn:not - returns true .
   */
  @org.junit.Test
  public void fnStringJoin11() {
    final XQuery query = new XQuery(
      "fn:not(fn:string-join((),\"A\"))",
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
   *  Evaluates The "string-join" function using it as a argument of a fn:not - returns true .
   */
  @org.junit.Test
  public void fnStringJoin12() {
    final XQuery query = new XQuery(
      "fn:not(fn:string-join((\"A\"),\"B\"))",
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
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = xs:string("A") $arg2 = "A" .
   */
  @org.junit.Test
  public void fnStringJoin13() {
    final XQuery query = new XQuery(
      "fn:string-join((xs:string(\"A\")),\"A\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "A")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = "A" $arg2 = xs:string("A") .
   */
  @org.junit.Test
  public void fnStringJoin14() {
    final XQuery query = new XQuery(
      "fn:string-join((\"A\"),xs:string(\"A\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "A")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = "A" $arg2 = "a" .
   */
  @org.junit.Test
  public void fnStringJoin15() {
    final XQuery query = new XQuery(
      "fn:string-join((\"A\"),\"a\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "A")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = "a" $arg2 = "A" .
   */
  @org.junit.Test
  public void fnStringJoin16() {
    final XQuery query = new XQuery(
      "fn:string-join((\"a\"),\"A\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = "string-join" $arg2 = "string-join" .
   */
  @org.junit.Test
  public void fnStringJoin17() {
    final XQuery query = new XQuery(
      "fn:string-join(\"string-join\",\"string-join\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "string-join")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = "string-joinstring-join" $arg2 = "string-join" .
   */
  @org.junit.Test
  public void fnStringJoin18() {
    final XQuery query = new XQuery(
      "fn:string-join((\"string-joinstring-join\"),\"string-join\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "string-joinstring-join")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = "****" $arg2 = "***" .
   */
  @org.junit.Test
  public void fnStringJoin19() {
    final XQuery query = new XQuery(
      "fn:string-join(\"****\",\"***\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "****")
    );
  }

  /**
   *  Evaluates The "string-join" function as per example 2 for this function in the F&O specs. .
   */
  @org.junit.Test
  public void fnStringJoin2() {
    final XQuery query = new XQuery(
      "fn:string-join(('Blow, ', 'blow, ', 'thou ', 'winter ', 'wind!'), '')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Blow, blow, thou winter wind!")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = "12345" $arg2 = "1234" .
   */
  @org.junit.Test
  public void fnStringJoin20() {
    final XQuery query = new XQuery(
      "fn:string-join(\"12345\",\"1234\")",
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
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = "string-join $arg2 = "nioj-gnirts ("string-join" backwards) .
   */
  @org.junit.Test
  public void fnStringJoin21() {
    final XQuery query = new XQuery(
      "fn:string-join(\"string-join\",\"nioj-gnirts\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "string-join")
    );
  }

  /**
   *  Default second argument is zero-length string in 3.0 .
   */
  @org.junit.Test
  public void fnStringJoin22() {
    final XQuery query = new XQuery(
      "fn:string-join((\"1\", \"2\", \"3\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "123")
    );
  }

  /**
   *  Default second argument is zero-length string in 3.0 .
   */
  @org.junit.Test
  public void fnStringJoin23() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $e := <e><a>1</a><b>2</b><c>3</c></e>\n" +
      "         return fn:string-join($e/*)\n" +
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
      assertStringValue(false, "123")
    );
  }

  /**
   *  Default second argument is zero-length string in 3.0 .
   */
  @org.junit.Test
  public void fnStringJoin24() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $e := <e><a>1</a><b></b><c>3</c></e>\n" +
      "         return fn:string-join($e/*)\n" +
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
      assertStringValue(false, "13")
    );
  }

  /**
   *  Default second argument is zero-length string in 3.0 .
   */
  @org.junit.Test
  public void fnStringJoin25() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $e := <e><a>1</a><b></b><c>3</c></e>\n" +
      "         return fn:string-join($e/d)\n" +
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
      assertStringValue(false, "")
    );
  }

  /**
   *  Default second argument is zero-length string in 3.0 .
   */
  @org.junit.Test
  public void fnStringJoin26() {
    final XQuery query = new XQuery(
      "\n" +
      "         fn:string-join((1 to 9)!string())\n" +
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
      assertStringValue(false, "123456789")
    );
  }

  /**
   *  Evaluates The "string-join" function as per example 3 for this function in the F&O specs. Use of fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnStringJoin3() {
    final XQuery query = new XQuery(
      "fn:count(fn:string-join((), 'separator'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = () $arg2 = "" .
   */
  @org.junit.Test
  public void fnStringJoin4() {
    final XQuery query = new XQuery(
      "fn:count(fn:string-join((),\"\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = "" $arg2 = "" .
   */
  @org.junit.Test
  public void fnStringJoin5() {
    final XQuery query = new XQuery(
      "fn:count(fn:string-join(\"\",\"\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = "" $arg2 = "A Character String". .
   */
  @org.junit.Test
  public void fnStringJoin6() {
    final XQuery query = new XQuery(
      "fn:count(fn:string-join(\"\",\"A Character String\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = () $arg2 = "A Character String" .
   */
  @org.junit.Test
  public void fnStringJoin7() {
    final XQuery query = new XQuery(
      "fn:count(fn:string-join((),\"A Character String\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = "AAAAABBBBBCCCCC" $arg2 = "BBBBB" .
   */
  @org.junit.Test
  public void fnStringJoin8() {
    final XQuery query = new XQuery(
      "fn:string-join((\"AAAAABBBBBCCCCC\"),\"BBBBB\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "AAAAABBBBBCCCCC")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = ("AAAAABBBBB") $arg2 = " " .
   */
  @org.junit.Test
  public void fnStringJoin9() {
    final XQuery query = new XQuery(
      "fn:string-join((\"AAAAABBBBB\"),\" \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "AAAAABBBBB")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = xs:string(lower bound) $arg2 = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnStringJoin2args1() {
    final XQuery query = new XQuery(
      "fn:string-join(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This is a characte")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = xs:string(mid range) $arg2 = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnStringJoin2args2() {
    final XQuery query = new XQuery(
      "fn:string-join(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This is a characte")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = xs:string(upper bound) $arg2 = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnStringJoin2args3() {
    final XQuery query = new XQuery(
      "fn:string-join(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This is a characte")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = xs:string(lower bound) $arg2 = xs:string(mid range) .
   */
  @org.junit.Test
  public void fnStringJoin2args4() {
    final XQuery query = new XQuery(
      "fn:string-join(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This is a characte")
    );
  }

  /**
   *  Evaluates The "string-join" function with the arguments set as follows: $arg1 = xs:string(lower bound) $arg2 = xs:string(upper bound) .
   */
  @org.junit.Test
  public void fnStringJoin2args5() {
    final XQuery query = new XQuery(
      "fn:string-join(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This is a characte")
    );
  }
}
