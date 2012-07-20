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
      final StringList missing = new StringList();
      final StringList obsolete = new StringList();
      if(n.endsWith(".bat")) libraries(n, libs, missing, obsolete);
      if(!missing.isEmpty()) {
        final StringBuilder sb = new StringBuilder();
        for(final String l : missing) sb.append(';').append(l);
        fail("Library not found in '" + n + "':\n" + sb.substring(1));
      }
      if(!obsolete.isEmpty()) {
        final StringBuilder sb = new StringBuilder();
        for(final String l : obsolete) sb.append(';').append(l);
        fail("Library obsolete in '" + n + "':\n" + sb.substring(1));
      }
    }
  }

  /**
   * Tests the library references in the Windows script.
   * @param name script name
   * @param libs libraries
   * @param missing missing libraries
   * @param obsolete obsolete libraries
   * @throws Exception exception
   */
  private void libraries(final String name, final HashSet<String> libs,
      final StringList missing, final StringList obsolete) throws Exception {

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

    for(final String l : libs) {
      if(l.contains("basex")) continue;
      if(!sl.remove(l)) missing.add(l);
    }
    for(final String l : sl) {
      if(l.endsWith(".jar")) obsolete.add(l);
    }
  }
}
