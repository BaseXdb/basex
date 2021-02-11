package org.basex.util;

import java.lang.reflect.*;
import java.util.*;

/**
 * This class assembles some reflection methods. Most exceptions are caught and replaced
 * by a {@code null} value.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Reflect {
  /** Cached constructors. */
  private static final HashMap<String, Constructor<?>> CONSTRUCTORS = new HashMap<>();
  /** Cached classes. */
  private static final HashMap<String, Class<?>> CLASSES = new HashMap<>();
  /** Cached fields. */
  private static final HashMap<String, Field> FIELDS = new HashMap<>();

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
      forName(Util.info(pattern, ext));
      return true;
    } catch(final Throwable ex) {
      Util.debug(ex);
      return false;
    }
  }

  /**
   * Caches and returns a reference to the specified class.
   * @param name fully qualified class name
   * @return reference, or {@code null} if the class is not found
   */
  public static Class<?> find(final String name) {
    try {
      return forName(name);
    } catch(final Throwable ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Caches and returns a reference to the specified class, or throws an exception.
   * @param name fully qualified class name
   * @return class reference
   * @throws ClassNotFoundException any exception or error
   */
  public static Class<?> forName(final String name) throws ClassNotFoundException {
    Class<?> c = CLASSES.get(name);
    if(c == null) {
      c = Class.forName(name);
      if(!Modifier.isPublic(c.getModifiers())) throw new ClassNotFoundException(name);
      CLASSES.put(name, c);
    }
    return c;
  }

  /**
   * Caches and returns a reference to the specified field or {@code null}.
   * @param clazz class to search for the constructor
   * @param name field name
   * @return reference or {@code null} if the field is not found
   */
  public static Field field(final Class<?> clazz, final String name) {
    final String key = clazz.getName() + name;
    Field f = FIELDS.get(key);
    if(f == null) {
      try {
        f = clazz.getField(name);
        FIELDS.put(key, f);
      } catch(final Throwable ignored) { }
    }
    return f;
  }

  /**
   * Caches and returns a reference to the class specified by the pattern,
   * or {@code null}.
   * @param pattern class pattern
   * @param ext optional extension
   * @return reference or {@code null} if the class is not found
   */
  public static Class<?> find(final String pattern, final Object... ext) {
    return find(Util.info(pattern, ext));
  }

  /**
   * Caches and returns a constructor by parameter types.
   * @param clazz class to search for the constructor
   * @param types constructor parameters
   * @param <O> class type
   * @return constructor, or {@code null} if the constructor is not found
   */
  public static <O> Constructor<O> find(final Class<O> clazz, final Class<?>... types) {
    if(clazz == null) return null;

    final StringBuilder sb = new StringBuilder(clazz.getName());
    for(final Class<?> c : types) sb.append(c.getName());
    final String key = sb.toString();

    @SuppressWarnings("unchecked")
    Constructor<O> m = (Constructor<O>) CONSTRUCTORS.get(key);
    if(m == null) {
      try {
        try {
          m = clazz.getConstructor(types);
        } catch(final Throwable ex) {
          Util.debug(ex);
          m = clazz.getDeclaredConstructor(types);
          m.setAccessible(true);
        }
        CONSTRUCTORS.put(key, m);
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
   * @return method, or {@code null} if the method is not found
   */
  public static Method method(final Class<?> clazz, final String name, final Class<?>... types) {
    if(clazz == null) return null;
    Method m = null;
    try {
      try {
        m = clazz.getMethod(name, types);
      } catch(final Throwable ex) {
        Util.debug(ex);
        m = clazz.getDeclaredMethod(name, types);
        m.setAccessible(true);
      }
    } catch(final Throwable ex) {
      Util.debug(ex);
    }
    return m;
  }

  /**
   * Returns a class instance.
   * @param clazz class
   * @param <O> type
   * @return instance or {@code null}
   */
  public static <O> O get(final Class<O> clazz) {
    try {
      return clazz != null ? clazz.getDeclaredConstructor().newInstance() : null;
    } catch(final Throwable ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Returns a class instance or {@code null}.
   * @param clazz class
   * @param args arguments
   * @param <O> class type
   * @return instance or {@code null}
   */
  public static <O> O get(final Constructor<O> clazz, final Object... args) {
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
   * @return result of method call or {@code null}
   */
  public static Object invoke(final Method method, final Object object, final Object... args) {
    try {
      return method != null ? method.invoke(object, args) : null;
    } catch(final Throwable ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Returns the value of a field.
   * @param field field to access
   * @param object object ({@code null} for static methods)
   * @return value of field
   */
  public static Object get(final Field field, final Object object) {
    try {
      return field != null ? field.get(object) : null;
    } catch(final Throwable ex) {
      Util.debug(ex);
      return null;
    }
  }
}
