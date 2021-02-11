package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Process Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ProcModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void execute() {
    final Function func = _PROC_EXECUTE;
    // queries
    query("exists(" + func.args("java", "x") + "/code)", true);
    query("exists(" + func.args("a b c") + "/error)", true);
    query("empty(" + func.args("a b c") + "/(output, code))", true);

    error(func.args("java", "-version", " map { 'encoding': 'xx' }"), PROC_ENCODING_X);
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

    error(func.args("java", "-version", " map { 'encoding': 'xx' }"), PROC_ENCODING_X);
    error(func.args("a b c"), PROC_ERROR_X);
  }
}
