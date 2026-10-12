package org.basex.query.value.seq;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for generic item sequences.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ItemSeqTest extends SandboxTest {
  /** Equality ignores unused entries of the backing arrays. */
  @Test public void equality() {
    final Item a = Str.get("a"), b = Itr.get(1), c = Str.get("c");
    final Item[] exact = { a, b }, spare = { a, b, null, null }, longer = { a, b, c };
    assertEquals(ItemSeq.get(exact, 2, null), ItemSeq.get(spare, 2, null));
    assertNotEquals(ItemSeq.get(longer, 2, null), ItemSeq.get(longer.clone(), 3, null));
  }
}
