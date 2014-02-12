package org.basex.util.options;

import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Tests on options.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class OptionsTest extends SandboxTest {
  /** Tests the effect of setting system properties. */
  @Test
  public void systemProperty() {
    final BooleanOption name = MainOptions.CHOP;
    final Boolean value = Boolean.FALSE;
    System.setProperty(Prop.DBPREFIX + name.name(), value.toString());
    try {
      assertEquals(value, new MainOptions().get(name));
    } finally {
      System.clearProperty(Prop.DBPREFIX + name.name());
    }
  }

  /**
   * Tests the {@link MainOptions#WRITEBACK} option.
   * @throws Exception exception
   */
  @Test
  public void writeBack() throws Exception {
    final BooleanOption name = MainOptions.WRITEBACK;
    context.options.set(name, true);

    final String input = "<a/>";
    final IOFile file = new IOFile(Prop.TMP + NAME + '/' + NAME);
    file.write(Token.token(input));

    // check if original file will be updated
    try {
      new XQuery("delete node doc('" + file + "')/a").execute(context);
      assertEquals("", Token.string(file.read()));
    } finally {
      context.options.set(name, false);
    }

    // original file will stay untouched
    file.write(Token.token(input));
    new XQuery("delete node doc('" + file + "')/a").execute(context);
    assertEquals(input, Token.string(file.read()));
  }
}
