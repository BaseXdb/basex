package org.basex.examples.api;

import java.io.*;
import java.util.*;

/**
 * This example shows how binary resource can be added to and retrieved from
 * the database.
 *
 * This example required a running database server instance.
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-14, BSD License
 */
public final class BinaryExample {
  /**
   * Main method.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  public static void main(final String[] args) throws IOException {
    // create session
    final BaseXClient session = new BaseXClient("localhost", 1984, "admin", "admin");

    try {
      // create empty database
      session.execute("create db database");
      System.out.println(session.info());

      // define input stream
      final byte[] bytes = new byte[256];
      for(int b = 0; b < bytes.length; b++) bytes[b] = (byte) b;
      final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

      // add document
      session.store("test.bin", bais);
      System.out.println(session.info());

      // receive data
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      session.execute("retrieve test.bin", baos);

      // should always yield true
      if(Arrays.equals(bytes, baos.toByteArray())) {
        System.out.println("Stored and retrieved bytes are equal.");
      } else {
        System.err.println("Stored and retrieved bytes differ!");
      }

      // drop database
      session.execute("drop db database");

    } finally {
      // close session
      session.close();
    }
  }
}
