package org.basex.http.webdav;

import static com.bradmcevoy.http.LockResult.*;
import java.io.*;
import java.util.UUID;

import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.server.*;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * WebDAV resource representing an abstract folder within a collection database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public abstract class BXAbstractResource extends BXResource implements
    CopyableResource, DeletableResource, MoveableResource, LockableResource {

  /**
   * Constructor.
   * @param d database name
   * @param p path to folder
   * @param m last modified date
   * @param h http context
   */
  protected BXAbstractResource(final String d, final String p, final long m,
      final HTTPContext h) {
    super(d, p, m, h);
  }

  @Override
  public void delete() throws BadRequestException {
    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        del();
      }
    }.eval();
  }

  @Override
  public void copyTo(final CollectionResource target, final String name)
      throws BadRequestException {

    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        if(target instanceof BXRoot)
          copyToRoot(name);
        else if(target instanceof BXFolder)
          copyTo((BXFolder) target, name);
      }
    }.eval();
  }

  @Override
  public void moveTo(final CollectionResource target, final String name)
      throws BadRequestException {

    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        if(target instanceof BXRoot)
          moveToRoot(name);
        else if(target instanceof BXFolder)
          moveTo((BXFolder) target, name);
      }
    }.eval();
  }

  /**
   * Lock this resource and return a token.
   *
   * @param timeout - in seconds, or null
   * @param lockInfo
   * @return - a result containing the token representing the lock if successful,
   * otherwise a failure reason code
   */
  @Override
  public LockResult lock(final LockTimeout timeout, final LockInfo lockInfo) throws
    NotAuthorizedException, PreConditionFailedException, LockedException {

    final String tokenId = UUID.randomUUID().toString();

    final FailureReason failureReason = new BXCode<FailureReason>(this) {
      @Override
      public FailureReason get() throws IOException {
        return lock(tokenId, timeout, lockInfo);
      }
    }.evalNoEx();

    return failureReason == null ?
      success(new LockToken(tokenId, lockInfo, timeout)) :
      failed(failureReason);
  }

  /**
   * Renew the lock and return new lock info.
   *
   * @param token
   * @return
   */
  @Override
  public LockResult refreshLock(final String token) throws NotAuthorizedException,
    PreConditionFailedException {
    return null;
  }

  /**
   * If the resource is currently locked, and the tokenId  matches the current
   * one, unlock the resource.
   *
   * @param tokenId
   */
  @Override
  public void unlock(final String tokenId) throws NotAuthorizedException,
    PreConditionFailedException {

    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        final String queryStr =
            "import module namespace w = 'http://basex.org/webdav';" +
            "w:delete-lock($lock-token)";

        LocalQuery q = http.session().query(queryStr);
        q.bind("lock-token", tokenId);

        q.execute();
      }
    }.evalNoEx();
  }

  /**
   * @return - the current lock, if the resource is locked, or null
   */
  @Override
  public LockToken getCurrentLock() {
    return new BXCode<LockToken>(this)
    {
      @Override
      public LockToken get() throws IOException {
        try {
          return getCurrentActiveLock();
        } catch(SAXException e) {
          throw new IOException(e);
        }
      }
    }.evalNoEx();
  }

  /**
   * Delete document or folder.
   * @throws IOException I/O exception
   */
  protected void del() throws IOException {
    final LocalSession session = http.session();
    session.execute(new Open(db));
    session.execute(new Delete(path));

    // create dummy, if parent is an empty folder
    final int ix = path.lastIndexOf(SEP);
    if(ix > 0) createDummy(path.substring(0, ix));
  }

  /**
   * Rename document or folder.
   * @param n new name
   * @throws IOException I/O exception
   */
  protected void rename(final String n) throws IOException {
    final LocalSession session = http.session();
    session.execute(new Open(db));
    session.execute(new Rename(path, n));

    // create dummy, if old parent is an empty folder
    final int i1 = path.lastIndexOf(SEP);
    if(i1 > 0) createDummy(path.substring(0, i1));

    // delete dummy, if new parent is an empty folder
    final int i2 = n.lastIndexOf(SEP);
    if(i2 > 0) deleteDummy(n.substring(0, i2));
  }

  /**
   * Copy folder to the root, creating a new database.
   * @param n new name of the folder (database)
   * @throws IOException I/O exception
   */
  protected abstract void copyToRoot(final String n) throws IOException;

  /**
   * Copy folder to another folder.
   * @param f target folder
   * @param n new name of the folder
   * @throws IOException I/O exception
   */
  protected abstract void copyTo(final BXFolder f, final String n) throws IOException;

  /**
   * Move folder to the root, creating a new database.
   * @param n new name of the folder (database)
   * @throws IOException I/O exception
   */
  protected void moveToRoot(final String n) throws IOException {
    // folder is moved to the root: create new database with it
    copyToRoot(n);
    del();
  }

  /**
   * Move folder to another folder.
   * @param f target folder
   * @param n new name of the folder
   * @throws IOException I/O exception
   */
  protected void moveTo(final BXFolder f, final String n) throws IOException {
    if(f.db.equals(db)) {
      // folder is moved to a folder in the same database
      rename(f.path + SEP + n);
    } else {
      // folder is moved to a folder in another database
      copyTo(f, n);
      del();
    }
  }

  protected FailureReason lock(final String tokenId, final LockTimeout timeout,
    final LockInfo lockInfo) throws IOException {

    createLock(tokenId, timeout, lockInfo);

    return null;
  }

  protected void createLock(final String tokenId, final LockTimeout timeout,
    final LockInfo lockInfo) throws IOException {
    final String queryStr =
        "import module namespace w = 'http://basex.org/webdav';" +
        "w:create-lock(" +
        "$resource," +
        "$lock-token," +
        "$lock-scope," +
        "$lock-type," +
        "$lock-depth," +
        "$lock-owner," +
        "$lock-timeout)";

    LocalQuery q = http.session().query(queryStr);
    q.bind("resource", db + SEP + path);
    q.bind("lock-token", tokenId);
    q.bind("lock-scope", lockInfo.scope.name().toLowerCase());
    q.bind("lock-type", lockInfo.type.name().toLowerCase());
    q.bind("lock-depth", lockInfo.depth.name().toLowerCase());
    q.bind("lock-owner", lockInfo.lockedByUser);
    final Long timeoutSeconds = timeout.getSeconds();
    q.bind("lock-timeout", timeoutSeconds == null ? Long.MAX_VALUE : timeoutSeconds);

    q.execute();
  }

  protected LockToken getCurrentActiveLock() throws IOException, SAXException {
    final String queryStr =
        "import module namespace w = 'http://basex.org/webdav';" +
        "w:get-locks($resource)";

    LocalQuery q = http.session().query(queryStr);
    q.bind("resource", db + SEP + path);

    LockToken result = null;
    if(q.more()) {
      String lockTokenStr = q.next();
      result = parseLockInfo(lockTokenStr);
    }

    return result;
  }

  public static LockToken parseLockInfo(String lockInfo) throws SAXException, IOException {
    XMLReader reader = XMLReaderFactory.createXMLReader();
    LockTokenSaxHandler handler = new LockTokenSaxHandler();
    reader.setContentHandler(handler);
    reader.parse(new InputSource(new StringReader(lockInfo)));
    return handler.lockToken;
  }

  public static final class LockTokenSaxHandler extends DefaultHandler {
    public final LockToken lockToken = new LockToken(null, new LockInfo(), null);
    private String elementName;

    @Override
    public void startElement(String uri, String localName, String name,
        Attributes attributes) throws SAXException {
      elementName = localName;
      super.startElement(uri, localName, name, attributes);
    }

    @Override
    public void endElement (String uri, String localName, String qName)
        throws SAXException {
      elementName = null;
      super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      String value = String.valueOf(ch, start, length);
      if("token".equals(elementName))
        lockToken.tokenId = value;
      else if("scope".equals(elementName))
        lockToken.info.scope = LockInfo.LockScope.valueOf(value.toUpperCase());
      else if("type".equals(elementName))
        lockToken.info.type = LockInfo.LockType.valueOf(value.toUpperCase());
      else if("depth".equals(elementName))
        lockToken.info.depth = LockInfo.LockDepth.valueOf(value.toUpperCase());
      else if("owner".equals(elementName))
        lockToken.info.lockedByUser = value;
      else if("timeout".equals(elementName))
        lockToken.timeout = LockTimeout.parseTimeout(value);
    }
  }
}
