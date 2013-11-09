package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the replace() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnReplace extends QT3TestSet {

  /**
   *  The flags argument cannot contain whitespace. .
   */
  @org.junit.Test
  public void kReplaceFunc1() {
    final XQuery query = new XQuery(
      "replace(\"input\", \"pattern\", \"replacement\", \" \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0001")
    );
  }

  /**
   *  The pattern can't be the empty sequence. .
   */
  @org.junit.Test
  public void kReplaceFunc2() {
    final XQuery query = new XQuery(
      "replace(\"input\", (), \"replacement\")",
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
   *  The replacement can't be the empty sequence. .
   */
  @org.junit.Test
  public void kReplaceFunc3() {
    final XQuery query = new XQuery(
      "replace(\"input\", \"pattern\", ())",
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
   *  The flags argument cannot contain 'X'. .
   */
  @org.junit.Test
  public void kReplaceFunc4() {
    final XQuery query = new XQuery(
      "replace(\"input\", \"pattern\", \"replacement\", \"X\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0001")
    );
  }

  /**
   *  Only four arguments are accepted. .
   */
  @org.junit.Test
  public void kReplaceFunc5() {
    final XQuery query = new XQuery(
      "replace(\"input\", \"pattern\", \"replacement\", \"\", ())",
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
   *  A '\' cannot occur at the end of the line. .
   */
  @org.junit.Test
  public void kReplaceFunc6() {
    final XQuery query = new XQuery(
      "replace(\"input\", \"in\", \"thisIsInvalid\\\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0004")
    );
  }

  /**
   *  A '$' cannot occur at the end of the line. .
   */
  @org.junit.Test
  public void kReplaceFunc7() {
    final XQuery query = new XQuery(
      "replace(\"input\", \"(input)\", \"thisIsInvalid$\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0004")
    );
  }

  /**
   *  A '\' cannot be used to escape whitespace. .
   */
  @org.junit.Test
  public void kReplaceFunc8() {
    final XQuery query = new XQuery(
      "replace(\"input\", \"in\", \"thisIsInvalid\\ \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0004")
    );
  }

  /**
   *  A '$' cannot be followed by whitespace. .
   */
  @org.junit.Test
  public void kReplaceFunc9() {
    final XQuery query = new XQuery(
      "replace(\"input\", \"in\", \"thisIsInvalid$ \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0004")
    );
  }

  /**
   *  Unexpectedly ending escape. .
   */
  @org.junit.Test
  public void k2ReplaceFunc1() {
    final XQuery query = new XQuery(
      "replace(\"a a a \", \"(a )\", \"replacment: \\1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0004")
    );
  }

  /**
   *  Unexpectedly ending escape. .
   */
  @org.junit.Test
  public void k2ReplaceFunc2() {
    final XQuery query = new XQuery(
      "replace(\"a a a \", \"(a )\", \"replacment: \\1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0004")
    );
  }

  /**
   *  Use a back reference that isn't preceeded by sufficiently many captures, and therefore match the empty sequence. .
   */
  @org.junit.Test
  public void k2ReplaceFunc3() {
    final XQuery query = new XQuery(
      "replace(\"abcd\", \"(a)\\2(b)\", \"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Use a back reference inside a character class. .
   */
  @org.junit.Test
  public void k2ReplaceFunc4() {
    final XQuery query = new XQuery(
      "replace(\"abcd\", \"(asd)[\\1]\", \"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Use a back reference inside a character class(#2). .
   */
  @org.junit.Test
  public void k2ReplaceFunc5() {
    final XQuery query = new XQuery(
      "replace(\"abcd\", \"(asd)[asd\\1]\", \"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Use a back reference inside a character class(#3). .
   */
  @org.junit.Test
  public void k2ReplaceFunc6() {
    final XQuery query = new XQuery(
      "replace(\"abcd\", \"(asd)[asd\\0]\", \"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Use a back reference inside a character class(#3). .
   */
  @org.junit.Test
  public void k2ReplaceFunc7() {
    final XQuery query = new XQuery(
      "replace(\"abcd\", \"1[asd\\0]\", \"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Use fn:replace inside user function. .
   */
  @org.junit.Test
  public void k2ReplaceFunc8() {
    final XQuery query = new XQuery(
      "declare function local:doReplace($input as xs:string?, $pattern as xs:string, $replacement as xs:string) as xs:string { fn:replace($input, $pattern, $replacement) }; <result> <para>{fn:replace(\"ThiY Ybcd.\", \"Y\", \"Q\")}</para> <para>{local:doReplace(\"ThiY iY a abYY.\", \"Y\", \"Q\")}</para> </result>, fn:replace(\"ThiY abcdY.\", \"Y\", \"Q\"), local:doReplace(\"ThiY iY a abYY.\", \"Y\", \"Q\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><para>ThiQ Qbcd.</para><para>ThiQ iQ a abQQ.</para></result>ThiQ abcdQ. ThiQ iQ a abQQ.", false)
    );
  }

  /**
   *  Tests a replace with prepared value evaluated to a boolean .
   */
  @org.junit.Test
  public void cbclFnReplace001() {
    final XQuery query = new XQuery(
      "\n" +
      "      boolean(replace(if(exists((1 to 10)[. mod 2 = 0])) then \"blah\" else (),\"a\",\"e\",\"m\"))\n" +
      "   ",
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
   *  Tests a prepared expression which matches the empty sequence .
   */
  @org.junit.Test
  public void cbclFnReplace002() {
    final XQuery query = new XQuery(
      "replace(\"a\",\"\",\"b\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0003")
    );
  }

  /**
   *  Tests empty regex on prepared fn:replace .
   */
  @org.junit.Test
  public void cbclFnReplace003() {
    final XQuery query = new XQuery(
      "\n" +
      "        replace(string-join(for $x in (1 to 10)[. mod 2 = 0] return string($x),\",\"),\"\",\"c\")\n" +
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
      error("FORX0003")
    );
  }

  /**
   *  Tests empty regex on prepared fn:replace .
   */
  @org.junit.Test
  public void cbclFnReplace004() {
    final XQuery query = new XQuery(
      "\n" +
      "        replace(string-join(for $x in (1 to 10)[. mod 2 = 0] return string($x),\",\"),\"\",\"c\",\"m\")\n" +
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
      error("FORX0003")
    );
  }

  /**
   *  Tests empty regex on prepared fn:replace .
   */
  @org.junit.Test
  public void cbclFnReplace005() {
    final XQuery query = new XQuery(
      "\n" +
      "        replace(string-join(for $x in (1 to 10)[. mod 2 = 0] return string($x),\",\"),\"\",\"c\",\"m\")\n" +
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
      error("FORX0003")
    );
  }

  /**
   * Evaluation of replace function with replacement = "*" as an example 1 for this function. .
   */
  @org.junit.Test
  public void fnReplace1() {
    final XQuery query = new XQuery(
      "replace(\"abracadabra\", \"bra\", \"*\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a*cada*")
    );
  }

  /**
   * Two alternatives within the pattern both match at the same position in the $input. The first one is chosen. .
   */
  @org.junit.Test
  public void fnReplace10() {
    final XQuery query = new XQuery(
      "fn:replace(\"abcd\", \"(ab)|(a)\", \"[1=$1][2=$2]\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "[1=ab][2=]cd")
    );
  }

  /**
   * Evaluation of fn:replace function with input set to empty sequence. Uses the fn:count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnReplace11() {
    final XQuery query = new XQuery(
      "fn:count(fn:replace((), \"bra\", \"*\"))",
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
   * Evaluate that calling function with flags omitted is same as flags being the zero length string. .
   */
  @org.junit.Test
  public void fnReplace12() {
    final XQuery query = new XQuery(
      "replace(\"abracadabra\", \"bra\", \"*\", \"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a*cada*")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\?" for an input string that contains "?". .
   */
  @org.junit.Test
  public void fnReplace13() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra?abracadabra\", \"\\?\", \"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\*" for an input string that contains "*". .
   */
  @org.junit.Test
  public void fnReplace14() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra*abracadabra\", \"\\*\", \"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\+" for an input string that contains "+". .
   */
  @org.junit.Test
  public void fnReplace15() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra+abracadabra\", \"\\+\", \"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\{" for an input string that contains "}". .
   */
  @org.junit.Test
  public void fnReplace16() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra{abracadabra\", \"\\{\", \"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\}" for an input string that contains "}". .
   */
  @org.junit.Test
  public void fnReplace17() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra}abracadabra\", \"\\}\", \"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\(" for an input string that contains "(". .
   */
  @org.junit.Test
  public void fnReplace18() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra(abracadabra\", \"\\(\", \"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\)" for an input string that contains ")". .
   */
  @org.junit.Test
  public void fnReplace19() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra)abracadabra\", \"\\)\", \"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern = "a.*a" and replacement = "*" as an example 2 for this function. .
   */
  @org.junit.Test
  public void fnReplace2() {
    final XQuery query = new XQuery(
      "replace(\"abracadabra\", \"a.*a\", \"*\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "*")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\[" for an input string that contains "[". .
   */
  @org.junit.Test
  public void fnReplace20() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra[abracadabra\", \"\\[\", \"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\]" for an input string that contains "]". .
   */
  @org.junit.Test
  public void fnReplace21() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra]abracadabra\", \"\\]\", \"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\-" for an input string that contains "-". .
   */
  @org.junit.Test
  public void fnReplace22() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra-abracadabra\", \"\\-\",\"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\." for an input string that contains ".". .
   */
  @org.junit.Test
  public void fnReplace23() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra.abracadabra\", \"\\.\",\"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\|" for an input string that contains "|". .
   */
  @org.junit.Test
  public void fnReplace24() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra|abracadabra\", \"\\|\",\"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\\" for an input string that contains "\". .
   */
  @org.junit.Test
  public void fnReplace25() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra\\abracadabra\", \"\\\\\",\"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\t" for an input string that contains the tab character. .
   */
  @org.junit.Test
  public void fnReplace26() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra\tabracadabra\", \"\\t\",\"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\n" for an input string that contains the newline character. .
   */
  @org.junit.Test
  public void fnReplace27() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra\n" +
      "abracadabra\", \"\\n\",\"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "aa{1}" (exact quantity) for an input string that contains the "aa" string. .
   */
  @org.junit.Test
  public void fnReplace28() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabraabracadabra\", \"aa{1}\",\"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrwithbracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "aa{1,}" (min quantity) for an input string that contains the "aa" string twice. .
   */
  @org.junit.Test
  public void fnReplace29() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabraabracadabraabracadabra\", \"aa{1,}\",\"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrwithbracadabrwithbracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern = "a.*?a" and replacement = "*" as an example 3 for this function. .
   */
  @org.junit.Test
  public void fnReplace3() {
    final XQuery query = new XQuery(
      "replace(\"abracadabra\", \"a.*?a\", \"*\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "*c*bra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "aa{1,2}" (range quantity) for an input string that contains the "aa" string twice. .
   */
  @org.junit.Test
  public void fnReplace30() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabraabracadabraabracadabra\", \"aa{1,2}\",\"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrwithbracadabrwithbracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "\^". .
   */
  @org.junit.Test
  public void fnReplace31() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra^abracadabra\", \"\\^\",\"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabrawithabracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern set to "^a". .
   */
  @org.junit.Test
  public void fnReplace32() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra\", \"^a\",\"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "withbracadabra")
    );
  }

  /**
   * Evaluation of replace function with pattern that does not match the input string. .
   */
  @org.junit.Test
  public void fnReplace33() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra\", \"ww\",\"with\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabra")
    );
  }

  /**
   * Evaluation of replace function with escaped $ sign in replacement string..
   */
  @org.junit.Test
  public void fnReplace37() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra\", \"a\", \"\\$\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "$br$c$d$br$")
    );
  }

  /**
   * Evaluation of replace function with escaped $ sign in replacement string..
   */
  @org.junit.Test
  public void fnReplace38() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra\", \"(a)\", \"\\$$1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "$abr$ac$ad$abr$a")
    );
  }

  /**
   * Evaluation of replace function with escaped \ sign in replacement string..
   */
  @org.junit.Test
  public void fnReplace39() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra\", \"a\", \"\\\\\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "\\br\\c\\d\\br\\")
    );
  }

  /**
   * Evaluation of replace function with pattern = "a" and replacement = "" as an example 4 for this function. .
   */
  @org.junit.Test
  public void fnReplace4() {
    final XQuery query = new XQuery(
      "replace(\"abracadabra\", \"a\", \"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "brcdbr")
    );
  }

  /**
   * Evaluation of replace with double-digit capture.
   */
  @org.junit.Test
  public void fnReplace40() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra\", \"((((( ((((( (((((a))))) ))))) )))))\", \"|$1$15|\", \"x\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "|aa|br|aa|c|aa|d|aa|br|aa|")
    );
  }

  /**
   * Evaluation of replace with double-digit capture.
   */
  @org.junit.Test
  public void fnReplace41() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra\", \"((((( ((((( (((((a))))) ))))) )))))\", \"$1520\", \"x\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a20bra20ca20da20bra20")
    );
  }

  /**
   * Evaluation of replace with double-digit capture beyond max capture value.
   */
  @org.junit.Test
  public void fnReplace42() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra\", \"((((( ((((( (((((a)(b))))) ))))) )))))\", \"($14.$15.$16.$17)\", \"x\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "(ab.a.b.ab7)racad(ab.a.b.ab7)ra")
    );
  }

  /**
   *  "." does NOT match CR in default mode.
   */
  @org.junit.Test
  public void fnReplace43() {
    final XQuery query = new XQuery(
      "fn:replace(concat('Mary', codepoints-to-string(13), 'Jones'), 'Mary.Jones', 'Jacob Jones')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("concat('Mary', codepoints-to-string(13), 'Jones')")
    );
  }

  /**
   *  "." does match CR in dot-all mode.
   */
  @org.junit.Test
  public void fnReplace44() {
    final XQuery query = new XQuery(
      "fn:replace(concat('Mary', codepoints-to-string(13), 'Jones'), 'Mary.Jones', 'Jacob Jones', 's')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'Jacob Jones'")
    );
  }

  /**
   * Evaluation of replace function with pattern = "a(.)" and replacement = "a$1$1" as an example 5 for this function. .
   */
  @org.junit.Test
  public void fnReplace5() {
    final XQuery query = new XQuery(
      "replace(\"abracadabra\", \"a(.)\", \"a$1$1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abbraccaddabbra")
    );
  }

  /**
   * Evaluation of replace function with pattern = ".*?" and replacement = "$1" as an example 6 for this function. Should raise an error .
   */
  @org.junit.Test
  public void fnReplace6() {
    final XQuery query = new XQuery(
      "replace(\"abracadabra\", \".*?\", \"$1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0003")
    );
  }

  /**
   * Evaluation of replace function with pattern = "A+" and replacement = "b" as an example 7 for this function. .
   */
  @org.junit.Test
  public void fnReplace7() {
    final XQuery query = new XQuery(
      "replace(\"AAAA\", \"A+\", \"b\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "b")
    );
  }

  /**
   * Evaluation of replace function with pattern = "A+?" and replacement = "b" as an example 8 for this function. .
   */
  @org.junit.Test
  public void fnReplace8() {
    final XQuery query = new XQuery(
      "replace(\"AAAA\", \"A+?\", \"b\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "bbbb")
    );
  }

  /**
   * Evaluation of replace function with pattern = "^(.*?)d(.*)" and replacement = "$1c$2" as an example 9 for this function. .
   */
  @org.junit.Test
  public void fnReplace9() {
    final XQuery query = new XQuery(
      "replace(\"darted\", \"^(.*?)d(.*)$\", \"$1c$2\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "carted")
    );
  }

  /**
   *  Evaluates The "replace" function with the arguments set as follows: $input = xs:string(lower bound) $pattern = xs:string(lower bound) $replacement = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnReplace3args1() {
    final XQuery query = new XQuery(
      "fn:replace(\"This is a characte\",\"This is a characte\",\"This is a characte\")",
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
   *  Evaluates The "replace" function with the arguments set as follows: $input = xs:string(mid range) $pattern = xs:string(lower bound) $replacement = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnReplace3args2() {
    final XQuery query = new XQuery(
      "fn:replace(\"This is a characte\",\"This is a characte\",\"This is a characte\")",
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
   *  Evaluates The "replace" function with the arguments set as follows: $input = xs:string(upper bound) $pattern = xs:string(lower bound) $replacement = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnReplace3args3() {
    final XQuery query = new XQuery(
      "fn:replace(\"This is a characte\",\"This is a characte\",\"This is a characte\")",
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
   *  Evaluates The "replace" function with the arguments set as follows: $input = xs:string(lower bound) $pattern = xs:string(mid range) $replacement = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnReplace3args4() {
    final XQuery query = new XQuery(
      "fn:replace(\"This is a characte\",\"This is a characte\",\"This is a characte\")",
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
   *  Evaluates The "replace" function with the arguments set as follows: $input = xs:string(lower bound) $pattern = xs:string(upper bound) $replacement = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnReplace3args5() {
    final XQuery query = new XQuery(
      "fn:replace(\"This is a characte\",\"This is a characte\",\"This is a characte\")",
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
   *  Evaluates The "replace" function with the arguments set as follows: $input = xs:string(lower bound) $pattern = xs:string(lower bound) $replacement = xs:string(mid range) .
   */
  @org.junit.Test
  public void fnReplace3args6() {
    final XQuery query = new XQuery(
      "fn:replace(\"This is a characte\",\"This is a characte\",\"This is a characte\")",
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
   *  Evaluates The "replace" function with the arguments set as follows: $input = xs:string(lower bound) $pattern = xs:string(lower bound) $replacement = xs:string(upper bound) .
   */
  @org.junit.Test
  public void fnReplace3args7() {
    final XQuery query = new XQuery(
      "fn:replace(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
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
   * Invalid flag for fn:matches fourth argument. .
   */
  @org.junit.Test
  public void fnReplaceErr1() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra\", \"bra\", \"*\", \"p\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0001")
    );
  }

  /**
   * the value of $replacement contains a "\" character that is not part of a "\\" pair, unless it is immediately followed by a "$" character. .
   */
  @org.junit.Test
  public void fnReplaceErr2() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra\", \"bra\", \"\\\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0004")
    );
  }

  /**
   * The value of $replacement contains a "$" character that is not immediately followed by a digit 0-9 and not immediately preceded by a "\". .
   */
  @org.junit.Test
  public void fnReplaceErr3() {
    final XQuery query = new XQuery(
      "fn:replace(\"abracadabra\", \"bra\", \"$y\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0004")
    );
  }
}
