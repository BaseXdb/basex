package org.basex.test.query;

import static org.junit.Assert.*;
import static org.basex.util.Token.*;
import java.nio.charset.Charset;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.XQuery;
import org.junit.Test;

/**
 * This class tests String <-> Token conversions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Leo Woerteler
 */
public class UTF8Test {
  /** Database context. */
  private static Context context = new Context();

  /** Codepoint of the MUSICAL SYMBOL PARENTHESIS NOTEHEAD. */
  private static final int CP = 0x1D156;

  /** UTF16 chars of the codepoint. */
  private static final char[] CA = Character.toChars(CP);

  /** String containing the codepoint. */
  private static final String STR = new String(CA);

  /** UTF-8 bytes of the codepoint. */
  private static final byte[] BA = STR.getBytes(Charset.availableCharsets().get(
      UTF8));

  /** Tests is characters outside of BMP survive token-creation. */
  @Test
  public final void surrogates() {
    assertEquals(STR, string(token(STR)));
  }

  /** Tests entity parsing with big codepoints. */
  @Test
  public final void surrogates2() {
    assertEquals(STR, query(String.format("'&#x%06x;'", CP)));
  }

  /** Tests string creation from bytes. */
  @Test
  public final void surrogates3() {
    assertEquals(STR, string(BA));
  }

  /** Tests reading a big codepoint from a token. */
  @Test
  public final void surrogates4() {
    assertEquals(CP, cp(BA, 0));
  }

  /**
   * Executes a query and returns the serialized result.
   * @param xq query
   * @return result
   */
  private static String query(final String xq) {
    try {
      return new XQuery(xq).execute(context);
    } catch(BaseXException e) {
      fail(e.getMessage());
      return null; // never reached
    }
  }
}
