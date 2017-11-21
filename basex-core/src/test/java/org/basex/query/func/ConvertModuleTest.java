package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Conversion Module.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ConvertModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void integerToBase() {
    query(_CONVERT_INTEGER_TO_BASE.args(4, 2), 100);
    query(_CONVERT_INTEGER_TO_BASE.args(65535, 2), "1111111111111111");
    query(_CONVERT_INTEGER_TO_BASE.args(65536, 2), "10000000000000000");
    query(_CONVERT_INTEGER_TO_BASE.args(4, 16), 4);
    query(_CONVERT_INTEGER_TO_BASE.args(65535, 16), "ffff");
    query(_CONVERT_INTEGER_TO_BASE.args(65536, 16), "10000");
    query(_CONVERT_INTEGER_TO_BASE.args(4, 10), 4);
    query(_CONVERT_INTEGER_TO_BASE.args(65535, 10), 65535);
    query(_CONVERT_INTEGER_TO_BASE.args(65536, 10), 65536);
    error(_CONVERT_INTEGER_TO_BASE.args(1, 1), CONVERT_BASE_X);
    error(_CONVERT_INTEGER_TO_BASE.args(1, 100), CONVERT_BASE_X);
    error(_CONVERT_INTEGER_TO_BASE.args(1, 100), CONVERT_BASE_X);
  }

  /** Test method. */
  @Test
  public void integerFromBase() {
    query(_CONVERT_INTEGER_FROM_BASE.args("100", 2), 4);
    query(_CONVERT_INTEGER_FROM_BASE.args("1111111111111111", 2), 65535);
    query(_CONVERT_INTEGER_FROM_BASE.args("10000000000000000", 2), 65536);
    query(_CONVERT_INTEGER_FROM_BASE.args("4", 16), 4);
    query(_CONVERT_INTEGER_FROM_BASE.args("ffff", 16), 65535);
    query(_CONVERT_INTEGER_FROM_BASE.args("FFFF", 16), 65535);
    query(_CONVERT_INTEGER_FROM_BASE.args("10000", 16), 65536);
    query(_CONVERT_INTEGER_FROM_BASE.args("4", 10), 4);
    query(_CONVERT_INTEGER_FROM_BASE.args("65535", 10), 65535);
    query(_CONVERT_INTEGER_FROM_BASE.args("65536", 10), 65536);
    error(_CONVERT_INTEGER_FROM_BASE.args("1", 1), CONVERT_BASE_X);
    error(_CONVERT_INTEGER_FROM_BASE.args("1", 100), CONVERT_BASE_X);
    error(_CONVERT_INTEGER_FROM_BASE.args("abc", 10), CONVERT_INTEGER_X_X);
    error(_CONVERT_INTEGER_FROM_BASE.args("012", 2), CONVERT_INTEGER_X_X);
  }

  /** Test method. */
  @Test
  public void binaryToBytes() {
    query(_CONVERT_BINARY_TO_BYTES.args(" xs:hexBinary('ff')"), -1);
    query(_CONVERT_BINARY_TO_BYTES.args(" xs:hexBinary('007f8081ff')"), "0\n127\n-128\n-127\n-1");
    query(_CONVERT_BINARY_TO_BYTES.args(" xs:base64Binary('QmFzZVggaXMgY29vbA==')"),
        "66\n97\n115\n101\n88\n32\n105\n115\n32\n99\n111\n111\n108");
    query(_CONVERT_BINARY_TO_BYTES.args(" xs:base64Binary(xs:hexBinary('4261736558'))"),
      "66\n97\n115\n101\n88");
    query(_CONVERT_BINARY_TO_BYTES.args(" xs:base64Binary(<x>AAE=</x>)"), "0\n1");
    query(_CONVERT_BINARY_TO_BYTES.args(_CONVERT_STRING_TO_BASE64.args("a")), 97);
    query(COUNT.args(_CONVERT_BINARY_TO_BYTES.args(_CONVERT_STRING_TO_BASE64.args("\u00e4"))), 2);
    query(COUNT.args(_CONVERT_BINARY_TO_BYTES.args(_CONVERT_STRING_TO_BASE64.args("123"))), 3);
  }

  /** Test method. */
  @Test
  public void binaryToIntegers() {
    query(_CONVERT_BINARY_TO_INTEGERS.args(" xs:hexBinary('ff')"), 255);
    query(_CONVERT_BINARY_TO_INTEGERS.args(" xs:hexBinary('007f8081ff')"), "0\n127\n128\n129\n255");
  }

  /** Test method. */
  @Test
  public void binaryToString() {
    query(_CONVERT_BINARY_TO_STRING.args(" xs:base64Binary(xs:hexBinary('41'))"), "A");
    query(_CONVERT_BINARY_TO_STRING.args(" xs:hexBinary('41')"), "A");
    query(_CONVERT_BINARY_TO_STRING.args(" xs:hexBinary('41')", "CP1252"), "A");
    query(_CONVERT_BINARY_TO_STRING.args(" xs:hexBinary('12')", "CP1252", true), "\uFFFD");
    error(_CONVERT_BINARY_TO_STRING.args(" xs:hexBinary('41')", "X"), CONVERT_ENCODING_X);
    error(_CONVERT_BINARY_TO_STRING.args(" xs:hexBinary('12')", "CP1252", false),
        CONVERT_STRING_X);
  }

  /** Test method. */
  @Test
  public void integersToHex() {
    query(_CONVERT_INTEGERS_TO_HEX.args(" xs:byte(65)"), "A");
    query(_CONVERT_INTEGERS_TO_HEX.args(" 65"), "A");
    query("xs:hexBinary('ff') = " + _CONVERT_INTEGERS_TO_HEX.args(" 255"), true);
    query(_CONVERT_INTEGERS_TO_HEX.args(" for $i in 48 to 50 return xs:byte($i)"), "012");
  }

  /** Test method. */
  @Test
  public void integersToBase64() {
    query(_CONVERT_INTEGERS_TO_BASE64.args(" xs:byte(97)"), "a");
    query(_CONVERT_INTEGERS_TO_BASE64.args(" 97"), "a");
    query(_CONVERT_INTEGERS_TO_BASE64.args(" ()"), "");
  }

  /** Test method. */
  @Test
  public void stringToBase64() {
    query("string( " + _CONVERT_STRING_TO_BASE64.args("a") + ')', "YQ==");
    query("string( " + _CONVERT_STRING_TO_BASE64.args("a", "UTF-8") + ')', "YQ==");
    query("string( " + _CONVERT_STRING_TO_BASE64.args("a", "US-ASCII") + ')', "YQ==");
    error(_CONVERT_STRING_TO_BASE64.args("\u00fc", "US-ASCII"), CONVERT_BINARY_X_X);
    error(_CONVERT_STRING_TO_BASE64.args("a", "X"), CONVERT_ENCODING_X);
  }

  /** Test method. */
  @Test
  public void stringToHex() {
    query("string( " + _CONVERT_STRING_TO_HEX.args("a") + ')', 61);
    query("string( " + _CONVERT_STRING_TO_HEX.args("a", "UTF-8") + ')', 61);
    query("string( " + _CONVERT_STRING_TO_HEX.args("a", "US-ASCII") + ')', 61);
    error(_CONVERT_STRING_TO_HEX.args("\u00fc", "US-ASCII"), CONVERT_BINARY_X_X);
    error(_CONVERT_STRING_TO_HEX.args("a", "X"), CONVERT_ENCODING_X);
  }

  /** Test method. */
  @Test
  public void integerToDateTime() {
    query(_CONVERT_INTEGER_TO_DATETIME.args(" 0"), "1970-01-01T00:00:00Z");
  }

  /** Test method. */
  @Test
  public void dateTimeToInteger() {
    query(_CONVERT_DATETIME_TO_INTEGER.args(" xs:dateTime('1970-01-01T00:00:00Z')"), 0);
    error(_CONVERT_DATETIME_TO_INTEGER.args(" xs:dateTime('600000000-01-01T00:00:00Z')"),
        INTRANGE_X);
  }

  /** Test method. */
  @Test
  public void integerToDatyTimeDuration() {
    query(_CONVERT_INTEGER_TO_DAYTIME.args(" 0"), "PT0S");
  }

  /** Test method. */
  @Test
  public void dayTimeDurationToInteger() {
    query(_CONVERT_DAYTIME_TO_INTEGER.args(" xs:dayTimeDuration('PT0S')"), 0);
    error(_CONVERT_DAYTIME_TO_INTEGER.args(" xs:dayTimeDuration('PT10000000000000000S')"),
        INTRANGE_X);
  }
}
