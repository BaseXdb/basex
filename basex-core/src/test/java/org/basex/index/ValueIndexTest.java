package org.basex.index;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.index.query.*;
import org.basex.index.value.*;
import org.junit.*;
import org.junit.Test;

/**
 * Tests for the value index.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Jens Erat
 */
public final class ValueIndexTest extends SandboxTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/test.xml";

  /** Set-up database. */
  @Before
  public void setUp() {
    execute(new CreateDB(NAME, FILE));
  }

  /**
   * Tests the text index.
   */
  @Test
  public void textIndexTest() {
    final LinkedHashMap<String, Integer> tokens = new LinkedHashMap<>();
    tokens.put("3", 3);
    tokens.put("3.4", 1);
    tokens.put("text in child", 1);
    tokens.put("nonexistant", 0);

    valueIndexTest(IndexType.TEXT, tokens);
  }

  /**
   * Tests the text index.
   */
  @Test
  public void attributeIndexTest() {
    final LinkedHashMap<String, Integer> tokens = new LinkedHashMap<>();
    tokens.put("context", 1);
    tokens.put("baz bar blu", 1);
    tokens.put("baz", 0);
    tokens.put("bar", 0);
    tokens.put("blu", 0);
    tokens.put("nonexistant", 0);

    valueIndexTest(IndexType.ATTRIBUTE, tokens);
  }

  /**
   * Tests the index: fetch results for different tokens, compare whether the right node was
   * returned and verify against the expected result size.
   * @param indexType text or attribute index
   * @param tokens map of search tokens
   */
  private void valueIndexTest(final IndexType indexType, final LinkedHashMap<String,
      Integer> tokens) {
    boolean text = IndexType.TEXT == indexType;
    ValueIndex index = text ? context.data().textIndex : context.data().attrIndex;
    for(final Entry<String, Integer> entry : tokens.entrySet()) {
      byte[] token = token(entry.getKey());
      IndexIterator it = index.iter(new IndexEntries(token, indexType));
      long count = 0;
      while(it.more()) {
        int pre = it.pre();
        assertEquals("Wrong result returned!", entry.getKey(),
            string(context.data().text(pre, text)));
        count++;
      }
      assertEquals("Wrong number of nodes returned: \"" + entry.getKey() + "\": ",
          (long) entry.getValue(), count);
    }
  }

}
