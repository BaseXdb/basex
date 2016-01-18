package org.basex.index;

import static org.junit.Assert.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.Test;

/**
 * Tests for the selective index feature (#59).
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class SelectiveIndexTest extends SandboxTest {
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
