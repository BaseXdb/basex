package org.basex.query.func.xslt;

import static org.basex.util.Reflect.*;

import java.util.concurrent.*;

import javax.xml.transform.*;

import org.basex.query.func.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
abstract class XsltFn extends StandardFunc {
  /** Transformer cache. */
  static final ConcurrentHashMap<String, Transformer> MAP = new ConcurrentHashMap<>();

  /** XSLT implementations. */
  static final String[] IMPL = {
    "", "Java", "1.0",
    "net.sf.saxon.TransformerFactoryImpl", "Saxon HE", "3.0",
    "com.saxonica.config.ProfessionalTransformerFactory", "Saxon PE", "3.0",
    "com.saxonica.config.EnterpriseTransformerFactory", "Saxon EE", "3.0"
  };
  /** Implementation offset. */
  static final int OFFSET;

  static {
    final String fac = TransformerFactory.class.getName();
    final String impl = System.getProperty(fac);
    // system property has already been set
    if(System.getProperty(fac) != null) {
      // modify processor and version
      IMPL[1] = impl;
      IMPL[2] = "Unknown";
      OFFSET = 0;
    } else {
      // search for existing processors
      int s = IMPL.length - 3;
      while(s != 0 && find(IMPL[s]) == null) s -= 3;
      OFFSET = s;
      // set processor, or use default processor
      if(s != 0) System.setProperty(fac, IMPL[s]);
    }
  }
}
