package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;
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

import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * This class generates and validates digital signatures for XML data.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
final class DigitalSignature {
  /** Canonicalization algorithms. */
  private static final TokenMap CANONICALS = new TokenMap();
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

  // initializations
  static {
    CANONICALS.put("inclusive-with-comments", CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS);
    CANONICALS.put("exclusive-with-comments", CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS);
    CANONICALS.put("inclusive", CanonicalizationMethod.INCLUSIVE);
    CANONICALS.put("exclusive", CanonicalizationMethod.EXCLUSIVE);

    DIGESTS.put("sha1", DigestMethod.SHA1);
    DIGESTS.put("sha256", DigestMethod.SHA256);
    DIGESTS.put("sha512", DigestMethod.SHA512);

    SIGNATURES.put("rsa_sha1", SignatureMethod.RSA_SHA1);
    SIGNATURES.put("dsa_sha1", SignatureMethod.DSA_SHA1);

    TYPES.add(DEFT);
    TYPES.add(ENVT);
  }

  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param info input info
   */
  DigitalSignature(final InputInfo info) {
    this.info = info;
  }

  /**
   * Generates a signature.
   * @param node node to be signed
   * @param can canonicalization algorithm
   * @param dig digest algorithm
   * @param sig signature algorithm
   * @param ns signature element namespace prefix
   * @param tp signature type (enveloped, enveloping, detached)
   * @param expr XPath expression which specifies node to be signed
   * @param cert certificate which contains keystore information for signing the node, may be null
   * @param qc query context
   * @return signed node
   * @throws QueryException query exception
   */
  Item generateSignature(final ANode node, final byte[] can, final byte[] dig, final byte[] sig,
      final byte[] ns, final byte[] tp, final byte[] expr, final ANode cert, final QueryContext qc)
      throws QueryException {

    // checking input variables
    byte[] b = can;
    if(b.length == 0) b = DEFC;
    b = CANONICALS.get(lc(b));
    if(b == null) throw CX_CANINV.get(info, can);
    final String canonicalization = string(b);

    b = dig;
    if(b.length == 0) b = DEFD;
    b = DIGESTS.get(lc(b));
    if(b == null) throw CX_DIGINV.get(info, dig);
    final String digest = string(b);

    b = sig;
    if(b.length == 0) b = DEFS;
    final byte[] tsig = b;
    b = SIGNATURES.get(lc(b));
    if(b == null) throw CX_SIGINV.get(info, sig);
    final String signature = string(b);
    final String keytype = string(tsig).substring(0, 3);

    b = tp;
    if(b.length == 0) b = DEFT;
    if(!TYPES.contains(lc(b))) throw CX_SIGTYPINV.get(info, tp);
    final byte[] type = b;

    final Item signedNode;
    try {
      final XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
      final PrivateKey pk;
      final KeyInfo ki;

      // dealing with given certificate details to initialize the keystore
      if(cert != null) {
        final Document ceDOM = toDOMNode(cert);
        if(!"digital-certificate".equals(ceDOM.getDocumentElement().getNodeName()))
          throw CX_INVNM.get(info, ceDOM);
        final NodeList ceChildren = ceDOM.getDocumentElement().getChildNodes();
        final int s = ceChildren.getLength();
        int ci = 0;
        // iterate child axis to retrieve keystore setup
        String ksURI = null, pkPW = null, kAlias = null, ksPW = null, ksTY = null;
        while(ci < s) {
          final Node cn = ceChildren.item(ci++);
          switch(cn.getNodeName()) {
            case "keystore-type":        ksTY = cn.getTextContent(); break;
            case "keystore-password":    ksPW = cn.getTextContent(); break;
            case "key-alias":            kAlias = cn.getTextContent(); break;
            case "private-key-password": pkPW = cn.getTextContent(); break;
            case "keystore-uri":         ksURI = cn.getTextContent(); break;
          }
        }

        // initialize the keystore
        final KeyStore ks;
        try {
          ks = KeyStore.getInstance(ksTY);
        } catch(final KeyStoreException ex) {
          throw CX_KSNULL_X.get(info, ex);
        }

        try(FileInputStream fis = new FileInputStream(ksURI)) {
          ks.load(fis, ksPW.toCharArray());
        }
        pk = (PrivateKey) ks.getKey(kAlias, pkPW.toCharArray());
        final X509Certificate x509ce = (X509Certificate) ks.getCertificate(kAlias);
        if(x509ce == null) throw CX_ALINV_X.get(info, kAlias);
        final PublicKey puk = x509ce.getPublicKey();
        final KeyInfoFactory kifactory = fac.getKeyInfoFactory();
        final KeyValue keyValue = kifactory.newKeyValue(puk);
        final ArrayList<XMLStructure> kiCont = new ArrayList<>();
        kiCont.add(keyValue);
        final List<Object> x509Content = new ArrayList<>();
        final X509IssuerSerial issuer = kifactory.newX509IssuerSerial(x509ce.
            getIssuerX500Principal().getName(), x509ce.getSerialNumber());
        x509Content.add(x509ce.getSubjectX500Principal().getName());
        x509Content.add(issuer);
        x509Content.add(x509ce);
        final X509Data x509Data = kifactory.newX509Data(x509Content);
        kiCont.add(x509Data);
        ki = kifactory.newKeyInfo(kiCont);

      // auto-generate keys if no certificate is provided
      } else {
        final KeyPairGenerator gen = KeyPairGenerator.getInstance(keytype);
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
        final NodeList xRes = (NodeList) xExpr.evaluate(inputNode, XPathConstants.NODESET);
        if(xRes.getLength() < 1) throw CX_XPINV.get(info, expr);
        tfList = new ArrayList<>(2);
        tfList.add(fac.newTransform(Transform.XPATH, new XPathFilterParameterSpec(string(expr))));
        tfList.add(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));

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
          fac.newSignatureMethod(signature, null), Collections.singletonList(ref));

      // prepare document signature
      final DOMSignContext signContext;
      final XMLSignature xmlSig;

      // enveloped signature
      if(eq(type, DEFT)) {
        signContext = new DOMSignContext(pk, inputNode.getDocumentElement());
        xmlSig = fac.newXMLSignature(si, ki);
      } else {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        final XMLStructure cont = new DOMStructure(inputNode.getDocumentElement());
        final XMLObject obj = fac.newXMLObject(Collections.singletonList(cont), "", null, null);
        xmlSig = fac.newXMLSignature(si, ki, Collections.singletonList(obj), null, null);
        signContext = new DOMSignContext(pk, inputNode);
      }

      // set Signature element namespace prefix if given
      if(ns.length > 0) signContext.setDefaultNamespacePrefix(string(ns));

      // actually sign the document
      xmlSig.sign(signContext);
      signedNode = NodeType.DOCUMENT_NODE.cast(inputNode, qc, null, info);

    } catch(final XPathExpressionException ex) {
      throw CX_XPINV.get(info, ex);
    } catch(final SAXException | IOException | ParserConfigurationException ex) {
      throw CX_IOEXC.get(info, ex);
    } catch(final KeyStoreException ex) {
      throw CX_KSEXC.get(info, ex);
    } catch(final MarshalException |  XMLSignatureException ex) {
      throw CX_SIGEXC.get(info, ex);
    } catch(final NoSuchAlgorithmException | CertificateException |
        InvalidAlgorithmParameterException ex) {
      throw CX_ALGEXC.get(info, ex);
    } catch(final UnrecoverableKeyException | KeyException ex) {
      throw CX_NOKEY.get(info, ex);
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
  Item validateSignature(final ANode node) throws QueryException {
    try {
      final Document doc = toDOMNode(node);
      final DOMValidateContext valContext = new DOMValidateContext(new MyKeySelector(), doc);
      final NodeList signl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
      if(signl.getLength() < 1) throw CX_NOSIG.get(info, node);
      valContext.setNode(signl.item(0));
      final XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
      final XMLSignature signature = fac.unmarshalXMLSignature(valContext);
      return Bln.get(signature.validate(valContext));

    } catch(final XMLSignatureException | SAXException | ParserConfigurationException |
        IOException ex) {
      throw CX_IOEXC.get(info, ex);
    } catch(final MarshalException ex) {
      throw CX_SIGEXC.get(info, ex);
    }
  }

  /**
   * Serializes the given XML node to a byte array.
   * @param node node to be serialized
   * @return byte array containing XML
   * @throws IOException exception
   */
  private static byte[] nodeToBytes(final ANode node) throws IOException {
    final ArrayOutput ao = new ArrayOutput();
    try(Serializer ser = Serializer.get(ao, SerializerMode.NOINDENT.get())) {
      ser.serialize(node);
    }
    return ao.finish();
  }

  /**
   * Creates a DOM node for the given input node.
   * @param node node
   * @return DOM node representation of input node
   * @throws SAXException exception
   * @throws IOException exception
   * @throws ParserConfigurationException exception
   */
  private static Document toDOMNode(final ANode node) throws SAXException, IOException,
  ParserConfigurationException {

    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(nodeToBytes(node)));
  }
}
