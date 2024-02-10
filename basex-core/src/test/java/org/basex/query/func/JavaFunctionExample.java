package org.basex.query.func;

import java.util.*;

import javax.xml.parsers.*;

import org.basex.util.list.*;
import org.w3c.dom.*;

/**
 * Java binding example.
 *
 * @author BaseX Team 2005-24, BSD License
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

  /**
   * Creates a document.
   * @return document
   * @throws Exception exception
   */
  public Document doc() throws Exception {
    return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
  }

  /**
   * Creates a text node.
   * @return text node
   * @throws Exception exception
   */
  public Node text() throws Exception {
    return doc().createTextNode("t");
  }

  /**
   * Creates a processing instruction.
   * @return processing instruction
   * @throws Exception exception
   */
  public Node pi() throws Exception {
    return doc().createProcessingInstruction("a", "b");
  }

  /**
   * Creates a comment.
   * @return comment
   * @throws Exception exception
   */
  public Node comment() throws Exception {
    return doc().createComment("c");
  }

  /**
   * Creates an element.
   * @return element
   * @throws Exception exception
   */
  public Node element() throws Exception {
    return doc().createElement("e");
  }

  /**
   * Creates an attribute.
   * @return attribute
   * @throws Exception exception
   */
  public Node attribute() throws Exception {
    return doc().createAttribute("a");
  }
}
