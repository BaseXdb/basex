package org.basex.query.func.cache;

import org.basex.util.options.*;

/**
 * Options for initializing caches.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CacheOptions extends Options {
  /** Maximum number of entries. */
  public static final NumberOption MAX_ENTRIES = new NumberOption("max-entries");
  /** Lifetime of entries in seconds. */
  public static final NumberOption TTL = new NumberOption("ttl");
}
