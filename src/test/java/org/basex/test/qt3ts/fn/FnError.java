package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the error() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnError extends QT3TestSet {

  /**
   *  A test whose essence is: `if(true()) then true() else error()`. .
   */
  @org.junit.Test
  public void kErrorFunc1() {
    final XQuery query = new XQuery(
      "if(true()) then true() else error()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `exactly-one((true(), error()))`. .
   */
  @org.junit.Test
  public void kErrorFunc10() {
    final XQuery query = new XQuery(
      "exactly-one((true(), error()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOER0000")
    );
  }

  /**
   *  A test whose essence is: `if(true()) then true() else error(QName("", "local"), "description")`. .
   */
  @org.junit.Test
  public void kErrorFunc2() {
    final XQuery query = new XQuery(
      "if(true()) then true() else error(QName(\"\", \"local\"), \"description\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `error(QName("", "local"), "description", "object", "wrong param")`. .
   */
  @org.junit.Test
  public void kErrorFunc3() {
    final XQuery query = new XQuery(
      "error(QName(\"\", \"local\"), \"description\", \"object\", \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `error( () )`. .
   */
  @org.junit.Test
  public void kErrorFunc4() {
    final XQuery query = new XQuery(
      "error( () )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  A test whose essence is: `error(QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0000'))`. .
   */
  @org.junit.Test
  public void kErrorFunc5() {
    final XQuery query = new XQuery(
      "error(QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0000'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOER0000")
    );
  }

  /**
   *  When fn:error() is passed a description, the first argument may be an empty sequence. .
   */
  @org.junit.Test
  public void kErrorFunc6() {
    final XQuery query = new XQuery(
      "error((), \"description\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOER0000")
    );
  }

  /**
   *  A test whose essence is: `if(false()) then error((), "description") else true()`. .
   */
  @org.junit.Test
  public void kErrorFunc7() {
    final XQuery query = new XQuery(
      "if(false()) then error((), \"description\") else true()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `error()`. .
   */
  @org.junit.Test
  public void kErrorFunc8() {
    final XQuery query = new XQuery(
      "error()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOER0000")
    );
  }

  /**
   *  A test whose essence is: `error(QName("", "XPDY6666"), "description", "error object")`. .
   */
  @org.junit.Test
  public void kErrorFunc9() {
    final XQuery query = new XQuery(
      "error(QName(\"\", \"XPDY6666\"), \"description\", \"error object\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("*")
    );
  }

  /**
   *  Combine fn:error() with a predicate. .
   */
  @org.junit.Test
  public void k2ErrorFunc1() {
    final XQuery query = new XQuery(
      "(1, 2, error())[2]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "2")
      ||
        error("FOER0000")
      )
    );
  }

  /**
   *  Using a QName with namespace 'none' as identifier. .
   */
  @org.junit.Test
  public void k2ErrorFunc2() {
    final XQuery query = new XQuery(
      "declare default element namespace \"\"; fn:error(xs:QName(\"onlyAnNCName\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("*")
    );
  }

  /**
   *  Use a QName with no namespace URI. .
   */
  @org.junit.Test
  public void k2ErrorFunc3() {
    final XQuery query = new XQuery(
      "error(QName(\"\", \"FOO\"), \"DESCRIPTION\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("*")
    );
  }

  /**
   *  Evaluation of "fn:error" function with no arguments .
   */
  @org.junit.Test
  public void fnError1() {
    final XQuery query = new XQuery(
      "fn:error()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOER0000")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "SENR0001". .
   */
  @org.junit.Test
  public void fnError10() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:SENR0001'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SENR0001")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "SEPM0004". .
   */
  @org.junit.Test
  public void fnError11() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:SEPM0004'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SEPM0004")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "SEPM0009". .
   */
  @org.junit.Test
  public void fnError12() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:SEPM0009'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SEPM0009")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "SEPM0010". .
   */
  @org.junit.Test
  public void fnError13() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:SEPM0010'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SEPM0010")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "SEPM0016". .
   */
  @org.junit.Test
  public void fnError14() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:SEPM0016'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SEPM0016")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "SERE0003". .
   */
  @org.junit.Test
  public void fnError15() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:SERE0003'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SERE0003")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "SERE0005". .
   */
  @org.junit.Test
  public void fnError16() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:SERE0005'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SERE0005")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "SERE0006". .
   */
  @org.junit.Test
  public void fnError17() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:SERE0006'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SERE0006")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "SERE0008". .
   */
  @org.junit.Test
  public void fnError18() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:SERE0008'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SERE0008")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "SERE0012". .
   */
  @org.junit.Test
  public void fnError19() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:SERE0012'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SERE0012")
    );
  }

  /**
   *  Evaluation of "fn:error" function as per example 2 from the Funcs. and Ops. Specifications. .
   */
  @org.junit.Test
  public void fnError2() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.example.com/HR', 'myerr:toohighsal'), 'Does not apply because salary is too high')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("*")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "SERE0014". .
   */
  @org.junit.Test
  public void fnError20() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:SERE0014'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SERE0014")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "SESU0007". .
   */
  @org.junit.Test
  public void fnError22() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:SESU0007'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SESU0007")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "SESU0011". .
   */
  @org.junit.Test
  public void fnError23() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:SESU0011'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SESU0011")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XPDY0002". .
   */
  @org.junit.Test
  public void fnError25() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XPDY0002'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XPST0010". .
   */
  @org.junit.Test
  public void fnError26() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XPST0010'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0010")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XPST0080". .
   */
  @org.junit.Test
  public void fnError27() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XPST0080'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0080")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XPTY0018". .
   */
  @org.junit.Test
  public void fnError28() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XPTY0018'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0018")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQDY0027". .
   */
  @org.junit.Test
  public void fnError29() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQDY0027'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0027")
    );
  }

  /**
   *  Evaluation of "fn:error" function with wrong argument type. .
   */
  @org.junit.Test
  public void fnError3() {
    final XQuery query = new XQuery(
      "fn:error('Wrong Argument Type')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQDY0061". .
   */
  @org.junit.Test
  public void fnError30() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQDY0061'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0061")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQDY0084". .
   */
  @org.junit.Test
  public void fnError31() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQDY0084'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0084")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0009". .
   */
  @org.junit.Test
  public void fnError32() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0009'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0009")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0012". .
   */
  @org.junit.Test
  public void fnError33() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0012'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0012")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0013". .
   */
  @org.junit.Test
  public void fnError34() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0013'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0013")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0016". .
   */
  @org.junit.Test
  public void fnError35() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0016'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0016")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0035". .
   */
  @org.junit.Test
  public void fnError36() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0035'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0035")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0036". .
   */
  @org.junit.Test
  public void fnError37() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0036'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0036")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0046". .
   */
  @org.junit.Test
  public void fnError38() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0046'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0046")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0047". .
   */
  @org.junit.Test
  public void fnError39() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0047'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0047")
    );
  }

  /**
   *  Evaluation of "fn:error" for error code "FOCH0004". .
   */
  @org.junit.Test
  public void fnError4() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOCH0004'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOCH0004")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0048". .
   */
  @org.junit.Test
  public void fnError40() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0048'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0048")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0054". .
   */
  @org.junit.Test
  public void fnError41() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0054'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0054")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0055". .
   */
  @org.junit.Test
  public void fnError42() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0055'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0055")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0057". .
   */
  @org.junit.Test
  public void fnError43() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0057'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0057")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0058". .
   */
  @org.junit.Test
  public void fnError44() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0058'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0058")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0060". .
   */
  @org.junit.Test
  public void fnError45() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0060'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0060")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0073. .
   */
  @org.junit.Test
  public void fnError46() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0073'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0073")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0075". .
   */
  @org.junit.Test
  public void fnError47() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0075'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0075")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0076". .
   */
  @org.junit.Test
  public void fnError48() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0076'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0076")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0079". .
   */
  @org.junit.Test
  public void fnError49() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0079'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0079")
    );
  }

  /**
   *  Evaluation of "fn:error" with first argument set to empty string for 3rd signature. .
   */
  @org.junit.Test
  public void fnError5() {
    final XQuery query = new XQuery(
      "fn:error((), 'err:FOER0000')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOER0000")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQST0087". .
   */
  @org.junit.Test
  public void fnError50() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQST0087'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0087")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "XQTY0030". .
   */
  @org.junit.Test
  public void fnError51() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:XQTY0030'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQTY0030")
    );
  }

  /**
   *  Evaluation of "fn:error" with first argument set to empty string for 4rd signature. .
   */
  @org.junit.Test
  public void fnError6() {
    final XQuery query = new XQuery(
      "fn:error((), 'err:FOER0000','error raised by this test by setting first argument to empty sequence')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOER0000")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "FODT0001". .
   */
  @org.junit.Test
  public void fnError7() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FODT0001'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODT0001")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "FORG0009". .
   */
  @org.junit.Test
  public void fnError8() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FORG0009'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0009")
    );
  }

  /**
   *  Evaluation of "fn:error" set to raise error "FOTY0012". .
   */
  @org.junit.Test
  public void fnError9() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOTY0012'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOTY0012")
    );
  }
}
