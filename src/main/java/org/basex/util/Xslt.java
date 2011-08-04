package org.basex.util;

import static org.basex.util.Reflect.*;
import static org.basex.util.Token.*;

import java.io.ByteArrayInputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.basex.io.IO;
import org.basex.io.out.ArrayOutput;
import org.basex.util.hash.TokenObjMap;

/**
 * This class performs XSLT transformations with Java's internal
 * XSLT 1.0 processor or Saxon's XSLT 2.0 processor.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Xslt {
  /** Saxon factory class. */
  public static final String SAXONIMPL = "net.sf.saxon.TransformerFactoryImpl";
  /** Saxon flag. */
  public static final boolean SAXON = find(SAXONIMPL) != null;

  static {
    if(SAXON) System.setProperty(TransformerFactory.class.getName(), SAXONIMPL);
  }

  /**
   * Uses Java's XSLT implementation to perform an XSL transformation.
   * @param in input
   * @param xsl style sheet
   * @param par parameters
   * @return transformed result
   * @throws Exception exception
   */
  public byte[] transform(final IO in, final IO xsl,
      final TokenObjMap<Object> par) throws Exception {

    // create transformer
    final TransformerFactory tc = TransformerFactory.newInstance();
    final Transformer tr =  tc.newTransformer(
        new StreamSource(new ByteArrayInputStream(xsl.content())));

    // bind parameters
    for(final byte[] key : par) tr.setParameter(string(key), par.get(key));

    // create serializer
    final ArrayOutput ao = new ArrayOutput();

    // do transformation and return result
    tr.transform(new StreamSource(new ByteArrayInputStream(in.content())),
        new StreamResult(ao));
    return ao.toArray();
  }
}
