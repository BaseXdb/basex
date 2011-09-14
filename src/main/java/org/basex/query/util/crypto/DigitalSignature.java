package org.basex.query.util.crypto;

import static org.basex.query.util.Err.*;
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
import java.security.KeyStore;
import java.security.KeyStoreException;
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
  private static final String[] CANONICALIZATIONMETHODS =
    {
    CanonicalizationMethod.EXCLUSIVE,
    CanonicalizationMethod.INCLUSIVE,
    CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS,
    CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS
    };
  /** Tokens. */
  private static final byte[][] DIGESTS =
    {
    token("SHA1"),
    token("SHA256"),
    token("SHA512")
    };
  private static final String[] DIGESTMETHODS =
    {
    DigestMethod.SHA1,
    DigestMethod.SHA256,
    DigestMethod.SHA512
    };
  /** Tokens. */
  private static final byte[][] SIGNATURES =
    {
    token("RSA-SHA1"),
    token("DSA-SHA1")
    };
  private static final String[] SIGNATUREMETHODS =
    {
    SignatureMethod.RSA_SHA1,
    SignatureMethod.DSA_SHA1
    };
  /** Tokens. */
  private static final byte[][] SIGNATURETYPES =
    {
    token("enveloped"),
    token("enveloping"),
    token("detached")
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
   * @param node node to be signed
   * @param canonicalization canonicalization algorithm
   * @param digest digest algorithm
   * @param signature signature algorithm
   * @param nsPrefix signature element namespace prefix
   * @param type signature type (enveloped, enveloping, detached)
   *
   * @return signed node
   * @throws QueryException query exception
   */
  public Item generateSignature(final ANode node, final byte[] canonicalization,
      final byte[] digest, final byte[] signature,
      final byte[] nsPrefix, final byte[] type)
          throws QueryException {
    
    // variables to check parameters for correctnes
    int l = 0;
    int i = 0;
    byte[] b = canonicalization;
    
    // check if given canonicalization method is valid and initialize if so
    if(b.length == 0)
      b = CANONICALIZATIONS[DEFAULT];
    l = CANONICALIZATIONS.length;
    // compare input to valid options
    while(i < l && !eq(CANONICALIZATIONS[i], b)) i++;
    // if all options have been considered and none fits ...
    if(i == l)
      CRYPTOCANINV.thrw(input, b);
    // map to right canonicalization method
    final String CM = CANONICALIZATIONMETHODS[i];
    
    b = digest;
    l = 0;
    i = 0;
    if(b.length == 0)
      b = DIGESTS[DEFAULT];
    l = DIGESTS.length;
    while(i < l && !eq(DIGESTS[i], b)) i++;
    if(i == l)
      CRYPTODIGINV.thrw(input, b);
    final String DA = DIGESTMETHODS[i];
    
    b = signature;
    l = 0;
    i = 0;
    if(b.length == 0)
      b = SIGNATURES[DEFAULT];
    l = SIGNATURES.length;
    while(i < l && !eq(SIGNATURES[i], b)) i++;
    if(i == l)
      CRYPTOSIGINV.thrw(input, b);
    final String SA = SIGNATUREMETHODS[i];
    final String keytype = string(substring(SIGNATURES[i], 0, 3));
    
    b = type;
    if(b.length == 0)
      b = SIGNATURETYPES[DEFAULT];
    else if(!eq(b, SIGNATURETYPES))
      CRYPTOSIGTYPINV.thrw(input, b);
    final String ST = "enveloped";
    
    Item signedNode = null;

    try {

      final XMLSignatureFactory fac = XMLSignatureFactory.getInstance();
      final Reference ref = fac.newReference("",
          fac.newDigestMethod(DA, null), Collections.
          singletonList(fac.newTransform(Transform.ENVELOPED,
              (TransformParameterSpec) null)), null, null);

      final SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CM,
              (C14NMethodParameterSpec) null),
              fac.newSignatureMethod(SA, null),
              Collections.singletonList(ref));

      // auto-generated keypair and signature
      final KeyPairGenerator gen =
          KeyPairGenerator.getInstance(keytype);
      gen.initialize(512);
      final KeyPair kp = gen.generateKeyPair();
      final KeyInfoFactory kif = fac.getKeyInfoFactory();
      final KeyValue kv = kif.newKeyValue(kp.getPublic());
      final KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));
      final PrivateKey pk = kp.getPrivate();

      // enveloped certificate
      final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      final Serializer ser = Serializer.get(byteOut, null);
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

      final DOMValidateContext valContext = new DOMValidateContext(new MyKeySelector(), nl.item(0));
      final XMLSignatureFactory fac = XMLSignatureFactory.getInstance();
      final XMLSignature signature = fac.unmarshalXMLSignature(valContext);

      return Bln.get(signature.validate(valContext));

    } catch(FileNotFoundException e1) {
      e1.printStackTrace();
    } catch(IOException e1) {
      e1.printStackTrace();
    } catch(SAXException e) {
      e.printStackTrace();
    } catch(ParserConfigurationException e) {
      e.printStackTrace();
    } catch(MarshalException e) {
      e.printStackTrace();
    } catch(XMLSignatureException e) {
      e.printStackTrace();
    }

    return Bln.get(false);
  }
}