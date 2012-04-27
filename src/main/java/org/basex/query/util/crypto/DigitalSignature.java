package org.basex.query.util.crypto;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

import javax.xml.crypto.*;
import javax.xml.crypto.dom.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.*;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * This class generates and validates digital signatures for XML data.
 *
 * @author BaseX Team 2005-12, BSD License
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
  private static final byte[] DEFD = token("sha1");
  /** Default signature algorithm. */
  private static final byte[] DEFS = token("rsa_sha1");
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
    DIGESTS.add(token("sha256"), token(DigestMethod.SHA256));
    DIGESTS.add(token("sha512"), token(DigestMethod.SHA512));

    SIGNATURES.add(DEFS, token(SignatureMethod.RSA_SHA1));
    SIGNATURES.add(token("dsa_sha1"), token(SignatureMethod.DSA_SHA1));

    TYPES.add(DEFT);
    TYPES.add(ENVT);
//    TYPES.add(DETT);
  }

  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param ii input info
   */
  public DigitalSignature(final InputInfo ii) {
    info = ii;
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
   * @param ii input info
   *
   * @return signed node
   * @throws QueryException query exception
   */
  public Item generateSignature(final ANode node, final byte[] c,
      final byte[] d, final byte[] sig, final byte[] ns, final byte[] t,
      final byte[] expr, final ANode ce, final InputInfo ii) throws QueryException {

    // checking input variables
    byte[] b = c;
    if(b.length == 0) b = DEFC;
    b = CANONICALIZATIONS.get(lc(b));
    if(b == null) CRYPTOCANINV.thrw(info, c);
    final String canonicalization = string(b);

    b = d;
    if(b.length == 0) b = DEFD;
    b = DIGESTS.get(lc(b));
    if(b == null) CRYPTODIGINV.thrw(info, d);
    final String digest = string(b);

    b = sig;
    if(b.length == 0) b = DEFS;
    final byte[] tsig = b;
    b = SIGNATURES.get(lc(b));
    if(b == null) CRYPTOSIGINV.thrw(info, sig);
    final String signature = string(b);
    final String keytype = string(tsig).substring(0, 3);

    b = t;
    if(b.length == 0) b = DEFT;
    if(!TYPES.contains(lc(b))) CRYPTOSIGTYPINV.thrw(info, t);
    final byte[] type = b;

    Item signedNode = null;

    try {

      final XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

      final PrivateKey pk;
      final PublicKey puk;
      final KeyInfo ki;

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
          CRYPTOINVNM.thrw(info, ceDOM);
        final NodeList ceChildren = ceDOM.getDocumentElement().getChildNodes();
        final int s = ceChildren.getLength();
        int ci = 0;
        // iterate child axis to retrieve keystore setup
        while(ci < s) {
          final Node cn = ceChildren.item(ci++);
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
        final KeyStore ks = KeyStore.getInstance(ksTY);

        if(ks == null) CRYPTOKSNULL.thrw(info, ks);

        ks.load(new FileInputStream(ksURI), ksPW.toCharArray());
        pk = (PrivateKey) ks.getKey(kAlias, pkPW.toCharArray());
        final X509Certificate x509ce = (X509Certificate)
            ks.getCertificate(kAlias);
        if(x509ce == null) CRYPTOALINV.thrw(info, kAlias);
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

      final Document inputNode = toDOMNode(node);
      final List<Transform> tfList;


      // validating a given XPath expression to get nodes to be signed
      if(expr.length > 0) {
        final XPathFactory xpf = XPathFactory.newInstance();
        final XPathExpression xExpr = xpf.newXPath().compile(string(expr));
        final NodeList xRes = (NodeList) xExpr.evaluate(inputNode,
            XPathConstants.NODESET);
        if(xRes.getLength() < 1)
          CRYPTOXPINV.thrw(info, expr);
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
      final DOMSignContext signContext;
      final XMLSignature xmlSig;

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
      signedNode = NodeType.DOC.cast(inputNode, ii);

    } catch(final XPathExpressionException e) {
      CRYPTOXPINV.thrw(info, e);
    } catch(final SAXException e) {
      CRYPTOIOEXC.thrw(info, e);
    } catch(final IOException e) {
      CRYPTOIOEXC.thrw(info, e);
    } catch(final ParserConfigurationException e) {
      CRYPTOIOEXC.thrw(info, e);
    } catch(final KeyStoreException e) {
      CRYPTOKSEXC.thrw(info, e);
    } catch(final MarshalException e) {
      CRYPTOSIGEXC.thrw(info, e);
    } catch(final XMLSignatureException e) {
      CRYPTOSIGEXC.thrw(info, e);
    } catch(final NoSuchAlgorithmException e) {
      CRYPTOALGEXC.thrw(info, e);
    } catch(final CertificateException e) {
      CRYPTOALGEXC.thrw(info, e);
    } catch(final UnrecoverableKeyException e) {
      CRYPTONOKEY.thrw(info, e);
    } catch(final KeyException e) {
      CRYPTONOKEY.thrw(info, e);
    } catch(final InvalidAlgorithmParameterException e) {
      CRYPTOALGEXC.thrw(info, e);
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
        CRYPTONOSIG.thrw(info, node);
      valContext.setNode(signl.item(0));
      final XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
      final XMLSignature signature = fac.unmarshalXMLSignature(valContext);
      coreVal = signature.validate(valContext);

    } catch(final XMLSignatureException e) {
      CRYPTOIOEXC.thrw(info, e);
    } catch(final SAXException e) {
      CRYPTOIOEXC.thrw(info, e);
    } catch(final ParserConfigurationException e) {
      CRYPTOIOEXC.thrw(info, e);
    } catch(final IOException e) {
      CRYPTOIOEXC.thrw(info, e);
    } catch(final MarshalException e) {
      CRYPTOSIGEXC.thrw(info, e);
    }

    return Bln.get(coreVal);
  }

  /**
   * Serializes the given XML node to a byte array.
   * @param n node to be serialized
   * @return byte array containing XML
   * @throws IOException exception
   */
  private static byte[] nodeToBytes(final ANode n) throws IOException {
    final ByteArrayOutputStream b = new ByteArrayOutputStream();
    final Serializer s = Serializer.get(b, new SerializerProp("format=no"));
    s.item(n);
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