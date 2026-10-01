package org.basex.server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests the notation of client addresses.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ClientInfoTest {
  /** Client addresses. */
  @Test public void address() {
    assertNull(ClientInfo.address(null, 80));
    assertEquals("127.0.0.1:1984", ClientInfo.address("127.0.0.1", 1984));
    assertEquals("127.0.0.1", ClientInfo.address("127.0.0.1", -1));
    assertEquals("[0:0:0:0:0:0:0:1]:1984", ClientInfo.address("0:0:0:0:0:0:0:1", 1984));
    assertEquals("[0:0:0:0:0:0:0:1]:1984", ClientInfo.address("[0:0:0:0:0:0:0:1]", 1984));
    assertEquals("[::1]", ClientInfo.address("::1", -1));
  }
}
