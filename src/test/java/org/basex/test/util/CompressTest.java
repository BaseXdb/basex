package org.basex.test.util;

import static org.junit.Assert.*;
import static org.basex.util.Token.*;
import java.util.Arrays;
import org.basex.core.Context;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.util.Compress;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;
import org.basex.util.Util;
import org.junit.Test;

/**
 * Class for testing the {@link Compress} methods.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class CompressTest {
  /** Verbose flag. */
  private static final boolean VERBOSE = false;
  /** Number of occurrences. */
  private static int[] occ = new int[256];

  /** Test. */
  @Test
  public void test1() {
    test(token(" abcdefghijklmnopqrstuvwxyz"));
  }

  /** Test. */
  @Test
  public void test2() {
    test(token("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
  }

  /** Test. */
  @Test
  public void test3() {
    test(token("abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ " +
        "1234567890"));
  }

  /** Test. */
  @Test
  public void test4() {
    final byte[] b = new byte[65];
    for(int i = 0; i < b.length; i++) b[i] = (byte) i;
    test(b);
  }

  /** Test. */
  @Test
  public void test5() {
    final byte[] b = new byte[256];
    for(int i = 0; i < b.length; i++) b[i] = (byte) i;
    test(b);
  }

  /** Test. */
  @Test
  public void test6() {
    final byte[] b = new byte[4096];
    for(int i = 0; i < b.length; i++) b[i] = (byte) (i & 0xFF);
    test(b);
  }

  /** Test.
   * @throws Exception exception */
  @Test
  public void test7() throws Exception {
    testTexts("etc/xml/xmark.xml");
  }

  /** Test.
   * @throws Exception exception */
  @Test
  public void test8() throws Exception {
    testTexts("etc/xml/factbook.zip");
  }

  /**
   * Test on all text nodes of a document.
   * @param file file to be parsed
   * @throws Exception exception
   */
  private void testTexts(final String file) throws Exception {
    final Context ctx = new Context();
    final String query = "let $doc := doc('" + file + "')" +
      "for $i in $doc//(@hohoho | text()) return data($i)";

    final TokenList tl = new TokenList();
    final TokenBuilder tb = new TokenBuilder();
    final QueryProcessor qp = new QueryProcessor(query, ctx);
    final Iter ir = qp.iter();
    Item it = null;
    while((it = ir.next()) != null) {
      final byte[] token = it.atom();
      tl.add(token);
      tb.add(token);
      tb.add(' ');
    }
    qp.close();

    test(tl.toArray());
    test(tb.finish());
    ctx.close();
  }

  /**
   * Tests the correctness of the compressed tokens.
   * @param tokens test tokens
   */
  private void test(final byte[]... tokens) {
    int tl = 0;
    int cl = 0;
    final Compress comp = new Compress();
    for(final byte[] token : tokens) {
      final byte[] cpr = comp.pack(token);
      tl += token.length;
      cl += cpr.length;
      if(token != cpr) {
        final byte[] pln = comp.unpack(cpr);
        if(!eq(token, pln)) {
          fail("\n[E] " + Arrays.toString(token) + ",\n[F] " +
              Arrays.toString(pln));
        }
      }
    }
    if(VERBOSE) {
      for(final byte[] token : tokens) {
        for(final byte t : token) occ[t & 0xFF]++;
      }
      Util.outln((cl == tl ? "= " : "+ ") + tl + " -> " + cl);
    }
  }
}
