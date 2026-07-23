(:~
 : WebDAV interface for BaseX, implemented with RESTXQ.
 :
 : Databases are exposed as the top-level collections of the WebDAV file system;
 : folders are derived from the paths of the resources stored in a database.
 : This module implements WebDAV classes 1 and 2 (RFC 4918, write locks).
 :
 : @author BaseX Team, BSD License
 :)
module namespace dav = 'webdav/webdav';

import module namespace res = 'webdav/resources' at 'resources.xqm';
import module namespace resp = 'webdav/responses' at 'responses.xqm';
import module namespace lock = 'webdav/locks' at 'locks.xqm';

declare namespace DAV = 'DAV:';

(:~ Methods supported by this implementation. :)
declare variable $dav:ALLOW :=
  'OPTIONS, HEAD, GET, PUT, DELETE, MKCOL, COPY, MOVE, PROPFIND, LOCK, UNLOCK';

(: OPTIONS ======================================================================================:)

(:~
 : Announces the WebDAV capabilities of the root collection.
 : @return response
 :)
declare
  %rest:path('/webdav')
  %rest:method('OPTIONS')
function dav:options-root() as element(rest:response) {
  dav:options('')
};

(:~
 : Announces the WebDAV capabilities of a resource.
 : @param  $path  path below the WebDAV root
 : @return response
 :)
declare
  %rest:path('/webdav/{$path=.*}')
  %rest:method('OPTIONS')
function dav:options(
  $path  as xs:string
) as element(rest:response) {
  web:response-header((), {
    'DAV': '1, 2',
    'MS-Author-Via': 'DAV',
    'Allow': $dav:ALLOW,
    'Content-Length': '0'
  }, { 'status': 200, 'message': 'OK' })
};

(: PROPFIND =====================================================================================:)

(:~
 : Returns the properties of the root collection.
 : @param  $depth  requested depth
 : @param  $body   request body
 : @return multistatus response
 :)
declare
  %rest:path('/webdav')
  %rest:method('PROPFIND', '{$body}')
  %rest:header-param('Depth', '{$depth}', '1')
function dav:propfind-root(
  $depth  as xs:string,
  $body   as item()?
) as item()+ {
  dav:propfind('', $depth, $body)
};

(:~
 : Returns the properties of a resource and, depending on the requested depth,
 : of its members.
 : @param  $path   path below the WebDAV root
 : @param  $depth  requested depth
 : @param  $body   request body
 : @return multistatus response
 :)
declare
  %rest:path('/webdav/{$path=.*}')
  %rest:method('PROPFIND', '{$body}')
  %rest:header-param('Depth', '{$depth}', '1')
function dav:propfind(
  $path   as xs:string,
  $depth  as xs:string,
  $body   as item()?
) as item()+ {
  let $rc := res:lookup($path)
  return if (empty($rc)) {
    web:error(404, 'Not Found')
  } else {
    let $request := dav:request($body)/self::DAV:propfind
    let $members :=
      switch ($depth)
        case '0' return ()
        case 'infinity' return res:descendants($rc)
        default return res:children($rc)
    return (
      web:response-header({ 'method': 'xml' }, (),
        { 'status': 207, 'message': 'Multi-Status' }),
      resp:multistatus(($rc, $members) ! resp:response(., $request))
    )
  }
};

(: PUT ==========================================================================================:)

(:~
 : Stores a resource. A single path step creates a new database.
 : @param  $path  path below the WebDAV root
 : @param  $body  request body
 : @return response
 :)
declare
  %updating
  %rest:path('/webdav/{$path=.*}')
  %rest:method('PUT', '{$body}')
function dav:put(
  $path  as xs:string,
  $body  as item()?
) {
  let $ref := res:parse($path)
  let $db := $ref?db
  return if (empty($db)) {
    web:error(405, 'Method Not Allowed')
  } else if (dav:locked($path)) {
    web:error(423, 'Locked')
  } else if ($ref?path = '') {
    (: a resource at the root level becomes a new database :)
    dav:create(replace($db, '\.[^.]+$', ''), $db, $body),
    dav:respond(201, 'Created')
  } else if (not(db:exists($db))) {
    web:error(409, 'Conflict')
  } else {
    let $exists := db:exists($db, $ref?path)
    return (
      dav:store($db, $ref?path, $body),
      dav:clear-dummy($db, res:parent($ref?path)),
      if ($exists) { dav:respond(204, 'No Content') } else { dav:respond(201, 'Created') }
    )
  }
};

(: DELETE =======================================================================================:)

(:~
 : Deletes a resource, a collection or a database.
 : @param  $path  path below the WebDAV root
 : @return response
 :)
