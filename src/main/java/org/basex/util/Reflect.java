package org.basex.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * This class assembles some reflection methods. If exceptions occur, a
 * {@code null} reference is returned or a runtime exception is thrown.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Reflect {
  /** Cached constructors. */
  private static final HashMap<String, Constructor<?>> CONS =
    new HashMap<String, Constructor<?>>();
  /** Cached classes. */
  private static final HashMap<String, Class<?>> CLASSES =
    new HashMap<String, Class<?>>();
  /** Cached fields. */
  private static final HashMap<String, Field> FIELDS =
      new HashMap<String, Field>();

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
   * Returns a reference to the specified class, or {@code null}.
   * @param name class name
   * @return reference, or {@code null} if the class is not found
   */
  public static Class<?> find(final String name) {
    final Class<?> c = CLASSES.get(name);
    if(c != null) return c;
    try {
      return cache(name, Class.forName(name));
    } catch(final Throwable ex) {
      return null;
    }
  }

  /**
   * Returns a reference to a class located in the specified jar loader,
   * or {@code null}.
   * @param name class name
   * @param jar jar loader
   * @return reference, or {@code null} if the class is not found
   */
  public static Class<?> find(final String name, final JarLoader jar) {
    try {
      return cache(name, Class.forName(name, true, jar));
    } catch(final Throwable ex) {
      return null;
    }
  }

  /**
   * Caches the specified class.
   * @param name class name
   * @param c class
   * @return reference, or {@code null} if the class is not found
   */
  private static Class<?> cache(final String name, final Class<?> c) {
    try {
      if(!Reflect.accessible(c)) return null;
      CLASSES.put(name, c);
    } catch(final Throwable ex) { }
    return c;
  }

  /**
   * Returns a reference to the specified field, or {@code null}.
   * @param clazz class to search for the constructor
   * @param name class name
   * @return reference, or {@code null} if the field is not found
   */
  public static Field field(final Class<?> clazz, final String name) {
    final String key = clazz.getName() + name;
    Field f = FIELDS.get(key);
    if(f == null) {
      try {
        f = clazz.getField(name);
        FIELDS.put(key, f);
      } catch(final Throwable ex) { }
    }
    return f;
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

    Constructor<?> m = CONS.get(key);
    if(m == null) {
      try {
        try {
          m = clazz.getConstructor(types);
        } catch(final Throwable ex) {
          m = clazz.getDeclaredConstructor(types);
          m.setAccessible(true);
        }
        CONS.put(key, m);
      } catch(final Throwable ex) {
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
      } catch(final Throwable ex) {
        m = clazz.getDeclaredMethod(name, types);
        m.setAccessible(true);
      }
    } catch(final Throwable ex) {
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
    } catch(final Throwable ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Returns a class instance, or {@code null}.
   * @param clazz class
   * @param args arguments
   * @return instance
   */
  public static Object get(final Constructor<?> clazz, final Object... args) {
    try {
      return clazz != null ? clazz.newInstance(args) : null;
    } catch(final Throwable ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Invokes the specified method.
   * @param method method to run
   * @param object object ({@code null} for static methods)
   * @param args arguments
   * @return result of method call
   */
  public static Object invoke(final Method method, final Object object,
      final Object... args) {

    try {
      return method != null ? method.invoke(object, args) : null;
    } catch(final Throwable ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Check if a class is accessible.
   * @param cls class
   * @return {@code true} if a class is accessible
   */
  private static boolean accessible(final Class<?> cls) {
    // non public classes cannot be instantiated
    return Modifier.isPublic(cls.getModifiers());
  }
}
