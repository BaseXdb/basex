package org.basex.util.ft;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.EnumSet;

/**
 * Functions for judging which classes (eg. tokenizers, stemmers) match to
 * chosen language.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Jens Erat
 */
abstract class LanguageImpl implements Comparable<LanguageImpl> {
  @Override
  public final int compareTo(final LanguageImpl o) {
    // Higher precedence value = better
    return o.prec() - prec();
  }

  /**
   * Returns the precedence of the processor.
   * @return precedence
   */
  abstract int prec();

  /**
   * Checks if the specified language is supported.
   * @param ln language
   * @return true if language is supported
   */
  public boolean supports(final Language ln) {
    for(final Language lt : languages()) if(ln == lt) return true;
    return false;
  }

  /**
   * Returns the supported languages.
   * @return languages
   */
  abstract EnumSet<Language> languages();

  /**
   * Find a class by name.
   * @param name class name
   * @return {@code null} if the class is not found
   */
  static Class<?> findClass(final String name) {
    try {
      return Class.forName(name);
    } catch(final Exception e) {
      return null;
    }
  }

  /**
   * Find a method by name and parameter types.
   * @param c class to search for the method
   * @param name method name
   * @param parameterTypes method parameters
   * @return {@code null} if the class is not found
   */
  static Method findMethod(final Class<?> c, final String name,
      final Class<?>... parameterTypes) {

    if(c == null) return null;
    try {
      try {
        return c.getMethod(name, parameterTypes);
      } catch(final Exception e) {
        final Method m = c.getDeclaredMethod(name, parameterTypes);
        m.setAccessible(true);
        return m;
      }
    } catch(final Exception e) {
      return null;
    }
  }

  /**
   * Find a constructor by parameter types.
   * @param c class to search for the constructor
   * @param parameterTypes constructor parameters
   * @return {@code null} if the class is not found
   */
  static Constructor<?> findConstructor(final Class<?> c,
      final Class<?>... parameterTypes) {

    if(c == null) return null;
    try {
      return c.getConstructor(parameterTypes);
    } catch(final Exception e) {
      return null;
    }
  }
}
