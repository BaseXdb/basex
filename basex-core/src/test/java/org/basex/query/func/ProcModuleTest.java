package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.query.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the functions of the Process Module.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ProcModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void system() {
    query(_PROC_SYSTEM.args("java", "-version"), "");
    error(_PROC_SYSTEM.args("java", "-version", " map {'encoding': 'xx'}"), PROC_ENCODING_X);
    query("try { " + _PROC_SYSTEM.args("a b c") + "} catch proc:code9999 {}", "");
  }

  /** Test method. */
  @Test
  public void execute() {
    query("count(" + _PROC_EXECUTE.args("java", "-version") + "/*)", 3);
    query(_PROC_EXECUTE.args("a b c") + "/code/text()", 9999);
  }

  /** Test method. */
  @Test
  public void fork() {
    query(_PROC_FORK.args("java", "-version"), "");
    query(_PROC_FORK.args("a b c"), "");
  }

  /** Test method. */
  @Test
  public void property() {
    query(_PROC_PROPERTY.args("path.separator"), File.pathSeparator);

    Prop.put("A", "B");
    try {
      query(_PROC_PROPERTY.args("A"), "B");
      query(_PROC_PROPERTY.args("XYZ"), "");
    } finally {
      Prop.clear();
    }
  }

  /** Test method. */
  @Test
  public void propertyNames() {
    // checks if all system properties exist (i.e., have a value)
    query(_PROC_PROPERTY_NAMES.args() + "[empty(" + _PROC_PROPERTY.args(" .") + ")]", "");

    Prop.put("A", "B");
    try {
      query(_PROC_PROPERTY_NAMES.args() + "[. = 'A']", "A");
      query(_PROC_PROPERTY_NAMES.args() + "[. = 'XYZ']", "");
    } finally {
      Prop.clear();
    }
  }
}
