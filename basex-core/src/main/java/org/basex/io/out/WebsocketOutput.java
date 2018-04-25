package org.basex.io.out;

import java.io.*;
import java.nio.*;
import java.util.*;

import org.eclipse.jetty.websocket.api.*;

/**
 * This class is a stream-wrapper.
 *
 * @author BaseX Team 2005-18, BSD License
 */
public class WebsocketOutput extends OutputStream {

  /**
   * The RemoteEndpoint of the Websocket.
   * */
  RemoteEndpoint os;

  /**
   * The bytearray for building the messages.
   * @TODO: Search for better Implementation (List of Objects not very efficient)
   * */
  ArrayList<Byte> bytes = new ArrayList<>();

  /**
   * Standardconstructor.
   * @param os RemoteEndpoint
   * */
  public WebsocketOutput(final RemoteEndpoint os) {
    this.os = os;
  }

  @Override
  public void write(final int b) throws IOException {
    bytes.add((byte) b);
  }

  /**
   * Send the Message.
   * */
  @Override
  public void flush() throws IOException {
    // Convert the bytearraylist to bytebuffer
    ByteBuffer buffer = ByteBuffer.allocate(bytes.size());
    for(Byte byt : bytes) {
        buffer.put(byt);
    }

    System.out.println("websocket write " + new String(buffer.array(), "UTF-8"));
    //os.sendBytes(result);
    os.sendString(new String(buffer.array(), "UTF-8"));
  }
}
