package org.basex.query.util.crypto;

import java.security.Key;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.List;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyValue;

public class MyKeySelector extends KeySelector {

  private class MyKeySelectorResult implements KeySelectorResult {

    private Key publicKey;

    @Override
    public Key getKey() {
      return publicKey;
    }

    MyKeySelectorResult(PublicKey key) {
      publicKey = key;
    }
  }

  @Override
  public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose,
      AlgorithmMethod method, XMLCryptoContext context)
      throws KeySelectorException {
    if(keyInfo == null)
      throw new KeySelectorException("KeyInfo null");

    SignatureMethod sm = (SignatureMethod)method;
    List list = keyInfo.getContent();
    for(int i = 0; i < list.size(); i++)
    {
        XMLStructure xmlStructure = (XMLStructure)list.get(i);
        if(!(xmlStructure instanceof KeyValue))
            continue;
        PublicKey pk = null;
        try
        {
            pk = ((KeyValue)xmlStructure).getPublicKey();
        }
        catch(KeyException ke)
        {
            throw new KeySelectorException(ke);
        }
        if(algEquals(sm.getAlgorithm(), pk.getAlgorithm()))
            return new MyKeySelectorResult(pk);
    }

    throw new KeySelectorException("No KeyValue element found!");
  }

  private static boolean algEquals(String algURI, String algName) {
    if(algName.equalsIgnoreCase("DSA") &&
        algURI.equalsIgnoreCase("http://www.w3.org/2000/09/xmldsig#dsa-sha1"))
      return true;
    return algName.equalsIgnoreCase("RSA") &&
        algURI.equalsIgnoreCase("http://www.w3.org/2000/09/xmldsig#rsa-sha1");
  }
}
