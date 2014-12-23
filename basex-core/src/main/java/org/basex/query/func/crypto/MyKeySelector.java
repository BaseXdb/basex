package org.basex.query.func.crypto;

import java.security.*;
import java.util.*;

import javax.security.cert.*;
import javax.security.cert.Certificate;
import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.keyinfo.*;

/**
 * Extracts a key from a given {@link KeyInfo} object.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
final class MyKeySelector extends KeySelector {
  /**
   * Wrapper for KeySelector results.
   *
   * @author BaseX Team 2005-14, BSD License
   * @author Lukas Kircher
   */
  private static class MyKeySelectorResult implements KeySelectorResult {
    /** Key. */
    private final Key pk;

    @Override
    public Key getKey() {
      return pk;
    }

    /**
     * Constructor.
     * @param key key
     */
    MyKeySelectorResult(final PublicKey key) {
      pk = key;
    }
  }

  @Override
  public KeySelectorResult select(final KeyInfo ki, final Purpose p, final AlgorithmMethod m,
      final XMLCryptoContext c) throws KeySelectorException {

    if(ki == null) throw new KeySelectorException("KeyInfo is null");

    final SignatureMethod sm = (SignatureMethod) m;
    @SuppressWarnings("unchecked")
    final List<Object> list = ki.getContent();

    for(final Object l : list) {
      final XMLStructure s = (XMLStructure) l;
      PublicKey pk = null;
      if(s instanceof KeyValue) {
        try {
          pk = ((KeyValue) s).getPublicKey();
        } catch(final KeyException ke) {
          throw new KeySelectorException(ke);
        }

      } else if(s instanceof X509Data) {
        for(final Object d : ((X509Data) s).getContent()) {
          if(d instanceof X509Certificate) {
            pk = ((Certificate) d).getPublicKey();
          }
        }
      }

      if(pk != null) {
        final String sa = sm.getAlgorithm();
        final String ka = pk.getAlgorithm();
        if("DSA".equalsIgnoreCase(ka) && "http://www.w3.org/2000/09/xmldsig#dsa-sha1".equals(sa) ||
          "RSA".equalsIgnoreCase(ka) && "http://www.w3.org/2000/09/xmldsig#rsa-sha1".equals(sa)) {
          return new MyKeySelectorResult(pk);
        }
      }
    }

    throw new KeySelectorException("No KeyValue element found");
  }
}