package org.basex.test.util;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.nio.charset.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.test.*;
import org.junit.*;

/**
 * This class tests String <-> Token conversions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class UTF8Test extends SandboxTest {
  /** UTF8 character set. */
  private static final Charset CS_UTF8 = Charset.availableCharsets().get(UTF8);

  /**
   * Tests for all valid code points if characters are properly converted.
   */
  @Test
  public void string2token2string() {
    for(int i = 0; i <= Character.MAX_CODE_POINT; i++) {
      final String s = new String(Character.toChars(i));
      assertEquals(s, string(token(s)));
    }
  }

  /**
   * Tests for all valid code points if characters are properly converted.
   */
  @Test
  public void token2string() {
    for(int i = 0; i <= Character.MAX_CODE_POINT; i++) {
      if(i >= Character.MIN_SURROGATE && i <= Character.MAX_SURROGATE) continue;
      final String s = new String(Character.toChars(i));
      assertEquals(s, string(s.getBytes(CS_UTF8)));
    }
  }

  /**
   * Tests entity parsing with codepoints.
   * @throws BaseXException database exception
   */
  @Test
  public void entities() throws BaseXException {
    for(int i = 0xA0; i <= Character.MAX_CODE_POINT; i++) {
      final String qu = new XQuery("'&#" + i + ";'").execute(context);
      assertEquals(new String(Character.toChars(i)), qu);
      if(i == 0x400) i = 0xFFF;
      else if(i == 0x1400) i = 0xFFFF;
      else if(i == 0x10400) i = 0x10FBFF;
    }
  }
}
