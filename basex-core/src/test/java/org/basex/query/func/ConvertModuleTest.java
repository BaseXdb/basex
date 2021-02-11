package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Conversion Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ConvertModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void binaryToBytes() {
    final Function func = _CONVERT_BINARY_TO_BYTES;
    // successful queries
    query(func.args(" xs:hexBinary('ff')"), -1);
    query(func.args(" xs:hexBinary('007f8081ff')"), "0\n127\n-128\n-127\n-1");
    query(func.args(" xs:base64Binary('QmFzZVggaXMgY29vbA==')"),
        "66\n97\n115\n101\n88\n32\n105\n115\n32\n99\n111\n111\n108");
    query(func.args(" xs:base64Binary(xs:hexBinary('4261736558'))"), "66\n97\n115\n101\n88");
    query(func.args(" xs:base64Binary(<x>AAE=</x>)"), "0\n1");
    query(func.args(_CONVERT_STRING_TO_BASE64.args("a")), 97);
    query(COUNT.args(func.args(_CONVERT_STRING_TO_BASE64.args("\u00e4"))), 2);
    query(COUNT.args(func.args(_CONVERT_STRING_TO_BASE64.args("123"))), 3);
  }

  /** Test method. */
  @Test public void binaryToIntegers() {
    final Function func = _CONVERT_BINARY_TO_INTEGERS;
    // successful queries
    query(func.args(" xs:hexBinary('ff')"), 255);
    query(func.args(" xs:hexBinary('007f8081ff')"), "0\n127\n128\n129\n255");
  }

  /** Test method. */
  @Test public void binaryToString() {
    final Function func = _CONVERT_BINARY_TO_STRING;
    // successful queries
    query(func.args(" xs:base64Binary(xs:hexBinary('41'))"), "A");
    query(func.args(" xs:hexBinary('41')"), "A");
    query(func.args(" xs:hexBinary('41')", "CP1252"), "A");
    query(func.args(" xs:hexBinary('12')", "CP1252", true), "\uFFFD");
    error(func.args(" xs:hexBinary('41')", "X"), CONVERT_ENCODING_X);
    error(func.args(" xs:hexBinary('12')", "CP1252", false), CONVERT_STRING_X);
  }

  /** Test method. */
  @Test public void dateTimeToInteger() {
    final Function func = _CONVERT_DATETIME_TO_INTEGER;
    // successful queries
    query(func.args(" xs:dateTime('1970-01-01T00:00:00Z')"), 0);
    error(func.args(" xs:dateTime('600000000-01-01T00:00:00Z')"), INTRANGE_X);
  }

  /** Test method. */
  @Test public void dayTimeDurationToInteger() {
    final Function func = _CONVERT_DAYTIME_TO_INTEGER;
    // successful queries
    query(func.args(" xs:dayTimeDuration('PT0S')"), 0);
    error(func.args(" xs:dayTimeDuration('PT10000000000000000S')"), INTRANGE_X);
  }

  /** Test method. */
  @Test public void decodeKey() {
    final Function func = _CONVERT_DECODE_KEY;
    query(func.args("_"), "");
    query(func.args("__"), "_");
    query(func.args("_0021"), "!");
    query(func.args("_0021", true), "_0021");

    error(func.args(""), CONVERT_KEY_X);
    error(func.args("___"), CONVERT_KEY_X);
    error(func.args("_0"), CONVERT_KEY_X);
    error(func.args("_00"), CONVERT_KEY_X);
    error(func.args("_002"), CONVERT_KEY_X);
    error(func.args("\n"), CONVERT_KEY_X);
    error(func.args("1"), CONVERT_KEY_X);
  }

  /** Test method. */
  @Test public void encodeKey() {
    final Function func = _CONVERT_ENCODE_KEY;
    query(func.args(""), "_");
    query(func.args("_"), "__");
    query(func.args("!"), "_0021");
    query(func.args("!", true), "_");
  }

  /** Test method. */
  @Test public void integerFromBase() {
    final Function func = _CONVERT_INTEGER_FROM_BASE;
    // successful queries
    query(func.args("100", 2), 4);
    query(func.args("1111111111111111", 2), 65535);
    query(func.args("10000000000000000", 2), 65536);
    query(func.args("4", 16), 4);
    query(func.args("ffff", 16), 65535);
    query(func.args("FFFF", 16), 65535);
    query(func.args("10000", 16), 65536);
    query(func.args("4", 10), 4);
    query(func.args("65535", 10), 65535);
    query(func.args("65536", 10), 65536);
    error(func.args("1", 1), CONVERT_BASE_X);
    error(func.args("1", 100), CONVERT_BASE_X);
    error(func.args("abc", 10), CONVERT_INTEGER_X_X);
    error(func.args("012", 2), CONVERT_INTEGER_X_X);
  }

  /** Test method. */
  @Test public void integersToBase64() {
    final Function func = _CONVERT_INTEGERS_TO_BASE64;
    // successful queries
    query(func.args(" xs:byte(97)"), "a");
    query(func.args(" 97"), "a");
    query(func.args(" ()"), "");
  }

  /** Test method. */
  @Test public void integersToHex() {
    final Function func = _CONVERT_INTEGERS_TO_HEX;
    // successful queries
    query(func.args(" xs:byte(65)"), "A");
    query(func.args(" 65"), "A");
    query("xs:hexBinary('ff') = " + func.args(" 255"), true);
    query(func.args(" for $i in 48 to 50 return xs:byte($i)"), "012");
  }

  /** Test method. */
  @Test public void integerToBase() {
    final Function func = _CONVERT_INTEGER_TO_BASE;
    // successful queries
    query(func.args(4, 2), 100);
    query(func.args(65535, 2), "1111111111111111");
    query(func.args(65536, 2), "10000000000000000");
    query(func.args(4, 16), 4);
    query(func.args(65535, 16), "ffff");
    query(func.args(65536, 16), "10000");
    query(func.args(4, 10), 4);
    query(func.args(65535, 10), 65535);
    query(func.args(65536, 10), 65536);
    error(func.args(1, 1), CONVERT_BASE_X);
    error(func.args(1, 100), CONVERT_BASE_X);
    error(func.args(1, 100), CONVERT_BASE_X);
  }

  /** Test method. */
  @Test public void integerToDateTime() {
    final Function func = _CONVERT_INTEGER_TO_DATETIME;
    // successful queries
    query(func.args(" 0"), "1970-01-01T00:00:00Z");
  }

  /** Test method. */
  @Test public void integerToDateTimeDuration() {
    final Function func = _CONVERT_INTEGER_TO_DAYTIME;
    // successful queries
    query(func.args(" 0"), "PT0S");
  }

  /** Test method. */
  @Test public void stringToBase64() {
    final Function func = _CONVERT_STRING_TO_BASE64;
    // successful queries
    query("string( " + func.args("a") + ')', "YQ==");
    query("string( " + func.args("a", "UTF-8") + ')', "YQ==");
    query("string( " + func.args("a", "US-ASCII") + ')', "YQ==");
    error(func.args("\u00fc", "US-ASCII"), CONVERT_BINARY_X_X);
    error(func.args("a", "X"), CONVERT_ENCODING_X);
  }

  /** Test method. */
  @Test public void stringToHex() {
    final Function func = _CONVERT_STRING_TO_HEX;
    // successful queries
    query("string( " + func.args("a") + ')', 61);
    query("string( " + func.args("a", "UTF-8") + ')', 61);
    query("string( " + func.args("a", "US-ASCII") + ')', 61);
    error(func.args("\u00fc", "US-ASCII"), CONVERT_BINARY_X_X);
    error(func.args("a", "X"), CONVERT_ENCODING_X);
  }
}
