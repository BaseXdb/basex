package org.basex.io.serial;

import static org.basex.util.Token.*;
import static org.basex.util.XMLToken.*;

import java.io.*;

import org.basex.query.value.item.*;

/**
 * This class contains the common behavior of XHTML and HTML serializers.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
abstract class XhtmlHtmlSerializer extends MarkupSerializer {
  /**
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @param versions supported versions
   * @throws IOException I/O exception
   */
  protected XhtmlHtmlSerializer(final OutputStream os, final SerializerOptions sopts,
      final String... versions) throws IOException {
    super(os, sopts, versions);
  }

  @Override
  protected final QNm elementName(final QNm name) {
    return mustRewrite(name) ? new QNm(name.local(), name.uri()) : name;
  }

  @Override
  protected final void adjustNamespaces(final QNm name) {
    if(!mustRewrite(name)) return;
    final byte[] prefix = name.prefix();
    boolean unused = true;
    for(final Att att : attributes) {
      final byte[] an = att.name();
      if(startsWith(an, prefix) && indexOf(an, ':') == prefix.length) {
        unused = false;
        break;
      }
    }
    for(int i = namespaces.size() - 1; i >= 0; --i) {
      final byte[] nsName = namespaces.get(i).name();
      if(nsName.length == 0 || unused && eq(nsName, prefix)) namespaces.remove(i);
    }
    addNamespace(EMPTY, name.uri());
  }

  /**
   * Checks if the namespace prefix of an element name must be rewritten.
   * @param name name to be checked
   * @return result of check
   */
  private boolean mustRewrite(final QNm name) {
    return html5 && name.hasPrefix() && eq(name.uri(), XHTML_URI, MATHML_URI, SVG_URI);
  }
}
