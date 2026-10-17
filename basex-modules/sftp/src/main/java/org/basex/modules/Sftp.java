package org.basex.modules;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.Util;
import org.basex.util.list.*;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.*;

/**
 * SFTP package.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Sftp extends QueryModule implements com.jcraft.jsch.Logger {
  /** Logs. */
  private TokenList logs = new TokenList();

  /** Constructor. */
  public Sftp() {
    JSch.setLogger(this);
  }

  /**
   * Connects to an SFTP server.
   * @param server server path
   * @param user user name
   * @param password password
   * @return SFTP channel
   * @throws QueryException query exception
   */
  public ChannelSftp connect(final Str server, final Str user, final Str password)
      throws QueryException {
    return connect(server, user, password, Itr.get(22));
  }

  /**
   * Connects to an FTP server.
   * @param server server path
   * @param user user name
   * @param password password
   * @param port port
   * @return SFTP channel
   * @throws QueryException query exception
   */
  public ChannelSftp connect(final Str server, final Str user, final Str password, final Itr port)
      throws QueryException {
    return connect(server, user, password, port, XQMap.empty());
  }

  /**
   * Connects to an SFTP server.
   * @param server server path
   * @param user user name
   * @param password password
   * @param port port
   * @param map options (ProxyHost, ProxyPort, StrictHostKeyChecking=no, etc.).
   *   If a host and port is supplied, it overwrites the global configuration.
   *   The host can also be an empty string.
   * @return SFTP channel
   * @throws QueryException query exception
   */
  public ChannelSftp connect(final Str server, final Str user, final Str password, final Itr port,
      final XQMap map) throws QueryException {

    // parse options
    String proxyHost = null;
    int proxyPort = 0;

    final Properties config = new Properties();
    for(final Map.Entry<Object, Object> entry : map.toJava().entrySet()) {
      final String name = entry.getKey().toString(), value = entry.getValue().toString();
      if(name.equals("ProxyHost")) {
        proxyHost = value;
      } else if(name.equals("ProxyPort")) {
        proxyPort = Integer.parseInt(value);
      } else {
        config.put(name, value);
      }
    }
    if(proxyHost == null) {
      final StaticOptions soptions = queryContext.context.soptions;
      proxyHost = soptions.get(StaticOptions.PROXYHOST);
      if(proxyPort == 0) proxyPort = soptions.get(StaticOptions.PROXYPORT);
    }

    Session session = null;
    try {
      session = new JSch().getSession(user.toJava(), server.toJava(), (int) port.itr());
      if(!proxyHost.isEmpty()) session.setProxy(new ProxyHTTP(proxyHost, proxyPort));
      session.setConfig(config);
      session.setPassword(password.string());
      session.connect();

      final ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
      channel.connect();
      return channel;
    } catch(final JSchException ex) {
      if(session != null) session.disconnect();
      throw error(ex);
    }
  }

  /**
   * Disconnects from the server.
   * @param sftp SFTP channel
   * @throws QueryException query exception
   */
  public void disconnect(final ChannelSftp sftp) throws QueryException {
    run(() -> {
      sftp.getSession().disconnect();
    });
  }

  /**
   * Lists the file names of a remote SFTP directory.
   * @param sftp SFTP channel
   * @param path remote path
   * @return files
   * @throws QueryException query exception
   */
  public Value list(final ChannelSftp sftp, final Str path) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(queryContext);
    run(() -> {
      for(final Object o : sftp.ls(path.toJava())) {
        vb.add(Str.get(((LsEntry) o).getFilename()));
      }
    });
    return vb.value();
  }

  /**
   * Returns the attributes of a remote file.
   * @param sftp SFTP channel
   * @param path remote path
   * @return attribute map
   * @throws QueryException query exception
   */
  public XQMap attributes(final ChannelSftp sftp, final Str path) throws QueryException {
    final MapBuilder map = new MapBuilder();
    run(() -> {
      final SftpATTRS stat = sftp.lstat(path.toJava());
      map.put("last-modified", Dtm.get(stat.getMTime() * 1000L));
      map.put("size", Itr.get(stat.getSize()));
      map.put("dir", Bln.get(stat.isDir()));
    });
    return map.map();
  }

  /**
   * Retrieves the specified file as text.
   * @param sftp SFTP channel
   * @param path remote path
   * @return text
   * @throws QueryException query exception
   */
  public Str getText(final ChannelSftp sftp, final Str path) throws QueryException {
    return Str.get(get(sftp, path));
  }

  /**
   * Retrieves the specified file as binary.
   * @param sftp SFTP channel
   * @param path remote path
   * @return binary
   * @throws QueryException query exception
   */
  public B64 getBinary(final ChannelSftp sftp, final Str path) throws QueryException {
    return B64.get(get(sftp, path));
  }

  /**
   * Retrieves the specified file.
   * @param sftp SFTP channel
   * @param path remote path
   * @return file contents
   * @throws QueryException query exception
   */
  private byte[] get(final ChannelSftp sftp, final Str path) throws QueryException {
    final ArrayOutput ao = new ArrayOutput();
    run(() -> sftp.get(path.toJava(), ao));
    return ao.toArray();
  }

  /**
   * Retrieves the specified file and stores it locally.
   * @param sftp SFTP channel
   * @param path remote path
   * @param local local path
   * @throws QueryException query exception
   */
  public void getFile(final ChannelSftp sftp, final Str path, final Str local)
      throws QueryException {
    run(() -> {
      try(BufferOutput out = new BufferOutput(new IOFile(local.toJava()))) {
        sftp.get(path.toJava(), out);
      }
    });
  }

  /**
   * Uploads a string to the FTP server.
   * @param sftp SFTP channel
   * @param string textual string to be uploaded
   * @param path remote path
   * @throws QueryException query exception
   */
  public void putText(final ChannelSftp sftp, final Str string, final Str path)
      throws QueryException {
    put(sftp, new ArrayInput(string.string()), path);
  }

  /**
   * Uploads a binary to the FTP server.
   * @param sftp SFTP channel
   * @param item binary item to be uploaded
   * @param path remote path
   * @throws QueryException query exception
   */
  public void putBinary(final ChannelSftp sftp, final Bin item, final Str path)
      throws QueryException {
    put(sftp, item.input(null), path);
  }

  /**
   * Uploads a file.
   * @param sftp SFTP channel
   * @param input data to be stored
   * @param path remote path
   * @throws QueryException query exception
   */
  private void put(final ChannelSftp sftp, final InputStream input, final Str path)
      throws QueryException {
    run(() -> sftp.put(input, path.toJava()));
  }

  /**
   * Uploads a local file to the FTP server.
   * @param sftp SFTP channel
   * @param local local path
   * @param path remote path
   * @throws QueryException query exception
   */
  public void putFile(final ChannelSftp sftp, final Str local, final Str path)
      throws QueryException {
    run(() -> {
      try(BufferInput in = new BufferInput(new IOFile(local.toJava()))) {
        sftp.put(in, path.toJava());
      }
    });
  }

  /**
   * Moves a file.
   * @param sftp SFTP channel
   * @param oldPath old path
   * @param newPath new path
   * @throws QueryException query exception
   */
  public void move(final ChannelSftp sftp, final Str oldPath, final Str newPath)
      throws QueryException {
    run(() -> sftp.rename(oldPath.toJava(), newPath.toJava()));
  }

  /**
   * Removes a file.
   * @param sftp SFTP channel
   * @param path path
   * @throws QueryException query exception
   */
  public void delete(final ChannelSftp sftp, final Str path) throws QueryException {
    run(() -> sftp.rm(path.toJava()));
  }

  /**
   * Removes a directory.
   * @param sftp SFTP channel
   * @param path path
   * @throws QueryException query exception
   */
  public void deleteDir(final ChannelSftp sftp, final Str path) throws QueryException {
    run(() -> sftp.rmdir(path.toJava()));
  }

  /**
   * Changes to a directory.
   * @param sftp SFTP channel
   * @param path path
   * @throws QueryException query exception
   */
  public void cd(final ChannelSftp sftp, final Str path) throws QueryException {
    run(() -> sftp.cd(path.toJava()));
  }

  /**
   * Returns and resets the log information.
   * @return logs
   */
  public Value logs() {
    final Value value = StrSeq.get(logs);
    logs = new TokenList();
    return value;
  }

  /**
   * Runs an SFTP operation and catches errors.
   * @param op operation
   * @throws QueryException query exception
   */
  public void run(final SftpFunction op) throws QueryException {
    try {
      op.apply();
    } catch(final Exception ex) {
      throw error(ex);
    }
  }

  /**
   * Throws a query exception for the specified exception.
   * @param ex exception
   * @return query exception
   */
  private static QueryException error(final Exception ex) {
    return new QueryException("SFTP: " + Util.message(ex));
  }

  @Override
  public boolean isEnabled(final int level) {
    return true;
  }

  @Override
  public void log(final int level, final String message) {
    logs.add(level + ": " + message);
  }
}
