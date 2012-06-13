package org.basex.test;

import static org.junit.Assert.*;

import java.util.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.junit.*;

/**
 * Tests if Windows scripts are valid.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class WindowsScripts {
  /**
   * Tests the library references in the Windows script.
   * @throws Exception exception
   */
  @Test
  public void libraries() throws Exception {
    final HashSet<String> libs = new HashSet<String>();
    for(final String s : new String[] { "lib" }) {
      for(final IOFile f : new IOFile(s).children()) libs.add(f.name());
    }

    if(libs.isEmpty()) {
      System.err.println("WindowsScripts: no library files found.");
    } else {
      for(final IOFile f : new IOFile("etc").children()) {
        final String n = f.name();
        if(n.endsWith(".bat")) libraries(n, libs);
      }
    }
  }

  /**
   * Tests the library references in the Windows script.
   * @param name script name
   * @param libs libraries
   * @throws Exception exception
   */
  private void libraries(final String name, final HashSet<String> libs) throws Exception {
    final HashSet<String> sl = new HashSet<String>();
    final NewlineInput nli = new NewlineInput(IO.get("etc/" + name));
    for(String s; (s = nli.readLine()) != null;) {
      for(final String p : s.split(";")) {
        if(p.contains("%LIB%")) sl.add(p.replace("%LIB%/", ""));
      }
    }
    nli.close();

    for(final String l : libs) {
      if(l.contains("basex")) continue;
      assertTrue("Library not found in '" + name + "': " + l, sl.remove(l));
    }
    final StringBuilder sb = new StringBuilder();
    for(final String l : sl) sb.append("\n- ").append(l);
    assertTrue("Libraries superfluous in '" + name + "':" + sb, sl.isEmpty());
  }
}
