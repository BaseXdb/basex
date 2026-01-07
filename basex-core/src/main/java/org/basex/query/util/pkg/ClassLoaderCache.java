package org.basex.query.util.pkg;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.basex.util.*;

/**
 * Class loader cache.
 */
final class ClassLoaderCache {
  /** Default class loader. */
  private static final ClassLoader LOADER = ClassLoaderCache.class.getClassLoader();
  /** Class loaders by key. */
  private static final ConcurrentHashMap<List<String>, Loader> CACHE = new ConcurrentHashMap<>();

  /** Private constructor. */
  private ClassLoaderCache() {
  }

  /**
   * Acquire cache entry for a module. If the module is not cached, or if any of the files have
   * changed, a new loader is created. Otherwise, the existing loader is returned and its reference
   * count is incremented.
   * @param fileUrls URLs
   * @return cache entry
   * @throws IOException I/O exception
   */
  public static Loader acquire(final List<String> fileUrls) throws IOException {
    final List<String> keys = normalize(fileUrls);
    final int n = keys.size();
    final URL[] urls = new URL[n];
    final long[] lastModified = new long[n];

    try {
      int i = 0;
      for(final String key : keys) {
        final URL url = new URL(key);
        if(!"file".equals(url.getProtocol())) throw Util.notExpected();
        lastModified[i] = Files.getLastModifiedTime(Paths.get(url.toURI())).toMillis();
        urls[i++] = url;
      }
    } catch(final URISyntaxException | MalformedURLException ex) {
      throw Util.notExpected(ex);
    }
    return CACHE.compute(keys, (k, v) -> {
      if(v != null) {
        if(Arrays.equals(v.lastModified, lastModified)) {
          v.refs.incrementAndGet();
          return v;
        }
        v.invalidate();
      }
      return new Loader(new URLClassLoader(urls, LOADER), lastModified);
    });
  }

  /**
   * Invalidate cache entry for the given URLs, if any.
   * @param fileUrls URLs
   */
  public static void invalidate(final List<String> fileUrls) {
    final Loader loader = CACHE.remove(normalize(fileUrls));
    if(loader != null) loader.invalidate();
  }

  /**
   * Normalizes a list of strings by sorting them.
   * @param strings list of strings
   * @return normalized list
   */
  private static List<String> normalize(final List<String> strings) {
    final List<String> normalized = new ArrayList<>(strings);
    normalized.sort(String::compareTo);
    return normalized;
  }

  /** Cache entry. */
  public static final class Loader {
    /** The class loader. */
    private final URLClassLoader loader;
    /** The time stamps collected for the URLs at loader creation time. */
    private final long[] lastModified;
    /** Number of references to this instance. */
    private final AtomicInteger refs = new AtomicInteger(1);
    /** Whether we had to replace this instance due to file changes. */
    private volatile boolean stale;
    /** Cached classes. */
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

    /**
     * Constructor.
     * @param loader class loader
     * @param lastModified the time stamps collected for the URLs at loader creation time
     */
    private Loader(final URLClassLoader loader, final long[] lastModified) {
      this.loader = loader;
      this.lastModified = lastModified;
    }

    /**
     * Caches and returns a reference to the specified class.
     * @param name fully qualified class name
     * @return reference, or {@code null} if the class is not found
     */
    public Class<?> find(final String name) {
      final Class<?> cached = classes.get(name);
      if(cached != null) return cached;
      try {
        final Class<?> c = Class.forName(name, true, loader);
        classes.putIfAbsent(name, c);
        return c;
      } catch(final ClassNotFoundException ex) {
        Util.debug(ex);
        return null;
      }
    }

    /**
     * Release this Loader after use, but keep it cached.
     */
    public void release() {
      if(refs.decrementAndGet() == 0 && stale) close();
    }

    /**
     * Invalidate this Loader.
     */
    private void invalidate() {
      stale = true;
      if(refs.get() == 0) close();
    }

    /**
     * Close the class loader, ignoring any exceptions.
     */
    private void close() {
      try {
        loader.close();
      } catch(final IOException ex) {
        Util.stack(ex);
      }
      classes.clear();
    }
  }
}
