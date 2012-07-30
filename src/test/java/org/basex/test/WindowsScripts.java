package org.basex.test;

import static org.junit.Assert.*;

import java.util.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.list.*;
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
    for(final String s : new String[] { "lib", "../basex/lib" }) {
      final int l = libs.size();
      for(final IOFile f : new IOFile(s).children()) libs.add(f.name());
      if(l == libs.size())
        Util.errln(Util.name(this) + ": test skipped (no library files found)");
    }

    for(final IOFile f : new IOFile("etc").children()) {
      final String n = f.name();
      if(n.endsWith(".bat") && !libraries(n, libs)) {
        final TokenBuilder tb = new TokenBuilder("set CP=%CP%");
        for(final IOFile l : new IOFile("lib").children()) {
          if(!l.name().contains("basex")) tb.add(";%LIB%/").add(l.name());
        }
        Util.errln(tb.toString());
        fail(n + ": see STDERR output");
      }
    }
  }

  /**
   * Tests the library references in the Windows script.
   * @param name script name
   * @param libs libraries
   * @return result of check
   * @throws Exception exception
   */
  private boolean libraries(final String name, final HashSet<String> libs)
      throws Exception {

    final HashSet<String> sl = new HashSet<String>();
    final NewlineInput nli = new NewlineInput(IO.get("etc/" + name));
    try {
      for(String s; (s = nli.readLine()) != null;) {
        for(final String p : s.split(";")) {
          if(p.contains("%LIB%")) sl.add(p.replace("%LIB%/", ""));
        }
      }
    } finally {
      nli.close();
    }

    final StringList mis = new StringList();
    for(final String l : libs) {
      if(!l.contains("basex") && !sl.remove(l)) mis.add(l);
    }
    final StringList obs = new StringList();
    for(final String l : sl) {
      if(l.endsWith(".jar")) obs.add(l);
    }
    if(!mis.isEmpty()) Util.errln("Missing: " + Arrays.toString(mis.toArray()));
    if(!obs.isEmpty()) Util.errln("Obsolete: " + Arrays.toString(obs.toArray()));
    return mis.isEmpty() && obs.isEmpty();
  }
}
