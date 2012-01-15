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
import org.basex.util.list.TokenList;
import org.junit.Test;

/**
 * Class for testing the {@link Compress} methods.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CompressTest {
  /** Test. */
  @Test
  public void test1() {
    run(token(" abcdefghijklmnopqrstuvwxyz"));
  }

  /** Test. */
  @Test
  public void test2() {
    run(token("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
  }

  /** Test. */
  @Test
  public void test3() {
    run(token("abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ " +
        "1234567890"));
  }

  /** Test. */
  @Test
  public void test4() {
    final byte[] b = new byte[65];
    for(int i = 0; i < b.length; i++) b[i] = (byte) i;
    run(b);
  }

  /** Test. */
  @Test
  public void test5() {
    final byte[] b = new byte[256];
    for(int i = 0; i < b.length; i++) b[i] = (byte) i;
    run(b);
  }

  /** Test. */
  @Test
  public void test6() {
    final byte[] b = new byte[4096];
    for(int i = 0; i < b.length; i++) b[i] = (byte) (i & 0xFF);
    run(b);
  }

  /** Test.
   * @throws Exception exception */
  @Test
  public void test7() throws Exception {
    texts("src/test/resources/xmark.xml");
  }

  /** Test.
   * @throws Exception exception */
  @Test
  public void test8() throws Exception {
    texts("src/test/resources/factbook.zip");
  }

  /**
   * Test on all text nodes of a document.
   * @param file file to be parsed
   * @throws Exception exception
   */
  private void texts(final String file) throws Exception {
    final Context ctx = new Context();
    final String query = "let $doc := doc('" + file + "')" +
      "for $i in $doc//(@hohoho | text()) return data($i)";

    final TokenList tl = new TokenList();
    final TokenBuilder tb = new TokenBuilder();
    final QueryProcessor qp = new QueryProcessor(query, ctx);
    final Iter ir = qp.iter();
    for(Item it; (it = ir.next()) != null;) {
      final byte[] token = it.string(null);
      tl.add(token);
      tb.add(token).add(' ');
    }
    qp.close();

    run(tl.toArray());
    run(tb.finish());
    ctx.close();
  }

  /**
   * Tests the correctness of the compressed tokens.
   * @param tokens test tokens
   */
  private void run(final byte[]... tokens) {
    final Compress comp = new Compress();
    for(final byte[] token : tokens) {
      final byte[] cpr = comp.pack(token);
      if(token != cpr) {
        final byte[] pln = comp.unpack(cpr);
        if(!eq(token, pln)) {
          fail("\n[E] " + Arrays.toString(token) + ",\n[F] " +
              Arrays.toString(pln));
        }
      }
    }
  }
}
