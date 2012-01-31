package org.basex.test;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Text;
import org.basex.core.cmd.CreateDB;
import org.junit.After;
import org.junit.Test;

/**
 * Tests the command-line arguments of the starter class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class BaseXTest extends MainTest {
  /** Delete the test files. */
  @After
  public void clean() {
    assertTrue("Could not delete input file.", !IN.exists() || IN.delete());
  }

  /**
   * Tests a query file.
   * @throws IOException I/O exception
   */
  @Test
  public void queryFile() throws IOException {
    final String query = "1";
    IN.write(token(query));
    equals(query, IN.path());
  }

  /**
   * Test variable bindings.
   * @throws IOException I/O exception
   */
  @Test
  public void bind() throws IOException {
    equals("1", "-ba=1", "-q$a");
    equals("2", "-ba=1", "-bb=1", "-q$a+$b");
    equals("3", "-ba=1", "-bb=2", "-q$a+$b");
    IN.write(token("$a"));
    equals("4", "-ba=4", IN.toString());
    equals("5,6;7'", "-ba=5,6;7'", "-q$a");
    // bind quote (to be checked in client/server mode)
    //equals("\"", "-ba=\"", "-q$a");
    // bind variables with namespaces
    equals("8", "-b{}a=8", "-q$a");
    equals("9", "-b'':a=9", "-q$a");
    equals("A", "-b{URI}a=A", "-qdeclare namespace a='URI'; $a:a");
    equals("B", "-b'URI':b=B", "-qdeclare namespace b='URI'; $b:b");
  }

  /**
   * Test variable bindings.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public void bindErr() throws IOException {
    run("-ba=A", "-qdeclare variable $a as xs:integer external; $a");
  }

  /**
   * Test variable bindings with namespaces.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public void bindNSErr() throws IOException {
    run("X'\"", "-b{URI}ln=X'\"", IN.toString());
  }

  /**
   * Test command execution.
   * @throws IOException I/O exception
   */
  @Test
  public void input() throws IOException {
    final String in = "<a/>";
    final Context ctx = new Context();
    new CreateDB(NAME, in).execute(ctx);
    equals(in, "-i" + NAME, "-q.");
    ctx.close();
  }

  /**
   * Test command execution.
   * @throws IOException I/O exception
   */
  @Test
  public void command() throws IOException {
    equals("1", "-cxquery 1");
  }

  /**
   * Command error.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public void commandErr() throws IOException {
    run("-1");
  }

  /**
   * Test query evaluation.
   * @throws IOException I/O exception
   */
  @Test
  public void query() throws IOException {
    equals("3", "-q1+2");
  }

  /**
   * Query error.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public void queryErr() throws IOException {
    run("-q1+");
  }

  /**
   * Test different number of runs.
   * @throws IOException I/O exception
   */
  @Test
  public void runs() throws IOException {
    equals("2", "-r10", "-q2");
  }

  /**
   * Test different number of runs.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public void runErr() throws IOException {
    run("-rx", "-q2");
  }

  /**
   * Test serialization parameters.
   * @throws IOException I/O exception
   */
  @Test
  public void serial() throws IOException {
    equals("1", "-smethod=text", "-q<a>1</a>");
  }

  /**
   * Test verbose mode.
   * @throws IOException I/O exception
   */
  @Test
  public void verbose() throws IOException {
    contains(Text.QUERY_EXECUTED_X.replaceAll(" %.*", ""), "-v", "-q1");
    contains(Text.TOTAL_TIME_CC, "-V", "-q1");
  }

  /**
   * Test serialization parameters.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public void serialErr() throws IOException {
    run("-sm=x", "-q2");
  }

  /**
   * Turn off whitespace chopping.
   * @throws IOException I/O exception
   */
  @Test
  public void chop() throws IOException {
    final String in = "<a> X </a>";
    IN.write(token(in));
    equals(in, "-w", "-i" + IN, "-q.");
  }

  /**
   * Turn off serialization.
   * @throws IOException I/O exception
   */
  @Test
  public void noSerialization() throws IOException {
    equals("", "-z", "-q1");
  }
}
