package org.basex.query.util.crypto;

import java.security.Key;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.List;

import javax.security.cert.X509Certificate;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;

/**
 * Extracs a key from a given {@link KeyInfo} object.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class MyKeySelector extends KeySelector {

  /**
   * Wrapper for KeySelector results.
   *
   * @author BaseX Team 2005-11, BSD License
   * @author Lukas Kircher
   */
  private class MyKeySelectorResult implements KeySelectorResult {

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
  public KeySelectorResult select(final KeyInfo ki, final Purpose p,
      final AlgorithmMethod m, final XMLCryptoContext c)
      throws KeySelectorException {

    if(ki == null)
      throw new KeySelectorException("KeyInfo is null");

    final SignatureMethod sm = (SignatureMethod) m;
    @SuppressWarnings("unchecked")
    final List<Object> l = ki.getContent();

    for(int i = 0; i < l.size(); i++) {
        final XMLStructure s = (XMLStructure) l.get(i);
        PublicKey pk = null;
        if(s instanceof KeyValue) {
          try {
            pk = ((KeyValue) s).getPublicKey();
          } catch(KeyException ke) {
            throw new KeySelectorException(ke);
          }

        } else if(s instanceof X509Data) {
          for(final Object d : ((X509Data) s).getContent())
            if(d instanceof X509Certificate)
                pk = ((X509Certificate) d).getPublicKey();
        }

        if(pk != null) {
          final String sa = sm.getAlgorithm();
          final String ka = pk.getAlgorithm();
          if(ka.equalsIgnoreCase("DSA") && sa.equalsIgnoreCase(
              "http://www.w3.org/2000/09/xmldsig#dsa-sha1") ||
              ka.equalsIgnoreCase("RSA") && sa.equalsIgnoreCase(
                  "http://www.w3.org/2000/09/xmldsig#rsa-sha1"))
            return new MyKeySelectorResult(pk);
        }
    }

    throw new KeySelectorException("No KeyValue element found");
  }
}