declare
  %updating
  %rest:path('/webdav/{$path=.*}')
  %rest:DELETE
function dav:delete(
  $path  as xs:string
) {
  let $rc := res:lookup($path)
  return if (empty($rc) or $rc?kind = 'root') {
    web:error(404, 'Not Found')
  } else if (dav:locked-tree($path)) {
    web:error(423, 'Locked')
  } else if ($rc?path = '') {
    lock:release($path),
    db:drop($rc?db),
    dav:respond(204, 'No Content')
  } else {
    lock:release($path),
    db:delete($rc?db, $rc?path),
    dav:add-dummy($rc?db, res:parent($rc?path)),
    dav:respond(204, 'No Content')
  }
};

(: MKCOL ========================================================================================:)

(:~
 : Creates a collection. A single path step creates a new database.
 : @param  $path  path below the WebDAV root
 : @return response
 :)
declare
  %updating
  %rest:path('/webdav/{$path=.*}')
  %rest:method('MKCOL')
function dav:mkcol(
  $path  as xs:string
) {
  let $ref := res:parse($path)
  let $db := $ref?db
  return if (empty($db)) {
    web:error(405, 'Method Not Allowed')
  } else if (dav:locked($path)) {
    web:error(423, 'Locked')
  } else if ($ref?path = '') {
    if (db:exists($db)) {
      web:error(405, 'Method Not Allowed')
    } else {
      db:create($db),
      dav:respond(201, 'Created')
    }
  } else if (not(db:exists($db))) {
    web:error(409, 'Conflict')
  } else if (exists(res:lookup($path))) {
    web:error(405, 'Method Not Allowed')
  } else {
    dav:add-dummy($db, $ref?path),
    dav:clear-dummy($db, res:parent($ref?path)),
    dav:respond(201, 'Created')
  }
};

(: MOVE, COPY ===================================================================================:)

(:~
 : Moves a resource or collection to the destination given in the request header.
 : @param  $path  path below the WebDAV root
 : @return response
 :)
declare
  %updating
  %rest:path('/webdav/{$path=.*}')
  %rest:method('MOVE')
function dav:move(
  $path  as xs:string
) {
  let $rc := res:lookup($path)
  let $destination := dav:destination()
  let $target := res:parse($destination)
  return if (empty($rc) or $rc?kind = 'root') {
    web:error(404, 'Not Found')
  } else if (empty($target?db)) {
    web:error(400, 'Bad Request')
  } else if (dav:locked-tree($path) or dav:locked($destination)) {
    web:error(423, 'Locked')
  } else if ($rc?db = $target?db) {
    lock:release($path),
    db:rename($rc?db, $rc?path, $target?path),
    dav:add-dummy($rc?db, res:parent($rc?path)),
    dav:clear-dummy($rc?db, res:parent($target?path)),
    dav:respond(201, 'Created')
  } else {
    lock:release($path),
    dav:copy-to($rc, $target),
    db:delete($rc?db, $rc?path),
    dav:add-dummy($rc?db, res:parent($rc?path)),
    dav:respond(201, 'Created')
  }
};

(:~
 : Copies a resource or collection to the destination given in the request header.
 : @param  $path  path below the WebDAV root
 : @return response
 :)
declare
  %updating
  %rest:path('/webdav/{$path=.*}')
  %rest:method('COPY')
function dav:copy(
  $path  as xs:string
) {
  let $rc := res:lookup($path)
  let $destination := dav:destination()
  let $target := res:parse($destination)
  return if (empty($rc) or $rc?kind = 'root') {
    web:error(404, 'Not Found')
  } else if (empty($target?db)) {
    web:error(400, 'Bad Request')
  } else if (dav:locked($destination)) {
    web:error(423, 'Locked')
  } else {
    dav:copy-to($rc, $target),
    dav:clear-dummy($target?db, res:parent($target?path)),
    dav:respond(201, 'Created')
  }
};

(: LOCK, UNLOCK =================================================================================:)

(:~
 : Locks a resource, or refreshes an existing lock.
 : @param  $path     path below the WebDAV root
 : @param  $depth    requested depth
 : @param  $timeout  requested timeout
 : @param  $body     request body
 : @return response
 :)
declare
  %updating
  %rest:path('/webdav/{$path=.*}')
  %rest:method('LOCK', '{$body}')
  %rest:header-param('Depth', '{$depth}', 'infinity')
  %rest:header-param('Timeout', '{$timeout}', '')
