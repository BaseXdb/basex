package org.basex.util;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests String <-> Token conversions.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class UTF8Test extends SandboxTest {
  /**
   * Tests for all valid code points if characters are properly converted.
   */
  @Test public void string2token2string() {
    for(int cp = 0; cp <= Character.MAX_CODE_POINT; cp++) {
      if(!Character.isSurrogate((char) cp)) {
        final String s = new String(Character.toChars(cp));
        assertEquals(s, string(token(s)));
      }
    }
  }

  /**
   * Tests entity parsing with codepoints.
   */
  @Test public void entities() {
    for(int cp = 0xA0; cp <= Character.MAX_CODE_POINT; cp++) {
      final String qu = query("'&#" + cp + ";'");
      assertEquals(new String(Character.toChars(cp)), qu);
      if(cp == 0x400) cp = 0xFFF;
      else if(cp == 0x1400) cp = 0xFFFF;
      else if(cp == 0x10400) cp = 0x10FBFF;
    }
  }
}
