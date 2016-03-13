package org.basex.query.util.pkg;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Module loader.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class ModuleLoader {
  /** Default class loader. */
  private static final ClassLoader LOADER = Thread.currentThread().getContextClassLoader();
  /** Close method. */
  private static final Method CLOSE = Reflect.method(QueryResource.class, "close");

  /** Database context. */
  private final Context context;
  /** Cached URLs to be added to the class loader. */
  private final ArrayList<URL> urls = new ArrayList<>(0);
  /** Java modules. */
  private final HashSet<Object> javaModules = new HashSet<>();
  /** Current class loader. */
  private ClassLoader loader = LOADER;

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
    if(loader instanceof JarLoader) ((JarLoader) loader).close();
    for(final Object jm : javaModules) {
      for(final Class<?> c : jm.getClass().getInterfaces()) {
        if(c == QueryResource.class) Reflect.invoke(CLOSE, jm);
      }
    }
  }

  /**
   * Adds a package from the repository or a Java class.
   * @param uri module uri
   * @param ii input info
   * @param qp query parser
   * @return if the package has been found
   * @throws QueryException query exception
   */
  public boolean addImport(final String uri, final InputInfo ii, final QueryParser qp)
      throws QueryException {

    // add Java repository package
    final String repoPath = context.soptions.get(StaticOptions.REPOPATH);
    final boolean java = uri.startsWith(JAVAPREF);
    final String className;
    if(java) {
      className = uri.substring(JAVAPREF.length());
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
          addRepo(id, new HashSet<String>(), new HashSet<String>(), ii, qp);
          return true;
        }
      }
      // check XQuery modules
      final String path = Strings.uri2path(uri);
      for(final String suffix : IO.XQSUFFIXES) {
        final IOFile file = new IOFile(repoPath, path + suffix);
        if(file.exists()) {
          qp.module(file.path(), uri, ii);
          return true;
        }
      }
      // convert to Java notation
      className = Strings.className(path);
    }

    // load Java module
    final IOFile jar = new IOFile(repoPath, Strings.uri2path(className) + IO.JARSUFFIX);
    if(jar.exists()) addURL(jar);

    // create Java class instance
    final Class<?> clz;
    try {
      clz = findClass(className);
    } catch(final ClassNotFoundException ex) {
      if(java) throw WHICHCLASS_X.get(ii, ex.getMessage());
      return false;
    } catch(final Throwable th) {
      final Throwable t = Util.rootException(th);
      throw MODINIT_X_X_X.get(ii, className, t.getMessage(), Util.className(t));
    }

    // instantiate class
    try {
      javaModules.add(clz.newInstance());
      return true;
    } catch(final Throwable ex) {
      throw MODINST_X_X.get(ii, className, ex);
    }
  }

  /**
   * Returns a reference to the specified class.
   * @param name fully classified class name
   * @return found class or {@code null}
   * @throws Throwable any exception or error: {@link ClassNotFoundException},
   *   {@link NoClassDefFoundError}, {@link LinkageError} or {@link ExceptionInInitializerError}.
   */
  public Class<?> findClass(final String name) throws Throwable {
    // add cached URLs to class loader
    final int us = urls.size();
    if(us != 0) {
      loader = new JarLoader(urls.toArray(new URL[us]), loader);
      urls.clear();
    }
    // no external classes added: use default class loader
    return loader == LOADER ? Reflect.forName(name) : Class.forName(name, true, loader);
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

  // PRIVATE METHODS ====================================================================

  /**
   * Adds a package from the package repository.
   * @param id package id
   * @param toLoad list with packages to be loaded
   * @param loaded already loaded packages
   * @param ii input info
   * @param qp query parser
   * @throws QueryException query exception
   */
  private void addRepo(final String id, final HashSet<String> toLoad, final HashSet<String> loaded,
      final InputInfo ii, final QueryParser qp) throws QueryException {

    // return if package is already loaded
    if(loaded.contains(id)) return;

    // find package in package dictionary
    Pkg pkg = context.repo.pkgDict().get(id);
    if(pkg == null) throw BXRE_NOTINST_X.get(ii, id);
    final IOFile pkgDir = context.repo.path(pkg.dir());

    // parse package descriptor
    final IO pkgDesc = new IOFile(pkgDir, PkgText.DESCRIPTOR);
    if(!pkgDesc.exists()) Util.debug(PkgText.MISSDESC, id);

    pkg = new PkgParser(ii).parse(pkgDesc);
    // check if package contains a jar descriptor
    final IOFile jarDesc = new IOFile(pkgDir, PkgText.JARDESC);
    // choose module directory (support for both 2010 and 2012 specs)
    IOFile modDir = new IOFile(pkgDir, PkgText.CONTENT);
    if(!modDir.exists()) modDir = new IOFile(pkgDir, pkg.abbrev());

    // add jars to classpath
    if(jarDesc.exists()) {
      final JarDesc desc = new JarParser(ii).parse(jarDesc);
      for(final byte[] u : desc.jars) addURL(new IOFile(modDir, string(u)));
    }

    // package has dependencies -> they have to be loaded first => put package
    // in list with packages to be loaded
    if(!pkg.dep.isEmpty()) toLoad.add(id);
    for(final PkgDep d : pkg.dep) {
      if(d.name != null) {
        // we consider only package dependencies here
        final String depId = new PkgValidator(context.repo, ii).depPkg(d);
        if(depId == null) throw BXRE_NOTINST_X.get(ii, d.name);
        if(toLoad.contains(depId)) throw CIRCMODULE.get(ii);
        addRepo(depId, toLoad, loaded, ii, qp);
      }
    }
    for(final PkgComponent comp : pkg.comps) {
      qp.module(new IOFile(modDir, comp.file).path(), comp.uri, ii);
    }
    if(toLoad.contains(id)) toLoad.remove(id);
    loaded.add(id);
  }

  /**
   * Adds a URL to the cache.
   * @param jar jar file to be added
   */
  private void addURL(final IOFile jar) {
    try {
      urls.add(new URL(jar.url()));
    } catch(final MalformedURLException ex) {
      Util.errln(ex);
    }
  }
}
