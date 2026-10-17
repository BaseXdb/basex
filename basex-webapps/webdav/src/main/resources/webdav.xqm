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
  'OPTIONS, HEAD, GET, PUT, DELETE, MKCOL, COPY, MOVE, PROPFIND, PROPPATCH, LOCK, UNLOCK';

(: OPTIONS ======================================================================================:)

(:~
 : Announces the WebDAV capabilities of a resource.
 : @param  $path  path below the WebDAV root (empty for the root collection)
 : @return response
 :)
declare
  %rest:path('/webdav')
  %rest:path('/webdav/{$path=.*}')
  %rest:method('OPTIONS')
function dav:options(
  $path  as xs:string?
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
 : Returns the properties of a resource and, depending on the requested depth,
 : of its members.
 : @param  $path   path below the WebDAV root (empty for the root collection)
 : @param  $depth  requested depth
 : @param  $body   request body
 : @return multistatus response
 :)
declare
  %rest:path('/webdav')
  %rest:path('/webdav/{$path=.*}')
  %rest:method('PROPFIND', '{$body}')
  %rest:header-param('Depth', '{$depth}', '1')
function dav:propfind(
  $path   as xs:string?,
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
    (: the lock store is read once, not once per reported resource :)
    let $locks := lock:active()
    return (
      web:response-header({ 'method': 'xml' }, (),
        { 'status': 207, 'message': 'Multi-Status' }),
      <DAV:multistatus>{ ($rc, $members) ! resp:response(., $request, $locks) }</DAV:multistatus>
    )
  }
};

(: PROPPATCH ====================================================================================:)

(:~
 : Rejects the modification of properties. The request is answered with a multistatus
 : response, as clients rely on PROPPATCH to store their metadata.
 : @param  $path  path below the WebDAV root (empty for the root collection)
 : @param  $body  request body
 : @return multistatus response
 :)
declare
  %rest:path('/webdav')
  %rest:path('/webdav/{$path=.*}')
  %rest:method('PROPPATCH', '{$body}')
function dav:proppatch(
  $path  as xs:string?,
  $body  as item()?
) as item()+ {
  let $rc := res:lookup($path)
  let $request := dav:request($body)/self::DAV:propertyupdate
  return if (empty($rc)) {
    web:error(404, 'Not Found')
  } else if (empty($request)) {
    web:error(400, 'Bad Request')
  } else if (dav:locked($path)) {
    web:error(423, 'Locked')
  } else {
    web:response-header({ 'method': 'xml' }, (),
      { 'status': 207, 'message': 'Multi-Status' }),
    <DAV:multistatus>{ resp:propertyupdate($rc, $request) }</DAV:multistatus>
  }
};

(: PUT ==========================================================================================:)

(:~
 : Stores a resource. A single path step creates a new database.
 : @param  $path           path below the WebDAV root (empty for the root collection)
 : @param  $if-match       If-Match header
 : @param  $if-none-match  If-None-Match header
 : @return response
 :)
declare
  %updating
  %rest:path('/webdav')
  %rest:path('/webdav/{$path=.*}')
  %rest:method('PUT')
  %rest:header-param('If-Match', '{$if-match}')
  %rest:header-param('If-None-Match', '{$if-none-match}')
function dav:put(
  $path           as xs:string?,
  $if-match       as xs:string*,
  $if-none-match  as xs:string*
) {
  let $ref := res:parse($path)
  let $db := $ref?db
  return if (empty($db)) {
    update:output(dav:not-allowed())
  } else if (dav:unmet(res:lookup($path), $if-match, $if-none-match) or dav:if-failed($path)) {
    web:error(412, 'Precondition Failed')
  } else if (dav:locked($path)) {
    web:error(423, 'Locked')
  } else if ($ref?path = '') {
    (: a resource at the root level becomes a new database :)
    db:create(replace($db, '\.[^.]+$', ''), dav:content(), $db),
    dav:respond(201, 'Created')
  } else if (not(db:exists($db)) or dav:no-parent($db, $ref?path)) {
    web:error(409, 'Conflict')
  } else {
    let $exists := db:exists($db, $ref?path)
    return (
      db:put($db, dav:content(), $ref?path),
      dav:clear-dummy($db, res:parent($ref?path)),
      if ($exists) { dav:respond(204, 'No Content') } else { dav:respond(201, 'Created') }
    )
  }
};

