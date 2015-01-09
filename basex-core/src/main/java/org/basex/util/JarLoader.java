package org.basex.util;

import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

/**
 * Custom class loader for loading jar files. This class is needed because JDK
 * does not offer a fine and easy way to delete open jars:
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5041014
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 * @author Rositsa Shadura
 */
public final class JarLoader extends URLClassLoader {
  /**
   * Constructor.
   * @param urls of jars to be loaded
   * @param parent the parent class loader for delegation
   */
  public JarLoader(final URL[] urls, final ClassLoader parent) {
    super(urls, parent);
  }

  @Override
  public void close() {
    try {
      final Field ucp = URLClassLoader.class.getDeclaredField("ucp");
      ucp.setAccessible(true);
      final Object cp = ucp.get(this);
      final Field loaders = cp.getClass().getDeclaredField("loaders");
      loaders.setAccessible(true);
      for(final Object jl : (Collection<?>) loaders.get(cp)) {
        try {
          final Field jar = jl.getClass().getDeclaredField("jar");
          jar.setAccessible(true);
          ((ZipFile) jar.get(jl)).close();
        } catch(final Throwable th) {
          Util.errln(th);
        }
      }
    } catch(final Throwable th) {
      Util.errln(th);
    }
  }
}
