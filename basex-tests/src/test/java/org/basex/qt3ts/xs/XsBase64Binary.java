package org.basex.qt3ts.xs;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for conversion to/from base64Binary.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class XsBase64Binary extends QT3TestSet {

  /**
   *  Convert empty string to b64 .
   */
  @org.junit.Test
  public void base64001() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"\"))",
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
   *  Convert single byte to b64 .
   */
  @org.junit.Test
  public void base64002() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "AA==")
    );
  }

  /**
   *  Convert single byte to b64 .
   */
  @org.junit.Test
  public void base64003() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"01\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "AQ==")
    );
  }

  /**
   *  Convert single byte to b64 .
   */
  @org.junit.Test
  public void base64004() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"ff\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "/w==")
    );
  }

  /**
   *  Convert two bytes to b64 .
   */
  @org.junit.Test
  public void base64005() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"0000\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "AAA=")
    );
  }

  /**
   *  Convert two bytes to b64 .
   */
  @org.junit.Test
  public void base64006() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"00ff\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "AP8=")
    );
  }

  /**
   *  Convert two bytes to b64 .
   */
  @org.junit.Test
  public void base64007() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"80c0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "gMA=")
    );
  }

  /**
   *  Convert three bytes to b64 .
   */
  @org.junit.Test
  public void base64008() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"aabbcc\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "qrvM")
    );
  }

  /**
   *  Convert three bytes to b64 .
   */
  @org.junit.Test
  public void base64009() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"010203\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "AQID")
    );
  }

  /**
   *  Convert four bytes to b64 .
   */
  @org.junit.Test
  public void base64010() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"01020304\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "AQIDBA==")
    );
  }

  /**
   *  Convert five bytes to b64 .
   */
  @org.junit.Test
  public void base64011() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"0102030405\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "AQIDBAU=")
    );
  }

  /**
   *  Convert six bytes to b64 .
   */
  @org.junit.Test
  public void base64012() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"010203040506\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "AQIDBAUG")
    );
  }

  /**
   *  Convert seven bytes to b64 .
   */
  @org.junit.Test
  public void base64013() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"01020304050607\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "AQIDBAUGBw==")
    );
  }

  /**
   *  Convert a longer byte array to b64 .
   */
  @org.junit.Test
  public void base64014() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"0102030405060708090A0B0C0D0E0F10111213131415161718191A1B1C1D1F202122232425262728292A2B2C2D2E2F\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "AQIDBAUGBwgJCgsMDQ4PEBESExMUFRYXGBkaGxwdHyAhIiMkJSYnKCkqKywtLi8=")
    );
  }

  /**
   *  Convert empty string to b64 .
   */
  @org.junit.Test
  public void base64101() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"\")",
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
   *  Convert single byte to b64 .
   */
  @org.junit.Test
  public void base64102() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\"AA==\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "00")
    );
  }

  /**
   *  Convert single byte to b64 .
   */
  @org.junit.Test
  public void base64103() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\"AQ==\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "01")
    );
  }

  /**
   *  Convert single byte to b64 .
   */
  @org.junit.Test
  public void base64104() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\"/w==\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "FF")
    );
  }

  /**
   *  Convert two bytes to b64 .
   */
  @org.junit.Test
  public void base64105() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\"AAA=\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0000")
    );
  }

  /**
   *  Convert two bytes to b64 .
   */
  @org.junit.Test
  public void base64106() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\"AP8=\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "00FF")
    );
  }

  /**
   *  Convert two bytes to b64 .
   */
  @org.junit.Test
  public void base64107() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\"gMA=\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "80C0")
    );
  }

  /**
   *  Convert three bytes to b64 .
   */
  @org.junit.Test
  public void base64108() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\"qrvM\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "AABBCC")
    );
  }

  /**
   *  Convert three bytes to b64 .
   */
  @org.junit.Test
  public void base64109() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\"AQID\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "010203")
    );
  }

  /**
   *  Convert four bytes to b64 .
   */
  @org.junit.Test
  public void base64110() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\"AQIDBA==\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "01020304")
    );
  }

  /**
   *  Convert five bytes to b64 .
   */
  @org.junit.Test
  public void base64111() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\"AQIDBAU=\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0102030405")
    );
  }

  /**
   *  Convert six bytes to b64 .
   */
  @org.junit.Test
  public void base64112() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\"AQIDBAUG\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "010203040506")
    );
  }

  /**
   *  Convert seven bytes to b64 .
   */
  @org.junit.Test
  public void base64113() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\"AQIDBAUGBw==\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "01020304050607")
    );
  }

  /**
   *  Convert a longer string of bytes to b64 .
   */
  @org.junit.Test
  public void base64114() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\"AQIDBAUGBwgJCgsMDQ4PEBESExMUFRYXGBkaGxwdHyAhIiMkJSYnKCkqKywtLi8=\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0102030405060708090A0B0C0D0E0F10111213131415161718191A1B1C1D1F202122232425262728292A2B2C2D2E2F")
    );
  }

  /**
   *  Convert a longer string of bytes to b64; include whitespace .
   */
  @org.junit.Test
  public void base64115() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\" AQIDBAUG BwgJCgsM DQ4PEBES ExMUFRYX \n" +
      " GBkaGxwdH yAhIiMkJ SYnKCkqK y w t L i 8 = \"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0102030405060708090A0B0C0D0E0F10111213131415161718191A1B1C1D1F202122232425262728292A2B2C2D2E2F")
    );
  }

  /**
   *  Invalid input: not a multiple of 4 characters .
   */
  @org.junit.Test
  public void base64901() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"AQI\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  Invalid input: '=' not at end .
   */
  @org.junit.Test
  public void base64902() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"AQ=I\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  Invalid input: '=' not at end .
   */
  @org.junit.Test
  public void base64903() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"=AQI\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  Invalid input: too many '=' signs .
   */
  @org.junit.Test
  public void base64904() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"qrvM====\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  Invalid input: disallowed character .
   */
  @org.junit.Test
  public void base64905() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"gMA-\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  Invalid input: wrong character before final = .
   */
  @org.junit.Test
  public void base64906() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"AP9=\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  Invalid input: wrong character before final = .
   */
  @org.junit.Test
  public void base64907() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"Ay==\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   * Tests for the xs:base64Binary constructor.
   */
  @org.junit.Test
  public void cbclBase64binary001() {
    final XQuery query = new XQuery(
      "count(xs:base64Binary(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }
}