(: DELETE =======================================================================================:)

(:~
 : Deletes a resource, a collection or a database.
 : @param  $path           path below the WebDAV root (empty for the root collection)
 : @param  $if-match       If-Match header
 : @param  $if-none-match  If-None-Match header
 : @return response
 :)
declare
  %updating
  %rest:path('/webdav')
  %rest:path('/webdav/{$path=.*}')
  %rest:DELETE
  %rest:header-param('If-Match', '{$if-match}')
  %rest:header-param('If-None-Match', '{$if-none-match}')
function dav:delete(
  $path           as xs:string?,
  $if-match       as xs:string*,
  $if-none-match  as xs:string*
) {
  let $rc := res:lookup($path)
  return if (empty($rc) or $rc?kind = 'root') {
    web:error(404, 'Not Found')
  } else if (dav:unmet($rc, $if-match, $if-none-match)) {
    web:error(412, 'Precondition Failed')
  } else if (dav:locked-tree($path)) {
    web:error(423, 'Locked')
  } else if ($rc?path = '') {
    lock:release($path),
    db:drop($rc?db),
    dav:respond(204, 'No Content')
  } else {
    lock:release($path),
    db:delete($rc?db, $rc?path),
    dav:add-dummy($rc?db, $rc?path),
    dav:respond(204, 'No Content')
  }
};

(: MKCOL ========================================================================================:)

(:~
 : Creates a collection. A single path step creates a new database.
 : @param  $path  path below the WebDAV root (empty for the root collection)
 : @return response
 :)
declare
  %updating
  %rest:path('/webdav')
  %rest:path('/webdav/{$path=.*}')
  %rest:method('MKCOL')
function dav:mkcol(
  $path  as xs:string?
) {
  let $ref := res:parse($path)
  let $db := $ref?db
  return if (empty($db)) {
    update:output(dav:not-allowed())
  } else if (exists(request:body())) {
    (: request bodies are reserved for extensions that are not supported :)
    web:error(415, 'Unsupported Media Type')
  } else if (dav:locked($path)) {
    web:error(423, 'Locked')
  } else if ($ref?path = '') {
    if (db:exists($db)) {
      update:output(dav:not-allowed())
    } else {
      db:create($db),
      dav:respond(201, 'Created')
    }
  } else if (not(db:exists($db)) or dav:no-parent($db, $ref?path)) {
    web:error(409, 'Conflict')
  } else if (exists(res:lookup($path))) {
    update:output(dav:not-allowed())
  } else {
    dav:put-dummy($db, $ref?path),
    dav:clear-dummy($db, res:parent($ref?path)),
    dav:respond(201, 'Created')
  }
};

(: MOVE, COPY ===================================================================================:)

(:~
 : Moves a resource or collection to the destination given in the request header.
 : @param  $path       path below the WebDAV root (empty for the root collection)
 : @param  $overwrite  overwrite an existing target ('T' or 'F')
 : @return response
 :)
declare
  %updating
  %rest:path('/webdav')
  %rest:path('/webdav/{$path=.*}')
  %rest:method('MOVE')
  %rest:header-param('Overwrite', '{$overwrite}', 'T')
function dav:move(
  $path       as xs:string?,
  $overwrite  as xs:string
) {
  let $rc := res:lookup($path)
  let $destination := dav:destination()
  let $target := res:parse($destination)
  let $existing := res:lookup($destination)
  let $paths := dav:target-paths($rc, $target, 'infinity')
  return if (empty($rc) or $rc?kind = 'root') {
    web:error(404, 'Not Found')
  } else if (empty($target?db)) {
    web:error(400, 'Bad Request')
  } else if (exists($existing) and $overwrite = 'F') {
    web:error(412, 'Precondition Failed')
  } else if ($target?path != '' and not(db:exists($target?db)) or
      dav:no-parent($target?db, $target?path) or
      ($rc?kind != 'resource' and $existing?kind = 'resource')) {
    (: a resource and a collection cannot share a path, and both changes are applied at once :)
    web:error(409, 'Conflict')
  } else if (dav:locked-tree($path) or dav:locked-tree($destination)) {
    web:error(423, 'Locked')
  } else if ($rc?path = '' and $target?path = '' and empty($existing)) {
    (: a database is renamed instead of being copied resource by resource :)
    lock:release($path),
    db:alter($rc?db, $target?db),
    dav:respond(201, 'Created')
  } else if ($rc?db = $target?db and empty($existing)) {
    (: renaming cannot overwrite: an existing target is replaced resource by resource :)
    lock:release($path),
    db:rename($rc?db, $rc?path, $target?path),
    dav:add-dummy($rc?db, $rc?path),
    dav:clear-dummy($rc?db, res:parent($target?path)),
    dav:relocated($existing)
  } else {
    lock:release($path),
    dav:clear-target($existing, $paths),
    dav:copy-to($rc, $target),
    db:delete($rc?db, $rc?path),
    dav:add-dummy($rc?db, $rc?path),
    dav:relocated($existing)
  }
};

