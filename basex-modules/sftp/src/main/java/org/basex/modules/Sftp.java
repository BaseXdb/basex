package org.basex.modules;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.Strings;
import org.basex.util.Token;
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
public final class Sftp extends QueryModule {
  /** Module prefix. */
  private static final byte[] PREFIX = Token.token("sftp");
  /** Module namespace URI. */
  private static final byte[] URI = Token.token("http://basex.org/modules/sftp");

  /** Logs. */
  private final TokenList logs = new TokenList();
  /** Logger of the sessions opened by this module instance. */
  private final Logger logger = new Logger() {
    @Override
    public boolean isEnabled(final int level) {
      return true;
    }

    @Override
    public void log(final int level, final String message) {
      synchronized(logs) {
        logs.add(level + ": " + message);
      }
    }
  };

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
    String proxyHost = null, identity = null, knownHosts = null;
    byte[] passphrase = null;
    int proxyPort = 0, timeout = 0;

    final Properties config = new Properties();
    for(final Map.Entry<Object, Object> entry : map.toJava().entrySet()) {
      final String name = entry.getKey().toString(), value = entry.getValue().toString();
      switch(name) {
        case "ProxyHost" -> proxyHost = value;
        case "ProxyPort" -> proxyPort = integer(name, value);
        case "Timeout" -> timeout = integer(name, value);
        case "IdentityFile" -> identity = value;
        case "Passphrase" -> passphrase = Token.token(value);
        case "KnownHosts" -> knownHosts = value;
        default -> config.put(name, value);
      }
    }
    if(passphrase != null && identity == null) {
      throw error("options", "Passphrase requires IdentityFile.");
    }
    if(proxyHost == null) {
      final StaticOptions soptions = queryContext.context.soptions;
      proxyHost = soptions.get(StaticOptions.PROXYHOST);
      if(proxyPort == 0) proxyPort = soptions.get(StaticOptions.PROXYPORT);
    }

