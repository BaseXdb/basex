package org.basex.query.util.pkg;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * Class loader cache.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
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
   * Acquires a cache entry for a module, replacing it if its files or dependencies have changed.
   * @param fileUrls URLs
   * @param deps loaders of the packages the module depends on
   * @return cache entry
   * @throws IOException I/O exception
   */
  static Loader acquire(final List<String> fileUrls, final List<Loader> deps) throws IOException {
    final List<String> keys = new ArrayList<>(fileUrls);
    keys.sort(null);
    final int n = keys.size();
    final URL[] urls = new URL[n];
    final long[] stamps = new long[n * 2];

    try {
      int i = 0, s = 0;
      for(final String key : keys) {
        final URI uri = new URI(key);
        if(!"file".equals(uri.getScheme())) throw Util.notExpected();
        final BasicFileAttributes attrs = Files.readAttributes(Paths.get(uri),
            BasicFileAttributes.class);
        stamps[s++] = attrs.lastModifiedTime().toMillis();
        stamps[s++] = attrs.size();
        urls[i++] = uri.toURL();
      }
    } catch(final URISyntaxException | MalformedURLException ex) {
      throw Util.notExpected(ex);
    }
    return CACHE.compute(keys, (k, v) -> {
      if(v != null) {
        if(Arrays.equals(v.stamps, stamps) && v.loader.deps.equals(deps)) {
          v.refs.incrementAndGet();
          return v;
        }
        v.invalidate();
      }
      return new Loader(new PkgClassLoader(urls, deps), stamps);
    });
  }

  /**
   * Invalidates all cache entries with files in the specified directory.
   * @param dir directory
   */
  static void invalidate(final IOFile dir) {
    for(final List<String> key : CACHE.keySet()) {
      if(Checks.any(key, url -> dir.contains((IOFile) IO.get(url)))) {
        final Loader loader = CACHE.remove(key);
        if(loader != null) loader.invalidate();
      }
    }
  }

  /** Class loader that falls back to the loaders of package dependencies. */
  private static final class PkgClassLoader extends URLClassLoader {
    static {
      registerAsParallelCapable();
    }

    /** Loaders of the packages this package depends on. */
    private final List<Loader> deps;

    /**
     * Constructor.
     * @param urls URLs
     * @param deps loaders of the packages this package depends on
     */
    private PkgClassLoader(final URL[] urls, final List<Loader> deps) {
      super(urls, LOADER);
      this.deps = deps;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
      try {
        return super.findClass(name);
      } catch(final ClassNotFoundException ex) {
        for(final Loader dep : deps) {
          try {
            return dep.loader.loadClass(name);
          } catch(final ClassNotFoundException ignore) {
            // try next dependency
          }
        }
        throw ex;
      }
    }

    @Override
    public URL findResource(final String name) {
      final URL url = super.findResource(name);
      if(url != null) return url;
      for(final Loader dep : deps) {
        final URL u = dep.loader.findResource(name);
        if(u != null) return u;
      }
      return null;
    }

    @Override
    public Enumeration<URL> findResources(final String name) throws IOException {
      final ArrayList<URL> urls = Collections.list(super.findResources(name));
      for(final Loader dep : deps) urls.addAll(Collections.list(dep.loader.findResources(name)));
      return Collections.enumeration(urls);
    }
  }

  /** Cache entry. */
  static final class Loader {
    /** The class loader. */
    private final PkgClassLoader loader;
    /** Modification times and sizes of the files, collected at loader creation time. */
    private final long[] stamps;
    /** Number of references to this instance. */
    private final AtomicInteger refs = new AtomicInteger(1);
    /** Cached classes. */
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    /** Whether we had to replace this instance due to file changes. */
    private volatile boolean stale;

    /**
     * Constructor.
     * @param loader class loader
     * @param stamps modification times and sizes of the files
     */
    private Loader(final PkgClassLoader loader, final long[] stamps) {
      this.loader = loader;
      this.stamps = stamps;
    }

    /**
     * Caches and returns a reference to the specified class.
     * @param name fully qualified class name
     * @return reference, or {@code null} if the class is not found
     */
    Class<?> find(final String name) {
      final Class<?> cached = classes.get(name);
      if(cached != null) return cached;
      try {
        final Class<?> c = Class.forName(name, false, loader);
        classes.putIfAbsent(name, c);
        return c;
      } catch(final ClassNotFoundException ignore) {
        // class not found
        return null;
      }
    }

    /**
     * Checks if the specified class was defined by this loader.
     * @param clz class
     * @return result of check
     */
    boolean defines(final Class<?> clz) {
      return clz.getClassLoader() == loader;
    }

    /**
     * Releases this Loader after use, but keeps it cached.
     */
    void release() {
      if(refs.decrementAndGet() == 0 && stale) close();
    }

    /**
     * Invalidates this Loader.
     */
    private void invalidate() {
      stale = true;
      if(refs.get() == 0) close();
    }

    /**
     * Closes the class loader, ignoring any exceptions.
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
