package org.basex.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarFile;

/**
 * Custom class loader for loading jar files. This class is needed because JDK
 * does not offer a fine and easy way to delete open jars. The source code was
 * taken from
 * http://snipplr.com/view/24224/class-loader-which-close-opened-jar-files/ and
 * slightly modified.
 *
 * @author Vitali Yemialyanchyk, www.stopka.us
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class JarLoader extends URLClassLoader {
  /** Jar files to close. */
  private final HashSet<String> setJarFileNames2Close = new HashSet<String>();

  /**
   * Constructor.
   * @param urls of jars to be loaded.
   */
  public JarLoader(final URL[] urls) {
    super(urls);
  }

  /**
   * Closes the class loader.
   */
  @Override
  public void close() {
    setJarFileNames2Close.clear();
    closeClassLoader(this);
    finalizeNativeLibs(this);
    cleanupJarFileFactory();
  }

  /**
   * Cleans up jar file factory cache.
   * @return result
   */
  @SuppressWarnings({ "nls", "unchecked"})
  public boolean cleanupJarFileFactory() {
    boolean res = false;
    Class<?> classJarURLConnection = null;
    try {
      classJarURLConnection =
        Class.forName("sun.net.www.protocol.jar.JarURLConnection");
    } catch(final ClassNotFoundException e) {
      Util.err(e.getMessage());
    }
    if(classJarURLConnection == null) {
      return res;
    }
    Field f = null;
    try {
      f = classJarURLConnection.getDeclaredField("factory");
    } catch(final NoSuchFieldException e) {
      Util.err(e.getMessage());
    }
    if(f == null) {
      return res;
    }
    f.setAccessible(true);
    Object obj = null;
    try {
      obj = f.get(null);
    } catch(final IllegalAccessException e) {
      Util.err(e.getMessage());
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
    } catch(final NoSuchFieldException e) {
      Util.err(e.getMessage());
    } catch(final IllegalAccessException e) {
      Util.err(e.getMessage());
    }
    HashMap<Object, Object> urlCache = null;
    try {
      f = classJarFileFactory.getDeclaredField("urlCache");
      f.setAccessible(true);
      obj = f.get(null);
      if(obj instanceof HashMap) {
        urlCache = (HashMap<Object, Object>) obj;
      }
    } catch(final NoSuchFieldException e) {
      Util.err(e.getMessage());
    } catch(final IllegalAccessException e) {
      Util.err(e.getMessage());
    }
    if(urlCache != null) {
      final HashMap<Object, Object> urlCacheTmp =
        (HashMap<Object, Object>) urlCache.clone();
      final Iterator<Object> it = urlCacheTmp.keySet().iterator();
      while(it.hasNext()) {
        obj = it.next();
        if(!(obj instanceof JarFile)) {
          continue;
        }
        final JarFile jarFile = (JarFile) obj;
        if(setJarFileNames2Close.contains(jarFile.getName())) {
          try {
            jarFile.close();
          } catch(final IOException e) {
            Util.err(e.getMessage());
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
      final Iterator<Object> it = fileCacheTmp.keySet().iterator();
      while(it.hasNext()) {
        final Object key = it.next();
        obj = fileCache.get(key);
        if(!(obj instanceof JarFile)) {
          continue;
        }
        final JarFile jarFile = (JarFile) obj;
        if(setJarFileNames2Close.contains(jarFile.getName())) {
          try {
            jarFile.close();
          } catch(final IOException e) {
            Util.err(e.getMessage());
          }
          fileCache.remove(key);
        }
      }
      res = true;
    }
    setJarFileNames2Close.clear();
    return res;
  }

  /**
   * Closes jar files of class loader.
   * @param cl class loader
   * @return result
   */
  @SuppressWarnings({ "nls", "unchecked"})
  public boolean closeClassLoader(final ClassLoader cl) {
    boolean res = false;
    if(cl == null) {
      return res;
    }
    final Class<?> classURLClassLoader = URLClassLoader.class;
    Field f = null;
    try {
      f = classURLClassLoader.getDeclaredField("ucp");
    } catch(final NoSuchFieldException e1) {
      Util.err(e1.getMessage());
    }
    if(f != null) {
      f.setAccessible(true);
      Object obj = null;
      try {
        obj = f.get(cl);
      } catch(final IllegalAccessException e1) {
        Util.err(e1.getMessage());
      }
      if(obj != null) {
        final Object ucp = obj;
        f = null;
        try {
          f = ucp.getClass().getDeclaredField("loaders");
        } catch(final NoSuchFieldException e1) {
          Util.err(e1.getMessage());
        }
        if(f != null) {
          f.setAccessible(true);
          ArrayList<Object> loaders = null;
          try {
            loaders = (ArrayList<Object>) f.get(ucp);
            res = true;
          } catch(final IllegalAccessException e1) {
            Util.err(e1.getMessage());
          }
          for(int i = 0; loaders != null && i < loaders.size(); i++) {
            obj = loaders.get(i);
            f = null;
            try {
              f = obj.getClass().getDeclaredField("jar");
            } catch(final NoSuchFieldException e) {
              Util.err(e.getMessage());
            }
            if(f != null) {
              f.setAccessible(true);
              try {
                obj = f.get(obj);
              } catch(final IllegalAccessException e1) {
                Util.err(e1.getMessage());
              }
              if(obj instanceof JarFile) {
                final JarFile jarFile = (JarFile) obj;
                setJarFileNames2Close.add(jarFile.getName());
                try {
                  jarFile.close();
                } catch(final IOException e) {
                  Util.err(e.getMessage());
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
   * @param cl class loader
   * @return result
   */
  @SuppressWarnings({ "nls", "unchecked"})
  public boolean finalizeNativeLibs(final ClassLoader cl) {
    boolean res = false;
    final Class<?> classClassLoader = ClassLoader.class;
    java.lang.reflect.Field nativeLibraries = null;
    try {
      nativeLibraries = classClassLoader.getDeclaredField("nativeLibraries");
    } catch(final NoSuchFieldException e1) {
      Util.err(e1.getMessage());
    }
    if(nativeLibraries == null) {
      return res;
    }
    nativeLibraries.setAccessible(true);
    Object obj = null;
    try {
      obj = nativeLibraries.get(cl);
    } catch(final IllegalAccessException e1) {
      Util.err(e1.getMessage());
    }
    if(!(obj instanceof Vector)) {
      return res;
    }
    res = true;
    final Vector<Object> nativeLib = (Vector<Object>) obj;
    for(final Object lib : nativeLib) {
      java.lang.reflect.Method finalize = null;
      try {
        finalize = lib.getClass().getDeclaredMethod("finalize", new Class[0]);
      } catch(final NoSuchMethodException e) {
        Util.err(e.getMessage());
      }
      if(finalize != null) {
        finalize.setAccessible(true);
        try {
          finalize.invoke(lib, new Object[0]);
        } catch(final IllegalAccessException e) {
          Util.err(e.getMessage());
        } catch(final InvocationTargetException e) {
          Util.err(e.getMessage());
        }
      }
    }
    return res;
  }
}
