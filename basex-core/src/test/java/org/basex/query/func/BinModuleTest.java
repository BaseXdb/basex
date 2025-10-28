package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Binary Module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void and() {
    final Function func = _BIN_AND;
    // successful queries
    hexQuery(func.args(" ()", hex("00")),            "");
    hexQuery(func.args(hex("00"), " ()"),            "");
    hexQuery(func.args(hex(""), hex("")),         "");
    hexQuery(func.args(hex("8081"), hex("7F7E")), "0000");
    // errors
    error(func.args(hex("00"), hex("")), BIN_DLA_X_X);
  }

  /** Test method. */
  @Test public void bin() {
    final Function func = _BIN_BIN;
    // successful queries
    hexQuery(func.args(" ()"),              "");
    hexQuery(func.args(""),                 "");
    hexQuery(func.args("0"),                "00");
    hexQuery(func.args("00"),               "00");
    hexQuery(func.args("000000000"),        "0000");
    hexQuery(func.args("0 "),              "00");
    hexQuery(func.args("0_0 "),            "00");
    hexQuery(func.args("1"),                "01");
    hexQuery(func.args("10"),               "02");
    hexQuery(func.args("11111111"),         "FF");
    hexQuery(func.args("111111111"),        "01FF");
    hexQuery(func.args("1101000111010101"), "D1D5");
    hexQuery(func.args("1000111010101"),    "11D5");

    // errors
    error(func.args("X"), BIN_NNC);
  }

  /** Test method. */
  @Test public void decodeString() {
    final Function func = _BIN_DECODE_STRING;
    // successful queries
    query(func.args(hex("31")),                  1);
    query(func.args(hex("31"), "US-ASCII"),      1);
    query(func.args(hex("31"), "UTF-8"),         1);
    query(func.args(hex("3132"), "UTF-8", 1, 1), 2);

    query(func.args(hex("40EFBBBF4142")), "@\uFEFFAB");
    query(func.args(hex("EFBBBF4142")),   "AB");
    error(func.args(hex("BBBF4142")),     BIN_CE_X);
    error(func.args(hex("BF4142")),       BIN_CE_X);
    query(func.args(hex("4142")),         "AB");
    query(func.args(hex("42")),           "B");
    query(func.args(hex("")),             "");

    query(func.args(hex("40EFBBBF4142"), " ()", 0), "@\uFEFFAB");
    query(func.args(hex("40EFBBBF4142"), " ()", 1), "\uFEFFAB");
    error(func.args(hex("40EFBBBF4142"), " ()", 2), BIN_CE_X);
    error(func.args(hex("40EFBBBF4142"), " ()", 3), BIN_CE_X);
    query(func.args(hex("40EFBBBF4142"), " ()", 4), "AB");
    query(func.args(hex("40EFBBBF4142"), " ()", 5), "B");
    query(func.args(hex("40EFBBBF4142"), " ()", 6), "");

    query(func.args(hex("40EFBBBF4142"), "utf-8", 0), "@\uFEFFAB");
    query(func.args(hex("40EFBBBF4142"), "utf-8", 1), "\uFEFFAB");
    error(func.args(hex("40EFBBBF4142"), "utf-8", 2), BIN_CE_X);
    error(func.args(hex("40EFBBBF4142"), "utf-8", 3), BIN_CE_X);
    query(func.args(hex("40EFBBBF4142"), "utf-8", 4), "AB");
    query(func.args(hex("40EFBBBF4142"), "utf-8", 5), "B");
    query(func.args(hex("40EFBBBF4142"), "utf-8", 6), "");

    query(func.args(hex("40EFBBBF4142"), "utf-16", 0), "\u40EF\uBBBF\u4142");
    error(func.args(hex("40EFBBBF4142"), "utf-16", 1), BIN_CE_X);
    query(func.args(hex("40EFBBBF4142"), "utf-16", 2), "\uBBBF\u4142");
    error(func.args(hex("40EFBBBF4142"), "utf-16", 3), BIN_CE_X);
    query(func.args(hex("40EFBBBF4142"), "utf-16", 4), "\u4142");
    error(func.args(hex("40EFBBBF4142"), "utf-16", 5), BIN_CE_X);
    query(func.args(hex("40EFBBBF4142"), "utf-16", 6), "");

    query(func.args(hex("40EFBBBF4142"), "utf-16be", 0), "\u40EF\uBBBF\u4142");
    error(func.args(hex("40EFBBBF4142"), "utf-16be", 1), BIN_CE_X);
    query(func.args(hex("40EFBBBF4142"), "utf-16be", 2), "\uBBBF\u4142");
    error(func.args(hex("40EFBBBF4142"), "utf-16be", 3), BIN_CE_X);
    query(func.args(hex("40EFBBBF4142"), "utf-16be", 4), "\u4142");
    error(func.args(hex("40EFBBBF4142"), "utf-16be", 5), BIN_CE_X);
    query(func.args(hex("40EFBBBF4142"), "utf-16be", 6), "");

    query(func.args(hex("40EFBBBF4142"), "utf-16le", 0), "\uEF40\uBFBB\u4241");
    error(func.args(hex("40EFBBBF4142"), "utf-16le", 1), BIN_CE_X);
    query(func.args(hex("40EFBBBF4142"), "utf-16le", 2), "\uBFBB\u4241");
    error(func.args(hex("40EFBBBF4142"), "utf-16le", 3), BIN_CE_X);
    query(func.args(hex("40EFBBBF4142"), "utf-16le", 4), "\u4241");
    error(func.args(hex("40EFBBBF4142"), "utf-16le", 5), BIN_CE_X);
    query(func.args(hex("40EFBBBF4142"), "utf-16le", 6), "");

    query(func.args(hex("40EFBBBF4142"), "iso-8859-1", 0), "@\u00EF\u00BB\u00BFAB");
    query(func.args(hex("40EFBBBF4142"), "iso-8859-1", 1), "\u00EF\u00BB\u00BFAB");
    query(func.args(hex("40EFBBBF4142"), "iso-8859-1", 2), "\u00BB\u00BFAB");
    query(func.args(hex("40EFBBBF4142"), "iso-8859-1", 3), "\u00BFAB");
    query(func.args(hex("40EFBBBF4142"), "iso-8859-1", 4), "AB");
    query(func.args(hex("40EFBBBF4142"), "iso-8859-1", 5), "B");
    query(func.args(hex("40EFBBBF4142"), "iso-8859-1", 6), "");

    // errors
    error(func.args(hex(""), "UTF-8", -1),    BIN_IOOR_X_X);
    error(func.args(hex(""), "UTF-8", 0, -1), BIN_NS_X);
    error(func.args(hex(""), "UTF-8", 1, 0),  BIN_IOOR_X_X);
    error(func.args(hex(""), "UTF-8", 0, 1),  BIN_IOOR_X_X);
    error(func.args(hex(""), "X"),            BIN_UE_X);
    error(func.args(hex("FF"), "UTF-8"),      BIN_CE_X);
    error(func.args(_BIN_HEX.args("03")),     BIN_CE_X);
  }

  /** Test method. */
  @Test public void encodeString() {
    final Function func = _BIN_ENCODE_STRING;
    // successful queries
    hexQuery(func.args(""),                "");
    hexQuery(func.args("", "US-ASCII"),    "");
    hexQuery(func.args("a", "US-ASCII"),   "61");
    hexQuery(func.args("\u00c4", "UTF-8"), "C384");
    // errors
    error(func.args("", "X"),              BIN_UE_X);
    error(func.args("\u00c4", "US-ASCII"), BIN_CE_X);
  }

  /** Test method. */
  @Test public void find() {
    final Function func = _BIN_FIND;
    // successful queries
    query(func.args(hex("1122"),   0, hex("11")), 0);
    query(func.args(hex("1122"),   1, hex("11")), "");
    query(func.args(hex("112233"), 0, hex("22")), 1);
    query(func.args(hex(""), 0, hex("")), 0);
    // errors
    error(func.args(hex(""), -1, hex("11")), BIN_IOOR_X_X);
    error(func.args(hex(""), 1, hex("11")),  BIN_IOOR_X_X);
  }

  /** Test method. */
  @Test public void fromOctets() {
    final Function func = _BIN_FROM_OCTETS;
    // successful queries
    hexQuery(func.args(0),            "00");
    hexQuery(func.args(" (1, 127)"),   "017F");
    hexQuery(func.args(" (128, 255)"), "80FF");
    // errors
    error(func.args(-1),  BIN_OOR_X);
    error(func.args(256), BIN_OOR_X);
  }

  /** Test method. */
  @Test public void hex() {
    final Function func = _BIN_HEX;
    // successful queries
    hexQuery(func.args(" ()"),            "");
    hexQuery(func.args(""),               "");
    hexQuery(func.args("1"),              "01");
    hexQuery(func.args("FF"),             "FF");
    hexQuery(func.args("FF "),            "FF");
    hexQuery(func.args("F_F"),            "FF");
    hexQuery(func.args("111"),            "0111");
    hexQuery(func.args("FFF"),            "0FFF");
    hexQuery(func.args("000"),            "0000");
    hexQuery(func.args("FFFFF"),          "0FFFFF");
    hexQuery(func.args("FFFFFFFFFFFFF"),  "0FFFFFFFFFFFFF");
    hexQuery(func.args("10000000000000"), "10000000000000");
    hexQuery(func.args("10000000000000"), "10000000000000");
    hexQuery(func.args("11223F4E"),       "11223F4E");
    hexQuery(func.args("1223F4E"),        "01223F4E");
    // errors
    error(func.args("X"), BIN_NNC);
  }

  /** Test method. */
  @Test public void insertBefore() {
    final Function func = _BIN_INSERT_BEFORE;
    // successful queries
    hexQuery(func.args(" ()", 0, " ()"),                   "");
    hexQuery(func.args(hex("12"),   0, " ()"),          "12");
    hexQuery(func.args(hex("12"),   1, " ()"),          "12");
    hexQuery(func.args(hex("1234"), 0, hex("00")),   "001234");
    hexQuery(func.args(hex("1234"), 1, hex("56")),   "125634");
    hexQuery(func.args(hex("1234"), 2, hex("56")),   "123456");
    hexQuery(func.args(hex("12"),   0, hex("3456")), "345612");
    hexQuery(func.args(hex("12"),   1, hex("3456")), "123456");
    hexQuery(func.args(hex("12"),   1, hex("34")),   "1234");
    // errors
    error(func.args(hex(""), -1, " ()"), BIN_IOOR_X_X);
    error(func.args(hex(""),  1, " ()"), BIN_IOOR_X_X);
  }

  /** Test method. */
  @Test public void join() {
    final Function func = _BIN_JOIN;
    // successful queries
    hexQuery(func.args(" ()"),                                          "");
    hexQuery(func.args(" (" + hex("") + ')'),                        "");
    hexQuery(func.args(" (" + hex("FF") + ')'),                      "FF");
    hexQuery(func.args(" (" + hex("FF") + ',' + hex("FF") + ')'), "FFFF");
    hexQuery(func.args(" (1 to 3) ! " + hex("11")),                 "111111");
  }

  /** Test method. */
  @Test public void length() {
    final Function func = _BIN_LENGTH;
    // successful queries
    query(func.args(hex("")),         0);
    query(func.args(hex("FF")),       1);
    query(func.args(hex("12345678")), 4);
  }

  /** Test method. */
  @Test public void not() {
    final Function func = _BIN_NOT;
    // successful queries
    hexQuery(func.args(" ()"),          "");
    hexQuery(func.args(hex("00")),   "FF");
    hexQuery(func.args(hex("8081")), "7F7E");
  }

  /** Test method. */
  @Test public void octal() {
    final Function func = _BIN_OCTAL;
    // successful queries
    hexQuery(func.args(" ()"),      "");
    hexQuery(func.args(""),         "");
    hexQuery(func.args("0"),        "00");
    hexQuery(func.args("00"),       "00");
    hexQuery(func.args("00 "),      "00");
    hexQuery(func.args("0_0"),      "00");
    hexQuery(func.args("000"),      "0000");
    hexQuery(func.args("007"),      "0007");
    hexQuery(func.args("1"),        "01");
    hexQuery(func.args("10"),       "08");
    hexQuery(func.args("77"),       "3F");
    hexQuery(func.args("11223047"), "252627");
    // errors
    error(func.args("X"), BIN_NNC);
  }

  /** Test method. */
  @Test public void or() {
    final Function func = _BIN_OR;
    // successful queries
    hexQuery(func.args(" ()", hex("00")),            "");
    hexQuery(func.args(hex("00"), " ()"),            "");
    hexQuery(func.args(hex(""), hex("")),         "");
    hexQuery(func.args(hex("8081"), hex("7F7E")), "FFFF");
    // errors
    error(func.args(hex("00"), hex("")), BIN_DLA_X_X);
  }

  /** Test method. */
  @Test public void packDouble() {
    final Function func = _BIN_PACK_DOUBLE;
    // successful queries
    hexQuery(func.args(0),                            "0000000000000000");
    hexQuery(func.args(1),                            "3FF0000000000000");
    hexQuery(func.args(-1),                           "BFF0000000000000");
    hexQuery(func.args(" -0e0"),                      "8000000000000000");
    hexQuery(func.args(" 0e0"),                       "0000000000000000");
    hexQuery(func.args(" xs:double('INF')"),          "7FF0000000000000");
    hexQuery(func.args(" xs:double('-INF')"),         "FFF0000000000000");
    hexQuery(func.args(" xs:double('NaN')"),          "7FF8000000000000");
    hexQuery(func.args(1, "most-significant-first"),  "3FF0000000000000");
    hexQuery(func.args(1, "big-endian"),              "3FF0000000000000");
    hexQuery(func.args(1, "BE"),                      "3FF0000000000000");
    hexQuery(func.args(1, "least-significant-first"), "000000000000F03F");
    hexQuery(func.args(1, "little-endian"),           "000000000000F03F");
    hexQuery(func.args(1, "LE"),                      "000000000000F03F");
    // errors
    error(func.args(1, "X"), BIN_USO_X);
  }

  /** Test method. */
  @Test public void packFloat() {
    final Function func = _BIN_PACK_FLOAT;
    // successful queries
    hexQuery(func.args(0),                            "00000000");
    hexQuery(func.args(1),                            "3F800000");
    hexQuery(func.args(-1),                           "BF800000");
    hexQuery(func.args(" -0e0"),                      "80000000");
    hexQuery(func.args(" 0e0"),                       "00000000");
    hexQuery(func.args(" xs:float('INF')"),           "7F800000");
    hexQuery(func.args(" xs:float('-INF')"),          "FF800000");
    hexQuery(func.args(" xs:float('NaN')"),           "7FC00000");
    hexQuery(func.args(1, "most-significant-first"),  "3F800000");
    hexQuery(func.args(1, "big-endian"),              "3F800000");
    hexQuery(func.args(1, "BE"),                      "3F800000");
    hexQuery(func.args(1, "least-significant-first"), "0000803F");
    hexQuery(func.args(1, "little-endian"),           "0000803F");
    hexQuery(func.args(1, "LE"),                      "0000803F");
    // errors
    error(func.args(1, "X"), BIN_USO_X);
  }

  /** Test method. */
  @Test public void packInteger() {
    final Function func = _BIN_PACK_INTEGER;
    // successful queries
    hexQuery(func.args(1, 0),                            "");
    hexQuery(func.args(1, 1),                            "01");
    hexQuery(func.args(1, 2),                            "0001");
    hexQuery(func.args(Long.MAX_VALUE, 8),               "7FFFFFFFFFFFFFFF");
    hexQuery(func.args(-1, 8),                           "FFFFFFFFFFFFFFFF");
    hexQuery(func.args(-1, 9),                           "FFFFFFFFFFFFFFFFFF");
    hexQuery(func.args(1, 2, "most-significant-first"),  "0001");
    hexQuery(func.args(1, 2, "big-endian"),              "0001");
    hexQuery(func.args(1, 2, "BE"),                      "0001");
    hexQuery(func.args(1, 2, "least-significant-first"), "0100");
    hexQuery(func.args(1, 2, "little-endian"),           "0100");
    hexQuery(func.args(1, 2, "LE"),                      "0100");
    // errors
    error(func.args(1, -1), BIN_NS_X);
    error(func.args(1, 1, "X"), BIN_USO_X);
  }

  /** Test method. */
  @Test public void padLeft() {
    final Function func = _BIN_PAD_LEFT;
    // successful queries
    hexQuery(func.args(hex(""),   1),      "00");
    hexQuery(func.args(hex(""),   1, 255), "FF");
    hexQuery(func.args(hex("01"), 2, 127), "7F7F01");
    // errors
    error(func.args(hex(""), -1),     BIN_NS_X);
    error(func.args(hex(""), 0, 256), BIN_OOR_X);
  }

  /** Test method. */
  @Test public void padRight() {
    final Function func = _BIN_PAD_RIGHT;
    // successful queries
    hexQuery(func.args(hex(""),   1), "00");
    hexQuery(func.args(hex(""),   1, 255), "FF");
    hexQuery(func.args(hex("01"), 2, 127), "017F7F");
    // errors
    error(func.args(hex(""), -1),      BIN_NS_X);
    error(func.args(hex(""),  0, 256), BIN_OOR_X);
  }

  /** Test method. */
  @Test public void part() {
    final Function func = _BIN_PART;
    // successful queries
    hexQuery(func.args(" ()",        0),    "");
    hexQuery(func.args(hex("FF"), 0),    "FF");
    hexQuery(func.args(hex("FF"), 0, 1), "FF");
    hexQuery(func.args(hex("FF"), 1),    "");
    hexQuery(func.args(hex("FF"), 1, 0), "");
    // errors
    error(func.args(hex("FF"), -1),    BIN_IOOR_X_X);
    error(func.args(hex("FF"), 0, -1), BIN_NS_X);
    error(func.args(hex("FF"), 2),     BIN_IOOR_X_X);
    error(func.args(hex("FF"), 0, 2),  BIN_IOOR_X_X);
  }

  /** Test method. */
  @Test public void shift() {
    final Function func = _BIN_SHIFT;
    // successful queries
    hexQuery(func.args(" ()", 1),                "");
    hexQuery(func.args(hex("77"), 0),         "77");
    hexQuery(func.args(hex("33"), 1),         "66");
    hexQuery(func.args(hex("66"), -1),        "33");
    hexQuery(func.args(hex("0066"), 8),       "6600");
    hexQuery(func.args(hex("6600"), -8),      "0066");
    hexQuery(func.args(hex("12345678"), 16),  "56780000");
    hexQuery(func.args(hex("12345678"), -16), "00001234");
    hexQuery(func.args(hex("00C641"), 7),     "632080");
    hexQuery(func.args(hex("00000001"), 1),   "00000002");
    hexQuery(func.args(hex("00000001"), 31),  "80000000");
    hexQuery(func.args(hex("00000001"), 32),  "00000000");
    hexQuery(func.args(hex("FFFFFFFF"), -31), "00000001");
    hexQuery(func.args(hex("FFFFFFFF"), -32), "00000000");

    hexQuery(func.args(hex("00000000"), 111111111), "00000000");
    hexQuery(func.args(hex("00000000"), -111111111), "00000000");
  }

  /** Test method. */
  @Test public void toOctets() {
    final Function func = _BIN_TO_OCTETS;
    // successful queries
    query(func.args(hex("")),     "");
    query(func.args(hex("00")),   0);
    query(func.args(hex("FF")),   255);
    query(func.args(hex("1122")), "17\n34");
  }

  /** Test method. */
  @Test public void unpackDouble() {
    final Function func = _BIN_UNPACK_DOUBLE;
    // successful queries
    query(func.args(hex("0000000000000000"), 0),                           0);
    query(func.args(hex("3FF0000000000000"), 0),                           1);
    query(func.args(hex("BFF0000000000000"), 0),                           -1);
    query(func.args(hex("8000000000000000"), 0),                           "-0");
    query(func.args(hex("0000000000000000"), 0),                           0);
    query(func.args(hex("7FF0000000000000"), 0),                           "INF");
    query(func.args(hex("FFF0000000000000"), 0),                           "-INF");
    query(func.args(hex("7FF8000000000000"), 0),                           "NaN");
    query(func.args(hex("3FF0000000000000"), 0, "most-significant-first"), 1);
    query(func.args(hex("3FF0000000000000"), 0, "big-endian"),             1);
    query(func.args(hex("3FF0000000000000"), 0, "BE"),                     1);
    query(func.args(hex("000000000000F03F"), 0, "least-significant-first"), 1);
    query(func.args(hex("000000000000F03F"), 0, "little-endian"),          1);
    query(func.args(hex("000000000000F03F"), 0, "LE"),                     1);
    // errors
    error(func.args(hex("0000000000000000"), -1),     BIN_IOOR_X_X);
    error(func.args(hex("0000000000000000"), 1),      BIN_IOOR_X_X);
    error(func.args(hex("00"), 0),                    BIN_IOOR_X_X);
    error(func.args(hex("0000000000000000"), 0, "X"), BIN_USO_X);
  }

  /** Test method. */
  @Test public void unpackFloat() {
    final Function func = _BIN_UNPACK_FLOAT;
    // successful queries
    query(func.args(hex("00000000"), 0),                           0);
    query(func.args(hex("3F800000"), 0),                           1);
    query(func.args(hex("BF800000"), 0),                           -1);
    query(func.args(hex("80000000"), 0),                           "-0");
    query(func.args(hex("00000000"), 0),                           0);
    query(func.args(hex("7F800000"), 0),                           "INF");
    query(func.args(hex("FF800000"), 0),                           "-INF");
    query(func.args(hex("7FC00000"), 0),                           "NaN");
    query(func.args(hex("3F800000"), 0, "most-significant-first"), 1);
    query(func.args(hex("3F800000"), 0, "big-endian"),             1);
    query(func.args(hex("3F800000"), 0, "BE"),                     1);
    query(func.args(hex("0000803F"), 0, "least-significant-first"), 1);
    query(func.args(hex("0000803F"), 0, "little-endian"),          1);
    query(func.args(hex("0000803F"), 0, "LE"),                     1);
    // errors
    error(func.args(hex("00000000"), -1),     BIN_IOOR_X_X);
    error(func.args(hex("00000000"), 1),      BIN_IOOR_X_X);
    error(func.args(hex("00"), 0),            BIN_IOOR_X_X);
    error(func.args(hex("00000000"), 0, "X"), BIN_USO_X);
  }

  /** Test method. */
  @Test public void unpackInteger() {
    final Function func = _BIN_UNPACK_INTEGER;
    // successful queries
    query(func.args(hex("01"), 0, 0), 0);
    query(func.args(hex("01"), 0, 1), 1);
    query(func.args(hex("FF"), 0, 1), -1);
    query(func.args(hex("0001"), 0, 2), 1);
    query(func.args(hex("7FFF"), 0, 2), 32767);
    query(func.args(hex("FFFF"), 0, 2), -1);
    query(func.args(hex("0001"), 0, 2, "most-significant-first"), 1);
    query(func.args(hex("0001"), 0, 2, "big-endian"), 1);
    query(func.args(hex("0001"), 0, 2, "BE"), 1);
    query(func.args(hex("0100"), 0, 2, "least-significant-first"), 1);
    query(func.args(hex("0100"), 0, 2, "little-endian"), 1);
    query(func.args(hex("0100"), 0, 2, "LE"), 1);
    // errors
    error(func.args(hex("00"), -1, 0),     BIN_IOOR_X_X);
    error(func.args(hex("00"), 0, -1),     BIN_NS_X);
    error(func.args(hex("00"), 0, 2),      BIN_IOOR_X_X);
    error(func.args(hex("00"), 0, 0, "X"), BIN_USO_X);
  }

  /** Test method. */
  @Test public void unpackUnsignedInteger() {
    final Function func = _BIN_UNPACK_UNSIGNED_INTEGER;
    // successful queries
    query(func.args(hex("01"), 0, 0), 0);
    query(func.args(hex("01"), 0, 1), 1);
    query(func.args(hex("FF"), 0, 1), (1 << 8) - 1);
    query(func.args(hex("0001"), 0, 2), 1);
    query(func.args(hex("7FFF"), 0, 2), 32767);
    query(func.args(hex("FFFF"), 0, 2), (1 << 16) - 1);
    query(func.args(hex("FFFFFFFF"), 0, 4), (1L << 32) - 1);
    query(func.args(hex("01FFFFFFFF"), 0, 5), (1L << 33) - 1);
    query(func.args(hex("FFFFFFFFFF"), 0, 5), (1L << 40) - 1);
    query(func.args(hex("FFFFFFFFFFFF"), 0, 6), (1L << 48) - 1);
    query(func.args(hex("0001"), 0, 2, "most-significant-first"), 1);
    query(func.args(hex("0001"), 0, 2, "big-endian"), 1);
    query(func.args(hex("0001"), 0, 2, "BE"), 1);
    query(func.args(hex("0100"), 0, 2, "least-significant-first"), 1);
    query(func.args(hex("0100"), 0, 2, "little-endian"), 1);
    query(func.args(hex("0100"), 0, 2, "LE"), 1);
    // errors
    error(func.args(hex("00"), -1, 0),     BIN_IOOR_X_X);
    error(func.args(hex("00"), 0, -1),     BIN_NS_X);
    error(func.args(hex("00"), 0, 2),      BIN_IOOR_X_X);
    error(func.args(hex("00"), 0, 0, "X"), BIN_USO_X);
  }

  /** Test method. */
  @Test public void xor() {
    final Function func = _BIN_XOR;
    // successful queries
    hexQuery(func.args(" ()", hex("00")),            "");
    hexQuery(func.args(hex("00"), " ()"),            "");
    hexQuery(func.args(hex(""), hex("")),         "");
    hexQuery(func.args(hex("80"), hex("7F")),     "FF");
    hexQuery(func.args(hex("1234"), hex("4321")), "5115");
    // errors
    error(func.args(hex("00"), hex("")), BIN_DLA_X_X);
  }

  /**
   * Checks if a query yields the specified hex string.
   * @param query query string
   * @param result expected query result
   */
  private static void hexQuery(final String query, final Object result) {
    query("string(xs:hexBinary(" + query + "))", result);
  }

  /**
   * Returns a query string to convert the specified input to a base64 binary.
   * @param input input string
   * @return query
   */
  private static String hex(final String input) {
    return " xs:hexBinary('" + input + "')";
  }
}
