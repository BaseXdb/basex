package org.basex.util.http;

import java.net.*;
import java.net.http.*;

import org.basex.io.*;

/**
 * HTTP clients, indexed by their redirect policy and sharing a single cookie store.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HttpClients {
  /** Cookie handler (can be {@code null}). */
  private final CookieHandler cookies;
  /** Cached client instances. */
  private final HttpClient[] clients = new HttpClient[2];

  /**
   * Constructor.
   * @param cookies cookie handler (can be {@code null})
   */
  public HttpClients(final CookieHandler cookies) {
    this.cookies = cookies;
  }

  /**
   * Returns a client instance.
   * @param redirect follow redirects
   * @return client
   */
  public synchronized HttpClient get(final boolean redirect) {
    final int i = redirect ? 1 : 0;
    if(clients[i] == null) clients[i] = IOUrl.client(redirect, cookies);
    return clients[i];
  }
}
