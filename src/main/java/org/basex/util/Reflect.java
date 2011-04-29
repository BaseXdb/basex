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
  /** Cached methods. */
  private static HashMap<String, Method> methods =
    new HashMap<String, Method>();

  /** Hidden constructor. */
  private Reflect() { }

  /**
   * Checks if the specified package is available.
   * @param pack package name
   * @return result of check
   */
  public static boolean available(final String pack) {
    return Package.getPackage(pack) != null;
  }

  /**
   * Returns a class reference to one of the specified classes, or {@code null}.
   * @param names class names
   * @return reference, or {@code null} if the class is not found
   */
  public static Class<?> find(final String... names) {
    for(final String n : names) {
      try {
        Class<?> c = classes.get(n);
        if(c == null) {
          c = Class.forName(n);
          classes.put(n, c);
        }
        return c;
      } catch(final Exception ex) {
      }
    }
    return null;
  }

  /**
   * Finds a public, protected or private method by name and parameter types.
   * @param clazz class to search for the method
   * @param name method name
   * @param types method parameters
   * @return reference, or {@code null} if the method is not found
   */
  public static Method find(final Class<?> clazz, final String name,
      final Class<?>... types) {

    if(clazz == null) return null;

    final StringBuilder sb = new StringBuilder(clazz.getName());
    for(final Class<?> c : types) sb.append(c.getName());
    final String key = sb.toString();

    Method m = methods.get(key);
    if(m == null) {
      try {
        try {
          m = clazz.getMethod(name, types);
        } catch(final Exception ex) {
          m = clazz.getDeclaredMethod(name, types);
          m.setAccessible(true);
        }
        methods.put(key, m);
      } catch(final Exception ex) {
        Util.debug(ex);
      }
    }
    return m;
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
}
