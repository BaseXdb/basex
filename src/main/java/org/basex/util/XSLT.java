package org.basex.util;

import java.io.ByteArrayInputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.basex.io.ArrayOutput;
import org.basex.io.IO;

/**
 * This class contains methods to do XSLT transformation with Java's internal
 * XSLT 1.0 processor or Saxon's XSLT 2.0 processor.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class XSLT {
  /**
   * Transforms the specified input.
   * @param in input
   * @param tpl style sheet
   * @param params parameter
   * @return transformed result
   * @throws Exception exception
   */
  public byte[] transform(final IO in, final IO tpl,
      @SuppressWarnings("unused") final TokenMap params)
      throws Exception {

    TransformerFactory tc = TransformerFactory.newInstance();
    Transformer tr =  tc.newTransformer(
        new StreamSource(new ByteArrayInputStream(tpl.content())));

    final ArrayOutput ao = new ArrayOutput();
    tr.transform(new StreamSource(new ByteArrayInputStream(in.content())),
        new StreamResult(ao));

    return ao.toArray();
  }
}
