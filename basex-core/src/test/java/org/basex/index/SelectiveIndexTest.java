package org.basex.index;

import static org.junit.Assert.*;

import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.Test;

/**
 * Tests for the selective index feature (#59).
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class SelectiveIndexTest extends SandboxTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/selective.xml";

  /**
   * Tests the text index.
   * @throws BaseXException database exception
   */
  @Test
  public void textIndex() throws BaseXException {
    for(final Map.Entry<String, Integer> entry : map().entrySet()) {
      context.options.set(MainOptions.TEXTINCLUDE, entry.getKey());
      new CreateDB(NAME, FILE).execute(context);
      final int size = context.data().textIndex.size();
      assertEquals("TextIndex: \"" + entry.getKey() + "\": ", entry.getValue().intValue(), size);
    }
  }

  /**
   * Tests the attribute index.
   * @throws BaseXException database exception
   */
  @Test
  public void attrIndex() throws BaseXException {
    try {
      for(final Map.Entry<String, Integer> entry : map().entrySet()) {
        context.options.set(MainOptions.ATTRINCLUDE, entry.getKey());
        new CreateDB(NAME, FILE).execute(context);
        final int size = context.data().attrIndex.size();
        assertEquals("AttrIndex: \"" + entry.getKey() + "\": ", entry.getValue().intValue(), size);
      }
    } finally {
      context.options.set(MainOptions.ATTRINCLUDE, "");
    }
  }

  /**
   * Tests the full-text index.
   * @throws BaseXException database exception
   */
  @Test
  public void ftIndex() throws BaseXException {
    context.options.set(MainOptions.FTINDEX, true);
    try {
      for(final Map.Entry<String, Integer> entry : map().entrySet()) {
        final String key = entry.getKey();
        final int value = entry.getValue();
        context.options.set(MainOptions.FTINCLUDE, key);
        new CreateDB(NAME, FILE).execute(context);
        assertEquals("FTIndex: \"" + key + "\": ", value, context.data().ftxtIndex.size());
      }
    } finally {
      context.options.set(MainOptions.FTINCLUDE, "");
      context.options.set(MainOptions.FTINDEX, false);
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
