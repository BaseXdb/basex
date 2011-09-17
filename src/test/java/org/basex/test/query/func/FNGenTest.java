package org.basex.test.query.func;

import static org.junit.Assert.*;
import static org.basex.util.Token.*;
import org.basex.core.Prop;
import org.basex.io.IOFile;
import org.basex.query.func.Function;
import org.basex.query.util.Err;
import org.basex.test.query.AdvancedQueryTest;
import org.basex.util.Util;
import org.junit.Test;

/**
 * This class tests the functions of the <code>FNGen</code> class.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNGenTest extends AdvancedQueryTest {
  /** Test database name. */
  private static final String NAME = Util.name(FNGenTest.class);
  /** Text file. */
  private static final String TEXT = "etc/test/input.xml";

  /**
   * Test method for the fn:unparsed-text() function.
   * @throws Exception exception
   */
  @Test
  public void fnUnparsedText() throws Exception {
    final String fun = check(Function.PARSETXT);
    contains(fun + "('" + TEXT + "')", "?&gt;&lt;html");
    contains(fun + "('" + TEXT + "', 'US-ASCII')", "?&gt;&lt;html");
    final IOFile io = new IOFile(Prop.TMP, NAME);
    io.write(token("A\r\nB"));
    assertEquals(query("string-length(" + fun + "('" + io.path() + "'))"), "3");
    io.write(token("A\nB"));
    assertEquals(query("string-length(" + fun + "('" + io.path() + "'))"), "3");
    io.write(token("A\rB"));
    assertEquals(query("string-length(" + fun + "('" + io.path() + "'))"), "3");
    io.write(token("A\r\nB\rC\nD"));
    assertEquals(query("util:to-bytes(" + fun + "('" + io.path() + "'))"),
        "65 10 66 10 67 10 68");
    assertTrue(io.delete());
    error(fun + "('" + TEXT + "', 'xyz')", Err.WHICHENC);
  }

  /**
   * Test method for the fn:parse-xml() function.
   */
  @Test
  public void fnParseXML() {
    final String fun = check(Function.PARSEXML);
    contains(fun + "('<x>a</x>')//text()", "a");
  }

  /**
   * Test method for the fn:serialize() function.
   */
  @Test
  public void fnSerialize() {
    final String fun = check(Function.SERIALIZE);
    contains(fun + "(<x/>)", "&lt;x/&gt;");
    contains(fun + "(<x/>, " + serialParams("") + ")", "&lt;x/&gt;");
    contains(fun + "(<x>a</x>, " +
        serialParams("<method value='text'/>") + ")", "a");
  }
}
