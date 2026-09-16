package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Process Module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProcModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void execute() {
    final Function func = _PROC_EXECUTE;
    // queries
    query("exists(" + func.args("java", "x") + "?code)", true);
    query("exists(" + func.args("a b c") + "?error[.])", true);
    query("empty(" + func.args("a b c") + "?code)", true);
    query(func.args("a b c") + "?output", "");

    error(func.args("java", "-version", " { 'encoding': 'xx' }"), PROC_ENCODING_X);
  }

  /** Test method. */
  @Test public void fork() {
    final Function func = _PROC_FORK;
    // queries
    query(func.args("java", "-version"), "");
    query(func.args("a b c"), "");
  }

  /** Test method. */
  @Test public void property() {
    final Function func = _PROC_PROPERTY;
    // queries
    query(func.args("path.separator"), File.pathSeparator);

    Prop.put("A", "B");
    try {
      query(func.args("A"), "B");
      query(func.args("XYZ"), "");
    } finally {
      Prop.clear();
    }
  }

  /** Test method. */
  @Test public void propertyNames() {
    final Function func = _PROC_PROPERTY_NAMES;
    // queries
    // checks if all system properties exist (i.e., have a value)
    query(func.args() + "[empty(" + _PROC_PROPERTY.args(" .") + ")]", "");

    Prop.put("A", "B");
    try {
      query(func.args() + "[. = 'A']", "A");
      query(func.args() + "[. = 'XYZ']", "");
    } finally {
      Prop.clear();
    }
  }

  /** Test method. */
  @Test public void system() {
    final Function func = _PROC_SYSTEM;
    // queries
    query(func.args("java", "-version"), "");
    query("try { " + func.args("java", "x") + "} catch proc:* { 'error' }", "error");

    error(func.args("java", "-version", " { 'encoding': 'xx' }"), PROC_ENCODING_X);
    error(func.args("a b c"), PROC_ERROR_X);
  }

  /**
   * Test method.
   * @throws IOException I/O exception
   */
  @Test public void options() throws IOException {
    final Function func = _PROC_SYSTEM;
    final String echo = echo();

    // encoding of input and output
    query(func.args("java", echo, " { 'input': 'ü' }"), "ü");
    query(func.args("java", echo, " { 'input': 'ü', 'encoding': 'ISO-8859-1' }"), "ü");
    query("string-length(" + func.args("java", echo,
        " { 'input': xs:hexBinary('C3BC'), 'encoding': 'ISO-8859-1' }") + ")", 2);
    // encoding inferred from byte order mark
    query(func.args("java", echo, " { 'input': xs:hexBinary('FEFF0041') }"), "A");
    query(func.args("java", "-version", " { 'input': (), 'dir': (), 'encoding': () }"), "");
    // standard input is closed if no input is supplied
    query(func.args("java", echo, " { 'timeout': 60 }"), "");

    // binary output
    query("string(xs:hexBinary(" + func.args("java", echo,
        " { 'input': xs:hexBinary('610D0A00FF'), 'binary': true() }") + "))", "610D0A00FF");
    query(func.args("java", echo, " { 'binary': true() }") + " instance of xs:base64Binary", true);

    // newlines
    query("string-to-codepoints(" + func.args("java", echo,
        " { 'input': xs:hexBinary('610D0A620D') }") + ")", "97\n10\n98\n10");
    query("string-to-codepoints(" + func.args("java", echo,
        " { 'input': xs:hexBinary('610D0A620D'), 'normalize-newlines': false() }") + ")",
        "97\n13\n10\n98\n13");

    // invalid characters
    error(func.args("java", echo, " { 'input': xs:hexBinary('6100') }"), PROC_ENCODING_X);
    error(func.args("java", echo, " { 'input': xs:hexBinary('61FF') }"), PROC_ENCODING_X);
    query("string-to-codepoints(" + func.args("java", echo,
        " { 'input': xs:hexBinary('6100FF'), 'fallback': true() }") + ")", "97\n65533\n65533");
  }

  /**
   * Test method.
   * @throws IOException I/O exception
   */
  @Test public void executeOptions() throws IOException {
    final Function func = _PROC_EXECUTE;
    final String echo = echo();

    query(func.args("java", echo, " { 'input': 'ab' }") + "?output", "ab");
    query(func.args("java", echo, " { 'input': 'ab' }") + "?code", 0);
    query(func.args("java", echo, " { 'input': 'ab' }") + "?error", "");
    query(func.args("java", echo, " { 'input': 'ab', 'binary': true() }") +
        "?output instance of xs:base64Binary", true);
    // error output is decoded with replacement characters
    query("contains(" + func.args("java", "x") + "?error, 'x')", true);
  }

  /**
   * Test method.
   * @throws IOException I/O exception
   */
  @Test public void timeout() throws IOException {
    // the program writes a file after three seconds
    final IOFile file = new IOFile(sandbox(), "timeout.txt");
    final String program = program("Sleep", "Thread.sleep(3000); " +
      "java.nio.file.Files.writeString(java.nio.file.Path.of(args[0]), \"x\");");
    final String args = " ('" + program + "', '" + file.path() + "')";

    // the process is terminated when the timeout is exceeded
    error(_PROC_SYSTEM.args("java", args, " { 'timeout': 1 }"), PROC_TIMEOUT);
    Performance.sleep(5000);
    assertFalse(file.exists());
  }

  /**
   * Test method.
   * @throws IOException I/O exception
   */
  @Test public void forkInput() throws IOException {
    // the program copies standard input to a file
    final IOFile file = new IOFile(sandbox(), "fork.txt");
    final String program = program("Copy",
      "java.nio.file.Path path = java.nio.file.Path.of(args[0]); " +
      "java.nio.file.Path tmp = java.nio.file.Path.of(args[0] + \".tmp\"); " +
      "java.nio.file.Files.write(tmp, System.in.readAllBytes()); " +
      "java.nio.file.Files.move(tmp, path);");
    final String args = " ('" + program + "', '" + file.path() + "')";

    query(_PROC_FORK.args("java", args, " { 'input': 'abc' }"), "");
    for(int i = 0; i < 300 && !file.exists(); i++) Performance.sleep(100);
    assertEquals("abc", Token.string(file.read()));
  }

  /**
   * Creates a Java program that copies standard input to standard output.
   * @return path to the program
   * @throws IOException I/O exception
   */
  private static String echo() throws IOException {
    return program("Echo", "System.in.transferTo(System.out);");
  }

  /**
   * Creates a Java program.
   * @param name class name
   * @param body body of the main method
   * @return path to the program
   * @throws IOException I/O exception
   */
  private static String program(final String name, final String body) throws IOException {
    final IOFile file = new IOFile(sandbox(), name + ".java");
    file.write("class " + name + " { public static void main(String[] args) throws Exception { " +
      body + " } }");
    return file.path();
  }
}
