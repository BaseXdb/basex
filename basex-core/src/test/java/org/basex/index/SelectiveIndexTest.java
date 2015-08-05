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

  /** Map with index filters. */
  private static final LinkedHashMap<String, Integer> FILTERS = new LinkedHashMap<>();

  static {
    FILTERS.put("", 5);
    FILTERS.put("*", 5);
    FILTERS.put("*:*", 5);
    FILTERS.put("a", 1);
    FILTERS.put("*:c", 2);
    FILTERS.put("Q{ns}*", 2);
    FILTERS.put("Q{ns}c", 1);
  }

  /**
   * Tests the text index.
   * @throws BaseXException database exception
   */
  @Test
  public void textIndex() throws BaseXException {
    try {
      for(final Map.Entry<String, Integer> entry : FILTERS.entrySet()) {
        context.options.set(MainOptions.TEXTINCLUDE, entry.getKey());
        new CreateDB(NAME, FILE).execute(context);
        final int size = context.data().textIndex.size();
        assertEquals("TextIndex: \"" + entry.getKey() + "\": ", entry.getValue().intValue(), size);
      }
    } finally {
      context.options.set(MainOptions.TEXTINCLUDE, "");
    }
  }

  /**
   * Tests the attribute index.
   * @throws BaseXException database exception
   */
  @Test
  public void attrIndex() throws BaseXException {
    try {
      for(final Map.Entry<String, Integer> entry : FILTERS.entrySet()) {
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
      for(final Map.Entry<String, Integer> entry : FILTERS.entrySet()) {
        context.options.set(MainOptions.FTINCLUDE, entry.getKey());
        new CreateDB(NAME, FILE).execute(context);
        final int size = context.data().ftxtIndex.size();
        assertEquals("FTIndex: \"" + entry.getKey() + "\": ", entry.getValue().intValue(), size);
      }
    } finally {
      context.options.set(MainOptions.FTINDEX, false);
      context.options.set(MainOptions.FTINCLUDE, "");
    }
  }
}
