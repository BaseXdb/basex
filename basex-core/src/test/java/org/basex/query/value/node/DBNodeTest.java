package org.basex.query.value.node;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.data.*;
import org.basex.io.*;
import org.junit.jupiter.api.*;

/**
 * Tests for database nodes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DBNodeTest extends SandboxTest {
  /**
   * Equal nodes have equal hash codes.
   * @throws IOException I/O exception
   */
  @Test public void hashCodes() throws IOException {
    final Data data = new DBNode(new IOContent("<a><b/></a>")).data();
    final DBNode node1 = new DBNode(data, 2), node2 = new DBNode(data, 2);
    assertEquals(node1, node2);
    assertEquals(node1.hashCode(), node2.hashCode());
    assertNotEquals(node1, new DBNode(data, 1));
  }
}
