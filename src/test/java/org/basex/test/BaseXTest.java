package org.basex.test;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.basex.BaseX;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.io.IOFile;
import org.basex.util.Util;
import org.junit.After;
import org.junit.Test;

/**
 * Tests the command-line arguments of the standalone starter class.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class BaseXTest {
  /** Test database name. */
  private static final String DB = Util.name(BaseXTest.class);
  /** Output file. */
  private static final IOFile OUT = new IOFile(Prop.TMP + DB + ".out");
  /** Input file. */
  private static final IOFile IN = new IOFile(Prop.TMP + DB + ".in");

  /** Delete the test files. */
  @After
  public void clean() {
    assertTrue("Could not delete input file.", !IN.exists() || IN.delete());
    assertTrue("Could not delete output file.", !OUT.exists() || OUT.delete());
  }

  /**
   * Tests q query file.
   * @throws IOException I/O exception
   */
  @Test
  public void queryFile() throws IOException {
    final String query = "1";
    IN.write(token(query));
    new BaseX("-o" + OUT + " " + IN);
    assertEquals(query, string(OUT.read()));
  }

  /**
   * Test variable bindings.
   * @throws IOException I/O exception
   */
  @Test
  public void bind() throws IOException {
    IN.write(token("$a"));
    new BaseX("-ba=1 -o" + OUT + " " + IN);
    assertEquals("1", string(OUT.read()));

    IN.write(token("$a + $b"));
    new BaseX("-ba=1 -bb=2 -o" + OUT + " " + IN);
    assertEquals("3", string(OUT.read()));

    new BaseX("-ba=1,b=2 -o" + OUT + " " + IN);
    assertEquals("3", string(OUT.read()));
  }

  /**
   * Test variable bindings.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public void bindErr() throws IOException {
    IN.write(token("declare variable $a as xs:integer external; $a"));
    new BaseX("-ba=A -o" + OUT + " " + IN);
  }

  /**
   * Test command execution.
   * @throws IOException I/O exception
   */
  @Test
  public void command() throws IOException {
    new BaseX("-o" + OUT + " -cxquery 1");
    assertEquals("1", string(OUT.read()));
  }

  /**
   * Command error.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public void commandErr() throws IOException {
    new BaseX("-c1");
  }

  /**
   * Test command execution.
   * @throws IOException I/O exception
   */
  @Test
  public void input() throws IOException {
    final String in = "<a/>";
    final Context ctx = new Context();
    new CreateDB(DB, in).execute(ctx);
    new BaseX("-i" + DB + " -o" + OUT + " -q.");
    assertEquals(in, string(OUT.read()));
    ctx.close();
  }

  /**
   * Test query evaluation.
   * @throws IOException I/O exception
   */
  @Test
  public void query() throws IOException {
    new BaseX("-o" + OUT + " -q1+2");
    assertEquals("3", string(OUT.read()));
  }

  /**
   * Query error.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public void queryErr() throws IOException {
    new BaseX("-q1+");
  }

  /**
   * Test different number of runs.
   * @throws IOException I/O exception
   */
  @Test
  public void runs() throws IOException {
    new BaseX("-r10 -o" + OUT + " -q2");
    assertEquals("2", string(OUT.read()));
  }

  /**
   * Test different number of runs.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public void runErr() throws IOException {
    new BaseX("-rx -o" + OUT + " -q2");
  }

  /**
   * Test serialization parameters.
   * @throws IOException I/O exception
   */
  @Test
  public void serial() throws IOException {
    new BaseX("-smethod=text -o" + OUT + " -q<a>1</a>");
    assertEquals("1", string(OUT.read()));
  }

  /**
   * Test serialization parameters.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public void serialErr() throws IOException {
    new BaseX("-sm=x -o" + OUT + " -q2");
  }

  /**
   * Write back updates.
   * @throws IOException I/O exception
   */
  @Test
  public void writeBack() throws IOException {
    IN.write(token("<a>X</a>"));
    new BaseX("-i " + IN + " -u -qdelete node //text() ");
    assertEquals("<a/>", string(IN.read()));
  }

  /**
   * Turn off whitespace chopping.
   * @throws IOException I/O exception
   */
  @Test
  public void chop() throws IOException {
    final String in = "<a> X </a>";
    IN.write(token(in));
    new BaseX("-w -i" + IN + " -o" + OUT + " -q.");
    assertEquals(in, string(OUT.read()));
  }

  /**
   * Turn off serialization.
   * @throws IOException I/O exception
   */
  @Test
  public void noSerialization() throws IOException {
    new BaseX("-z -o" + OUT + " -q1");
    assertEquals("", string(OUT.read()));
  }
}