(:~
 : Copies a resource or collection to the destination given in the request header.
 : @param  $path       path below the WebDAV root (empty for the root collection)
 : @param  $overwrite  overwrite an existing target ('T' or 'F')
 : @param  $depth      requested depth
 : @return response
 :)
declare
  %updating
  %rest:path('/webdav')
  %rest:path('/webdav/{$path=.*}')
  %rest:method('COPY')
  %rest:header-param('Overwrite', '{$overwrite}', 'T')
  %rest:header-param('Depth', '{$depth}', 'infinity')
function dav:copy(
  $path       as xs:string?,
  $overwrite  as xs:string,
  $depth      as xs:string
) {
  let $rc := res:lookup($path)
  let $destination := dav:destination()
  let $target := res:parse($destination)
  let $existing := res:lookup($destination)
  let $paths := dav:target-paths($rc, $target, $depth)
  return if (empty($rc) or $rc?kind = 'root') {
    web:error(404, 'Not Found')
  } else if (empty($target?db)) {
    web:error(400, 'Bad Request')
  } else if (exists($existing) and $overwrite = 'F') {
    web:error(412, 'Precondition Failed')
  } else if ($target?path != '' and not(db:exists($target?db)) or
      dav:no-parent($target?db, $target?path) or
      ($rc?kind != 'resource' and $existing?kind = 'resource')) {
    (: a resource and a collection cannot share a path, and both changes are applied at once :)
    web:error(409, 'Conflict')
  } else if (dav:locked-tree($destination)) {
    web:error(423, 'Locked')
  } else if ($rc?path = '' and $target?path = '' and empty($existing)) {
    (: a database is copied as a whole :)
    db:copy($rc?db, $target?db),
    dav:respond(201, 'Created')
  } else {
    dav:clear-target($existing, $paths),
    (: a collection copied with depth 0 becomes an empty collection :)
    if ($depth = '0' and $rc?kind != 'resource') {
      dav:put-dummy($target?db, $target?path)
    } else {
      dav:copy-to($rc, $target)
    },
    dav:clear-dummy($target?db, res:parent($target?path)),
    dav:relocated($existing)
  }
};

(: LOCK, UNLOCK =================================================================================:)

(:~
 : Locks a resource, or refreshes an existing lock.
 : @param  $path     path below the WebDAV root (empty for the root collection)
 : @param  $depth    requested depth
 : @param  $timeout  requested timeout
 : @param  $body     request body
 : @return response
 :)
declare
  %updating
  %rest:path('/webdav')
  %rest:path('/webdav/{$path=.*}')
  %rest:method('LOCK', '{$body}')
  %rest:header-param('Depth', '{$depth}', 'infinity')
  %rest:header-param('Timeout', '{$timeout}')
