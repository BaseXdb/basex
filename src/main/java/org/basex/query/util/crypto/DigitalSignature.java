package org.basex.query.util.crypto;

import static org.basex.util.Token.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Collections;

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.util.InputInfo;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class generates and validates digital signatures for XML data.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class DigitalSignature {

  /** Tokens. */
  private static final byte[][] CANONICALIZATIONS =
    {
    token("exclusive"),
    token("inclusive"),
    token("exclusive-with-comment"),
    token("inclusive-with-comment")
    };
  /** Tokens. */
  private static final byte[][] DIGESTS =
    {
    token("SHA1"),
    token("SHA256"),
    token("SHA512"),
    };
  /** Tokens. */
  private static final byte[][] SIGNATURES =
    {
    token("DSA-SHA1"),
    token("RSA-SHA1"),
    };
  /** Tokens. */
  private static final byte[][] SIGNATURETYPES =
    {
    token("enveloped"),
    token("enveloping"),
    token("detached"),
    };
  /** Index of default values in token arrays. */
  private static final int DEFAULT = 0;
  /** Input info. */
  private final InputInfo input;

  /**
   * Constructor.
   *
   * @param ii input info
   */
  public DigitalSignature(final InputInfo ii) {
    input = ii;
  }

  /**
   * Generates a signature.
   *
   * @return signature
   */
  public Item generateSignature(final ANode node, final byte[] canonAlg,
      final byte[] digestAlg, final byte[] signatureAlg,
      final byte[] signatureNS, final byte[] signatureType)
          throws QueryException {

    // initialize default values if arguments empty
    final byte[] a = canonAlg == null ? CANONICALIZATIONS[DEFAULT] : canonAlg;
    final byte[] d = digestAlg == null ? DIGESTS[DEFAULT] : digestAlg;
    final byte[] s = signatureAlg == null ? SIGNATURES[DEFAULT] : signatureAlg;
    final byte[] t = signatureType == null ? SIGNATURETYPES[DEFAULT] :
      signatureType;

    // check if arguments are valid
//    if(!eq(canonAlg, CANONICALIZATIONS))
//      CRYPTOCANINV.thrw(input, canonAlg);
//    if(!eq(digestAlg, DIGESTS))
//      CRYPTODIGINV.thrw(input, digestAlg);
//    if(!eq(signatureAlg, SIGNATURES))
//      CRYPTOSIGINV.thrw(input, signatureAlg);
//    if(!eq(signatureType, SIGNATURETYPES))
//      CRYPTOSIGINV.thrw(input, signatureType);

    Item signedNode = null;

    try {

      final XMLSignatureFactory fac = XMLSignatureFactory.getInstance();
      final Reference ref = fac.newReference("",
          fac.newDigestMethod(DigestMethod.SHA1, null), Collections.
          singletonList(fac.newTransform(Transform.ENVELOPED,
              (TransformParameterSpec) null)), null, null);

      final SignedInfo si = fac.newSignedInfo(fac.
          newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
              (C14NMethodParameterSpec) null),
              fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
              Collections.singletonList(ref));

      // auto-generated keypair and signature
      final KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
      gen.initialize(512);
      final KeyPair kp = gen.generateKeyPair();
      final KeyInfoFactory kif = fac.getKeyInfoFactory();
      final KeyValue kv = kif.newKeyValue(kp.getPublic());
      final KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));
      final PrivateKey pk = kp.getPrivate();

      // enveloped certificate
      final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      final Serializer ser = Serializer.
          get(byteOut, null);
      node.serialize(ser);
      ser.close();

      final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      final Document doc = dbf.newDocumentBuilder().parse(
          new ByteArrayInputStream(byteOut.toByteArray()));

      final DOMSignContext dsc = new DOMSignContext(pk, doc.getDocumentElement());
      XMLSignature xmlsig = fac.newXMLSignature(si, ki);
      xmlsig.sign(dsc);
      signedNode = NodeType.DOC.e(doc, input);

    } catch(NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch(InvalidAlgorithmParameterException e) {
      e.printStackTrace();
    } catch(KeyException e) {
      e.printStackTrace();
    } catch(MarshalException e) {
      e.printStackTrace();
    } catch(XMLSignatureException e) {
      e.printStackTrace();
    } catch(Exception e) {
      e.printStackTrace();
    }

    return signedNode;
  }

  /**
   * Validates a signature.
   * @param node input node
   *
   * @return true if signature valid
   */
  public Item validateSignature(final ANode node) {
    Serializer ser;
    Document doc = null;
    NodeList nl = null;
    try {

      final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      ser = Serializer.
          get(byteOut, null);
      node.serialize(ser);
      ser.close();
      final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      doc = dbf.newDocumentBuilder().parse(
          new ByteArrayInputStream(byteOut.toByteArray()));
      nl = doc.getElementsByTagName("Signature");

    } catch(FileNotFoundException e1) {
      e1.printStackTrace();
    } catch(IOException e1) {
      e1.printStackTrace();
    } catch(SAXException e) {
      e.printStackTrace();
    } catch(ParserConfigurationException e) {
      e.printStackTrace();
    }

    DOMValidateContext valContext = new DOMValidateContext(
        new X509KeySelector(), nl.item(0));
    final XMLSignatureFactory fac = XMLSignatureFactory.getInstance();

    XMLSignature signature = null;

    try {
      signature = fac.unmarshalXMLSignature(valContext);
      Bln res = Bln.get(signature.validate(valContext));
      return res;

    } catch(MarshalException e) {
      e.printStackTrace();
    } catch(XMLSignatureException e) {
      e.printStackTrace();
    }

    return Bln.get(false);
  }
}