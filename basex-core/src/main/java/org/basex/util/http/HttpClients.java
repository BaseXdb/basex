package org.basex.util.http;

import java.net.*;
import java.net.http.*;
import java.util.*;

import javax.net.ssl.*;

import org.basex.io.*;
import org.basex.query.*;

/**
 * HTTP clients, indexed by their redirect policy and sharing a single cookie store.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HttpClients implements QueryResource {
  /** Cookie handler (can be {@code null}). */
  private final CookieHandler cookies;
  /** Cached client instances. */
  private final HttpClient[] clients = new HttpClient[2];
  /** Client instances for specific connection configurations. */
  private final Map<ClientKey, HttpClient> configured = new HashMap<>();

  /**
   * Connection configuration of a request.
   * @param cookies use the cookie store
   * @param verify verify the certificates of HTTPS servers
   * @param proxy proxy URI, empty string for a direct connection (can be {@code null})
   * @param certificates key stores (can be {@code null})
   */
  public record ClientKey(boolean cookies, boolean verify, String proxy,
      Certificates.Stores certificates) { }

  /**
   * Constructor for clients with cookie support.
   */
  public HttpClients() {
    this(new CookieManager());
  }

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

  /**
   * Returns a cached client instance for a connection configuration.
   * @param key configuration
   * @return client, or {@code null} if no client has been created yet
   */
  public synchronized HttpClient get(final ClientKey key) {
    return configured.get(key);
  }

  /**
   * Creates, caches and returns a client instance for a connection configuration.
   * @param key configuration
   * @param context SSL context (can be {@code null})
   * @return client
   */
  public synchronized HttpClient add(final ClientKey key, final SSLContext context) {
    final HttpClient client =
      IOUrl.client(false, key.cookies() ? cookies : null, key.proxy(), context);
    configured.put(key, client);
    return client;
  }

  @Override
  public void close() {
    // clients are shut down by the JDK as soon as they become unreachable
  }
}
