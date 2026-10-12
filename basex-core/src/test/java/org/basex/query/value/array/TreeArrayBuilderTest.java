package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link TreeArrayBuilder}.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class TreeArrayBuilderTest extends ArrayTest {
  /** Tests building arrays only with {@link TreeArrayBuilder#add(Value)}. */
  @Test public void builderTestAscending() {
    for(int len = 0; len < 2_000; len++) {
      final TreeArrayBuilder ab = new TreeArrayBuilder();
      for(int i = 0; i < len; i++) ab.add(Itr.get(i));
      final XQArray array = ab.array();
      assertEquals(len, array.structSize());
      final Iterator<Value> iter = array.iterator(0);
      for(int i = 0; i < len; i++) {
        assertTrue(iter.hasNext());
        assertEquals(i, ((Itr) iter.next()).itr());
      }
      assertFalse(iter.hasNext());
    }
  }
}
