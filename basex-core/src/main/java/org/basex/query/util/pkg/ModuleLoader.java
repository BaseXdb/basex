package org.basex.query.util.pkg;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.java.*;
import org.basex.query.util.*;
import org.basex.query.util.pkg.ClassLoaderCache.*;
import org.basex.util.*;

/**
 * Module loader.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ModuleLoader {
  /**
   * Parsed package.
   * @param pkg package
   * @param modDir module directory
   */
  private record Parsed(Pkg pkg, IOFile modDir) { }

  /** Database context. */
  private final Context context;
  /** Java modules. */
  private final HashSet<Object> javaModules = new HashSet<>();
  /** Current class loaders. */
  private final ArrayList<Loader> loaders = new ArrayList<>();
  /** Resolved classes. */
  private final HashMap<String, Class<?>> resolved = new HashMap<>();
  /** Module files loaded from the repository, indexed by URI. */
  private final HashMap<String, ArrayList<IOFile>> loaded = new HashMap<>();

  /**
   * Constructor.
   * @param context database context
   */
  public ModuleLoader(final Context context) {
    this.context = context;
  }

  /**
   * Closes opened jar files, and calls close method of {@link QueryModule} instances
   * implementing {@link QueryResource}.
   */
  public synchronized void close() {
    for(final Object jm : javaModules) {
      if(jm instanceof final QueryResource qr) {
        try {
          qr.close();
        } catch(final RuntimeException ex) {
          Util.debug(ex);
        }
      }
    }
    javaModules.clear();
    for(final Loader l : loaders) l.release();
    loaders.clear();
    resolved.clear();
    loaded.clear();
  }

  /**
   * Adds a package from the repository or a Java class.
   * @param uri module URI
   * @param qp query parser
   * @param info input info (can be {@code null})
   * @return {@code true} if the package has been found
   * @throws QueryException query exception
   */
  public synchronized boolean addImport(final String uri, final QueryParser qp,
      final InputInfo info) throws QueryException {

    // add Java repository package
    final boolean java = uri.startsWith(JAVA_PREFIX_COLON);
    String className;
    if(java) {
      className = uri.substring(JAVA_PREFIX_COLON.length());
    } else {
      // no "java:" prefix: check EXPath repositories
      final Pkg pkg = pkg(context.repo, uri);
      if(pkg != null) {
        packages(context, pkg.id(), this, (file, u) -> qp.module(file.path(), u, info), info);
        return true;
      }
      // check XQuery modules
      final IOFile file = file(context.soptions.repoPath(), uri);
      if(file != null) {
        qp.module(file.path(), uri, info);
        return true;
      }
      // convert to Java notation
      className = Strings.uriToClasspath(Strings.uri2path(uri));
    }
    className = JavaCall.classPath(className);

    // create Java class instance
    final Class<?> clz;
    try {
      addLoader(jarUrls(context, className));
      clz = findClass(className);
    } catch(final ClassNotFoundException ex) {
      if(java) throw WHICHMODCLASS_X.get(info, className).cause(ex);
      Util.debug(ex);
      return false;
    } catch(final IOException | LinkageError ex) {
      throw MODINIT_X_X_X.get(info, className, ex.getMessage(), Util.className(ex)).cause(ex);
    }
    // classes outside the repository must be query modules, or require admin permission
    if(!QueryModule.class.isAssignableFrom(clz) && !repository(clz)) {
      qp.checkExternal(className, info);
    }

    // instantiate class
    try {
      javaModules.add(clz.getDeclaredConstructor().newInstance());
      return true;
    } catch(final Throwable ex) {
      throw MODINST_X_X.get(info, className, ex);
    }
  }

  /**
   * Returns the files of the XQuery modules with the specified URI in the repository,
   * and adds the class loaders of an EXPath package that defines the URI.
   * @param uri module URI
   * @param info input info (can be {@code null})
   * @return module files (empty if none exist)
   * @throws QueryException query exception
   */
  public synchronized ArrayList<IOFile> load(final String uri, final InputInfo info)
      throws QueryException {
    final ArrayList<IOFile> cached = loaded.get(uri);
    if(cached != null) return cached;

    final ArrayList<IOFile> files = files(context, uri, this, info);
    loaded.put(uri, files);
    return files;
  }

  /**
   * Returns the files of the XQuery modules with the specified URI in the repository.
   * @param context database context
   * @param uri module URI
   * @return module files (empty if none exist)
   */
  public static ArrayList<IOFile> files(final Context context, final String uri) {
    try {
      return files(context, uri, null, null);
    } catch(final QueryException ex) {
      Util.debug(ex);
      return new ArrayList<>();
    }
  }

  /**
   * Returns the files of the XQuery modules with the specified URI in the repository.
   * @param context database context
   * @param uri module URI
   * @param loader module loader that adds the class loaders of packages (can be {@code null})
   * @param info input info (can be {@code null})
   * @return module files (empty if none exist)
   * @throws QueryException query exception
   */
  private static ArrayList<IOFile> files(final Context context, final String uri,
      final ModuleLoader loader, final InputInfo info) throws QueryException {
    final ArrayList<IOFile> files = new ArrayList<>();
    final Pkg pkg = pkg(context.repo, uri);
    if(pkg != null) {
      // components of package dependencies may define the same URI
      packages(context, pkg.id(), loader, (file, u) -> {
        if(u.equals(uri)) files.add(file);
      }, info);
    } else {
      final IOFile file = file(context.soptions.repoPath(), uri);
      if(file != null) files.add(file);
    }
    return files;
  }

  /**
   * Returns the latest version of an EXPath package that defines the specified URI.
   * @param repo package repository
   * @param uri module URI
   * @return package (can be {@code null})
   */
  private static Pkg pkg(final EXPathRepo repo, final String uri) {
    Version ver = null;
    Pkg latest = null;
    for(final Pkg pkg : repo.pkgs(uri)) {
      final Version v = new Version(pkg.version);
      if(ver == null || v.compareTo(ver) > 0) {
        ver = v;
        latest = pkg;
      }
    }
    return latest;
  }

  /**
   * Returns the file of an XQuery module in the repository.
   * @param repo repository path
   * @param uri module URI
   * @return module file, or {@code null} if it does not exist
   */
  public static IOFile file(final IOFile repo, final String uri) {
    final String path = Strings.uri2path(uri);
    for(final String suffix : IO.XQSUFFIXES) {
      final IOFile file = new IOFile(repo, path + suffix);
      if(file.exists()) return file;
    }
    return null;
  }

  /**
   * Checks if the specified class was loaded from the repository.
   * @param clz class
   * @return result of check
   */
  private boolean repository(final Class<?> clz) {
    for(final Loader loader : loaders) {
      if(loader.defines(clz)) return true;
    }
    return false;
  }

  /**
   * Checks if the specified class was loaded by a class loader that is shared by all queries.
   * @param clz class
   * @return result of check
   */
  public static boolean global(final Class<?> clz) {
    final ClassLoader cl = clz.getClassLoader();
    if(cl == null) return true;
    for(ClassLoader l = ModuleLoader.class.getClassLoader(); l != null; l = l.getParent()) {
      if(l == cl) return true;
    }
    return false;
  }

  /**
   * Returns a reference to the specified class without initializing it.
   * @param name fully qualified class name
   * @return found class
   * @throws ClassNotFoundException class not found exception
   */
  public synchronized Class<?> findClass(final String name) throws ClassNotFoundException {
    Class<?> c = resolved.get(name);
    if(c != null) return c;
    try {
      return Reflect.forName(name, false);
    } catch(final ClassNotFoundException | LinkageError ex) {
      // class is not on the classpath, or the case of the name is wrong
      Util.debug(ex);
    }
    for(int i = loaders.size() - 1; i >= 0; --i) {
      c = loaders.get(i).find(name);
      if(c != null) {
        resolved.put(name, c);
        return c;
      }
    }
    throw new ClassNotFoundException(name);
  }

  /**
   * Returns an instance of the specified Java module class.
   * @param clz class to be found
   * @return instance or {@code null}
   */
  public synchronized Object findModule(final String clz) {
    for(final Object mod : javaModules) {
      if(mod.getClass().getName().equals(clz)) return mod;
    }
    return null;
  }

  /**
   * Calculates jar file URLs that are relevant for class loading in the context of the specified
   * package.
   * @param pkgPath package path
   * @param modDir module directory
   * @param info input info (can be {@code null})
   * @return list of jar file URLs (may be empty)
   * @throws QueryException query exception
   */
  static List<String> pkgUrls(final IOFile pkgPath, final IOFile modDir, final InputInfo info)
      throws QueryException {

    final ArrayList<String> urls = new ArrayList<>();
    // check if package contains a jar descriptor
    final IOFile jarDesc = new IOFile(pkgPath, PkgText.JARDESC);
    if(jarDesc.exists()) {
      final JarDesc desc = new JarParser(info).parse(jarDesc);
      for(final byte[] u : desc.jars) addURL(urls, new IOFile(modDir, string(u)));
    }
    return urls;
  }

  /**
   * Calculates jar file URLs that are relevant for class loading in the context of the specified
   * Java module.
   * @param context database context
   * @param className class name
   * @return list of jar file URLs (may be empty)
   */
  static List<String> jarUrls(final Context context, final String className) {
    final ArrayList<String> urls = new ArrayList<>();
    final IOFile jar = new IOFile(context.soptions.repoPath(),
        Strings.uri2path(className) + IO.JARSUFFIX);
    if(jar.exists()) addURL(urls, jar);
    return urls;
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Creates or gets a class loader for the specified URLs from the class loader cache,
   * and adds it to the list of loaders.
   * @param urls URLs
   * @throws IOException I/O exception
   */
  private void addLoader(final List<String> urls) throws IOException {
    if(urls.isEmpty()) return;
    // a cached loader is registered only once
    final Loader loader = ClassLoaderCache.acquire(urls);
    if(loaders.contains(loader)) loader.release();
    else loaders.add(loader);
  }

  /**
   * Visits a package and the packages it depends on from the package repository.
   * @param context database context
   * @param id package ID
   * @param loader module loader that adds the class loaders of packages (can be {@code null})
   * @param module action for the file and URI of each module component
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private static void packages(final Context context, final String id, final ModuleLoader loader,
      final QueryBiConsumer<IOFile, String> module, final InputInfo info) throws QueryException {

    final HashMap<String, Parsed> parsed = new HashMap<>();
    new Cycles<String>() {
      @Override
      protected Iterable<String> next(final String pkgId) throws QueryException {
        // find package in package dictionary
        final Pkg pkg = context.repo.pkgDict().get(pkgId);
        if(pkg == null) throw REPO_NOTFOUND_X.get(info, pkgId);
        final IOFile pkgPath = context.repo.path(pkg.path());
        final IOFile modDir = pkg.modDir(pkgPath);
        if(loader != null) {
          try {
            loader.addLoader(pkgUrls(pkgPath, modDir, info));
          } catch(final Throwable th) {
            final Throwable t = Util.rootException(th);
            throw MODINIT_X_X_X.get(info, pkg.name, t.getMessage(), Util.className(t)).cause(th);
          }
        }
        parsed.put(pkgId, new Parsed(pkg, modDir));

        // package dependencies are loaded first; only package dependencies are considered
        final ArrayList<String> deps = new ArrayList<>();
        for(final PkgDep dep : pkg.dep) {
          if(dep.name != null) {
            final String depId = new PkgValidator(context.repo, info).depPkg(dep);
            if(depId == null) throw REPO_NOTFOUND_X.get(info, dep.name);
            deps.add(depId);
          }
        }
        return deps;
      }

      @Override
      protected void finish(final String pkgId) throws QueryException {
        final Parsed p = parsed.get(pkgId);
        for(final PkgComponent comp : p.pkg.comps) {
          module.accept(new IOFile(p.modDir, comp.file), comp.uri);
        }
      }

      @Override
      protected QueryException error(final String pkgId) {
        return CIRCPKG_X.get(info, pkgId);
      }
    }.visit(id);
  }

  /**
   * Adds a jar URL and all extracted files to the list of URLs.
   * @param urls list of URLs
   * @param jar jar file to be added
   */
  private static void addURL(final List<String> urls, final IOFile jar) {
    urls.add(jar.url());
    // parse files of extracted subdirectory
    final IOFile extDir = new IOFile(jar.parent(), '.' + jar.dbName());
    if(extDir.exists()) {
      for(final IOFile file : extDir.children()) urls.add(file.url());
    }
  }
}