function dav:lock(
  $path     as xs:string?,
  $depth    as xs:string,
  $timeout  as xs:string*,
  $body     as item()?
) {
  let $info := dav:request($body)/self::DAV:lockinfo
  let $seconds := lock:timeout($timeout)
  return (
    lock:purge(),
    if (empty($info)) {
      dav:refresh($seconds)
    } else {
      dav:acquire($path otherwise '', $depth, $seconds, $info)
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
    update:output(dav:not-allowed())
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
        db:put-binary($ref?db, $res:EMPTY, $ref?path),
        dav:clear-dummy($ref?db, res:parent($ref?path))
      },
      update:output((
        web:response-header({ 'method': 'xml' },
          { 'Lock-Token': '<' || $new?token || '>' },
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
 : @param  $path   path below the WebDAV root (empty for the root collection)
 : @param  $token  lock token
 : @return response
 :)
declare
  %rest:path('/webdav')
  %rest:path('/webdav/{$path=.*}')
  %rest:method('UNLOCK')
  %rest:header-param('Lock-Token', '{$token}', '')
function dav:unlock(
  $path   as xs:string?,
  $token  as xs:string
) as element(rest:response) {
  let $id := replace($token, '^\s*<|>\s*$', '')
  let $lock := lock:get($id)
  return if (empty($lock) or $lock?path != ($path otherwise '')) {
    web:error(409, 'Conflict')
  } else {
    lock:remove($id),
    web:response-header((), (), { 'status': 204, 'message': 'No Content' })
  }
};

(: UNSUPPORTED METHODS ==========================================================================:)

(:~
 : Rejects methods that are not supported for WebDAV resources.
 : @param  $path  path below the WebDAV root (empty for the root collection)
 : @return response
 :)
declare
  %rest:path('/webdav')
  %rest:path('/webdav/{$path=.*}')
  %rest:POST
function dav:unsupported(
  $path  as xs:string?
) as element(rest:response) {
  dav:not-allowed()
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
 : Returns a response for a method that is not allowed.
 : @return response
 :)
declare %private function dav:not-allowed() as element(rest:response) {
  web:response-header((), { 'Allow': $dav:ALLOW },
    { 'status': 405, 'message': 'Method Not Allowed' })
};

(:~
 : Checks whether the parent collection of a database path does not exist.
 : @param  $db    database
 : @param  $path  database path
 : @return result of check
 :)
declare %private function dav:no-parent(
  $db    as xs:string,
  $path  as xs:string
) as xs:boolean {
  let $parent := res:parent($path)
  return $parent != '' and empty(res:lookup(res:join($db, $parent))[?kind = 'collection'])
};

(:~
 : Checks whether the lock tokens of the If header do not apply to a path.
 : @param  $path  path below the WebDAV root
 : @return result of check
 :)
declare %private function dav:if-failed(
  $path  as xs:string
) as xs:boolean {
  (: a supplied condition must name a lock that applies to the path :)
  exists(request:header('If')) and not(dav:if-tokens() = lock:covering($path)?token)
};

(:~
 : Returns the path a member of a copied resource will be written to.
 : @param  $rc      source description
 : @param  $child   member of the source
 : @param  $target  target database and path
 : @return database path
 :)
declare %private function dav:target-path(
  $rc      as res:any,
  $child   as res:any,
  $target  as res:ref
) as xs:string {
  (: the path of a database member has no leading separator to strip :)
  let $suffix := if ($rc?path = '') {
    $child?path
  } else {
    substring($child?path, string-length($rc?path) + 2)
  }
  return res:join($target?path, $suffix)
};

(:~
 : Returns the resource paths that a COPY or MOVE request will write.
 : @param  $rc      source description, or empty sequence
 : @param  $target  target database and path
 : @param  $depth   requested depth
 : @return database paths
 :)
declare %private function dav:target-paths(
  $rc      as res:any?,
  $target  as res:ref,
  $depth   as xs:string
) as xs:string* {
  if (empty($rc)) {
    ()
  } else if ($rc?kind = 'resource') {
    $target?path
  } else if ($depth = '0') {
    res:join($target?path, $res:DUMMY)
  } else {
    let $members := res:descendants($rc)
    for $child in $members
    let $path := dav:target-path($rc, $child, $target)
    return if ($child?kind = 'resource') {
      $path
    } else if (empty(dav:resources($members, $child))) {
      res:join($path, $res:DUMMY)
    }
  }
};

(:~
 : Returns the resources of a collection that are contained in a set of members.
 : @param  $members  members of the copied resource
 : @param  $rc       collection description
 : @return resources
 :)
declare %private function dav:resources(
  $members  as res:any*,
  $rc       as res:any
) as res:any* {
  $members[?kind = 'resource'][starts-with(?path, $rc?path || '/')]
};

(:~
 : Removes the members of a COPY or MOVE target that will not be overwritten.
 : @param  $rc     target description, or empty sequence if the target does not exist
 : @param  $paths  database paths that the request will write
 : @return updates
 :)
declare %updating %private function dav:clear-target(
  $rc     as res:any?,
  $paths  as xs:string*
) {
  (: deletions and writes are applied in one transaction: overwritten paths must be kept :)
  if (exists($rc)) {
    lock:release(res:path($rc)),
    for $stale in (if ($rc?kind = 'resource') { $rc } else { res:descendants($rc) })
    where $stale?kind = 'resource' and not($stale?path = $paths)
    return db:delete($rc?db, $stale?path)
  }
};

(:~
 : Reports the outcome of a COPY or MOVE request.
 : @param  $rc  target that existed before the request, or empty sequence
 : @return updates
 :)
declare %updating %private function dav:relocated(
  $rc  as res:any?
) {
  if (exists($rc)) { dav:respond(204, 'No Content') } else { dav:respond(201, 'Created') }
};

(:~
 : Returns the lock tokens supplied in the If request header. Only state tokens
 : are extracted; conditions on entity tags are not evaluated.
 : @return lock tokens
 :)
declare %private function dav:if-tokens() as xs:string* {
  tokenize(request:header('If'), '[<>]')[starts-with(., 'urn:uuid:')]
};

(:~
 : Checks whether the entity tag conditions of a request are not met.
 : @param  $rc             addressed resource, or empty sequence
 : @param  $if-match       If-Match header
 : @param  $if-none-match  If-None-Match header
 : @return result of check
 :)
declare %private function dav:unmet(
  $rc             as res:any?,
  $if-match       as xs:string*,
  $if-none-match  as xs:string*
) as xs:boolean {
  (exists($if-match) and not(dav:etag-match($if-match, $rc, false()))) or
  (exists($if-none-match) and dav:etag-match($if-none-match, $rc, true()))
};

(:~
 : Matches the entity tags of an If-Match or If-None-Match header against a resource.
 : @param  $tags  entity tags
 : @param  $rc    addressed resource, or empty sequence
 : @param  $weak  accept weak entity tags
 : @return result of match
 :)
declare %private function dav:etag-match(
  $tags  as xs:string+,
  $rc    as res:any?,
  $weak  as xs:boolean
) as xs:boolean {
  if ($tags = '*') {
    exists($rc)
  } else if ($rc?kind = 'resource') {
    let $etag := resp:etag($rc)
    return some $tag in $tags satisfies
      ($weak or not(starts-with($tag, 'W/'))) and replace($tag, '^W/', '') = $etag
  } else {
    false()
  }
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
  return dav:locked($path) or exists(lock:descendants($path)[not(?token = $tokens)])
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
    let $members := res:descendants($rc)
    for $child in $members
    let $path := dav:target-path($rc, $child, $target)
    return if ($child?kind = 'resource') {
      dav:copy-resource($child, $target?db, $path)
    } else if (empty(dav:resources($members, $child))) {
      (: an empty collection is represented by its dummy resource :)
      dav:put-dummy($target?db, $path)
    }
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
 : Returns the request body as the value to be stored: XML input becomes a
 : document, everything else binary data.
 : @return document or binary data
 :)
declare %private function dav:content() as item() {
  let $body := request:body()
  return if (empty($body)) {
    $res:EMPTY
  } else {
    try { fetch:binary-doc($body) } catch * { $body }
  }
};

(:~
 : Creates a dummy resource if the removal of a path leaves its collection empty.
 : @param  $db       database
 : @param  $removed  path that is removed
 : @return updates
 :)
declare %updating %private function dav:add-dummy(
  $db       as xs:string,
  $removed  as xs:string
) {
  (: the removal is not visible yet, so the removed resources are discounted :)
  let $path := res:parent($removed)
  return if ($path != '' and empty(db:list($db, $path)[not(. = $removed or
      starts-with(., $removed || '/'))])) {
    dav:put-dummy($db, $path)
  }
};

(:~
 : Creates the dummy resource of a collection.
 : @param  $db    database
 : @param  $path  collection path
 : @return updates
 :)
declare %updating %private function dav:put-dummy(
  $db    as xs:string,
  $path  as xs:string
) {
  db:put-binary($db, $res:EMPTY, res:join($path, $res:DUMMY))
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
  } else {
    try { parse-xml($body)/* } catch * { web:error(400, 'Bad Request') }
  }
};

(:~
 : Returns the target path of a MOVE or COPY request. Plus signs are escaped
 : first: decode-from-uri turns them into spaces, but in a URI path they are
 : literal characters.
 : @return path below the WebDAV root
 :)
declare %private function dav:destination() as xs:string {
  let $uri := replace(request:header('Destination'), '\+', '%2B')
  let $segments := parse-uri($uri)?path-segments
  return string-join(subsequence($segments, count(tokenize($res:ROOT, '/')[.]) + 2), '/')
};

