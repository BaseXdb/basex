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

public class MyKeySelector extends KeySelector {

  private class MyKeySelectorResult implements KeySelectorResult {

    private Key publicKey;

    @Override
    public Key getKey() {
      return publicKey;
    }

    MyKeySelectorResult(final PublicKey key) {
      publicKey = key;
    }
  }

  @Override
  public KeySelectorResult select(final KeyInfo keyInfo, final Purpose purpose,
      final AlgorithmMethod method, final XMLCryptoContext context)
      throws KeySelectorException {
    if(keyInfo == null)
      throw new KeySelectorException("KeyInfo null");

    SignatureMethod sm = (SignatureMethod) method;
    List list = keyInfo.getContent();

    for(int i = 0; i < list.size(); i++) {
        XMLStructure xmlStructure = (XMLStructure) list.get(i);

        PublicKey pk = null;
        if(xmlStructure instanceof KeyValue) {
          try {
            pk = ((KeyValue) xmlStructure).getPublicKey();
          } catch(KeyException ke) {
            throw new KeySelectorException(ke);
          }

          if(algEquals(sm.getAlgorithm(), pk.getAlgorithm()))
            return new MyKeySelectorResult(pk);

        } else if(xmlStructure instanceof X509Data) {
          for (Object data : ((X509Data) xmlStructure).getContent()) {
            if (data instanceof X509Certificate) {
                pk = ((X509Certificate) data).getPublicKey();
            }
          }
        }
    }
    throw new KeySelectorException("No KeyValue element found!");
  }

  private static boolean algEquals(final String algURI, final String algName) {
    if(algName.equalsIgnoreCase("DSA") &&
        algURI.equalsIgnoreCase("http://www.w3.org/2000/09/xmldsig#dsa-sha1"))
      return true;
    return algName.equalsIgnoreCase("RSA") &&
        algURI.equalsIgnoreCase("http://www.w3.org/2000/09/xmldsig#rsa-sha1");
  }
}
