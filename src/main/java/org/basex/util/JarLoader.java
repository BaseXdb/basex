package org.basex.util;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

/**
 * Custom class loader for loading jar files. This class is needed because JDK
 * does not offer a fine and easy way to delete open jars. The source code was
 * taken from
 * {@code http://snipplr.com/view/24224/class-loader-which-close-opened-jar-files/}
 * and slightly modified.
 *
 * @author Vitali Yemialyanchyk, {@code http://www.stopka.us}
 * @author BaseX Team 2005-12, BSD License
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

  /**
   * Closes the class loader.
   */
  public void close() {
    final HashSet<String> files = new HashSet<String>();
    closeClassLoader(files);
    finalizeNativeLibs();
    cleanupJarFileFactory(files);

    final ClassLoader cl = getParent();
    if(cl instanceof JarLoader) ((JarLoader) cl).close();
  }

  /**
   * Closes jar files of class loader.
   * @param files files to be closed
   * @return result
   */
  @SuppressWarnings("unchecked")
  private boolean closeClassLoader(final HashSet<String> files) {
    boolean res = false;
    final Class<?> classURLClassLoader = URLClassLoader.class;
    Field f = null;
    try {
      f = classURLClassLoader.getDeclaredField("ucp");
    } catch(final NoSuchFieldException ex) {
      Util.errln(ex);
    }
    if(f != null) {
      f.setAccessible(true);
      Object obj = null;
      try {
        obj = f.get(this);
      } catch(final IllegalAccessException ex) {
        Util.errln(ex);
      }
      if(obj != null) {
        final Object ucp = obj;
        f = null;
        try {
          f = ucp.getClass().getDeclaredField("loaders");
        } catch(final NoSuchFieldException ex) {
          Util.errln(ex);
        }
        if(f != null) {
          f.setAccessible(true);
          ArrayList<Object> loaders = null;
          try {
            loaders = (ArrayList<Object>) f.get(ucp);
            res = true;
          } catch(final IllegalAccessException ex) {
            Util.errln(ex);
          }
          for(int i = 0; loaders != null && i < loaders.size(); i++) {
            obj = loaders.get(i);
            f = null;
            try {
              f = obj.getClass().getDeclaredField("jar");
            } catch(final NoSuchFieldException ex) {
              Util.errln(ex);
            }
            if(f != null) {
              f.setAccessible(true);
              try {
                obj = f.get(obj);
              } catch(final IllegalAccessException ex) {
                Util.errln(ex);
              }
              if(obj instanceof JarFile) {
                final JarFile jarFile = (JarFile) obj;
                files.add(jarFile.getName());
                try {
                  jarFile.close();
                } catch(final IOException ex) {
                  Util.errln(ex);
                }
              }
            }
          }
        }
      }
    }
    return res;
  }

  /**
   * Finalizes native libraries.
   * @return result
   */
  @SuppressWarnings("unchecked")
  private boolean finalizeNativeLibs() {
    boolean res = false;
    final Class<?> classClassLoader = ClassLoader.class;
    Field nativeLibraries = null;
    try {
      nativeLibraries = classClassLoader.getDeclaredField("nativeLibraries");
    } catch(final NoSuchFieldException ex) {
      Util.errln(ex);
    }
    if(nativeLibraries == null) {
      return res;
    }
    nativeLibraries.setAccessible(true);
    Object obj = null;
    try {
      obj = nativeLibraries.get(this);
    } catch(final IllegalAccessException ex) {
      Util.errln(ex);
    }
    if(!(obj instanceof Vector)) {
      return res;
    }
    res = true;
    final Vector<Object> nativeLib = (Vector<Object>) obj;
    for(final Object lib : nativeLib) {
      Method finalize = null;
      try {
        finalize = lib.getClass().getDeclaredMethod("finalize", new Class[0]);
      } catch(final NoSuchMethodException ex) {
        Util.errln(ex);
      }
      if(finalize != null) {
        finalize.setAccessible(true);
        try {
          finalize.invoke(lib);
        } catch(final Exception ex) {
          Util.errln(ex);
        }
      }
    }
    return res;
  }

  /**
   * Cleans up jar file factory cache.
   * @param files files to be closed
   * @return result
   */
  @SuppressWarnings("unchecked")
  private boolean cleanupJarFileFactory(final HashSet<String> files) {
    boolean res = false;
    Class<?> classJarURLConnection = null;
    try {
      classJarURLConnection =
        Class.forName("sun.net.www.protocol.jar.JarURLConnection");
    } catch(final ClassNotFoundException ex) {
      Util.errln(ex);
    }
    if(classJarURLConnection == null) {
      return res;
    }
    Field f = null;
    try {
      f = classJarURLConnection.getDeclaredField("factory");
    } catch(final NoSuchFieldException ex) {
      Util.errln(ex);
    }
    if(f == null) {
      return res;
    }
    f.setAccessible(true);
    Object obj = null;
    try {
      obj = f.get(null);
    } catch(final IllegalAccessException ex) {
      Util.errln(ex);
    }
    if(obj == null) {
      return res;
    }
    final Class<?> classJarFileFactory = obj.getClass();
    HashMap<Object, Object> fileCache = null;
    try {
      f = classJarFileFactory.getDeclaredField("fileCache");
      f.setAccessible(true);
      obj = f.get(null);
      if(obj instanceof HashMap) {
        fileCache = (HashMap<Object, Object>) obj;
      }
    } catch(final NoSuchFieldException ex) {
      Util.errln(ex);
    } catch(final IllegalAccessException ex) {
      Util.errln(ex);
    }
    HashMap<Object, Object> urlCache = null;
    try {
      f = classJarFileFactory.getDeclaredField("urlCache");
      f.setAccessible(true);
      obj = f.get(null);
      if(obj instanceof HashMap) {
        urlCache = (HashMap<Object, Object>) obj;
      }
    } catch(final NoSuchFieldException ex) {
      Util.errln(ex);
    } catch(final IllegalAccessException ex) {
      Util.errln(ex);
    }
    if(urlCache != null) {
      final HashMap<Object, Object> urlCacheTmp =
        (HashMap<Object, Object>) urlCache.clone();
      for(final Object o : urlCacheTmp.keySet()) {
        obj = o;
        if(!(obj instanceof JarFile)) {
          continue;
        }
        final JarFile jarFile = (JarFile) obj;
        if(files.contains(jarFile.getName())) {
          try {
            jarFile.close();
          } catch(final IOException ex) {
            Util.errln(ex);
          }
          if(fileCache != null) {
            fileCache.remove(urlCache.get(jarFile));
          }
          urlCache.remove(jarFile);
        }
      }
      res = true;
    } else if(fileCache != null) {
      // urlCache := null
      final HashMap<Object, Object> fileCacheTmp =
        (HashMap<Object, Object>) fileCache.clone();
      for(final Object key : fileCacheTmp.keySet()) {
        obj = fileCache.get(key);
        if(!(obj instanceof JarFile)) {
          continue;
        }
        final JarFile jarFile = (JarFile) obj;
        if(files.contains(jarFile.getName())) {
          try {
            jarFile.close();
          } catch(final IOException ex) {
            Util.errln(ex);
          }
          fileCache.remove(key);
        }
      }
      res = true;
    }
    files.clear();
    return res;
  }
}
