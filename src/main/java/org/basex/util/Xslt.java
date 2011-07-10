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

import org.basex.io.ArrayOutput;
import org.basex.io.IO;

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
  /** Saxon Serializer. */
  private static final Class<?> SAXONPROPS =
    find(S9API + "Serializer$Property");
  /** Saxon flag. */
  public static final boolean SAXON = SAXONPROC != null;

  /**
   * Uses Java's XSLT implementation to perform an XSL transformation.
   * @param in input
   * @param xsl style sheet
   * @param params parameters
   * @param output serialization parameters
   * @return transformed result
   * @throws Exception exception
   */
  public byte[] transform(final IO in, final IO xsl,
      final TokenObjMap<Object> params, final TokenObjMap<Object> output)
      throws Exception {

    return SAXON ? transformSaxon(in, xsl, params, output) :
      transformJava(in, xsl, params, output);
  }

  /**
   * Uses Java's XSLT implementation to perform an XSL transformation.
   * @param in input
   * @param xsl style sheet
   * @param params parameters, or {@code null}
   * @param output serialization parameters, or {@code null}
   * @return transformed result
   * @throws Exception exception
   */
  private byte[] transformJava(final IO in, final IO xsl,
      final TokenObjMap<Object> params, final TokenObjMap<Object> output)
      throws Exception {

    final TransformerFactory tc = TransformerFactory.newInstance();
    final Transformer tr =  tc.newTransformer(
        new StreamSource(new ByteArrayInputStream(xsl.content())));

    for(final byte[] key : params) {
      tr.setParameter(string(key), params.get(key));
    }
    for(final byte[] key : output) {
      tr.setOutputProperty(string(key), output.get(key).toString());
    }

    final ArrayOutput ao = new ArrayOutput();
    tr.transform(new StreamSource(new ByteArrayInputStream(in.content())),
        new StreamResult(ao));

    return ao.toArray();
  }

  /**
   * Uses Saxon to perform an XSL transformation.
   * @param in input
   * @param xsl style sheet
   * @param params parameters, or {@code null}
   * @param output serialization parameters, or {@code null}
   * @return transformed result
   */
  private byte[] transformSaxon(final IO in, final IO xsl,
      final TokenObjMap<Object> params, final TokenObjMap<Object> output) {

    // execute transformation
    final ArrayOutput ao = new ArrayOutput();
    final Object xp = get(SAXONPROC, true);
    final Object xc = invoke(xp, "newXsltCompiler");
    final Source xslt = new SAXSource(xsl.inputSource());
    final Object xe = invoke(xc, "compile", xslt);
    final Object xt = invoke(xe, "load");
    if(xt == null) throw new NullPointerException(
        "No Saxon XSLT transformer found.");

    final Object xb = invoke(xp, "newDocumentBuilder");
    final Source input = new SAXSource(in.inputSource());
    invoke(xt, "setInitialContextNode", invoke(xb, "build", input));

    // create serializer
    final Object xs = get(SAXONSER, ao);
    invoke(xt, "setDestination", xs);
    for(final byte[] key : output) {
      final String k = string(key);
      for(final Object p : SAXONPROPS.getEnumConstants()) {
        if(p.toString().equals(k)) {
          invoke(xs, "setOutputProperty", p, output.get(key).toString());
        }
      }
    }

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
