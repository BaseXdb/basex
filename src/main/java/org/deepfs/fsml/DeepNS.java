package org.deepfs.fsml;

/**
 * All namespaces used in the DeepFile.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Bastian Lemke
 */
public enum DeepNS {
  /** DeepFS filesystem namespace. */
  DEEPURL("", "http://www.deepfs.org/fs/1.0/"),
  /** DeepFS metadata namespace. */
  FSMETA("", "http://www.deepfs.org/fsmeta/1.0/"),
  /** Dublin core metadata terms namespace. */
  DCTERMS("", "http://purl.org/dc/terms/");

  /** The namespace prefix. */
  private final String prefix;
  /** The namespace URI. */
  private final String uri;

  /**
   * Initializes a namespace instance.
   * @param p the prefix
   * @param u the URI
   */
  DeepNS(final String p, final String u) {
    prefix = p;
    uri = u;
  }

  /**
   * Adds the corresponding namespace prefix to the element.
   * @param elem the xml element to add the namespace prefix to
   * @return the element with namespace prefix
   */
  public String tag(final String elem) {
    return prefix.isEmpty() ? elem : prefix + ':' + elem;
  }

  @Override
  public String toString() {
    final StringBuilder str = new StringBuilder("xmlns");
    if(prefix.length() > 0) str.append(':').append(prefix);
    return str.append("=\"").append(uri).append("\"").toString();
  }
}
