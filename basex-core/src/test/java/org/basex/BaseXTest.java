package org.basex;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests the command-line arguments of the starter class.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class BaseXTest extends SandboxTest {
  /** Input file. */
  static final IOFile INPUT = new IOFile(Prop.TEMPDIR + NAME + ".in");

  /**
   * Deletes the test files.
   * @throws IOException I/O exception
   */
  @AfterEach public void clean() throws IOException {
    assertTrue(!INPUT.exists() || INPUT.delete(), "Could not delete input file.");
    run("-cDROP DB " + NAME);
  }

  /**
   * Test variable bindings.
   * @throws IOException I/O exception
   */
  @Test public void bind() throws IOException {
    equals("1", "-ba=1", "-qdeclare variable $a external; $a");
    equals("2", "-ba=1", "-bb=1",
        "-qdeclare variable $a external; declare variable $b external; $a + $b");
    equals("3", "-ba=1", "-bb=2",
        "-qdeclare variable $a external; declare variable $b external; $a + $b");

    INPUT.write("declare variable $a external; $a");
    equals("4", "-ba=4", INPUT.toString());
    equals("5,6;7'", "-ba=5,6;7'", "-qdeclare variable $a external; $a");

    // bind variables with namespaces
    equals("8", "-bQ{}a=8", "-qdeclare variable $a external; $a");
    equals("9", "-bQ{URI}a=9", "-qdeclare namespace a='URI'; declare variable $a:a external; $a:a");

    // check if parameters are evaluated in given order
    equals("12", "-ba=1", "-qdeclare variable $a external; $a",
        "-ba=2", "-qdeclare variable $a external; $a");
  }

  /**
   * Test variable bindings.
   */
  @Test public void bindErr() {
    assertThrows(BaseXException.class, () ->
      run("-ba=A", "-qdeclare variable $a as xs:integer external; $a"));
  }

  /**
   * Test variable bindings with namespaces.
   */
  @Test public void bindNSErr() {
    assertThrows(BaseXException.class, () -> run("X'\"", "-b{URI}ln=X'\"", INPUT.toString()));
  }

  /**
   * Test command execution.
   * @throws IOException I/O exception
   */
  @Test public void input() throws IOException {
    final String in = "<X/>";
    INPUT.write(in);
    equals(in, "-i" + INPUT, "-q.");
    equals(in, "-i" + in, "-q.");
  }

  /**
   * Test command execution.
   * @throws IOException I/O exception
   */
  @Test public void command() throws IOException {
    equals("1", "-cXQUERY 1");
    equals("\t", "-cXQUERY '&#x9;'");
  }

  /**
   * Command error.
   */
  @Test public void commandErr() {
    assertThrows(BaseXException.class, () -> run("-1"));
  }

  /**
   * Tests commands.
   * @throws IOException I/O exception
   */
  @Test public void commands() throws IOException {
    equals("12", "-c XQUERY 1", "-c XQUERY 2");
  }

  /**
   * Tests command scripts.
   * @throws IOException I/O exception
   */
  @Test public void commandFile() throws IOException {
    INPUT.write("xquery 1" + Prop.NL + "xquery 2" + Prop.NL);
    equals("12", "-C" + INPUT.path());
  }

  /**
   * Test query evaluation.
   * @throws IOException I/O exception
   */
  @Test public void query() throws IOException {
    equals("3", "-q1+2");
  }

  /**
   * Tests a query file.
   * @throws IOException I/O exception
   */
  @Test public void queryFile() throws IOException {
    INPUT.write("1");
    equals("1", "-Q", INPUT.path());
  }

  /**
   * Query error.
   */
  @Test public void queryErr() {
    assertThrows(BaseXException.class, () -> run("-q1+"));
  }

  /**
   * Test different number of runs.
   * @throws IOException I/O exception
   */
  @Test public void runs() throws IOException {
    equals("1", "-r2", "-q1");
  }

  /**
   * Test different number of runs.
   */
  @Test public void runErr() {
    assertThrows(BaseXException.class, () -> run("-rx", "-q2"));
  }

  /**
   * Test serialization parameters.
   * @throws IOException I/O exception
   */
  @Test public void serial() throws IOException {
    equals("1", "-smethod=text", "-q<a>1</a>");
    // check if parameters are evaluated in given order
    equals("1<x>1</x>", "-smethod=text", "-q<x>1</x>", "-smethod=xml", "-q<x>1</x>");
  }

  /**
   * Test verbose mode.
   * @throws IOException I/O exception
   */
  @Test public void verbose() throws IOException {
    contains(Text.QUERY_EXECUTED_X_X.replaceAll(" *%.*", ""), "-v", "-q1");
    contains(Text.TOTAL_TIME_CC, "-V", "-q1");
  }

  /**
   * Turn on whitespace stripping.
   * @throws IOException I/O exception
   */
  @Test public void stripws() throws IOException {
    final String in = "<a> </a>";
    INPUT.write(in);
    equals("<a/>", "-w", "-i" + INPUT, "-q.");
  }

  /**
   * Turn off serialization.
   * @throws IOException I/O exception
   */
  @Test public void noSerialization() throws IOException {
    equals("", "-z", "-q1");
  }
  /**
   * Runs a request with the specified arguments.
   * @param args command-line arguments
   * @return result
   * @throws IOException I/O exception
   */
  protected abstract String run(String... args) throws IOException;

  /**
   * Runs a request and compares the result with the expected result.
   * @param expected expected result
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  final void equals(final String expected, final String... args) throws IOException {
    assertEquals(expected, run(args));
  }

  /**
   * Runs a request and checks if the expected string is contained in the
   * result.
   * @param expected expected result
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  final void contains(final String expected, final String... args) throws IOException {
    final String result = run(args);
    if(!result.contains(expected)) fail('\'' + expected + "' not contained in '" + result + "'.");
  }
}
