package org.deepfs.fsml.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.basex.util.Util;

/**
 * Some utility methods for loading class files from folders, packages or jar
 * files.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Bastian Lemke
 */
public final class Loader extends ClassLoader {
  /** Hidden constructor. */
  private Loader() { }

  /**
   * Returns the {@link URL} for the package {@code pkg}.
   * @param pkg the name of the package to get the URL for
   * @return the URL
   */
  private static URL getURL(final String pkg) {
    String pkgPath = pkg.replace('.', '/');
    if(!pkgPath.startsWith("/")) pkgPath = "/" + pkgPath;
    return Loader.class.getResource(pkgPath);
  }

  /**
   * <p>
   * Loads all subclasses of {@code superClass} from the package
   * {@code pkg}. The classes may either be in a directory or inside a java
   * archive file. If one of the classes is already loaded, nothing is done (but
   * the class will be included in the results).
   * </p>
   * <p>
   * {@code superClass} may be an abstract class, an interface or a regular
   * class. Every class inside the package {@code pkg} that implements the
   * interface or extends the class is loaded.
   * </p>
   * <p>
   * This method breaks after the first error. Subsequent classes are not
   * loaded.
   * </p>
   * @param pkg the package to load the classes from
   * @param superClass either the interface that has to be implemented by the
   *          classes or a class that has to be extended
   * @return an array with all the loaded classes
   * @throws IOException if the classes are located inside a JAR file and any
   *           error occurs while reading from this file
   */
  public static Class<?>[] load(final Package pkg, final Class<?> superClass)
      throws IOException {
    final ArrayList<Class<?>> foundClasses = new ArrayList<Class<?>>();
    try {
      final String pkgName = pkg.getName();
      final URL pkgUrl = getURL(pkgName);
      if(pkgUrl == null) return new Class<?>[0];
      File packageDir;
      try {
        packageDir = new File(pkgUrl.toURI());
      } catch(final IllegalArgumentException ex) {
        packageDir = new File(pkgUrl.getFile());
      }
      if(packageDir.exists()) { // package located on disk (as directory)
        for(final File f : packageDir.listFiles()) {
          String fileName = f.getName();
          if(!fileName.endsWith(".class")) continue;
          fileName = fileName.substring(0, fileName.length() - 6);
          final String clazzName = pkgName + "." + fileName;
          if(clazzName.equals(superClass.getCanonicalName())) continue;
          final Class<?> clazz = Class.forName(clazzName, false,
              ClassLoader.getSystemClassLoader());
          if(superClass.isAssignableFrom(clazz)) {
            foundClasses.add(clazz);
          }
        }
      } else { // package is inside a JAR file
        final JarURLConnection con = (JarURLConnection) pkgUrl.openConnection();
        final JarFile jfile = con.getJarFile();
        final String starts = con.getEntryName();
        final Enumeration<JarEntry> e = jfile.entries();
        while(e.hasMoreElements()) {
          final JarEntry entry = e.nextElement();
          final String name = entry.getName();
          if(name.startsWith(starts)
              && name.lastIndexOf('/') <= starts.length() && // skip sub-pkgs
              name.endsWith(".class")) {
            String classname = name.substring(0, name.length() - 6);
            if(classname.startsWith("/")) classname = classname.substring(1);
            classname = classname.replace('/', '.');
            if(classname.equals(superClass.getCanonicalName())) continue;
            final Class<?> clazz = Class.forName(classname, false,
                ClassLoader.getSystemClassLoader());
            if(superClass.isAssignableFrom(clazz)) {
              foundClasses.add(clazz);
            }
          }
        }
      }
    } catch(final IOException ex) {
      throw ex;
    } catch(final Throwable t) {
      // catch all exceptions and JVM errors and break after the first error
      Util.errln("Failed to load class: %", t);
    }
    // return only the correctly initialized classes
    final int counter = initializeClasses(foundClasses);
    return foundClasses.subList(0, counter).toArray(new Class<?>[counter]);
    // return loadedClasses.toArray(new Class<?>[loadedClasses.size()]);
  }

  /**
   * Initializes the given classes. Breaks after the first error. Subsequent
   * classes are not initialized.
   * @param classes the classes to initialize
   * @return the number of successfully initialized classes (breaks after the
   *         first error)
   */
  private static int initializeClasses(final ArrayList<Class<?>> classes) {
    int counter = 0;
    for(final Class<?> c : classes) {
      try {
        Class.forName(c.getName(), true, ClassLoader.getSystemClassLoader());
        ++counter;
      } catch(final Throwable t) { // catch everything and break after an error
        Util.errln("Failed to load class (%)", t.getMessage());
        break;
      }
    }
    return counter;
  }
}
