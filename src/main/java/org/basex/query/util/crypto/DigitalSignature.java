package org.basex.query.util.crypto;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.Item;
import org.basex.util.InputInfo;

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
    if(!eq(canonAlg, CANONICALIZATIONS))
      CRYPTOCANINV.thrw(input, canonAlg);
    if(!eq(digestAlg, DIGESTS))
      CRYPTODIGINV.thrw(input, digestAlg);
    if(!eq(signatureAlg, SIGNATURES))
      CRYPTOSIGINV.thrw(input, signatureAlg);
    if(!eq(signatureType, SIGNATURETYPES))
      CRYPTOSIGINV.thrw(input, signatureType);

    try {

      XMLSignatureFactory fac = XMLSignatureFactory.getInstance();
      Reference ref = fac.newReference("",
          fac.newDigestMethod(DigestMethod.SHA1, null), Collections.
          singletonList(fac.newTransform(Transform.ENVELOPED,
              (TransformParameterSpec) null)), null, null);

      SignedInfo si = fac.newSignedInfo(fac.
          newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
              (C14NMethodParameterSpec) null),
              fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
              Collections.singletonList(ref));

    } catch(NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch(InvalidAlgorithmParameterException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Validates a signature.
   * @param node input node
   *
   * @return true if signature valid
   */
  public Item validateSignature(final ANode node) {
    return null;
  }
}
