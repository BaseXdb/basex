package org.basex.util.http;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.security.*;

import javax.net.ssl.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Certificates for HTTPS connections, supplied as key stores.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Certificates {
  /** Private constructor. */
  private Certificates() { }

  /**
   * Key stores of a request.
   * @param trust path to the trust store (can be {@code null})
   * @param trustPassword password of the trust store (can be {@code null})
   * @param client path to the client key store (can be {@code null})
   * @param clientPassword password of the client key store (can be {@code null})
   * @param key password of the client key (can be {@code null})
   */
  public record Stores(String trust, String trustPassword, String client, String clientPassword,
      String key) { }

  /**
   * Parses the key store options of a request.
   * @param certificates certificates
   * @param info input info (can be {@code null})
   * @return key stores
   * @throws QueryException query exception
   */
  public static Stores parse(final XQMap certificates, final InputInfo info)
      throws QueryException {

    final XQMap trust = map(certificates, "trust"), client = map(certificates, "client");
    return new Stores(path(trust, info), RequestOptions.string(trust, "keystore-password", info),
      path(client, info), RequestOptions.string(client, "keystore-password", info),
      RequestOptions.string(client, "password", info));
  }

  /**
   * Returns an SSL context for the supplied key stores.
   * @param stores key stores
   * @param verify verify the certificates of HTTPS servers
   * @param info input info (can be {@code null})
   * @return context
   * @throws QueryException query exception
   */
  public static SSLContext context(final Stores stores, final boolean verify,
      final InputInfo info) throws QueryException {

    try {
      final KeyManager[] kms = keyManagers(stores);
      // trust managers are replaced if the server certificate is not verified
      final TrustManager[] tms = verify ? trustManagers(stores) : IOUrl.trustAll();
      final SSLContext context = SSLContext.getInstance("TLS");
      context.init(kms, tms, new SecureRandom());
      return context;
    } catch(final GeneralSecurityException | IOException ex) {
      throw HTTP_INVALID_OPTION_X.get(info, Util.message(ex));
    }
  }

  /**
   * Returns the key managers for a client key store.
   * @param stores key stores
   * @return key managers, or {@code null} if no key store was supplied
   * @throws GeneralSecurityException security exception
   * @throws IOException I/O exception
   */
  private static KeyManager[] keyManagers(final Stores stores)
      throws GeneralSecurityException, IOException {

    if(stores.client == null) return null;
    final KeyManagerFactory kmf =
      KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(store(stores.client, stores.clientPassword),
      stores.key != null ? stores.key.toCharArray() : null);
    return kmf.getKeyManagers();
  }

  /**
   * Returns the trust managers for a trust store.
   * @param stores key stores
   * @return trust managers, or {@code null} if no trust store was supplied
   * @throws GeneralSecurityException security exception
   * @throws IOException I/O exception
   */
  private static TrustManager[] trustManagers(final Stores stores)
      throws GeneralSecurityException, IOException {

    if(stores.trust == null) return null;
    final TrustManagerFactory tmf =
      TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(store(stores.trust, stores.trustPassword));
    return tmf.getTrustManagers();
  }

  /**
   * Loads a key store.
   * @param path key store path
   * @param password key store password (can be {@code null})
   * @return key store
   * @throws GeneralSecurityException security exception
   * @throws IOException I/O exception
   */
  private static KeyStore store(final String path, final String password)
      throws GeneralSecurityException, IOException {

    final IO io = IO.get(path);
    final KeyStore store = KeyStore.getInstance(
      io.hasSuffix(".jks") ? "JKS" : KeyStore.getDefaultType());
    try(InputStream is = io.inputStream()) {
      store.load(is, password != null ? password.toCharArray() : null);
    }
    return store;
  }

  /**
   * Returns the path of a key store.
   * @param map key store options (can be {@code null})
   * @param info input info (can be {@code null})
   * @return path, or {@code null} if no key store was supplied
   * @throws QueryException query exception
   */
  private static String path(final XQMap map, final InputInfo info) throws QueryException {
    if(map == null) return null;
    if(RequestOptions.string(map, "alias", info) != null)
      throw HTTP_INVALID_OPTION_X.get(info, "Key store aliases are not supported yet");

    final String path = RequestOptions.string(map, "keystore", info);
    if(path == null) throw HTTP_INVALID_OPTION_X.get(info, "Missing key store path");
    return path;
  }

  /**
   * Returns a map entry.
   * @param map map
   * @param name entry name
   * @return map, or {@code null} if the entry is absent
   * @throws QueryException query exception
   */
  private static XQMap map(final XQMap map, final String name) throws QueryException {
    return map.get(Str.get(name)) instanceof final XQMap entry ? entry : null;
  }
}
