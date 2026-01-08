package org.basex.query.util.pkg;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.java.*;
import org.basex.query.util.pkg.ClassLoaderCache.*;
import org.basex.util.*;

/**
 * Module loader.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ModuleLoader {
  /** Close method. */
  private static final Method CLOSE = Reflect.method(QueryResource.class, "close");

  /** Database context. */
  private final Context context;
  /** Java modules. */
  private final HashSet<Object> javaModules = new HashSet<>();
  /** Current class loaders. */
  private ArrayList<Loader> loaders = new ArrayList<>();
  /** Resolved classes. */
  private final HashMap<String, Class<?>> resolved = new HashMap<>();

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
  public void close() {
    for(final Object jm : javaModules) {
      for(final Class<?> c : jm.getClass().getInterfaces()) {
        if(c == QueryResource.class) Reflect.invoke(CLOSE, jm);
      }
    }
    javaModules.clear();
    for(final Loader l : loaders) l.release();
    loaders.clear();
    resolved.clear();
  }

  /**
   * Adds a package from the repository or a Java class.
   * @param uri module URI
   * @param qp query parser
   * @param info input info (can be {@code null})
   * @return {@code true} if the package has been found
   * @throws QueryException query exception
   */
  public boolean addImport(final String uri, final QueryParser qp, final InputInfo info)
      throws QueryException {

    // add Java repository package
    final boolean java = uri.startsWith(JAVA_PREFIX_COLON);
    String className;
    if(java) {
      className = uri.substring(JAVA_PREFIX_COLON.length());
    } else {
      // no "java:" prefix: check EXPath repositories
      final HashSet<String> pkgs = context.repo.nsDict().get(uri);
      if(pkgs != null) {
        Version ver = null;
        String id = null;
        for(final String pkg : pkgs) {
          final Version v = new Version(Pkg.version(pkg));
          if(ver == null || v.compareTo(ver) > 0) {
            ver = v;
            id = pkg;
          }
        }
        if(id != null) {
          addRepo(id, new HashSet<>(), new HashSet<>(), qp, info);
          return true;
        }
      }
      // check XQuery modules
      final String repoPath = context.soptions.get(StaticOptions.REPOPATH);
      final String path = Strings.uri2path(uri);
      for(final String suffix : IO.XQSUFFIXES) {
        final IOFile file = new IOFile(repoPath, path + suffix);
        if(file.exists()) {
          qp.module(file.path(), uri, info);
          return true;
        }
      }
      // convert to Java notation
      className = Strings.uriToClasspath(path);
    }
    className = JavaCall.classPath(className);

    // create Java class instance
    final Class<?> clz;
    try {
      addLoader(jarUrls(context, className));
      clz = findClass(className);
    } catch(final ClassNotFoundException ex) {
      Util.debug(ex);
      if(java) throw WHICHMODCLASS_X.get(info, className);
      return false;
    } catch(final Throwable th) {
      final Throwable t = Util.rootException(th);
      throw MODINIT_X_X_X.get(info, className, t.getMessage(), Util.className(t));
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
   * Returns a reference to the specified class.
   * @param name fully classified class name
   * @return found class
   * @throws ClassNotFoundException class not found exception
   */
  public Class<?> findClass(final String name) throws ClassNotFoundException {
    Class<?> c = resolved.get(name);
    if(c != null) return c;
    c = Reflect.find(name);
    if(c != null) return c;
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
  public Object findModule(final String clz) {
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
    final String repoPath = context.soptions.get(StaticOptions.REPOPATH);
    final IOFile jar = new IOFile(repoPath, Strings.uri2path(className) + IO.JARSUFFIX);
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
    if(!urls.isEmpty()) loaders.add(ClassLoaderCache.acquire(urls));
  }

  /**
   * Adds a package from the package repository.
   * @param id package ID
   * @param toLoad list with packages to be loaded
   * @param loaded already loaded packages
   * @param qp query parser
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private void addRepo(final String id, final HashSet<String> toLoad, final HashSet<String> loaded,
      final QueryParser qp, final InputInfo info) throws QueryException {

    // return if package is already loaded
    if(loaded.contains(id)) return;

    // find package in package dictionary
    Pkg pkg = context.repo.pkgDict().get(id);
    if(pkg == null) throw REPO_NOTFOUND_X.get(info, id);
    final IOFile pkgPath = context.repo.path(pkg.path());

    // parse package descriptor
    final IO pkgDesc = new IOFile(pkgPath, PkgText.DESCRIPTOR);
    if(!pkgDesc.exists()) Util.debugln(PkgText.MISSDESC, id);

    pkg = new PkgParser(info).parse(pkgDesc);
    final IOFile modDir = pkg.modDir(pkgPath);
    try {
      addLoader(pkgUrls(pkgPath, modDir, info));
    } catch(final Throwable th) {
      final Throwable t = Util.rootException(th);
      throw MODINIT_X_X_X.get(info, pkg.name, t.getMessage(), Util.className(t));
    }

    // package has dependencies. they have to be loaded first
    // (put package in list with packages to be loaded)
    if(!pkg.dep.isEmpty()) toLoad.add(id);
    for(final PkgDep dep : pkg.dep) {
      if(dep.name != null) {
        // we consider only package dependencies here
        final String depId = new PkgValidator(context.repo, info).depPkg(dep);
        if(depId == null) throw REPO_NOTFOUND_X.get(info, dep.name);
        if(toLoad.contains(depId)) throw CIRCMODULE.get(info);
        addRepo(depId, toLoad, loaded, qp, info);
      }
    }
    for(final PkgComponent comp : pkg.comps) {
      qp.module(new IOFile(modDir, comp.file).path(), comp.uri, info);
    }
    toLoad.remove(id);
    loaded.add(id);
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
