package org.basex.gui.text;

/**
 * Declaration of a function or variable in a text.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param name name of the declaration, preceded by its annotations
 * @param args parameter names of a function, enclosed in parentheses (empty for a variable)
 * @param pos position of the name in the text
 * @param line line in which the declaration occurs
 */
public record Declaration(String name, String args, int pos, int line) {
  /**
   * Returns the name without namespace prefix (variables keep their dollar sign).
   * @return local name
   */
  public String local() {
    final int c = name.indexOf(':');
    return c == -1 ? name : (name.startsWith("$") ? "$" : "") + name.substring(c + 1);
  }

  @Override
  public String toString() {
    return name;
  }
}
