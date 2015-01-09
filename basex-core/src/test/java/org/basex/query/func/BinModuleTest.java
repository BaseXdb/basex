package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Binary Module.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class BinModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void hex() {
    hexQuery(_BIN_HEX.args("()"),            "");
    hexQuery(_BIN_HEX.args(""),               "");
    hexQuery(_BIN_HEX.args("1"),              "01");
    hexQuery(_BIN_HEX.args("FF"),             "FF");
    hexQuery(_BIN_HEX.args("111"),            "0111");
    hexQuery(_BIN_HEX.args("FFF"),            "0FFF");
    hexQuery(_BIN_HEX.args("000"),            "0000");
    hexQuery(_BIN_HEX.args("FFFFF"),          "0FFFFF");
    hexQuery(_BIN_HEX.args("FFFFFFFFFFFFF"),  "0FFFFFFFFFFFFF");
    hexQuery(_BIN_HEX.args("10000000000000"), "10000000000000");
    hexQuery(_BIN_HEX.args("10000000000000"), "10000000000000");
    query(_BIN_HEX.args("11223F4E"), "ESI/Tg==");
    query(_BIN_HEX.args("1223F4E"),  "ASI/Tg==");
    // errors
    error(_BIN_HEX.args("X"), BIN_NNC);
  }

  /** Test method. */
  @Test
  public void bin() {
    hexQuery(_BIN_BIN.args("()"),        "");
    hexQuery(_BIN_BIN.args(""),          "");
    hexQuery(_BIN_BIN.args("0"),         "00");
    hexQuery(_BIN_BIN.args("00"),        "00");
    hexQuery(_BIN_BIN.args("000000000"), "0000");
    hexQuery(_BIN_BIN.args("1"),         "01");
    hexQuery(_BIN_BIN.args("10"),        "02");
    hexQuery(_BIN_BIN.args("11111111"),  "FF");
    hexQuery(_BIN_BIN.args("111111111"), "01FF");
    query(_BIN_BIN.args("1101000111010101"), "0dU=");
    query(_BIN_BIN.args("1000111010101"),    "EdU=");
    // errors
    error(_BIN_BIN.args("X"), BIN_NNC);
  }

  /** Test method. */
  @Test
  public void octal() {
    hexQuery(_BIN_OCTAL.args("()"),  "");
    hexQuery(_BIN_OCTAL.args(""),    "");
    hexQuery(_BIN_OCTAL.args("0"),   "00");
    hexQuery(_BIN_OCTAL.args("00"),  "00");
    hexQuery(_BIN_OCTAL.args("000"), "0000");
    hexQuery(_BIN_OCTAL.args("007"), "0007");
    hexQuery(_BIN_OCTAL.args("1"),   "01");
    hexQuery(_BIN_OCTAL.args("10"),  "08");
    hexQuery(_BIN_OCTAL.args("77"),  "3F");
    query(_BIN_OCTAL.args("11223047"), "JSYn");
    // errors
    error(_BIN_OCTAL.args("X"), BIN_NNC);
  }

  /** Test method. */
  @Test
  public void toOctets() {
    query(_BIN_TO_OCTETS.args(base64("")),     "");
    query(_BIN_TO_OCTETS.args(base64("00")),   "0");
    query(_BIN_TO_OCTETS.args(base64("FF")),   "255");
    query(_BIN_TO_OCTETS.args(base64("1122")), "17 34");
  }

  /** Test method. */
  @Test
  public void fromOctets() {
    hexQuery(_BIN_FROM_OCTETS.args(" 0"),        "00");
    hexQuery(_BIN_FROM_OCTETS.args("(1,127)"),   "017F");
    hexQuery(_BIN_FROM_OCTETS.args("(128,255)"), "80FF");
    // errors
    error(_BIN_FROM_OCTETS.args(" -1"),  BIN_OOR_X);
    error(_BIN_FROM_OCTETS.args(" 256"), BIN_OOR_X);
  }

  /** Test method. */
  @Test
  public void length() {
    query(_BIN_LENGTH.args(base64("")),         "0");
    query(_BIN_LENGTH.args(base64("FF")),       "1");
    query(_BIN_LENGTH.args(base64("12345678")), "4");
  }

  /** Test method. */
  @Test
  public void part() {
    hexQuery(_BIN_PART.args("()",         0),    "");
    hexQuery(_BIN_PART.args(base64("FF"), 0),    "FF");
    hexQuery(_BIN_PART.args(base64("FF"), 0, 1), "FF");
    hexQuery(_BIN_PART.args(base64("FF"), 1),    "");
    hexQuery(_BIN_PART.args(base64("FF"), 1, 0), "");
    // errors
    error(_BIN_PART.args(base64("FF"), -1),    BIN_IOOR_X_X);
    error(_BIN_PART.args(base64("FF"), 0, -1), BIN_NS_X);
    error(_BIN_PART.args(base64("FF"), 2),     BIN_IOOR_X_X);
    error(_BIN_PART.args(base64("FF"), 0, 2),  BIN_IOOR_X_X);
  }

  /** Test method. */
  @Test
  public void join() {
    hexQuery(_BIN_JOIN.args("()"),                                          "");
    hexQuery(_BIN_JOIN.args('(' + base64("") + ')'),                        "");
    hexQuery(_BIN_JOIN.args('(' + base64("FF") + ')'),                      "FF");
    hexQuery(_BIN_JOIN.args('(' + base64("FF") + ',' + base64("FF") + ')'), "FFFF");
    hexQuery(_BIN_JOIN.args(" (1 to 3) ! " + base64("11")),                 "111111");
  }

  /** Test method. */
  @Test
  public void insertBefore() {
    hexQuery(_BIN_INSERT_BEFORE.args("()", 0, "()"),                     "");
    hexQuery(_BIN_INSERT_BEFORE.args(base64("12"),   0, "()"),           "12");
    hexQuery(_BIN_INSERT_BEFORE.args(base64("12"),   1, "()"),           "12");
    hexQuery(_BIN_INSERT_BEFORE.args(base64("1234"), 0, base64("56")),   "561234");
    hexQuery(_BIN_INSERT_BEFORE.args(base64("1234"), 1, base64("56")),   "125634");
    hexQuery(_BIN_INSERT_BEFORE.args(base64("1234"), 2, base64("56")),   "123456");
    hexQuery(_BIN_INSERT_BEFORE.args(base64("12"),   0, base64("3456")), "345612");
    hexQuery(_BIN_INSERT_BEFORE.args(base64("12"),   1, base64("3456")), "123456");
    hexQuery(_BIN_INSERT_BEFORE.args(base64("12"),   1, base64("34")),   "1234");
    // errors
    error(_BIN_INSERT_BEFORE.args(base64(""), -1, "()"), BIN_IOOR_X_X);
    error(_BIN_INSERT_BEFORE.args(base64(""),  1, "()"), BIN_IOOR_X_X);
  }

  /** Test method. */
  @Test
  public void padLeft() {
    hexQuery(_BIN_PAD_LEFT.args(base64(""),   1),      "00");
    hexQuery(_BIN_PAD_LEFT.args(base64(""),   1, 255), "FF");
    hexQuery(_BIN_PAD_LEFT.args(base64("01"), 2, 127), "7F7F01");
    // errors
    error(_BIN_PAD_LEFT.args(base64(""), -1),     BIN_NS_X);
    error(_BIN_PAD_LEFT.args(base64(""), 0, 256), BIN_OOR_X);
  }

  /** Test method. */
  @Test
  public void padRight() {
    hexQuery(_BIN_PAD_RIGHT.args(base64(""),   1), "00");
    hexQuery(_BIN_PAD_RIGHT.args(base64(""),   1, 255), "FF");
    hexQuery(_BIN_PAD_RIGHT.args(base64("01"), 2, 127), "017F7F");
    // errors
    error(_BIN_PAD_RIGHT.args(base64(""), -1),      BIN_NS_X);
    error(_BIN_PAD_RIGHT.args(base64(""),  0, 256), BIN_OOR_X);
  }

  /** Test method. */
  @Test
  public void find() {
    query(_BIN_FIND.args(base64("1122"),   0, base64("11")), 0);
    query(_BIN_FIND.args(base64("1122"),   1, base64("11")), "");
    query(_BIN_FIND.args(base64("112233"), 0, base64("22")), 1);
    query(_BIN_FIND.args(base64(""), 0, base64("")), 0);
    // errors
    error(_BIN_FIND.args(base64(""), -1, base64("11")), BIN_IOOR_X_X);
    error(_BIN_FIND.args(base64(""), 1, base64("11")),  BIN_IOOR_X_X);
  }

  /** Test method. */
  @Test
  public void decodeString() {
    query(_BIN_DECODE_STRING.args(base64("31")),                  "1");
    query(_BIN_DECODE_STRING.args(base64("31"), "US-ASCII"),      "1");
    query(_BIN_DECODE_STRING.args(base64("31"), "UTF-8"),         "1");
    query(_BIN_DECODE_STRING.args(base64("3132"), "UTF-8", 1, 1), "2");
    // errors
    error(_BIN_DECODE_STRING.args(base64(""), "UTF-8", -1),    BIN_IOOR_X_X);
    error(_BIN_DECODE_STRING.args(base64(""), "UTF-8", 0, -1), BIN_NS_X);
    error(_BIN_DECODE_STRING.args(base64(""), "UTF-8", 1, 0),  BIN_IOOR_X_X);
    error(_BIN_DECODE_STRING.args(base64(""), "UTF-8", 0, 1),  BIN_IOOR_X_X);
    error(_BIN_DECODE_STRING.args(base64(""), "X"),            BIN_UE_X);
    error(_BIN_DECODE_STRING.args(base64("FF"), "UTF-8"),      BIN_CE_X);
    error(_BIN_DECODE_STRING.args(_BIN_HEX.args("03")),        BIN_CE_X);
  }

  /** Test method. */
  @Test
  public void encodeString() {
    hexQuery(_BIN_ENCODE_STRING.args(""),                "");
    hexQuery(_BIN_ENCODE_STRING.args("", "US-ASCII"),    "");
    hexQuery(_BIN_ENCODE_STRING.args("a", "US-ASCII"),   "61");
    hexQuery(_BIN_ENCODE_STRING.args("\u00c4", "UTF-8"), "C384");
    // errors
    error(_BIN_ENCODE_STRING.args("", "X"),              BIN_UE_X);
    error(_BIN_ENCODE_STRING.args("\u00c4", "US-ASCII"), BIN_CE_X);
  }

  /** Test method. */
  @Test
  public void or() {
    hexQuery(_BIN_OR.args("()", base64("00")),            "");
    hexQuery(_BIN_OR.args(base64("00"), "()"),            "");
    hexQuery(_BIN_OR.args(base64(""), base64("")),        "");
    hexQuery(_BIN_OR.args(base64("8081"), base64("7F7E")), "FFFF");
    // errors
    error(_BIN_OR.args(base64("00"), base64("")), BIN_DLA_X_X);
  }

  /** Test method. */
  @Test
  public void xor() {
    hexQuery(_BIN_XOR.args("()", base64("00")),             "");
    hexQuery(_BIN_XOR.args(base64("00"), "()"),             "");
    hexQuery(_BIN_XOR.args(base64(""), base64("")),         "");
    hexQuery(_BIN_XOR.args(base64("80"), base64("7F")),     "FF");
    hexQuery(_BIN_XOR.args(base64("1234"), base64("4321")), "5115");
    // errors
    error(_BIN_XOR.args(base64("00"), base64("")), BIN_DLA_X_X);
  }

  /** Test method. */
  @Test
  public void and() {
    hexQuery(_BIN_AND.args("()", base64("00")),             "");
    hexQuery(_BIN_AND.args(base64("00"), "()"),             "");
    hexQuery(_BIN_AND.args(base64(""), base64("")),         "");
    hexQuery(_BIN_AND.args(base64("8081"), base64("7F7E")), "0000");
    // errors
    error(_BIN_AND.args(base64("00"), base64("")), BIN_DLA_X_X);
  }

  /** Test method. */
  @Test
  public void not() {
    hexQuery(_BIN_NOT.args("()"),           "");
    hexQuery(_BIN_NOT.args(base64("00")),   "FF");
    hexQuery(_BIN_NOT.args(base64("8081")), "7F7E");
  }

  /** Test method. */
  @Test
  public void shift() {
    hexQuery(_BIN_SHIFT.args("()", 1),                 "");
    hexQuery(_BIN_SHIFT.args(base64("77"), 0),         "77");
    hexQuery(_BIN_SHIFT.args(base64("33"), 1),         "66");
    hexQuery(_BIN_SHIFT.args(base64("66"), -1),        "33");
    hexQuery(_BIN_SHIFT.args(base64("0066"), 8),       "6600");
    hexQuery(_BIN_SHIFT.args(base64("6600"), -8),      "0066");
    hexQuery(_BIN_SHIFT.args(base64("12345678"), 16),  "56780000");
    hexQuery(_BIN_SHIFT.args(base64("12345678"), -16), "00001234");
  }

  /** Test method. */
  @Test
  public void packDouble() {
    hexQuery(_BIN_PACK_DOUBLE.args(0),                            "0000000000000000");
    hexQuery(_BIN_PACK_DOUBLE.args(1),                            "3FF0000000000000");
    hexQuery(_BIN_PACK_DOUBLE.args(-1),                           "BFF0000000000000");
    hexQuery(_BIN_PACK_DOUBLE.args(" -0e0"),                      "8000000000000000");
    hexQuery(_BIN_PACK_DOUBLE.args(" 0e0"),                       "0000000000000000");
    hexQuery(_BIN_PACK_DOUBLE.args("xs:double('INF')"),           "7FF0000000000000");
    hexQuery(_BIN_PACK_DOUBLE.args("xs:double('-INF')"),          "FFF0000000000000");
    hexQuery(_BIN_PACK_DOUBLE.args("xs:double('NaN')"),           "7FF8000000000000");
    hexQuery(_BIN_PACK_DOUBLE.args(1, "most-significant-first"),  "3FF0000000000000");
    hexQuery(_BIN_PACK_DOUBLE.args(1, "big-endian"),              "3FF0000000000000");
    hexQuery(_BIN_PACK_DOUBLE.args(1, "BE"),                      "3FF0000000000000");
    hexQuery(_BIN_PACK_DOUBLE.args(1, "least-significant-first"), "000000000000F03F");
    hexQuery(_BIN_PACK_DOUBLE.args(1, "little-endian"),           "000000000000F03F");
    hexQuery(_BIN_PACK_DOUBLE.args(1, "LE"),                      "000000000000F03F");
    // errors
    error(_BIN_PACK_DOUBLE.args(1, "X"), BIN_USO_X);
  }

  /** Test method. */
  @Test
  public void packFloat() {
    hexQuery(_BIN_PACK_FLOAT.args(0),                            "00000000");
    hexQuery(_BIN_PACK_FLOAT.args(1),                            "3F800000");
    hexQuery(_BIN_PACK_FLOAT.args(-1),                           "BF800000");
    hexQuery(_BIN_PACK_FLOAT.args(" -0e0"),                      "80000000");
    hexQuery(_BIN_PACK_FLOAT.args(" 0e0"),                       "00000000");
    hexQuery(_BIN_PACK_FLOAT.args("xs:float('INF')"),            "7F800000");
    hexQuery(_BIN_PACK_FLOAT.args("xs:float('-INF')"),           "FF800000");
    hexQuery(_BIN_PACK_FLOAT.args("xs:float('NaN')"),            "7FC00000");
    hexQuery(_BIN_PACK_FLOAT.args(1, "most-significant-first"),  "3F800000");
    hexQuery(_BIN_PACK_FLOAT.args(1, "big-endian"),              "3F800000");
    hexQuery(_BIN_PACK_FLOAT.args(1, "BE"),                      "3F800000");
    hexQuery(_BIN_PACK_FLOAT.args(1, "least-significant-first"), "0000803F");
    hexQuery(_BIN_PACK_FLOAT.args(1, "little-endian"),           "0000803F");
    hexQuery(_BIN_PACK_FLOAT.args(1, "LE"),                      "0000803F");
    // errors
    error(_BIN_PACK_FLOAT.args(1, "X"), BIN_USO_X);
  }

  /** Test method. */
  @Test
  public void packInteger() {
    hexQuery(_BIN_PACK_INTEGER.args(1, 0),                            "");
    hexQuery(_BIN_PACK_INTEGER.args(1, 1),                            "01");
    hexQuery(_BIN_PACK_INTEGER.args(1, 2),                            "0001");
    hexQuery(_BIN_PACK_INTEGER.args(" " + Long.MAX_VALUE, 8),         "7FFFFFFFFFFFFFFF");
    hexQuery(_BIN_PACK_INTEGER.args(-1, 8),                           "FFFFFFFFFFFFFFFF");
    hexQuery(_BIN_PACK_INTEGER.args(-1, 9),                           "FFFFFFFFFFFFFFFFFF");
    hexQuery(_BIN_PACK_INTEGER.args(1, 2, "most-significant-first"),  "0001");
    hexQuery(_BIN_PACK_INTEGER.args(1, 2, "big-endian"),              "0001");
    hexQuery(_BIN_PACK_INTEGER.args(1, 2, "BE"),                      "0001");
    hexQuery(_BIN_PACK_INTEGER.args(1, 2, "least-significant-first"), "0100");
    hexQuery(_BIN_PACK_INTEGER.args(1, 2, "little-endian"),           "0100");
    hexQuery(_BIN_PACK_INTEGER.args(1, 2, "LE"),                      "0100");
    // errors
    error(_BIN_PACK_INTEGER.args(1, -1), BIN_NS_X);
    error(_BIN_PACK_INTEGER.args(1, 1, "X"), BIN_USO_X);
  }

  /** Test method. */
  @Test
  public void unpackDouble() {
    query(_BIN_UNPACK_DOUBLE.args(base64("0000000000000000"), 0),                           0);
    query(_BIN_UNPACK_DOUBLE.args(base64("3FF0000000000000"), 0),                           1);
    query(_BIN_UNPACK_DOUBLE.args(base64("BFF0000000000000"), 0),                           -1);
    query(_BIN_UNPACK_DOUBLE.args(base64("8000000000000000"), 0),                           "-0");
    query(_BIN_UNPACK_DOUBLE.args(base64("0000000000000000"), 0),                           "0");
    query(_BIN_UNPACK_DOUBLE.args(base64("7FF0000000000000"), 0),                           "INF");
    query(_BIN_UNPACK_DOUBLE.args(base64("FFF0000000000000"), 0),                           "-INF");
    query(_BIN_UNPACK_DOUBLE.args(base64("7FF8000000000000"), 0),                           "NaN");
    query(_BIN_UNPACK_DOUBLE.args(base64("3FF0000000000000"), 0, "most-significant-first"), 1);
    query(_BIN_UNPACK_DOUBLE.args(base64("3FF0000000000000"), 0, "big-endian"),             1);
    query(_BIN_UNPACK_DOUBLE.args(base64("3FF0000000000000"), 0, "BE"),                     1);
    query(_BIN_UNPACK_DOUBLE.args(base64("000000000000F03F"), 0, "least-significant-first"), 1);
    query(_BIN_UNPACK_DOUBLE.args(base64("000000000000F03F"), 0, "little-endian"),          1);
    query(_BIN_UNPACK_DOUBLE.args(base64("000000000000F03F"), 0, "LE"),                     1);
    // errors
    error(_BIN_UNPACK_DOUBLE.args(base64("0000000000000000"), -1),     BIN_IOOR_X_X);
    error(_BIN_UNPACK_DOUBLE.args(base64("0000000000000000"), 1),      BIN_IOOR_X_X);
    error(_BIN_UNPACK_DOUBLE.args(base64("00"), 0),                    BIN_IOOR_X_X);
    error(_BIN_UNPACK_DOUBLE.args(base64("0000000000000000"), 0, "X"), BIN_USO_X);
  }

  /** Test method. */
  @Test
  public void unpackFloat() {
    query(_BIN_UNPACK_FLOAT.args(base64("00000000"), 0),                           0);
    query(_BIN_UNPACK_FLOAT.args(base64("3F800000"), 0),                           1);
    query(_BIN_UNPACK_FLOAT.args(base64("BF800000"), 0),                           -1);
    query(_BIN_UNPACK_FLOAT.args(base64("80000000"), 0),                           "-0");
    query(_BIN_UNPACK_FLOAT.args(base64("00000000"), 0),                           "0");
    query(_BIN_UNPACK_FLOAT.args(base64("7F800000"), 0),                           "INF");
    query(_BIN_UNPACK_FLOAT.args(base64("FF800000"), 0),                           "-INF");
    query(_BIN_UNPACK_FLOAT.args(base64("7FC00000"), 0),                           "NaN");
    query(_BIN_UNPACK_FLOAT.args(base64("3F800000"), 0, "most-significant-first"), 1);
    query(_BIN_UNPACK_FLOAT.args(base64("3F800000"), 0, "big-endian"),             1);
    query(_BIN_UNPACK_FLOAT.args(base64("3F800000"), 0, "BE"),                     1);
    query(_BIN_UNPACK_FLOAT.args(base64("0000803F"), 0, "least-significant-first"), 1);
    query(_BIN_UNPACK_FLOAT.args(base64("0000803F"), 0, "little-endian"),          1);
    query(_BIN_UNPACK_FLOAT.args(base64("0000803F"), 0, "LE"),                     1);
    // errors
    error(_BIN_UNPACK_FLOAT.args(base64("00000000"), -1),     BIN_IOOR_X_X);
    error(_BIN_UNPACK_FLOAT.args(base64("00000000"), 1),      BIN_IOOR_X_X);
    error(_BIN_UNPACK_FLOAT.args(base64("00"), 0),            BIN_IOOR_X_X);
    error(_BIN_UNPACK_FLOAT.args(base64("00000000"), 0, "X"), BIN_USO_X);
  }

  /** Test method. */
  @Test
  public void unpackInteger() {
    query(_BIN_UNPACK_INTEGER.args(base64("01"), 0, 0), 0);
    query(_BIN_UNPACK_INTEGER.args(base64("01"), 0, 1), 1);
    query(_BIN_UNPACK_INTEGER.args(base64("FF"), 0, 1), -1);
    query(_BIN_UNPACK_INTEGER.args(base64("0001"), 0, 2), 1);
    query(_BIN_UNPACK_INTEGER.args(base64("7FFF"), 0, 2), 32767);
    query(_BIN_UNPACK_INTEGER.args(base64("FFFF"), 0, 2), -1);
    query(_BIN_UNPACK_INTEGER.args(base64("0001"), 0, 2, "most-significant-first"), 1);
    query(_BIN_UNPACK_INTEGER.args(base64("0001"), 0, 2, "big-endian"), 1);
    query(_BIN_UNPACK_INTEGER.args(base64("0001"), 0, 2, "BE"), 1);
    query(_BIN_UNPACK_INTEGER.args(base64("0100"), 0, 2, "least-significant-first"), 1);
    query(_BIN_UNPACK_INTEGER.args(base64("0100"), 0, 2, "little-endian"), 1);
    query(_BIN_UNPACK_INTEGER.args(base64("0100"), 0, 2, "LE"), 1);
    // errors
    error(_BIN_UNPACK_INTEGER.args(base64("00"), -1, 0),     BIN_IOOR_X_X);
    error(_BIN_UNPACK_INTEGER.args(base64("00"), 0, -1),     BIN_NS_X);
    error(_BIN_UNPACK_INTEGER.args(base64("00"), 0, 2),      BIN_IOOR_X_X);
    error(_BIN_UNPACK_INTEGER.args(base64("00"), 0, 0, "X"), BIN_USO_X);
  }

  /** Test method. */
  @Test
  public void unpackUnsignedInteger() {
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("01"), 0, 0), 0);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("01"), 0, 1), 1);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("FF"), 0, 1), (1 << 8) - 1);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("0001"), 0, 2), 1);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("7FFF"), 0, 2), 32767);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("FFFF"), 0, 2), (1 << 16) - 1);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("FFFFFFFF"), 0, 4), (1L << 32) - 1);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("01FFFFFFFF"), 0, 5), (1L << 33) - 1);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("FFFFFFFFFF"), 0, 5), (1L << 40) - 1);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("FFFFFFFFFFFF"), 0, 6), (1L << 48) - 1);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("0001"), 0, 2, "most-significant-first"), 1);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("0001"), 0, 2, "big-endian"), 1);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("0001"), 0, 2, "BE"), 1);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("0100"), 0, 2, "least-significant-first"), 1);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("0100"), 0, 2, "little-endian"), 1);
    query(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("0100"), 0, 2, "LE"), 1);
    // errors
    error(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("00"), -1, 0),     BIN_IOOR_X_X);
    error(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("00"), 0, -1),     BIN_NS_X);
    error(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("00"), 0, 2),      BIN_IOOR_X_X);
    error(_BIN_UNPACK_UNSIGNED_INTEGER.args(base64("00"), 0, 0, "X"), BIN_USO_X);
  }

  /**
   * Checks if a query yields the specified hex string.
   * @param query query string
   * @param result expected query result
   */
  private static void hexQuery(final String query, final String result) {
    query("xs:hexBinary(" + query + ')', result);
  }

  /**
   * Returns a query string to convert the specified input to a base64 binary.
   * @param input input string
   * @return query
   */
  private static String base64(final String input) {
    return "xs:base64Binary(xs:hexBinary('" + input + "'))";
  }
}
