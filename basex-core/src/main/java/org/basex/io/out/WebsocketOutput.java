package org.basex.io.out;

import java.io.*;
import java.nio.*;

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
   * The standard Bytebuffersize.
   * */
  final int bytebufferSize = 10000;

  /**
   * The Bytebuffer for building the messages.
   * */
  ByteBuffer bbuf = ByteBuffer.allocate(bytebufferSize);

  /**
   * Standardconstructor.
   * @param os RemoteEndpoint
   * */
  public WebsocketOutput(final RemoteEndpoint os) {
    this.os = os;
  }

  @Override
  public void write(final int b) throws IOException {
    if(bbuf.remaining() == 0) {
      // Increase bbuf-capacity
    }
    bbuf.put((byte) b);
  }

  /**
   * Send the Message.
   * */
  @Override
  public void flush() throws IOException {
    System.out.println("websocket write " + new String(bbuf.array(), "UTF-8"));
    os.sendBytes(bbuf);
    bbuf = null;
    bbuf = ByteBuffer.allocate(bytebufferSize);
  }
}