function dav:lock(
  $path     as xs:string,
  $depth    as xs:string,
  $timeout  as xs:string,
  $body     as item()?
) {
  let $info := dav:request($body)/self::DAV:lockinfo
  let $seconds := lock:timeout($timeout)
  return (
    lock:purge(),
    if (empty($info)) {
      dav:refresh($seconds)
    } else {
      dav:acquire($path, $depth, $seconds, $info)
    }
  )
};

(:~
 : Extends the lifetime of the lock supplied in the If header.
 : @param  $seconds  requested timeout
 : @return response
 :)
declare %updating %private function dav:refresh(
  $seconds  as xs:integer
) {
  let $refreshed := dav:if-tokens() ! lock:refresh(., $seconds)
  return if (empty($refreshed)) {
    web:error(412, 'Precondition Failed')
  } else {
    update:output((
      web:response-header({ 'method': 'xml' }, (), { 'status': 200, 'message': 'OK' }),
      resp:lockinfo(head($refreshed))
    ))
  }
};

(:~
 : Creates a new lock. Locking an unmapped path creates an empty resource, so
 : that clients can reserve a name before writing.
 : @param  $path     path below the WebDAV root
 : @param  $depth    requested depth
 : @param  $seconds  requested timeout
 : @param  $info     lockinfo element of the request
 : @return response
 :)
declare %updating %private function dav:acquire(
  $path     as xs:string,
  $depth    as xs:string,
  $seconds  as xs:integer,
  $info     as element(DAV:lockinfo)
) {
  let $ref := res:parse($path)
  let $rc := res:lookup($path)
  return if (empty($ref?db)) {
    web:error(405, 'Method Not Allowed')
  } else if (empty($rc) and (not(db:exists($ref?db)) or $ref?path = '')) {
    web:error(409, 'Conflict')
  } else {
    (: the conflicting locks are checked and the new lock is registered atomically :)
    let $new := lock:acquire($path, $depth,
      if ($info/DAV:lockscope/DAV:shared) { 'shared' } else { 'exclusive' },
      $info/DAV:owner, $seconds, dav:if-tokens())
    return if (empty($new)) {
      web:error(423, 'Locked')
    } else {
      (: reserve the name with an empty resource if it does not exist yet :)
      if (empty($rc)) {
        db:put-binary($ref?db, xs:base64Binary(''), $ref?path),
        dav:clear-dummy($ref?db, res:parent($ref?path))
      },
      update:output((
        web:response-header({ 'method': 'xml' },
          { 'Lock-Token': '<' || $new/@token || '>' },
          if (empty($rc)) {
            { 'status': 201, 'message': 'Created' }
          } else {
            { 'status': 200, 'message': 'OK' }
          }),
        resp:lockinfo($new)
      ))
    }
  }
};

(:~
 : Releases a lock.
 : @param  $path   path below the WebDAV root
 : @param  $token  lock token
 : @return response
 :)
declare
  %rest:path('/webdav/{$path=.*}')
  %rest:method('UNLOCK')
  %rest:header-param('Lock-Token', '{$token}', '')
function dav:unlock(
  $path   as xs:string,
  $token  as xs:string
) as element(rest:response) {
  let $id := replace($token, '^\s*<|>\s*$', '')
  let $lock := lock:get($id)
  return if (empty($lock) or $lock/@path != $path) {
    web:error(409, 'Conflict')
  } else {
    lock:remove($id),
    web:response-header((), (), { 'status': 204, 'message': 'No Content' })
  }
};

(: HELPER FUNCTIONS =============================================================================:)

(:~
 : Returns a response with a status code and no body.
 : @param  $status   status code
 : @param  $message  status message
 : @return empty sequence
 :)
declare %updating %private function dav:respond(
  $status   as xs:integer,
  $message  as xs:string
) {
  update:output(web:response-header((), (), { 'status': $status, 'message': $message }))
};

(:~
 : Returns the lock tokens supplied in the If request header. Only state tokens
 : are extracted; conditions on entity tags are not evaluated.
 : @return lock tokens
 :)
declare %private function dav:if-tokens() as xs:string* {
  tokenize(request:header('If') otherwise '', '[<>]')[starts-with(., 'urn:uuid:')]
};

(:~
 : Checks whether a path is locked against the current request.
 : @param  $path  path below the WebDAV root
 : @return result of check
 :)
declare %private function dav:locked(
  $path  as xs:string
) as xs:boolean {
  not(lock:allows($path, dav:if-tokens()))
};

(:~
 : Checks whether a path or any of its descendants is locked against the
 : current request.
 : @param  $path  path below the WebDAV root
 : @return result of check
 :)
