package org.basex.util;

import java.lang.reflect.*;

/**
 * This class assembles some reflection methods. Lookups return {@code null} if a class or method
 * is not found; failing instantiations and invocations raise runtime exceptions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Reflect {
  /** Hidden constructor. */
  private Reflect() { }

  /**
   * Reports a failed lookup, unless the class or method was simply not found.
   * @param th throwable
   */
  private static void unexpected(final Throwable th) {
    if(!(th instanceof ClassNotFoundException || th instanceof NoSuchMethodException)) {
      Util.debug(th);
    }
  }

  /**
   * Returns a reference to the specified class.
   * @param name fully qualified class name
   * @return reference, or {@code null} if the class is not found
   */
  public static Class<?> find(final String name) {
    try {
      return forName(name);
    } catch(final Throwable ex) {
      unexpected(ex);
      return null;
    }
  }

  /**
   * Returns a reference to the specified class, or throws an exception.
   * @param name fully qualified class name
   * @return class reference
   * @throws ClassNotFoundException class not found
   */
  public static Class<?> forName(final String name) throws ClassNotFoundException {
    final Class<?> c = Class.forName(name);
    if(!Modifier.isPublic(c.getModifiers())) throw new ClassNotFoundException(name);
    return c;
  }

  /**
   * Returns a reference to the class specified by the pattern.
   * @param pattern class pattern
   * @param ext optional extension
   * @return reference or {@code null} if the class is not found
   */
  public static Class<?> find(final String pattern, final Object... ext) {
    return find(Util.info(pattern, ext));
  }

  /**
   * Finds a public, protected or private method by name and parameter types.
   * @param clazz class to search for the method (can be {@code null})
   * @param name method name
   * @param types method parameters
   * @return method, or {@code null} if the method is not found
   */
  public static Method method(final Class<?> clazz, final String name, final Class<?>... types) {
    if(clazz == null) return null;
    Method m = null;
    try {
      try {
        m = clazz.getMethod(name, types);
      } catch(final Throwable ex) {
        unexpected(ex);
        // lucene-stemmers: stem() is protected in BrazilianStemmer, FrenchStemmer, GermanStemmer
        m = clazz.getDeclaredMethod(name, types);
        m.setAccessible(true);
      }
    } catch(final Throwable ex) {
      unexpected(ex);
    }
    return m;
  }

  /**
   * Returns a class instance.
   * @param clazz class
   * @param <O> type
   * @return instance
   */
  public static <O> O get(final Class<O> clazz) {
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch(final Throwable ex) {
      throw Util.notExpected(ex);
    }
  }

  /**
   * Invokes the specified method.
   * @param method method to run
   * @param object object ({@code null} for static methods)
   * @param args arguments
   * @return result of method call
   */
  public static Object invoke(final Method method, final Object object, final Object... args) {
    try {
      return method.invoke(object, args);
    } catch(final Throwable ex) {
      throw Util.notExpected(ex);
    }
  }
}
