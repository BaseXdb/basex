package org.basex.modules;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.core.jobs.*;
import org.basex.core.jobs.Job.*;
import org.basex.query.*;
import org.basex.util.*;

import com.jcraft.jsch.*;

/**
 * Open SFTP sessions of a query.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SftpSessions implements QueryResource {
  /** Open sessions and their sockets (socket can be {@code null}). */
  private final Map<Session, Socket> sessions = new HashMap<>();
  /** Handle that unregisters the sessions from the job that opened them. */
  private final Binding binding = Job.closeOnStop(this::close);
  /** Indicates if the sessions have been closed. */
  private boolean closed;

  /**
   * Adds a session and tracks its socket, which is closed if the query is stopped.
   * @param session session
   * @param timeout connection timeout in milliseconds ({@code 0}: no timeout)
   * @throws JSchException JSch exception
   */
  synchronized void add(final Session session, final int timeout) throws JSchException {
    if(closed) throw new JSchException("Sessions have been closed.");
    sessions.put(session, null);
    session.setSocketFactory(new SocketFactory() {
      @Override
      public Socket createSocket(final String host, final int port) throws IOException {
        final Socket socket = new Socket();
        register(session, socket);
        socket.connect(new InetSocketAddress(host, port), timeout);
        return socket;
      }

      @Override
      public InputStream getInputStream(final Socket socket) throws IOException {
        return socket.getInputStream();
      }

      @Override
      public OutputStream getOutputStream(final Socket socket) throws IOException {
        return socket.getOutputStream();
      }
    });
  }

  /**
   * Registers the socket of a session.
   * @param session session
   * @param socket socket
   * @throws IOException I/O exception
   */
  private synchronized void register(final Session session, final Socket socket)
      throws IOException {
    if(!sessions.containsKey(session)) {
      socket.close();
      throw new SocketException("Session has been closed.");
    }
    sessions.put(session, socket);
  }

  /**
   * Disconnects and removes a session.
   * @param session session
   */
  void remove(final Session session) {
    final Socket socket;
    synchronized(this) {
      socket = sessions.remove(session);
    }
    close(session, socket);
  }

  @Override
  public void close() {
    final Map<Session, Socket> open;
    synchronized(this) {
      closed = true;
      open = new HashMap<>(sessions);
      sessions.clear();
    }
    binding.close();
    // disconnect outside the lock: unresponsive servers must not block other sessions
    open.forEach(SftpSessions::close);
  }

  /**
   * Disconnects a session and closes its socket.
   * @param session session
   * @param socket socket (can be {@code null})
   */
  private static void close(final Session session, final Socket socket) {
    session.disconnect();
    // a session that is still connecting ignores disconnect: close its socket
    if(socket != null) {
      try {
        socket.close();
      } catch(final IOException ex) {
        Util.debug(ex);
      }
    }
  }
}
