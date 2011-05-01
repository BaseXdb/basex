package org.basex.test.query.advanced;

import static org.basex.util.Token.*;

import java.util.Iterator;

import org.basex.core.Context;
import org.basex.util.TokenList;
import org.junit.Test;

public class PackageAPITest {

  /**
   * Tests reading of the repository content.
   */
  @Test
  public void testReadRepo() {
    // Create context
    final Context ctx = new Context();
    // Print namespace dictionary
    System.out.println("Namespaces: ");
    final Iterator<byte[]> nsi = ctx.repo.nsDict.iterator();
    byte[] nextNs;
    TokenList nsPkgs;
    while(nsi.hasNext()) {
      nextNs = nsi.next();
      if(nextNs != null) {
        nsPkgs = ctx.repo.nsDict.get(nextNs);
        System.out.print(string(nextNs) + ": ");
        for(int j = 0; j < nsPkgs.size(); j++) {
          System.out.print(string(nsPkgs.get(j)) + "; ");
        }
        System.out.println();
      }
    }
    System.out.println();
    // Print package dictionary
    System.out.println("Packages: ");
    final Iterator<byte[]> pkgi = ctx.repo.pkgDict.iterator();
    byte[] nextPkg;
    while(pkgi.hasNext()) {
      nextPkg = pkgi.next();
      if(nextPkg != null) {
        System.out.print(string(nextPkg) + ": ");
        System.out.println(string(ctx.repo.pkgDict.get(nextPkg)));
      }
    }
  }
}
