package org.basex.http.webdav.milton1;

import static org.basex.http.webdav.impl.Utils.*;
import static com.bradmcevoy.http.LockResult.*;
import java.io.*;
import java.util.Date;

import org.basex.core.Prop;
import org.basex.http.webdav.impl.ResourceMetaData;
import org.basex.http.webdav.impl.WebDAVService;

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
 * @author BaseX Team 2005-13, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public abstract class BXAbstractResource implements
    CopyableResource, DeletableResource, MoveableResource, LockableResource {
  /** Resource meta data. */
  protected final ResourceMetaData meta;
  /** WebDAV service implementation. */
  protected final WebDAVService<BXAbstractResource> service;

  /**
   * Constructor.
   * @param m resource meta data
   * @param s service
   */
  protected BXAbstractResource(final ResourceMetaData m,
      final WebDAVService<BXAbstractResource> s) {
    meta = m;
    service = s;
  }

  @Override
  public Object authenticate(final String user, final String pass) {
    if(user != null)
      new BXCode<Object>(this) {
        @Override
        public void run() throws IOException {
          service.authenticate(user, pass);
        }
      }.evalNoEx();
    return user;
  }

  @Override
  public boolean authorise(final Request request, final Request.Method method,
      final Auth auth) {
    return auth != null && auth.getTag() != null && service.authorise(auth.getUser(),
      "any", meta.db, meta.path);
  }

  @Override
  public String checkRedirect(final Request request) {
    return null;
  }

  @Override
  public String getRealm() {
    return Prop.NAME;
  }

  @Override
  public String getUniqueId() {
    return null;
  }

  @Override
  public String getName() {
    return name(meta.path);
  }

  @Override
  public Date getModifiedDate() {
    return meta.mdate;
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
   * @param lockInfo lock info
   * @return - a result containing the token representing the lock if successful,
   * otherwise a failure reason code
   */
  @Override
  public LockResult lock(final LockTimeout timeout, final LockInfo lockInfo) throws
    NotAuthorizedException, PreConditionFailedException, LockedException {
    return new BXCode<LockResult>(this) {
      @Override
      public LockResult get() throws IOException {
        return lockResource(timeout, lockInfo);
      }
    }.evalNoEx();
  }

  /**
   * Renew the lock and return new lock info.
   *
   * @param token lock token
   * @return loc result
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
   * @param tokenId lock token
   */
  @Override
  public void unlock(final String tokenId) throws NotAuthorizedException,
    PreConditionFailedException {
    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        service.unlock(tokenId);
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
    service.delete(meta.db, meta.path);
  }

  /**
   * Rename document or folder.
   * @param n new name
   * @throws IOException I/O exception
   */
  protected void rename(final String n) throws IOException {
    service.rename(meta.db, meta.path, n);
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
  void moveTo(final BXFolder f, final String n) throws IOException {
    if(f.meta.db.equals(meta.db)) {
      // folder is moved to a folder in the same database
      rename(f.meta.path + SEP + n);
    } else {
      // folder is moved to a folder in another database
      copyTo(f, n);
      del();
    }
  }

  LockResult lockResource(final LockTimeout timeout, final LockInfo lockInfo) throws
    IOException {

    final String tokenId = service.lock(meta.db, meta.path,
      lockInfo.scope.name().toLowerCase(),
      lockInfo.type.name().toLowerCase(),
      lockInfo.depth.name().toLowerCase(),
      lockInfo.lockedByUser,
      timeout.getSeconds());

    // TODO failed(failureReason);
    return success(new LockToken(tokenId, lockInfo, timeout));
  }

  LockToken getCurrentActiveLock() throws IOException, SAXException {
    final String lockInfoStr = service.lock(meta.db, meta.path);
    return lockInfoStr == null ? null : parseLockInfo(lockInfoStr);
  }

  private static LockToken parseLockInfo(String lockInfo) throws SAXException,
    IOException {
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
