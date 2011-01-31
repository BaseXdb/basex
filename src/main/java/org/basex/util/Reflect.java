package org.basex.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * This class assembles some reflection methods. If exceptions occur, a
 * {@code null} reference is returned or a runtime exception is thrown.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class Reflect {
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
        return Class.forName(n);
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

    try {
      try {
        return clazz.getMethod(name, types);
      } catch(final Exception ex) {
        final Method m = clazz.getDeclaredMethod(name, types);
        m.setAccessible(true);
        return m;
      }
    } catch(final Exception ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Finds a constructor by parameter types.
   * @param clazz class to search for the constructor
   * @param types constructor parameters
   * @return {@code null} if the class is not found
   */
  public static Constructor<?> find(final Class<?> clazz,
      final Class<?>... types) {
    try {
      try {
        return clazz.getConstructor(types);
      } catch(final Exception ex) {
        final Constructor<?> m = clazz.getDeclaredConstructor(types);
        m.setAccessible(true);
        return m;
      }
    } catch(final Exception ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Returns a class instance, or throws a runtime exception.
   * @param clazz class
   * @return instance
   */
  public static Object get(final Class<?> clazz) {
    try {
      return clazz.newInstance();
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
      return clazz.newInstance(args);
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
      return method.invoke(object, args);
    } catch(final Exception ex) {
      Util.debug(ex);
      return null;
    }
  }
}
