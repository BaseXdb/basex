package org.basex.index;

import static org.junit.Assert.*;

import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.*;
import org.junit.Test;

/**
 * Storage tests for the selective index feature (#59).
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class SelectiveIndexTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/selective.xml";

  /**
   * Tests the text index.
   */
  @Test
  public void textIndex() {
    for(final Entry<String, Integer> entry : map().entrySet()) {
      set(MainOptions.TEXTINCLUDE, entry.getKey());
      execute(new CreateDB(NAME, FILE));
      final int size = context.data().textIndex.size();
      assertEquals("TextIndex: \"" + entry.getKey() + "\": ", entry.getValue().intValue(), size);
    }
  }

  /**
   * Tests the attribute index.
   */
  @Test
  public void attrIndex() {
    try {
      for(final Entry<String, Integer> entry : map().entrySet()) {
        set(MainOptions.ATTRINCLUDE, entry.getKey());
        execute(new CreateDB(NAME, FILE));
        final int size = context.data().attrIndex.size();
        assertEquals("AttrIndex: \"" + entry.getKey() + "\": ", entry.getValue().intValue(), size);
      }
    } finally {
      set(MainOptions.ATTRINCLUDE, "");
    }
  }

  /**
   * Tests the token index.
   */
  @Test
  public void tokenIndex() {
    set(MainOptions.TOKENINDEX, true);
    try {
      for(final Entry<String, Integer> entry : map().entrySet()) {
        set(MainOptions.TOKENINCLUDE, entry.getKey());
        execute(new CreateDB(NAME, FILE));
        final int size = context.data().tokenIndex.size();
        assertEquals("TokenIndex: \"" + entry.getKey() + "\": ", entry.getValue().intValue(), size);
      }
    } finally {
      set(MainOptions.TOKENINCLUDE, "");
      set(MainOptions.TOKENINDEX, false);
    }
  }

  /**
   * Tests the full-text index.
   */
  @Test
  public void ftIndex() {
    set(MainOptions.FTINDEX, true);
    try {
      for(final Entry<String, Integer> entry : map().entrySet()) {
        final String key = entry.getKey();
        final int value = entry.getValue();
        set(MainOptions.FTINCLUDE, key);
        execute(new CreateDB(NAME, FILE));
        assertEquals("FTIndex: \"" + key + "\": ", value, context.data().ftIndex.size());
      }
    } finally {
      set(MainOptions.FTINCLUDE, "");
      set(MainOptions.FTINDEX, false);
    }
  }

  /**
   * Tests the id functions.
   */
  @Test
  public void id() {
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
