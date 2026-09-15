package org.basex.io.parse;

import java.io.*;

import org.basex.util.*;

/**
 * Target of XML construction events.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public interface XmlHandler {
  /**
   * Opens an element.
   * @param name element name
   * @param atts attributes
   * @param nsp namespace declarations
   * @throws IOException I/O exception
   */
  void openElem(byte[] name, Atts atts, Atts nsp) throws IOException;

  /**
   * Adds a text node.
   * @param value text
   * @throws IOException I/O exception
   */
  void text(byte[] value) throws IOException;

  /**
   * Adds a comment.
   * @param value comment text
   * @throws IOException I/O exception
   */
  void comment(byte[] value) throws IOException;

  /**
   * Adds a processing instruction.
   * @param pi name and value, separated by a space
   * @throws IOException I/O exception
   */
  void pi(byte[] pi) throws IOException;

  /**
   * Closes the current element.
   * @throws IOException I/O exception
   */
  void closeElem() throws IOException;
}