    Session session = null;
    try {
      final JSch jsch = new JSch();
      jsch.setInstanceLogger(logger);
      if(knownHosts != null) jsch.setKnownHosts(knownHosts);
      if(identity != null) addIdentity(jsch, identity, passphrase);
      session = jsch.getSession(user.toJava(), server.toJava(), (int) port.itr());
      // register session before connecting: stopping the query aborts the connection
      sessions().add(session, timeout);
      if(!proxyHost.isEmpty()) session.setProxy(new ProxyHTTP(proxyHost, proxyPort));
      session.setConfig(config);
      session.setPassword(password.string());
      session.connect(timeout);

      final ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
      channel.connect(timeout);
      return channel;
    } catch(final JSchException ex) {
      if(session != null) sessions().remove(session);
      // report a stopped query as such, not as a connection error
      queryContext.checkStop();
      throw error(ex);
    }
  }

  /**
   * Adds a private key and checks the passphrase of encrypted keys.
   * @param jsch JSch instance
   * @param identity path to the private key
   * @param passphrase passphrase (can be {@code null})
   * @throws QueryException query exception
   * @throws JSchException JSch exception
   */
  private static void addIdentity(final JSch jsch, final String identity,
      final byte[] passphrase) throws QueryException, JSchException {
    // loading does not decrypt the key: JSch decrypts it once when it is added
    final KeyPair key = KeyPair.load(jsch, identity);
    final boolean encrypted = key.isEncrypted();
    key.dispose();
    if(encrypted && passphrase == null) {
      throw error("passphrase", "Passphrase required: " + identity);
    }
    try {
      jsch.addIdentity(identity, passphrase);
    } catch(final JSchException ex) {
      // the key has been loaded before: an encrypted key can only fail to decrypt
      if(encrypted) throw error("passphrase", "Incorrect passphrase: " + identity);
      throw ex;
    }
  }

  /**
   * Parses a non-negative integer option.
   * @param name name of the option
   * @param value value
   * @return integer
   * @throws QueryException query exception
   */
  private static int integer(final String name, final String value) throws QueryException {
    final int i = Strings.toInt(value);
    if(i >= 0) return i;
    throw error("options", "Invalid value for " + name + ": " + value);
  }

  /**
   * Disconnects from the server.
   * @param sftp SFTP channel
   * @throws QueryException query exception
   */
  public void disconnect(final ChannelSftp sftp) throws QueryException {
    run(() -> sessions().remove(sftp.getSession()));
  }

  /**
   * Returns the open sessions of the current query.
   * @return sessions
   */
  private SftpSessions sessions() {
    return queryContext.resources.index(SftpSessions.class);
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
      for(final LsEntry entry : ls(sftp, path.toJava())) vb.add(Str.get(entry.getFilename()));
    });
    return vb.value();
  }

  /**
   * Returns the entries of a remote directory, excluding {@code .} and {@code ..}.
   * @param sftp SFTP channel
   * @param dir remote path
   * @return entries
   * @throws SftpException SFTP exception
   */
  private static List<LsEntry> ls(final ChannelSftp sftp, final String dir) throws SftpException {
    final List<LsEntry> entries = new ArrayList<>();
    for(final Object o : sftp.ls(dir)) {
      final LsEntry entry = (LsEntry) o;
      if(!Strings.eq(entry.getFilename(), ".", "..")) entries.add(entry);
    }
    return entries;
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
      final String string = path.toJava();
      attributes(sftp, string, sftp.lstat(string), map);
    });
    return map.map();
  }

  /**
   * Returns the names and attributes of the entries of a remote directory.
   * @param sftp SFTP channel
   * @param path remote path
   * @return maps with entry names and attributes
   * @throws QueryException query exception
   */
  public Value entries(final ChannelSftp sftp, final Str path) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(queryContext);
    run(() -> {
      final String dir = path.toJava();
      final String prefix = dir.isEmpty() || dir.endsWith("/") ? dir : dir + '/';
      for(final LsEntry entry : ls(sftp, dir)) {
        final String name = entry.getFilename();
        final MapBuilder map = new MapBuilder().put("name", name);
        attributes(sftp, prefix + name, entry.getAttrs(), map);
        vb.add(map.map());
      }
    });
    return vb.value();
  }

  /**
   * Adds file attributes to a map, following symbolic links.
   * @param sftp SFTP channel
   * @param path remote path
   * @param attrs attributes of the file or link
   * @param map map builder
   * @throws Exception exception
   */
  private static void attributes(final ChannelSftp sftp, final String path,
      final SftpATTRS attrs, final MapBuilder map) throws Exception {
    final boolean link = attrs.isLink();
    final SftpATTRS target = link ? stat(sftp, path) : null;
    // files and broken links: return the given attributes
    final SftpATTRS atts = target != null ? target : attrs;
    final int flags = atts.getFlags();
    if((flags & SftpATTRS.SSH_FILEXFER_ATTR_ACMODTIME) != 0) {
      // times are unsigned 32-bit values
      map.put("last-modified", Dtm.get(Integer.toUnsignedLong(atts.getMTime()) * 1000));
      map.put("last-accessed", Dtm.get(Integer.toUnsignedLong(atts.getATime()) * 1000));
    }
    map.put("size", Itr.get(atts.getSize()));
    map.put("dir", Bln.get(atts.isDir()));
    map.put("link", Bln.get(link));
    if((flags & SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS) != 0) {
      map.put("permissions", atts.getPermissionsString().substring(1));
    }
    if((flags & SftpATTRS.SSH_FILEXFER_ATTR_UIDGID) != 0) {
      map.put("uid", Itr.get(atts.getUId()));
      map.put("gid", Itr.get(atts.getGId()));
    }
  }

  /**
   * Checks if a remote file or directory exists.
   * @param sftp SFTP channel
   * @param path remote path
   * @return result of check
   * @throws QueryException query exception
   */
  public Bln exists(final ChannelSftp sftp, final Str path) throws QueryException {
    try {
      return Bln.get(stat(sftp, path.toJava()) != null);
    } catch(final SftpException ex) {
      throw error(ex);
    }
  }

  /**
   * Creates a remote directory and all missing parent directories.
   * @param sftp SFTP channel
   * @param path remote path
   * @throws QueryException query exception
   */
  public void createDir(final ChannelSftp sftp, final Str path) throws QueryException {
    run(() -> {
      final String string = path.toJava();
      // common case: directory exists
      final SftpATTRS target = stat(sftp, string);
      if(target != null && target.isDir()) return;
      final StringBuilder dir = new StringBuilder(string.startsWith("/") ? "/" : "");
      boolean created = false;
      for(final String segment : string.split("/")) {
        if(segment.isEmpty()) continue;
        final String current = dir.append(segment).toString();
        // below a created directory, all segments are missing
        final SftpATTRS attrs = created ? null : stat(sftp, current);
        if(attrs == null) {
          created = true;
          sftp.mkdir(current);
        } else if(!attrs.isDir()) {
          throw error("no-dir", "Not a directory: " + current);
        }
        dir.append('/');
      }
    });
  }

  /**
   * Returns the attributes of a remote file.
   * @param sftp SFTP channel
   * @param path remote path
   * @return attributes (can be {@code null})
   * @throws SftpException SFTP exception
   */
  private static SftpATTRS stat(final ChannelSftp sftp, final String path) throws SftpException {
    try {
      return sftp.stat(path);
    } catch(final SftpException ex) {
      if(ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) return null;
      throw ex;
    }
  }

  /**
   * Retrieves the specified file as text.
   * @param sftp SFTP channel
   * @param path remote path
   * @return text
   * @throws QueryException query exception
   */
  public Str getText(final ChannelSftp sftp, final Str path) throws QueryException {
    return text(get(sftp, path), null);
  }

  /**
   * Retrieves the specified file as text in the specified encoding.
   * @param sftp SFTP channel
   * @param path remote path
   * @param encoding encoding
   * @return text
   * @throws QueryException query exception
   */
  public Str getText(final ChannelSftp sftp, final Str path, final Str encoding)
      throws QueryException {
    final String enc = encoding(encoding);
    return text(get(sftp, path), enc);
  }

  /**
   * Retrieves the specified file as XML document.
   * @param sftp SFTP channel
   * @param path remote path
   * @return document node
   * @throws QueryException query exception
   */
  public DBNode getXml(final ChannelSftp sftp, final Str path) throws QueryException {
    final IOContent io = new IOContent(get(sftp, path), path.toJava());
    try {
      return new DBNode(Parser.xmlParser(io, queryContext.context.options));
    } catch(final IOException ex) {
      throw error("parse", Util.message(ex));
    }
  }

  /**
   * Decodes text and checks for invalid XML characters.
   * @param bytes bytes
   * @param encoding encoding (can be {@code null})
   * @return string
   * @throws QueryException query exception
   */
  private static Str text(final byte[] bytes, final String encoding) throws QueryException {
    try(TextInput ti = new TextInput(new IOContent(bytes), encoding)) {
      return Str.get(ti.fallback(false).content());
    } catch(final IOException ex) {
      throw error("encoding", Util.message(ex));
    }
  }

  /**
   * Returns a supported encoding.
   * @param encoding encoding
   * @return encoding
   * @throws QueryException query exception
   */
  private static String encoding(final Str encoding) throws QueryException {
    final String enc = encoding.toJava(), error = Strings.checkEncoding(enc);
    if(error != null) throw error("encoding", error);
    return Strings.normEncoding(enc, false);
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
  private static byte[] get(final ChannelSftp sftp, final Str path) throws QueryException {
    final ArrayOutput ao = new ArrayOutput();
    run(() -> sftp.get(path.toJava(), ao));
    return ao.finish();
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
    put(sftp, encode(string, null), path, ChannelSftp.OVERWRITE);
  }

  /**
   * Uploads a string in the specified encoding to a remote file.
   * @param sftp SFTP channel
   * @param string textual string to be uploaded
   * @param path remote path
   * @param encoding encoding
   * @throws QueryException query exception
   */
  public void putText(final ChannelSftp sftp, final Str string, final Str path,
      final Str encoding) throws QueryException {
    put(sftp, encode(string, encoding), path, ChannelSftp.OVERWRITE);
  }

  /**
   * Appends a string to a remote file.
   * @param sftp SFTP channel
   * @param string textual string to be appended
   * @param path remote path
   * @throws QueryException query exception
   */
  public void appendText(final ChannelSftp sftp, final Str string, final Str path)
      throws QueryException {
    put(sftp, encode(string, null), path, ChannelSftp.APPEND);
  }

  /**
   * Appends a string in the specified encoding to a remote file.
   * @param sftp SFTP channel
   * @param string textual string to be appended
   * @param path remote path
   * @param encoding encoding
   * @throws QueryException query exception
   */
  public void appendText(final ChannelSftp sftp, final Str string, final Str path,
      final Str encoding) throws QueryException {
    put(sftp, encode(string, encoding), path, ChannelSftp.APPEND);
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
    put(sftp, item.input(null), path, ChannelSftp.OVERWRITE);
  }

  /**
   * Appends a binary to a remote file.
   * @param sftp SFTP channel
   * @param item binary item to be appended
   * @param path remote path
   * @throws QueryException query exception
   */
  public void appendBinary(final ChannelSftp sftp, final Bin item, final Str path)
      throws QueryException {
    put(sftp, item.input(null), path, ChannelSftp.APPEND);
  }

  /**
   * Encodes a string.
   * @param string string
   * @param encoding encoding (can be {@code null})
   * @return input stream
   * @throws QueryException query exception
   */
  private static ArrayInput encode(final Str string, final Str encoding) throws QueryException {
    final String enc = encoding != null ? encoding(encoding) : null;
    try {
      return new ArrayInput(Token.encode(string.string(), enc));
    } catch(final CharacterCodingException ex) {
      throw error("encoding", "String cannot be encoded as " + enc + '.');
    }
  }

  /**
   * Uploads a file.
   * @param sftp SFTP channel
   * @param input data to be stored
   * @param path remote path
   * @param mode transfer mode
   * @throws QueryException query exception
   */
  private static void put(final ChannelSftp sftp, final InputStream input, final Str path,
      final int mode) throws QueryException {
    run(() -> sftp.put(input, path.toJava(), mode));
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
   * Changes the permissions of a remote file.
   * @param sftp SFTP channel
   * @param path path
   * @param permissions octal permissions (e.g. {@code 755})
   * @throws QueryException query exception
   */
  public void chmod(final ChannelSftp sftp, final Str path, final Str permissions)
      throws QueryException {
    final String perms = permissions.toJava();
    if(!perms.matches("[0-7]{3,4}")) throw error("format", "Invalid permissions: " + perms);
    run(() -> sftp.chmod(Integer.parseInt(perms, 8), path.toJava()));
  }

  /**
   * Returns the remote working directory.
   * @param sftp SFTP channel
   * @return path
   * @throws QueryException query exception
   */
  public Str pwd(final ChannelSftp sftp) throws QueryException {
    try {
      return Str.get(sftp.pwd());
    } catch(final SftpException ex) {
      throw error(ex);
    }
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
    synchronized(logs) {
      return StrSeq.get(logs.next());
    }
  }

  /**
   * Runs an SFTP operation and catches errors.
   * @param op operation
   * @throws QueryException query exception
   */
  private static void run(final SftpFunction op) throws QueryException {
    try {
      op.apply();
    } catch(final QueryException ex) {
      throw ex;
    } catch(final Exception ex) {
      throw error(ex);
    }
  }

  /**
   * Returns a query exception for the specified exception.
   * @param ex exception
   * @return query exception
   */
  private static QueryException error(final Exception ex) {
    final String code = ex instanceof final SftpException se ? switch(se.id) {
      case ChannelSftp.SSH_FX_NO_SUCH_FILE -> "not-found";
      case ChannelSftp.SSH_FX_PERMISSION_DENIED -> "permission";
      default -> "error";
    } : ex instanceof JSchException ? "connect" : "error";
    return error(code, Util.message(ex));
  }

  /**
   * Returns a query exception with an error code in the module namespace.
   * @param code error code
   * @param message error message
   * @return query exception
   */
  private static QueryException error(final String code, final String message) {
    return new QueryException(null, new QNm(PREFIX, code, URI), "SFTP: %", message);
  }
}
