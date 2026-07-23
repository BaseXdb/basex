package org.basex.query.value.seq;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the compact representations of integer sequences.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ItrSeqTest extends SandboxTest {
  /** Values are stored in the narrowest fitting representation. */
  @Test public void representations() {
    check("(1, 3, 2)", "1\n3\n2", root(BytSeq.class));
    check("(-129, 0, 128)", "-129\n0\n128", root(ShrSeq.class));
    check("(-32769, 0, 32768)", "-32769\n0\n32768", root(IntSeq.class));
    check("(-2147483649, 0, 2147483648)", "-2147483649\n0\n2147483648", root(LongSeq.class));

    // identical and consecutive values are collapsed
    check("(5, 5, 5)", "5\n5\n5", root(SingletonSeq.class));
    check("(1, 2, 3)", "1\n2\n3", root(RangeSeq.class));
  }

  /** Values at the limits of each representation. */
  @Test public void boundaries() {
    check("(-128, 0, 127)", "-128\n0\n127", root(BytSeq.class));
    check("(-32768, 0, 32767)", "-32768\n0\n32767", root(ShrSeq.class));
    check("(-2147483648, 0, 2147483647)", "-2147483648\n0\n2147483647", root(IntSeq.class));
    check("(-9223372036854775807 - 1, 0, 9223372036854775807)",
        "-9223372036854775808\n0\n9223372036854775807", root(LongSeq.class));

    // a single wide value widens the whole sequence
    check("(1, 3, 128)", "1\n3\n128", root(ShrSeq.class));
    check("(1, 3, 32768)", "1\n3\n32768", root(IntSeq.class));
    check("(1, 3, 5000000000)", "1\n3\n5000000000", root(LongSeq.class));
  }

  /**
   * Integer subtypes must not be degraded to xs:integer. A range can only represent xs:integer,
   * so consecutive or identical xs:byte values must not be collapsed to one.
   * The input is wrapped: otherwise, 'instance of' would be evaluated at compile time.
   */
  @Test public void subtypes() {
    // 0, 1, 2, 3: consecutive
    final String consecutive = "convert:binary-to-bytes(xs:base64Binary(" + wrap("AAECAw==") + "))";
    query(consecutive + " instance of xs:byte+", true);
    query("count(" + consecutive + ')', 4);
    query("string-join(" + consecutive + ", ',')", "0,1,2,3");

    // 0, 0, 0, 0: identical
    final String identical = "convert:binary-to-bytes(xs:base64Binary(" + wrap("AAAAAA==") + "))";
    query(identical + " instance of xs:byte+", true);
    query("string-join(" + identical + ", ',')", "0,0,0,0");

    // -128, -127, -1, -2: negative
    final String negative = "convert:binary-to-bytes(xs:base64Binary(" + wrap("gIH//g==") + "))";
    query(negative + " instance of xs:byte+", true);
    query("string-join(" + negative + ", ',')", "-128,-127,-1,-2");
  }

  /** Values survive a serialization round trip in every representation. */
  @Test public void store() {
    roundTrip("(1 to 100) ! (. mod 7)");
    roundTrip("(1 to 100) ! (0 - . mod 7)");
    roundTrip("(1 to 100) ! (. * 300)");
    roundTrip("(1 to 100) ! (. * 100000)");
    roundTrip("(1 to 100) ! (. * 3000000000)");
    roundTrip("(-9223372036854775807 - 1, 0, 9223372036854775807)");
    roundTrip("convert:binary-to-bytes(xs:base64Binary('AAECAw=='))");
  }

  /** Expressions that create integer sequences use compact representations. */
  @Test public void producers() {
    check("string-to-codepoints('hello')", "104\n101\n108\n108\n111", root(BytSeq.class));
    check("distinct-values((5, 3, 4, 2, 1, 6, 1))", "5\n3\n4\n2\n1\n6", root(BytSeq.class));
    check("index-of((5, 3, 5), 5)", "1\n3", root(BytSeq.class));
    check("array:index-of([ 5, 3, 5 ], 5)", "1\n3", root(BytSeq.class));
  }

  /**
   * Stores a value, reads it back and compares it with the original one.
   * @param query query yielding the value to be stored
   */
  private static void roundTrip(final String query) {
    query("let $value := " + query + " return (store:put('itrseq', $value), " +
        "deep-equal(store:get('itrseq'), $value), store:delete('itrseq'))", true);
  }
}
