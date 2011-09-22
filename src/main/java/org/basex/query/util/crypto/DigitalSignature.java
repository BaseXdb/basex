package org.basex.query.util.crypto;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerException;
import org.basex.io.serial.SerializerProp;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.util.InputInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
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
    token("inclusive-with-comments"),
    token("exclusive"),
    token("inclusive"),
    token("exclusive-with-comments"),
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
    token("RSA_SHA1"),
    token("DSA_SHA1")
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
   * @param xpathExpr XPath expression which specifies node to be signed
   * @param certificate certificate which contains keystore information for
   *        signing the node, may be null
   *
   * @return signed node
   * @throws QueryException query exception
   */
  public ANode generateSignature(final ANode node,
      final byte[] canonicalization, final byte[] digest,
      final byte[] signature, final byte[] nsPrefix, final byte[] type,
      final byte[] xpathExpr, final ANode certificate) throws QueryException {

    // variables to check if parameters correct
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

    ANode signedNode = null;

    try {

      final XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
      // TODO problem poss. due to empty reference?
      final Reference ref = fac.newReference("",
          fac.newDigestMethod(DA, null), Collections.
          singletonList(fac.newTransform(Transform.ENVELOPED,
              (TransformParameterSpec) null)), null, null);

      final SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CM,
              (C14NMethodParameterSpec) null),
              fac.newSignatureMethod(SA, null),
              Collections.singletonList(ref));

      DocumentBuilderFactory dbf = null;

      PrivateKey pk = null;
      PublicKey puk = null;
      KeyInfo ki = null;

      // dealing with given certificate details to initialize the keystore
      if(certificate != null) {
        Document cert = null;
        String kst = null;
        String kspw = null;
        String kal = null;
        String pkpw = null;
        String ksuri = null;

        cert = toDOMNode(certificate);
        final NodeList childs = cert.getDocumentElement().getChildNodes();
        final int s = childs.getLength();
        int ci = 0;
        while(ci < s) {
          final Node n = childs.item(ci++);
          final String name = n.getNodeName();
          if(name.equals("keystore-type"))
            kst = n.getTextContent();
          else if(name.equals("keystore-password"))
            kspw = n.getTextContent();
          else if(name.equals("key-alias"))
            kal = n.getTextContent();
          else if(name.equals("private-key-password"))
            pkpw = n.getTextContent();
          else if(name.equals("keystore-uri"))
            ksuri = n.getTextContent();
        }

        // initialize the keystore
        KeyStore ks = KeyStore.getInstance(kst);
        ks.load(new FileInputStream(ksuri), kspw.toCharArray());
        pk = (PrivateKey) ks.getKey(kal, pkpw.toCharArray());
        X509Certificate x509cert = (X509Certificate) ks.getCertificate(kal);
        puk = x509cert.getPublicKey();
        final KeyInfoFactory kif = fac.getKeyInfoFactory();
        KeyValue keyValue = kif.newKeyValue(puk);
        Vector content = new Vector();
        content.add(keyValue);
        List x509Content = new ArrayList();
        X509IssuerSerial issuer = kif.newX509IssuerSerial(x509cert.
            getIssuerX500Principal().getName(),
            x509cert.getSerialNumber());
        x509Content.add(x509cert.getSubjectX500Principal().getName());
        x509Content.add(issuer);
        x509Content.add(x509cert);
        X509Data x509Data = kif.newX509Data(x509Content);
        content.add(x509Data);
        ki = kif.newKeyInfo(content);

      // auto-generated keypair and signature
      } else {
        final KeyPairGenerator gen =
            KeyPairGenerator.getInstance(keytype);
        gen.initialize(512);
        final KeyPair kp = gen.generateKeyPair();
        final KeyInfoFactory kif = fac.getKeyInfoFactory();
        final KeyValue kv = kif.newKeyValue(kp.getPublic());
        ki = kif.newKeyInfo(Collections.singletonList(kv));
        pk = kp.getPrivate();
      }

      if(eq(type, SIGNATURETYPES[1]))
        CRYPTONOTSUPP.thrw(input, type);
      if(eq(type, SIGNATURETYPES[2]))
        CRYPTONOTSUPP.thrw(input, type);

      // enveloped certificate
      final Document doc = toDOMNode(node);
      final DOMSignContext dsc =
          new DOMSignContext(pk, doc.getDocumentElement());

      if(xpathExpr.length > 0) {
//        final String frag = string(nodeToBytes(node));
        CRYPTONOTSUPP.thrw(input, xpathExpr);
        }

      XMLSignature xmlsig = fac.newXMLSignature(si, ki);

      // set Signature element prefix, if given
      if(nsPrefix.length > 0)
        dsc.setDefaultNamespacePrefix(new String(nsPrefix));

      xmlsig.sign(dsc);
      signedNode = toDBNode(doc);

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
    } catch(SAXException e) {
      e.printStackTrace();
    } catch(IOException e) {
      e.printStackTrace();
    } catch(ParserConfigurationException e) {
      e.printStackTrace();
    } catch(KeyStoreException e) {
      e.printStackTrace();
    } catch(CertificateException e) {
      e.printStackTrace();
    } catch(UnrecoverableKeyException e) {
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
    boolean coreVal = false;

    try {

      final Document doc = toDOMNode(node);

      // TODO with specified namespace? what to change?
//      final NodeList nl = doc.getElementsByTagName("Signature");
      final DOMValidateContext valContext =
          new DOMValidateContext(new MyKeySelector(), doc);
      valContext.setNode(
          doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0));
      final XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
      final XMLSignature signature = fac.unmarshalXMLSignature(valContext);
      coreVal = signature.validate(valContext);

      if(!coreVal) {
        System.out.println("Signature failed core validation");
        boolean sv = signature.getSignatureValue().validate(valContext);
        System.out.println("signature validation status: " + sv);
        if(!sv) {
          // Check the validation status of each Reference.
          Iterator i = signature.getSignedInfo().getReferences().iterator();
          for(int j = 0; i.hasNext(); j++) {
            boolean refValid = ((Reference) i.next()).validate(valContext);
            System.out.println("ref[" + j + "] validity status: " + refValid);
          }
        }

      } else {
        System.out.println("Signature passed core validation");
      }

      return Bln.get(coreVal);

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

    return Bln.get(coreVal);
  }

  private static ANode toDBNode(final Node n) {
    String xmlString = null;

    try {

      TransformerFactory transfac = TransformerFactory.newInstance();
      Transformer trans = transfac.newTransformer();

      //create string from xml tree
      StringWriter sw = new StringWriter();
      StreamResult result = new StreamResult(sw);
      DOMSource source = new DOMSource(n);
      trans.transform(source, result);
      xmlString = sw.toString();

    } catch(TransformerException e) {
      e.printStackTrace();
    }

    DBNode dbn = null;

    try {

      final Parser parser = Parser.xmlParser(IO.get(xmlString), new Prop());
      final MemBuilder builder = new MemBuilder("", parser, new Prop());
      final Data mem = builder.build();
      dbn = new DBNode(mem, 1);

    } catch(IOException e) {
      e.printStackTrace();
    }

    return dbn;
  }

  private static byte[] nodeToBytes(final ANode n)
      throws SerializerException, IOException {

    final ByteArrayOutputStream b = new ByteArrayOutputStream();
    final Serializer s = Serializer.get(b,
        new SerializerProp("format=no"));
    //new SerializerProp("omit-xml-declaration=no,standalone=no,indent=no"));
    n.serialize(s);
    s.close();
    return b.toByteArray();
  }

  private static Document toDOMNode(final ANode n)
      throws SAXException, IOException, ParserConfigurationException {

    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    return dbf.newDocumentBuilder().parse(
        new ByteArrayInputStream(nodeToBytes(n)));
  }
}