declare %private function dav:locked-tree(
  $path  as xs:string
) as xs:boolean {
  let $tokens := dav:if-tokens()
  return dav:locked($path) or exists(lock:descendants($path)[not(@token = $tokens)])
};

(:~
 : Copies a resource or collection to another location.
 : @param  $rc      source description
 : @param  $target  target database and path
 : @return updates
 :)
declare %updating %private function dav:copy-to(
  $rc      as res:any,
  $target  as res:ref
) {
  if ($rc?kind = 'resource') {
    dav:copy-resource($rc, $target?db, $target?path)
  } else {
    for $child in res:descendants($rc)
    where $child?kind = 'resource'
    let $path := $target?path || substring($child?path, string-length($rc?path) + 1)
    return dav:copy-resource($child, $target?db, $path)
  }
};

(:~
 : Copies a single resource, preserving its storage type.
 : @param  $rc    source description
 : @param  $db    target database
 : @param  $path  target path
 : @return updates
 :)
declare %updating %private function dav:copy-resource(
  $rc    as res:resource,
  $db    as xs:string,
  $path  as xs:string
) {
  switch ($rc?type)
    case 'xml' return db:put($db, db:get($rc?db, $rc?path), $path)
    case 'value' return db:put-value($db, db:get-value($rc?db, $rc?path), $path)
    default return db:put-binary($db, db:get-binary($rc?db, $rc?path), $path)
};

(:~
 : Stores a request body. XML input is stored as a document, everything else as
 : a binary resource.
 : @param  $db    database
 : @param  $path  path
 : @param  $body  request body
 : @return updates
 :)
declare %updating %private function dav:store(
  $db    as xs:string,
  $path  as xs:string,
  $body  as item()?
) {
  if (empty($body)) {
    db:put-binary($db, xs:base64Binary(''), $path)
  } else if ($body instance of document-node()) {
    db:put($db, $body, $path)
  } else {
    try {
      db:put($db, parse-xml($body), $path)
    } catch * {
      db:put-binary($db, dav:binary($body), $path)
    }
  }
};

(:~
 : Creates a database and stores the request body as its first resource.
 : @param  $name  database name
 : @param  $path  resource path
 : @param  $body  request body
 : @return updates
 :)
declare %updating %private function dav:create(
  $name  as xs:string,
  $path  as xs:string,
  $body  as item()?
) {
  if (empty($body)) {
    db:create($name)
  } else if ($body instance of document-node()) {
    db:create($name, $body, $path)
  } else {
    try {
      db:create($name, parse-xml($body), $path)
    } catch * {
      db:create($name, dav:binary($body), $path)
    }
  }
};

(:~
 : Returns a request body as binary data.
 : @param  $body  request body
 : @return binary data
 :)
declare %private function dav:binary(
  $body  as item()
) as xs:base64Binary {
  if ($body instance of xs:string) {
    convert:string-to-base64($body)
  } else {
    xs:base64Binary($body)
  }
};

(:~
 : Creates a dummy resource if a collection would otherwise be empty.
 : @param  $db    database
 : @param  $path  collection path
 : @return updates
 :)
declare %updating %private function dav:add-dummy(
  $db    as xs:string,
  $path  as xs:string
) {
  if ($path != '' and empty(db:list($db, $path))) {
    db:put-binary($db, xs:base64Binary(''), res:join($path, $res:DUMMY))
  }
};

(:~
 : Removes the dummy resource of a collection.
 : @param  $db    database
 : @param  $path  collection path
 : @return updates
 :)
declare %updating %private function dav:clear-dummy(
  $db    as xs:string,
  $path  as xs:string
) {
  let $dummy := res:join($path, $res:DUMMY)
  where db:exists($db, $dummy)
  return db:delete($db, $dummy)
};

(:~
 : Parses the request body of a WebDAV request.
 : @param  $body  request body
 : @return document element, or empty sequence
 :)
declare %private function dav:request(
  $body  as item()?
) as element()? {
  if ($body instance of document-node()) {
    $body/*
  } else if (exists($body)) {
    try { parse-xml($body)/* } catch * { () }
  }
};

(:~
 : Returns the target path of a MOVE or COPY request. Plus signs are escaped
 : first: decode-from-uri turns them into spaces, but in a URI path they are
 : literal characters.
 : @return path below the WebDAV root
 :)
declare %private function dav:destination() as xs:string {
  let $uri := replace(request:header('Destination') otherwise '', '\+', '%2B')
  let $segments := parse-uri($uri)?path-segments
  return string-join(subsequence($segments, count(tokenize($res:ROOT, '/')[.]) + 2), '/')
};

