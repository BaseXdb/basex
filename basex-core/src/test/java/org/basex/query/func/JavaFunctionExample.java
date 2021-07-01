package org.basex.query.func;

import java.util.*;

import org.basex.util.list.*;

/**
 * Java binding example.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class JavaFunctionExample {
  /** Value. */
  Boolean var;

  /**
   * Constructor.
   */
  public JavaFunctionExample() {
  }

  /**
   * Constructor.
   * @param var argument
   */
  public JavaFunctionExample(final boolean var) {
    this.var = var;
  }

  /**
   * Constructor.
   * @param var argument
   */
  public JavaFunctionExample(final Boolean var) {
    this.var = var;
  }

  /**
   * Returns a string.
   * @param string argument
   * @return parameter
   */
  public String string(final String string) {
    return string;
  }

  /**
   * Returns a boolean.
   * @param bool argument
   * @return parameter
   */
  public boolean bool(final boolean bool) {
    return bool;
  }

  /**
   * Returns a boolean.
   * @param bool argument
   * @return parameter
   */
  public boolean ambiguous1(final boolean bool) {
    return bool;
  }

  /**
   * Returns a boolean object.
   * @param bool argument
   * @return parameter
   */
  public Boolean ambiguous1(final Boolean bool) {
    return bool;
  }

  /**
   * Returns a string.
   * @param string argument
   * @return parameter
   */
  public String ambiguous2(final String string) {
    return string;
  }

  /**
   * Returns an integer object.
   * @param integer argument
   * @return parameter
   */
  public Object ambiguous2(final Integer integer) {
    return integer;
  }

  /**
   * Returns an array with a null value.
   * @return array
   */
  public Object nullArray() {
    return new Object[] { null };
  }

  /**
   * Returns an array with a null value.
   * @param strings array argument
   * @return array
   */
  public String[] strings(final String[] strings) {
    return strings;
  }

  /**
   * Returns an array with a null value.
   * @param longs array argument
   * @return array
   */
  public long[] longs(final long[] longs) {
    return longs;
  }

  /**
   * Returns a character array.
   * @return array
   */
  public char[] chars() {
    return new char[] { 'a', 'b' };
  }

  /**
   * Returns an array with a null value.
   * @return array
   */
  public Object data() {
    final ArrayList<Object> list = new ArrayList<>();
    list.add("a");
    final StringList sl = new StringList();
    sl.add("b");
    list.add(sl);
    final HashSet<String> set = new HashSet<>();
    set.add("c");
    list.add(set);
    final HashMap<String, String> map = new HashMap<>();
    map.put("d", "e");
    list.add(map);
    return new Object[] { list, "f" };
  }

  /**
   * Throws an exception.
   */
  public static void error() {
    throw new RuntimeException("ERROR");
  }
}
