package org.basex.query.util.pkg;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.util.pkg.Package.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Module loader.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ModuleLoader {
  /** Java modules. */
  public final HashMap<Object, ArrayList<Method>> javaModules =
      new HashMap<Object, ArrayList<Method>>();
  /** Module URLs. */
  private final ArrayList<URL> urls = new ArrayList<URL>();
  /** Database context. */
  private final Context context;
  /** Jar loader. */
  private JarLoader jars;

  /**
   * Constructor.
   * @param ctx database context
   */
  public ModuleLoader(final Context ctx) {
    context = ctx;
  }

  /**
   * Closes opened jar files.
   */
  public void close() {
    if(jars != null) jars.close();
  }

  /**
   * Finds the instance of the specified Java module.
   * @param clz class to be found
   * @return instance, or {@code null}
   */
  public Object findJava(final String clz) {
    // check if class was imported as Java module
    for(final Object jm : javaModules.keySet()) {
      final Class<?> c = jm.getClass();
      if(c.getName().equals(clz)) return jm;
    }
    return null;
  }

  /**
   * Finds the specified class.
   * @param clz class to be found
   * @return found class, or {@code null}
   */
  public Class<?> find(final String clz) {
    // no modules specified
    if(urls.size() == 0) return null;
    // first call: create loader
    if(jars == null) {
      final ClassLoader cl = Thread.currentThread().getContextClassLoader();
      jars = new JarLoader(urls.toArray(new URL[urls.size()]), cl);
    }
    return Reflect.find(clz, jars);
  }

  /**
   * Loads a package from the package repository.
   * @param uri module uri
   * @param ii input info
   * @param qp query parser
   * @return if the package has been found
   * @throws QueryException query exception
   */
  public boolean load(final byte[] uri, final InputInfo ii, final QueryParser qp)
      throws QueryException {

    // check Java modules (inheriting {@link QueryModule})
    if(startsWith(uri, JAVAPRE)) {
      final String path = string(substring(uri, JAVAPRE.length));
      final Class<?> clz = Reflect.find(path);
      if(clz == null) qp.error(NOMODULE, path);

      final boolean qm = clz.getSuperclass() == QueryModule.class;
      final Object jm = Reflect.get(clz);
      if(jm == null) qp.error(NOINV, path);

      // add all public methods of the class (ignore methods from super classes)
      final ArrayList<Method> list = new ArrayList<Method>();
      for(final Method m : clz.getMethods()) {
        // if class is inherited from {@link QueryModule}, no super methods are accepted
        if(!qm || m.getDeclaringClass() == clz) list.add(m);
      }
      // put class into module cache
      javaModules.put(jm, list);
      return true;
    }

    final TokenSet pkgs = context.repo.nsDict().get(uri);
    if(pkgs == null) return false;

    // load packages with modules having the given uri
    for(final byte[] name : pkgs) {
      if(name != null) load(name, new TokenSet(), new TokenSet(), ii, qp);
    }
    return true;
  }

  /**
   * Recursively loads a package from the package repository.
   * @param name package name
   * @param toLoad list with packages to be loaded
   * @param loaded already loaded packages
   * @param ii input info
   * @param qp query parser
   * @throws QueryException query exception
   */
  private void load(final byte[] name, final TokenSet toLoad, final TokenSet loaded,
      final InputInfo ii, final QueryParser qp) throws QueryException {

    // return if package is already loaded
    if(loaded.contains(name)) return;

    // find package in package dictionary
    final byte[] pDir = context.repo.pkgDict().get(name);
    if(pDir == null) qp.error(NECPKGNOTINST, name);
    final IOFile pkgDir = context.repo.path(string(pDir));

    // parse package descriptor
    final IO pkgDesc = new IOFile(pkgDir, PkgText.DESCRIPTOR);
    if(!pkgDesc.exists()) Util.debug(PkgText.MISSDESC, string(name));

    final Package pkg = new PkgParser(context.repo, ii).parse(pkgDesc);
    // check if package contains a jar descriptor
    final IOFile jarDesc = new IOFile(pkgDir, PkgText.JARDESC);
    // add jars to classpath
    if(jarDesc.exists()) loadJars(jarDesc, pkgDir, string(pkg.abbrev), ii);

    // package has dependencies -> they have to be loaded first => put package
    // in list with packages to be loaded
    if(pkg.dep.size() != 0) toLoad.add(name);
    for(final Dependency d : pkg.dep) {
      if(d.pkg != null) {
      // we consider only package dependencies here
      final byte[] depPkg = new PkgValidator(context.repo, ii).depPkg(d);
      if(depPkg == null) {
        qp.error(NECPKGNOTINST, string(d.pkg));
      } else {
        if(toLoad.contains(depPkg)) qp.error(CIRCMODULE);
        load(depPkg, toLoad, loaded, ii, qp);
      }
     }
    }
    for(final Component comp : pkg.comps) {
      final String p = new IOFile(new IOFile(pkgDir, string(pkg.abbrev)),
          string(comp.file)).path();
      qp.module(token(p), comp.uri);
    }
    if(toLoad.id(name) != 0) toLoad.delete(name);
    loaded.add(name);
  }

  /**
   * Adds the jar files registered in jarDesc.
   * @param jarDesc jar descriptor
   * @param pkgDir package directory
   * @param modDir module directory
   * @param ii input info
   * @throws QueryException query exception
   */
  private void loadJars(final IOFile jarDesc, final IOFile pkgDir, final String modDir,
      final InputInfo ii) throws QueryException {

    // add new URLs
    final JarDesc desc = new JarParser(context, ii).parse(jarDesc);
    for(final byte[] u : desc.jars) {
      // assumes that jar is in the directory containing the xquery modules
      final IOFile p = new IOFile(new IOFile(pkgDir, modDir), string(u));
      try {
        urls.add(new URL(IO.FILEPREF + p));
      } catch(final MalformedURLException ex) {
        Util.errln(ex.getMessage());
      }
    }
  }
}
