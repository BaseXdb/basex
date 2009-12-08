package org.deepfs.fsml;

/** All namespaces used in the DeepFile. */
public enum DeepNS {

  /** XML schema namespace. */
  XS("xs", "http://www.w3.org/2001/XMLSchema"),

  /** XML schema instance namespace. */
  XSI("xsi", "http://www.w3.org/2001/XMLSchema-instance"),

  /** DeepFS filesystem namespace. */
  DEEPURL("", "http://www.deepfs.org/fs/1.0/"),

  /** DeepFS metadata namespace. */
  FSMETA("", "http://www.deepfs.org/fsmeta/1.0/"),

  /** Dublin Core metadata terms namespace. */
  DCTERMS("", "http://purl.org/dc/terms/");

  /** The namespace prefix. */
  private String prefix;
  /** The namespace URI. */
  private String uri;

  /**
   * Initializes a namespace instance.
   * @param p the prefix
   * @param u the URI
   */
  DeepNS(final String p, final String u) {
    prefix = p;
    uri = u;
  }

  @Override
  public String toString() {
    final StringBuilder str = new StringBuilder("xmlns");
    if(prefix.length() > 0) str.append(':').append(prefix);
    str.append("=\"").append(uri).append("\"");
    return str.toString();
  }

  /**
   * Adds the corresponding namespace prefix to the element.
   * @param element the xml element to add the namespace prefix to
   * @return the element with namespace prefix
   */
  public String tag(final String element) {
    return prefix.equals("") ? element : prefix + ':' + element;
  }
}