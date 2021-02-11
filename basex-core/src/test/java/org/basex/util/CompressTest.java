package org.basex.util;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;

/**
 * Class for testing the {@link Compress} methods.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CompressTest extends SandboxTest {
  /** Test. */
  @Test public void test1() {
    run(token(" abcdefghijklmnopqrstuvwxyz"));
  }

  /** Test. */
  @Test public void test2() {
    run(token("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
  }

  /** Test. */
  @Test public void test3() {
    run(token("abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ 1234567890"));
  }

  /** Test. */
  @Test public void test4() {
    final int bl = 65;
    final byte[] bytes = new byte[bl];
    for(int b = 0; b < bl; b++) bytes[b] = (byte) b;
    run(bytes);
  }

  /** Test. */
  @Test public void test5() {
    final int bl = 256;
    final byte[] bytes = new byte[bl];
    for(int b = 0; b < bytes.length; b++) bytes[b] = (byte) b;
    run(bytes);
  }

  /** Test. */
  @Test public void test6() {
    final int bl = 4096;
    final byte[] bytes = new byte[bl];
    for(int b = 0; b < bytes.length; b++) bytes[b] = (byte) (b & 0xFF);
    run(bytes);
  }

  /** Test.
   * @throws Exception exception */
  @Test public void test7() throws Exception {
    texts("src/test/resources/xmark.xml");
  }

  /** Test.
   * @throws Exception exception */
  @Test public void test8() throws Exception {
    texts("src/test/resources/factbook.zip");
  }

  /**
   * Test on all text nodes of a document.
   * @param file file to be parsed
   * @throws Exception exception
   */
  private static void texts(final String file) throws Exception {
    final String query = "let $doc := doc('" + file + "')" +
      "for $i in $doc//(@hohoho | text()) return data($i)";

    final TokenList tl = new TokenList();
    final TokenBuilder tb = new TokenBuilder();
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      final Iter iter = qp.iter();
      for(Item item; (item = iter.next()) != null;) {
        final byte[] token = item.string(null);
        tl.add(token);
        tb.add(token).add(' ');
      }
    }

    run(tl.finish());
    run(tb.finish());
  }

  /**
   * Tests the correctness of the compressed tokens.
   * @param tokens test tokens
   */
  private static void run(final byte[]... tokens) {
    for(final byte[] token : tokens) {
      final byte[] cpr = Compress.pack(token);
      if(token != cpr) {
        final byte[] pln = Compress.unpack(cpr);
        if(!eq(token, pln)) {
          fail("\n[E] " + Arrays.toString(token) + ",\n[F] " + Arrays.toString(pln));
        }
      }
    }
  }
}
