(: Copyright BaseX Team 2005-13, BSD License :)
module namespace w = 'http://basex.org/webdav';

(: Lock database name. :)
declare variable $w:locks-db := 'webdav-locks';

declare variable $w:err-locked := QName('http://basex.org/webdav/errors', 'w:locked_423');



(: Decomposes a path into segments :)
declare function w:path-segments(
  $path as xs:string
) as xs:string* {
  tokenize($path, '/')[string-length(.) gt 0]
};



declare function w:is-prefix(
  $x as xs:string*,
  $y as xs:string*
) as xs:boolean {
  every $r in
    for-each-pair($x,  $y, function($a, $b) { $a eq $b })
  satisfies $r
};



(: Check if a lock with the given path has locked (possibly indirectly) another resource. :)
declare function w:has-locked(
  $ancestor as xs:string,
  $descendant as xs:string,
  $depth as xs:string
) as xs:boolean {
  let $ancestor-segments := w:path-segments($ancestor),
      $descendant-segments := w:path-segments($descendant)
  return
    switch($depth)
    case '0'
    case '1'
      return
        count($ancestor-segments) + $depth cast as xs:integer eq count($descendant-segments)
        and
        w:is-prefix($ancestor-segments, $descendant-segments)
    case 'infinite'
      return
        w:is-prefix($ancestor-segments, $descendant-segments)
   default
     return false()
};



(: Calculates the lock expiration date-time given its timeout in seconds. :)
declare function w:get-expiry-dateTime(
  $timeout as xs:integer
) as xs:dateTime {
  current-dateTime() + xs:dayTimeDuration('PT' || $timeout || 'S')
};



(: Check if the lock database exists. :)
declare function w:lock-db-exists() as xs:boolean {
  db:exists($w:locks-db)
};



(: Create the database for the WebDAV locks, if it does not exist. :)
declare updating function w:init-lock-db() {
  if (w:lock-db-exists()) then
    ()
  else
    db:create($w:locks-db, <w:locks/>, $w:locks-db)
};



(: Open the lock database, if it exists; else empty sequence. :)
declare function w:open-lock-db() {
  if(w:lock-db-exists()) then
    db:open($w:locks-db)
  else
    ()
};



(: Find all active locks of the given resource :)
declare function w:get-locks-on(
  $resource as xs:string
) as element(w:lockinfo)* {
  w:open-lock-db()/w:locks/w:lockinfo[
    w:has-locked(w:path, $resource, 'infinite')
    and
    w:expiry cast as xs:dateTime gt current-dateTime()
  ]
};



(: Check if two locks are in conflict. :)
declare function w:in-conflict(
  $lock1 as element(w:lockinfo),
  $lock2 as element(w:lockinfo)
) as xs:boolean {
  (w:has-locked($lock1/w:path, $lock2/w:path, $lock1/w:depth) or w:has-locked($lock2/w:path, $lock1/w:path, $lock2/w:depth))
  and
  (w:is-exclusive($lock1) or w:is-exclusive($lock2))
  and
  $lock1/w:owner ne $lock2/w:owner
};



(: Check if a lock is exclusive. :)
declare function w:is-exclusive(
  $lock as element(w:lockinfo)
) as xs:boolean {
  $lock/w:scope eq 'exclusive'
};



(: Get all locks which are in conflict with the given one. :)
declare function w:get-conflicting-locks(
  $requested-lock as element(w:lockinfo)
) as element(w:lockinfo)* {
  for $existing-lock in w:get-locks-on($requested-lock/w:path)
  where w:in-conflict($requested-lock, $existing-lock)
  return $existing-lock
};



(: Renew a lock with the given token. :)
declare updating function w:refresh-lock(
  $lock-token as xs:string
) {
  let $lock := w:get-lock($lock-token)
  return
    if($lock) then
      replace value of node $lock/w:expiry
      with w:get-expiry-dateTime($lock/w:timeout)
    else
      ()
};



(: Return the lock with the given token. :)
declare function w:get-lock(
  $lock-token as xs:string
) as element(w:lockinfo)* {
  w:open-lock-db()/w:locks/w:lockinfo[w:token eq $lock-token]
};



(: Create a new lock for a given resource. :)
declare updating function w:create-lock(
  $resource as xs:string,
  $lock-token as xs:string,
  $lock-scope as xs:string,
  $lock-type as xs:string,
  $lock-depth as xs:string,
  $lock-owner as xs:string,
  $lock-timeout as xs:integer
) {
  let $lock-expiry := w:get-expiry-dateTime($lock-timeout),
      $requested-lock :=
        <w:lockinfo>
          <w:path>{ $resource }</w:path>
          <w:token>{ $lock-token }</w:token>
          <w:scope>{ $lock-scope }</w:scope>
          <w:type>{ $lock-type }</w:type>
          <w:depth>{ $lock-depth }</w:depth>
          <w:owner>{ $lock-owner }</w:owner>
          <w:timeout>{ $lock-timeout }</w:timeout>
          <w:expiry>{ $lock-expiry }</w:expiry>
        </w:lockinfo>,
      $conflicting-locks := w:get-conflicting-locks($requested-lock)
  return
    if(empty($conflicting-locks)) then
      insert node $requested-lock as last into w:open-lock-db()/w:locks
    else
      error($w:err-locked, 'Resource has a conflicting lock', $resource)
};



(: Remove a lock given its token. :)
declare updating function w:delete-lock(
  $lock-token as xs:string
) {
  delete node w:get-lock($lock-token)
};
