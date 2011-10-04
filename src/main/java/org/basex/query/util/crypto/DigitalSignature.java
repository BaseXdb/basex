package org.basex.query.util.crypto;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
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
import java.util.List;
import java.util.Vector;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
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
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerProp;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.util.InputInfo;
import org.basex.util.hash.TokenMap;
import org.basex.util.hash.TokenSet;
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

  /** Canonicalization algorithms. */
  private static final TokenMap CANONICALIZATIONS = new TokenMap();
  /** Signature digest algorithms. */
  private static final TokenMap DIGESTS = new TokenMap();
  /** Signature algorithms. */
  private static final TokenMap SIGNATURES = new TokenMap();
  /** Signature types. */
  private static final TokenSet TYPES = new TokenSet();
  /** Default canonicalization algorithm. */
  private static final byte[] DEFC = token("inclusive-with-comments");
  /** Default digest algorithm. */
  private static final byte[] DEFD = token("SHA1");
  /** Default signature algorithm. */
  private static final byte[] DEFS = token("RSA_SHA1");
  /** Default signature type enveloped. */
  private static final byte[] DEFT = token("enveloped");
  /** Signature type enveloping. */
  private static final byte[] ENVT = token("enveloping");
//  /** Signature type detached. */
//  private static final byte[] DETT = token("detached");

  // initializations
  static {
    CANONICALIZATIONS.add(DEFC, token(
        CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS));
    CANONICALIZATIONS.add(token("exclusive-with-comments"), token(
        CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS));
    CANONICALIZATIONS.add(token("inclusive"), token(
        CanonicalizationMethod.INCLUSIVE));
    CANONICALIZATIONS.add(token("exclusive"), token(
        CanonicalizationMethod.EXCLUSIVE));

    DIGESTS.add(DEFD, token(DigestMethod.SHA1));
    DIGESTS.add(token("SHA256"), token(DigestMethod.SHA256));
    DIGESTS.add(token("SHA512"), token(DigestMethod.SHA512));

    SIGNATURES.add(DEFS, token(SignatureMethod.RSA_SHA1));
    SIGNATURES.add(token("DSA_SHA1"), token(SignatureMethod.DSA_SHA1));

    TYPES.add(DEFT);
    TYPES.add(ENVT);
//    TYPES.add(DETT);
  }

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
   * @param c canonicalization algorithm
   * @param d digest algorithm
   * @param sig signature algorithm
   * @param ns signature element namespace prefix
   * @param t signature type (enveloped, enveloping, detached)
   * @param expr XPath expression which specifies node to be signed
   * @param ce certificate which contains keystore information for
   *        signing the node, may be null
   *
   * @return signed node
   * @throws QueryException query exception
   */
  public ANode generateSignature(final ANode node, final byte[] c,
      final byte[] d, final byte[] sig, final byte[] ns, final byte[] t,
      final byte[] expr, final ANode ce) throws QueryException {

    // checking input variables
    byte[] b = c;
    if(b.length == 0) b = DEFC;
    b = CANONICALIZATIONS.get(b);
    if(b == null) CRYPTOCANINV.thrw(input, b);
    final String canonicalization = string(b);

    b = d;
    if(b.length == 0) b = DEFD;
    b = DIGESTS.get(b);
    if(b == null) CRYPTODIGINV.thrw(input, b);
    final String digest = string(b);

    b = sig;
    if(b.length == 0) b = DEFS;
    final byte[] tsig = b;
    b = SIGNATURES.get(b);
    if(b == null) CRYPTOSIGINV.thrw(input, b);
    final String signature = string(b);
    final String keytype = string(tsig).substring(0, 3);

    b = t;
    if(b.length == 0) b = DEFT;
    final int ti = TYPES.id(b);
    if(ti == 0) CRYPTOSIGTYPINV.thrw(input, b);
    final byte[] type = b;

    ANode signedNode = null;

    try {

      final XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

      PrivateKey pk = null;
      PublicKey puk = null;
      KeyInfo ki = null;

      // dealing with given certificate details to initialize the keystore
      if(ce != null) {
        String ksTY = null;
        String ksPW = null;
        String kAlias = null;
        String pkPW = null;
        String ksURI = null;

        final Document ceDOM = toDOMNode(ce);
        if(!ceDOM.getDocumentElement().getNodeName().
            equals("digital-certificate"))
          CRYPTOINVNM.thrw(input, ceDOM);
        final NodeList ceChilds = ceDOM.getDocumentElement().getChildNodes();
        final int s = ceChilds.getLength();
        int ci = 0;
        // iterate child axis to retrieve keystore setup
        while(ci < s) {
          final Node cn = ceChilds.item(ci++);
          final String name = cn.getNodeName();
          if(name.equals("keystore-type"))
            ksTY = cn.getTextContent();
          else if(name.equals("keystore-password"))
            ksPW = cn.getTextContent();
          else if(name.equals("key-alias"))
            kAlias = cn.getTextContent();
          else if(name.equals("private-key-password"))
            pkPW = cn.getTextContent();
          else if(name.equals("keystore-uri"))
            ksURI = cn.getTextContent();
        }

        // initialize the keystore
        KeyStore ks = KeyStore.getInstance(ksTY);

        if(ks == null) CRYPTOKSNULL.thrw(input, ks);

        ks.load(new FileInputStream(ksURI), ksPW.toCharArray());
        pk = (PrivateKey) ks.getKey(kAlias, pkPW.toCharArray());
        final X509Certificate x509ce = (X509Certificate)
            ks.getCertificate(kAlias);
        puk = x509ce.getPublicKey();
        final KeyInfoFactory kifactory = fac.getKeyInfoFactory();
        final KeyValue keyValue = kifactory.newKeyValue(puk);
        final Vector<XMLStructure> kiCont = new Vector<XMLStructure>();
        kiCont.add(keyValue);
        final List<Object> x509Content = new ArrayList<Object>();
        final X509IssuerSerial issuer = kifactory.newX509IssuerSerial(x509ce.
            getIssuerX500Principal().getName(),
            x509ce.getSerialNumber());
        x509Content.add(x509ce.getSubjectX500Principal().getName());
        x509Content.add(issuer);
        x509Content.add(x509ce);
        final X509Data x509Data = kifactory.newX509Data(x509Content);
        kiCont.add(x509Data);
        ki = kifactory.newKeyInfo(kiCont);

      // auto-generate keys if no certificate is provided
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

      Document inputNode = toDOMNode(node);
      List<Transform> tfList = null;


      // validating a given XPath expression to get nodes to be signed
      if(expr.length > 0) {
        final XPathFactory xpf = XPathFactory.newInstance();
        final XPathExpression xExpr = xpf.newXPath().compile(string(expr));
        final NodeList xRes = (NodeList) xExpr.evaluate(inputNode,
            XPathConstants.NODESET);
        if(xRes.getLength() < 1)
          CRYPTOXPINV.thrw(input, expr);
        tfList = new ArrayList<Transform>(2);
        tfList.add(fac.newTransform(Transform.XPATH,
            new XPathFilterParameterSpec(string(expr))));
        tfList.add(fac.newTransform(Transform.ENVELOPED,
            (TransformParameterSpec) null));

      } else {
        tfList = Collections.singletonList(fac.newTransform(
            Transform.ENVELOPED, (TransformParameterSpec) null));
      }


      // creating reference element
      final Reference ref = fac.newReference("",
          fac.newDigestMethod(digest, null), tfList, null, null);

      // creating signed info element
      final SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(
          canonicalization, (C14NMethodParameterSpec) null),
              fac.newSignatureMethod(signature, null),
              Collections.singletonList(ref));


      // prepare document signature
      DOMSignContext signContext = null;
      XMLSignature xmlSig = null;

      // enveloped signature
      if(eq(type, DEFT)) {
        signContext = new DOMSignContext(pk, inputNode.getDocumentElement());
        xmlSig = fac.newXMLSignature(si, ki);

        // detached signature
//      } else if(eq(type, DETT)) {
//      final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        dbf.setNamespaceAware(true);
//        inputNode = dbf.newDocumentBuilder().newDocument();
//        signContext = new DOMSignContext(pk, inputNode);
//        xmlSig = fac.newXMLSignature(si, ki);

        // enveloping signature
      } else {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        final XMLStructure cont = new DOMStructure(
            inputNode.getDocumentElement());
        final XMLObject obj = fac.newXMLObject(Collections.singletonList(cont),
            "", null, null);
        xmlSig = fac.newXMLSignature(si, ki, Collections.singletonList(obj),
            null, null);
        signContext = new DOMSignContext(pk, inputNode);
      }


      // set Signature element namespace prefix, if given
      if(ns.length > 0)
        signContext.setDefaultNamespacePrefix(new String(ns));

      // actually sign the document
      xmlSig.sign(signContext);
      signedNode = toDBNode(inputNode);

    } catch(XPathExpressionException e) {
      CRYPTOXPINV.thrw(input, e);
    } catch(SAXException e) {
      CRYPTOIOEXC.thrw(input, e);
    } catch(IOException e) {
      CRYPTOIOEXC.thrw(input, e);
    } catch(ParserConfigurationException e) {
      CRYPTOIOEXC.thrw(input, e);
    } catch(KeyStoreException e) {
      CRYPTOKSEXC.thrw(input, e);
    } catch(MarshalException e) {
      CRYPTOSIGEXC.thrw(input, e);
    } catch(XMLSignatureException e) {
      CRYPTOSIGEXC.thrw(input, e);
    } catch(NoSuchAlgorithmException e) {
      CRYPTOALGEXC.thrw(input, e);
    } catch(CertificateException e) {
      CRYPTOALGEXC.thrw(input, e);
    } catch(UnrecoverableKeyException e) {
      CRYPTONOKEY.thrw(input, e);
    } catch(KeyException e) {
      CRYPTONOKEY.thrw(input, e);
    } catch(InvalidAlgorithmParameterException e) {
      CRYPTOALGEXC.thrw(input, e);
    }

    return signedNode;
  }

  /**
   * Validates a signature.
   * @param node input node
   *
   * @return true if signature valid
   * @throws QueryException query exception
   */
  public Item validateSignature(final ANode node) throws QueryException {
    boolean coreVal = false;

    try {

      final Document doc = toDOMNode(node);
      final DOMValidateContext valContext =
          new DOMValidateContext(new MyKeySelector(), doc);
      final NodeList signl = doc.getElementsByTagNameNS(XMLSignature.XMLNS,
          "Signature");
      if(signl.getLength() < 1)
        CRYPTONOSIG.thrw(input, node);
      valContext.setNode(signl.item(0));
      final XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
      final XMLSignature signature = fac.unmarshalXMLSignature(valContext);
      coreVal = signature.validate(valContext);

      return Bln.get(coreVal);

    } catch(XMLSignatureException e) {
      CRYPTOIOEXC.thrw(input, e);
    } catch(SAXException e) {
      CRYPTOIOEXC.thrw(input, e);
    } catch(ParserConfigurationException e) {
      CRYPTOIOEXC.thrw(input, e);
    } catch(IOException e) {
      CRYPTOIOEXC.thrw(input, e);
    } catch(MarshalException e) {
      CRYPTOSIGEXC.thrw(input, e);
    }

    return Bln.get(coreVal);
  }

  /**
   * Creates a BaseX database node from the given DOM node.
   * @param n DOM node
   * @return database node
   * @throws QueryException query exception
   */
  private ANode toDBNode(final Node n) throws QueryException {
    String xmlString = null;

    DBNode dbn = null;

    try {
      TransformerFactory transfac = TransformerFactory.newInstance();
      Transformer trans = transfac.newTransformer();

      //create string from xml tree
      StringWriter sw = new StringWriter();
      StreamResult result = new StreamResult(sw);
      DOMSource source = new DOMSource(n);
      trans.transform(source, result);
      xmlString = sw.toString();

      final Parser parser = Parser.xmlParser(IO.get(xmlString), new Prop());
      final MemBuilder builder = new MemBuilder("", parser, new Prop());
      final Data mem = builder.build();
      dbn = new DBNode(mem, 1);

    } catch(IOException e) {
      CRYPTOIOEXC.thrw(input, e);
    } catch(TransformerException e) {
      CRYPTOIOEXC.thrw(input, e);
    }

    return dbn;
  }

  /**
   * Serializes the given XML node to a byte array.
   * @param n node to be serialized
   * @return byte array containing XML
   * @throws IOException exception
   */
  private static byte[] nodeToBytes(final ANode n)
      throws IOException {

    final ByteArrayOutputStream b = new ByteArrayOutputStream();
    final Serializer s = Serializer.get(b,
        new SerializerProp("format=no"));
    n.serialize(s);
    s.close();
    return b.toByteArray();
  }

  /**
   * Creates a DOM node for the given input node.
   * @param n node
   * @return DOM node representation of input node
   * @throws SAXException exception
   * @throws IOException exception
   * @throws ParserConfigurationException exception
   */
  private static Document toDOMNode(final ANode n)
      throws SAXException, IOException, ParserConfigurationException {

    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    return dbf.newDocumentBuilder().parse(
        new ByteArrayInputStream(nodeToBytes(n)));
  }
}