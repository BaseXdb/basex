package org.basex.gui.text;

/**
 * Definition of a function or variable that is referenced in a text.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param location location of the imported module that contains the definition
 *   ({@code null}: the definition is contained in the text)
 * @param name name of the function or variable (variables are prefixed with a dollar sign)
 * @param pos position of the name in the text ({@code -1}: unknown)
 * @param start start of the referencing name
 * @param end end of the referencing name
 */
record Definition(String location, String name, int pos, int start, int end) {
  /**
   * Indicates if the definition was found.
   * @return result of check
   */
  boolean found() {
    return location != null || pos != -1;
  }
}
