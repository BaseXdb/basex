package org.basex;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.core.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests the command-line arguments of the starter class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class BaseXTest extends MainTest {
  /**
   * Deletes the test files.
   * @throws IOException I/O exception
   */
  @AfterEach public void clean() throws IOException {
    assertTrue(!INPUT.exists() || INPUT.delete(), "Could not delete input file.");
    run("-cdrop db " + NAME);
  }

  /**
   * Tests a query file.
   * @throws IOException I/O exception
   */
  @Test public void queryFile() throws IOException {
    final String query = "1";
    INPUT.write(query);
    equals(query, INPUT.path());
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
    equals("8", "-b{}a=8", "-qdeclare variable $a external; $a");
    equals("9", "-b{URI}a=9", "-qdeclare namespace a='URI'; declare variable $a:a external; $a:a");

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
    equals("1", "-cxquery 1");
    equals("\t", "-cxquery '&#x9;'");
  }

  /**
   * Command error.
   */
  @Test public void commandErr() {
    assertThrows(BaseXException.class, () -> run("-1"));
  }

  /**
   * Tests command scripts.
   * @throws IOException I/O exception
   */
  @Test public void commands() throws IOException {
    INPUT.write("xquery 1" + Prop.NL + "xquery 2" + Prop.NL);
    equals("12", "-c" + INPUT.path());
  }

  /**
   * Test query evaluation.
   * @throws IOException I/O exception
   */
  @Test public void query() throws IOException {
    equals("3", "-q1+2");
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
   * Turn off whitespace chopping.
   * @throws IOException I/O exception
   */
  @Test public void chop() throws IOException {
    final String in = "<a> CHOP </a>";
    INPUT.write(in);
    equals(in, "-w", "-i" + INPUT, "-q.");
  }

  /**
   * Turn off serialization.
   * @throws IOException I/O exception
   */
  @Test public void noSerialization() throws IOException {
    equals("", "-z", "-q1");
  }
}
