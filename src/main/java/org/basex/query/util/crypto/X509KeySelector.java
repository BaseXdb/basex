package org.basex.query.util.crypto;

import java.security.Key;
import java.security.PublicKey;
import java.util.Iterator;

import javax.security.cert.X509Certificate;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;

/**
 * KeySelector implementation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class X509KeySelector extends KeySelector {

  @Override
  public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose,
      AlgorithmMethod method, XMLCryptoContext context)
      throws KeySelectorException {

    Iterator ki = keyInfo.getContent().iterator();
    while(ki.hasNext()) {
      XMLStructure info = (XMLStructure) ki.next();
      if(!(info instanceof X509Data)) continue;
      X509Data x509Data = (X509Data) info;
      Iterator xi = x509Data.getContent().iterator();
      while(xi.hasNext()) {
        Object o = xi.next();
        if(!(o instanceof X509Certificate)) continue;
        final PublicKey key = ((X509Certificate) o).getPublicKey();
        if(algEquals(method.getAlgorithm(), key.getAlgorithm())) {
          return new KeySelectorResult() {
            @Override
            public Key getKey() {
              return key;
            }
          };
        }
      }
    }
    throw new KeySelectorException("key not found");
  }

  private static boolean algEquals(String algURI, String algName) {
    if ((algName.equalsIgnoreCase("DSA") && algURI.equalsIgnoreCase(
        SignatureMethod.DSA_SHA1)) || (algName.equalsIgnoreCase("RSA") &&
            algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)))
      return true;

    return false;
  }
}
