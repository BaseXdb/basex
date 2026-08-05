package org.basex.gui;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class exchanges file paths with a GUI instance that is already running.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GUIInstance {
  /** Handshake token. */
  private static final String TOKEN = Prop.NAME + " GUI";
  /** First port of the dynamic port range. */
  private static final int PORT = 49152;
  /** Number of ports in the dynamic port range. */
  private static final int PORTS = 16384;
  /** Connection and read timeout (ms). */
  private static final int TIMEOUT = 500;

  /** Private constructor. */
  private GUIInstance() { }

  /**
   * Delegates the specified files to a GUI instance that is already running.
   * @param files files to be opened
   * @return {@code true} if a running instance has confirmed the request
   */
  public static boolean delegate(final String[] files) {
    try(Socket socket = new Socket()) {
      socket.connect(address(files), TIMEOUT);
      socket.setSoTimeout(TIMEOUT);
      final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
      out.writeUTF(TOKEN);
      out.writeInt(files.length);
      for(final String file : files) out.writeUTF(file);
      out.flush();
      return new DataInputStream(socket.getInputStream()).readBoolean();
    } catch(final IOException ex) {
      Util.debug(ex);
      return false;
    }
  }

  /**
   * Listens for files that are delegated by other GUI instances.
   * @param gui reference to the main window
   * @param files files that have been opened by this instance
   * @param handler handler for the received files
   */
  public static void listen(final GUI gui, final String[] files,
      final Consumer<String[]> handler) {
    final ServerSocket server;
    try {
      server = new ServerSocket();
      server.bind(address(files));
    } catch(final IOException ex) {
      // port is occupied by another application: continue without handshake
      Util.debug(ex);
      return;
    }

    // release the port when the window is closed
    gui.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(final WindowEvent e) {
        try {
          server.close();
        } catch(final IOException ex) {
          Util.debug(ex);
        }
      }
    });

    final Thread thread = new Thread(() -> {
      while(!server.isClosed()) {
        try(Socket socket = server.accept()) {
          socket.setSoTimeout(TIMEOUT);
          final DataInputStream in = new DataInputStream(socket.getInputStream());
          if(!in.readUTF().equals(TOKEN)) continue;
          final StringList paths = new StringList();
          for(int f = in.readInt(); f > 0; f--) paths.add(in.readUTF());
          new DataOutputStream(socket.getOutputStream()).writeBoolean(true);
          handler.accept(paths.finish());
        } catch(final IOException ex) {
          Util.debug(ex);
        }
      }
    }, TOKEN);
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Returns the loopback address that is assigned to the home directory of the specified files.
   * @param files files to be opened
   * @return socket address
   */
  private static InetSocketAddress address(final String[] files) {
    String path = new IOFile(homeDir(files)).path();
    if(!Prop.CASE) path = path.toLowerCase(Locale.ENGLISH);
    return new InetSocketAddress(InetAddress.getLoopbackAddress(),
        PORT + Math.floorMod(path.hashCode(), PORTS));
  }

  /**
   * Returns the home directory that is responsible for the specified files.
   * @param files files to be opened
   * @return home directory
   */
  private static String homeDir(final String[] files) {
    // consider first file: check if it is located in a home directory or one of its descendants
    if(files.length > 0) {
      final IOFile parent = new IOFile(files[0]).parent();
      if(parent != null) {
        final String home = Prop.homeDir(parent.path());
        if(home != null) return home;
      }
    }
    return Prop.HOMEDIR;
  }
}
