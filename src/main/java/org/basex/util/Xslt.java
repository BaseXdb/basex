package org.basex.util;

import static org.basex.util.Reflect.*;
import static org.basex.util.Token.*;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
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
  /** Saxon API. */
  private static final String S9API = "net.sf.saxon.s9api.";
  /** Saxon processor. */
  private static final Constructor<?> SAXONPROC =
    find(find(S9API + "Processor"), boolean.class);
  /** Saxon QName. */
  private static final Constructor<?> SAXONQNAME =
    find(find(S9API + "QName"), String.class);
  /** Saxon QName. */
  private static final Constructor<?> SAXONVALUE =
    find(find(S9API + "XdmAtomicValue"), String.class);
  /** Saxon Serializer. */
  private static final Constructor<?> SAXONSER =
    find(find(S9API + "Serializer"), OutputStream.class);
  /** Saxon flag. */
  public static final boolean SAXON = SAXONPROC != null;

  /**
   * Uses Java's XSLT implementation to perform an XSL transformation.
   * @param in input
   * @param xsl style sheet
   * @param params parameters
   * @return transformed result
   * @throws Exception exception
   */
  public byte[] transform(final IO in, final IO xsl,
      final TokenObjMap<Object> params) throws Exception {

    return SAXON ? transformSaxon(in, xsl, params) :
      transformJava(in, xsl, params);
  }

  /**
   * Uses Java's XSLT implementation to perform an XSL transformation.
   * @param in input
   * @param xsl style sheet
   * @param params parameters, or {@code null}
   * @return transformed result
   * @throws Exception exception
   */
  private byte[] transformJava(final IO in, final IO xsl,
      final TokenObjMap<Object> params) throws Exception {

    // create transformer
    final TransformerFactory tc = TransformerFactory.newInstance();
    final Transformer tr =  tc.newTransformer(
        new StreamSource(new ByteArrayInputStream(xsl.content())));

    // bind parameters
    for(final byte[] key : params) {
      tr.setParameter(string(key), params.get(key));
    }

    // create serializer
    final ArrayOutput ao = new ArrayOutput();

    // do transformation and return result
    tr.transform(new StreamSource(new ByteArrayInputStream(in.content())),
        new StreamResult(ao));
    return ao.toArray();
  }

  /**
   * Uses Saxon to perform an XSL transformation.
   * @param in input
   * @param xsl style sheet
   * @param params parameters, or {@code null}
   * @return transformed result
   * @throws Exception exception
   */
  private byte[] transformSaxon(final IO in, final IO xsl,
      final TokenObjMap<Object> params) throws Exception {

    // create transformer
    final ArrayOutput ao = new ArrayOutput();
    final Object xp = get(SAXONPROC, true);
    final Object xc = invoke(xp, "newXsltCompiler");
    final Source xslt = new SAXSource(xsl.inputSource());
    final Object xe = invoke(xc, "compile", xslt);
    final Object xt = invoke(xe, "load");
    final Object xb = invoke(xp, "newDocumentBuilder");
    final Source input = new SAXSource(in.inputSource());
    invoke(xt, "setInitialContextNode", invoke(xb, "build", input));

    // create serializer
    final Object xs = get(SAXONSER, ao);
    invoke(xt, "setDestination", xs);

    // bind parameters
    for(final byte[] key : params) {
      final Object k = get(SAXONQNAME, string(key));
      final Object v = get(SAXONVALUE, params.get(key).toString());
      invoke(xt, "setParameter", k, v);
    }

    // do transformation and return result
    invoke(xt, "transform");
    invoke(xt, "close");
    return ao.toArray();
  }
}
