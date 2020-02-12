package org.basex.index;

import static org.junit.Assert.*;

import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.Test;

/**
 * Storage tests for the selective index feature (#59).
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class SelectiveIndexTest extends SandboxTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/selective.xml";

  /**
   * Tests the text index.
   */
  @Test public void textIndex() {
    map().forEach((key, value) -> {
      set(MainOptions.TEXTINCLUDE, key);
      execute(new CreateDB(NAME, FILE));
      final int size = context.data().textIndex.size();
      assertEquals("TextIndex: \"" + key + "\": ", value.intValue(), size);
    });
  }

  /**
   * Tests the attribute index.
   */
  @Test public void attrIndex() {
    try {
      map().forEach((key, value) -> {
        set(MainOptions.ATTRINCLUDE, key);
        execute(new CreateDB(NAME, FILE));
        final int size = context.data().attrIndex.size();
        assertEquals("AttrIndex: \"" + key + "\": ", value.intValue(), size);
      });
    } finally {
      set(MainOptions.ATTRINCLUDE, "");
    }
  }

  /**
   * Tests the token index.
   */
  @Test public void tokenIndex() {
    set(MainOptions.TOKENINDEX, true);
    try {
      map().forEach((key, value) -> {
        set(MainOptions.TOKENINCLUDE, key);
        execute(new CreateDB(NAME, FILE));
        final int size = context.data().tokenIndex.size();
        assertEquals("TokenIndex: \"" + key + "\": ", value.intValue(), size);
      });
    } finally {
      set(MainOptions.TOKENINCLUDE, "");
      set(MainOptions.TOKENINDEX, false);
    }
  }

  /**
   * Tests the full-text index.
   */
  @Test public void ftIndex() {
    set(MainOptions.FTINDEX, true);
    try {
      map().forEach((key, value) -> {
        set(MainOptions.FTINCLUDE, key);
        execute(new CreateDB(NAME, FILE));
        assertEquals("FTIndex: \"" + key + "\": ", (int) value, context.data().ftIndex.size());
      });
    } finally {
      set(MainOptions.FTINCLUDE, "");
      set(MainOptions.FTINDEX, false);
    }
  }

  /**
   * Tests the id functions.
   */
  @Test public void id() {
    set(MainOptions.TOKENINDEX, true);
    try {
      final String idref = "idref=\"B C\"";
      final String file = "<xml id=\"A\" " + idref + "/>";
      final String[] includes = {
        "", "*", "Q{}*", "Q{id}",
        "Q{}id", "id", "*:id", "Q{}idref", "idref", "*:idref",
        "Q{}x", "Q{}x", "x", "*:x", "Q{}x",
        // does not work, as it would currently indicate that idref is included in the index
        "Q{}ref", "ref", "*:ref",
      };
      for(final String include : includes) {
        try {
          set(MainOptions.ATTRINCLUDE, include);
          set(MainOptions.TOKENINCLUDE, include);
          execute(new CreateDB(NAME, file));
          query("id('A', .)", file);
          query("idref('B', .)", idref);
        } catch(final AssertionError ae) {
          throw new AssertionError(ae.getMessage() + "\nInclude: '" + include + '\'');
        }
      }
    } finally {
      set(MainOptions.ATTRINCLUDE, "");
      set(MainOptions.TOKENINCLUDE, "");
      set(MainOptions.TOKENINDEX, false);
    }
  }

  /**
   * Returns a map with name tests.
   * @return map
   */
  private static HashMap<String, Integer> map() {
    final LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
    map.put("", 5);
    map.put("*", 5);
    map.put("*:*", 5);
    map.put("a", 1);
    map.put("*:c", 2);
    map.put("Q{ns}*", 2);
    map.put("Q{ns}c", 1);
    return map;
  }
}
