package org.basex.http;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests the recognition of forwarded client addresses.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestStateTest {
  /** IPv4 and IPv6 addresses. */
  @Test public void ipAddress() {
    for(final String address : new String[] { "192.168.0.1", "::1", "[::1]", "2001:db8::1",
        "[2001:DB8:0:0:0:0:0:1]", "::ffff:192.168.0.1" }) {
      assertTrue(RequestState.IP_ADDRESS.matcher(address).matches(), address);
    }
    for(final String address : new String[] { "", "unknown", "cafe", "1.2.3", "host.example",
        "192.168.0.1:80" }) {
      assertFalse(RequestState.IP_ADDRESS.matcher(address).matches(), address);
    }
  }
}
