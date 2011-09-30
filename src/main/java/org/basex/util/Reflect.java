package org.basex.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * This class assembles some reflection methods. If exceptions occur, a
 * {@code null} reference is returned or a runtime exception is thrown.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Reflect {
  /** Cached constructors. */
  private static HashMap<String, Constructor<?>> cons =
    new HashMap<String, Constructor<?>>();
  /** Cached classes. */
  private static HashMap<String, Class<?>> classes =
    new HashMap<String, Class<?>>();
  /** Class loader for jars. */
  public static JarLoader jarLoader;

  /** Hidden constructor. */
  private Reflect() { }

  /**
   * Checks if the class specified by the pattern is available.
   * @param pattern class pattern
   * @param ext optional extension
   * @return result of check
   */
  public static boolean available(final String pattern, final Object... ext) {
    try {
      Class.forName(Util.info(pattern, ext));
      return true;
    } catch(final ClassNotFoundException ex) {
      return false;
    }
  }

  /**
   * Returns a class reference to the specified class, or {@code null}.
   * @param name class name
   * @return reference, or {@code null} if the class is not found
   */
  public static Class<?> find(final String name) {
    Class<?> c = classes.get(name);
    try {
      if(c == null) {
        try {
          c = Class.forName(name);
          classes.put(name, c);
          return c;
        } catch(final ClassNotFoundException ex) {
          if(jarLoader != null) {
            c = Class.forName(name, true, jarLoader);
            classes.put(name, c);
          }
          return c;
        }
      }
      return c;
    } catch(final Exception ex) {
    }
    return null;
  }


  /**
   * Returns a reference to the class specified by the pattern, or {@code null}.
   * @param pattern class pattern
   * @param ext optional extension
   * @return reference, or {@code null} if the class is not found
   */
  public static Class<?> find(final String pattern, final Object... ext) {
    return find(Util.info(pattern, ext));
  }

  /**
   * Returns a class reference to one of the specified classes, or {@code null}.
   * @param names class names
   * @return reference, or {@code null} if the class is not found
   */
  public static Class<?> find(final String[] names) {
    for(final String n : names) {
      final Class<?> c = find(n);
      if(c != null) return c;
    }
    return null;
  }

  /**
   * Finds a constructor by parameter types.
   * @param clazz class to search for the constructor
   * @param types constructor parameters
   * @return {@code null} if the class is not found
   */
  public static Constructor<?> find(final Class<?> clazz,
      final Class<?>... types) {

    if(clazz == null) return null;

    final StringBuilder sb = new StringBuilder(clazz.getName());
    for(final Class<?> c : types) sb.append(c.getName());
    final String key = sb.toString();

    Constructor<?> m = cons.get(key);
    if(m == null) {
      try {
        try {
          m = clazz.getConstructor(types);
        } catch(final Exception ex) {
          m = clazz.getDeclaredConstructor(types);
          m.setAccessible(true);
        }
        cons.put(key, m);
      } catch(final Exception ex) {
        Util.debug(ex);
      }
    }
    return m;
  }

  /**
   * Finds a public, protected or private method by name and parameter types.
   * @param clazz class to search for the method
   * @param name method name
   * @param types method parameters
   * @return reference, or {@code null} if the method is not found
   */
  public static Method method(final Class<?> clazz, final String name,
      final Class<?>... types) {

    if(clazz == null) return null;

    Method m = null;
    try {
      try {
        m = clazz.getMethod(name, types);
      } catch(final Exception ex) {
        m = clazz.getDeclaredMethod(name, types);
        m.setAccessible(true);
      }
      //methods.put(key, m);
    } catch(final Exception ex) {
      Util.debug(ex);
    }
    return m;
  }

  /**
   * Returns a class instance, or throws a runtime exception.
   * @param clazz class
   * @return instance
   */
  public static Object get(final Class<?> clazz) {
    try {
      return clazz != null ? clazz.newInstance() : null;
    } catch(final Exception ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Returns a class instance, or throws a runtime exception.
   * @param clazz class
   * @param args arguments
   * @return instance
   */
  public static Object get(final Constructor<?> clazz, final Object... args) {
    try {
      return clazz != null ? clazz.newInstance(args) : null;
    } catch(final Exception ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Invoked the specified method.
   * @param method method to run
   * @param object object ({@code null} for static methods)
   * @param args arguments
   * @return result of method call
   */
  public static Object invoke(final Method method, final Object object,
      final Object... args) {

    try {
      return method != null ? method.invoke(object, args) : null;
    } catch(final Exception ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Invoked the specified method.
   * @param object object
   * @param method method to run
   * @param args arguments
   * @return result of method call
   * @throws Exception exception
   */
  public static Object invoke(final Object object, final String method,
      final Object... args) throws Exception {

    if(object == null) return null;

    final Class<?>[] clz = new Class<?>[args.length];
    for(int a = 0; a < args.length; a++) clz[a] = args[a].getClass();

    final Class<?> c = object.getClass();
    Method m = method(c, method, clz);

    if(m == null) {
      // method not found: replace arguments with first interfaces
      for(int a = 0; a < args.length; a++) {
        final Class<?>[] ic = clz[a].getInterfaces();
        if(ic.length != 0) clz[a] = ic[0];
      }
      m = method(c, method, clz);

      while(m == null) {
        // method not found: replace arguments with super classes
        boolean same = true;
        for(int a = 0; a < args.length; a++) {
          final Class<?> ic = clz[a].getSuperclass();
          if(ic != null && ic != Object.class) {
            clz[a] = ic;
            same = false;
          }
        }
        if(same) return null;
        m = method(c, method, clz);
      }
    }
    return m.invoke(object, args);
  }

  /**
   * Sets the class loader for jars.
   * @param l loader
   */
  public static void setJarLoader(final JarLoader l) {
    jarLoader = l;
  }
}
