(:~
 : Resource model: maps WebDAV paths to databases and database resources.
 :
 : A WebDAV path consists of a database name and a database path. The root
 : collection lists all databases, a database is a collection, and folders are
 : derived from the paths of the resources they contain.
 :
 : @author BaseX Team, BSD License
 :)
module namespace res = 'webdav/resources';

(:~ Dummy resource that keeps otherwise empty collections alive. :)
declare variable $res:DUMMY := '.empty';

(:~ Path the service is mounted at; the %rest:path annotations must match it. :)
declare variable $res:ROOT := '/webdav';

(:~ Database name and database path of a WebDAV path. :)
declare record res:ref(
  db    as xs:string?,
  path  as xs:string
);

(:~ The root collection, a database or a folder. :)
declare record res:entry(
  kind      as xs:string,
  db        as xs:string?,
  path      as xs:string,
  name      as xs:string,
  modified  as xs:dateTime
);

(:~ A stored resource, with the metadata of its contents. :)
declare record res:resource(
  kind          as xs:string,
  db            as xs:string?,
  path          as xs:string,
  name          as xs:string,
  modified      as xs:dateTime,
  size          as xs:integer,
  type          as xs:string,
  content-type  as xs:string
);

(:~ The root collection, a database, a folder or a resource. :)
declare type res:any as (res:entry | res:resource);

(:~
 : Splits a WebDAV path into a database name and a database path.
 : @param  $path  path below the WebDAV root
 : @return database (empty for the root collection) and database path
 :)
declare function res:parse(
  $path  as xs:string
) as res:ref {
  let $steps := tokenize($path, '/')[.]
  return res:ref(head($steps), string-join(tail($steps), '/'))
};

(:~
 : Joins a collection path and a resource name.
 : @param  $path  collection path (may be empty)
 : @param  $name  resource name
 : @return joined path
 :)
declare function res:join(
  $path  as xs:string,
  $name  as xs:string
) as xs:string {
  if ($path = '') { $name } else { $path || '/' || $name }
};

(:~
 : Returns the last step of a path.
 : @param  $path  path
 : @return name
 :)
declare function res:name(
  $path  as xs:string
) as xs:string {
  tokenize($path, '/')[last()]
};

(:~
 : Returns the parent collection path.
 : @param  $path  path
 : @return parent path (empty string for top-level entries)
 :)
declare function res:parent(
  $path  as xs:string
) as xs:string {
  string-join(tokenize($path, '/')[position() < last()], '/')
};

(:~
 : Returns the WebDAV path of a resource description.
 : @param  $rc  resource description
 : @return path below the WebDAV root
 :)
declare function res:path(
  $rc  as res:any
) as xs:string {
  res:join($rc?db otherwise '', $rc?path)
};

(:~
 : Looks up the resource that is addressed by a WebDAV path.
 : A resource takes precedence over a collection of the same name.
 : @param  $path  path below the WebDAV root
 : @return resource description, or empty sequence if the path does not exist
 :)
declare function res:lookup(
  $path  as xs:string
) as res:any? {
  let $ref := res:parse($path)
  let $db := $ref?db
  return if (empty($db)) {
    res:entry('root', (), '', '', current-dateTime())
  } else if (not(db:exists($db))) {
    ()
  } else if ($ref?path = '') {
    res:collection($db, '', $db)
  } else if (db:exists($db, $ref?path)) {
    res:resource($db, $ref?path, db:list-details($db, $ref?path)[1])
  } else if (exists(db:list($db, $ref?path))) {
    res:collection($db, $ref?path, res:name($ref?path))
  } else {
    ()
  }
};

(:~
 : Returns the members of a collection. Dummy resources are skipped.
 : @param  $rc  collection description
 : @return members
 :)
declare function res:children(
  $rc  as res:any
) as res:any* {
  if ($rc?kind = 'root') {
    for $db in db:list-details()
    return res:entry('collection', $db, '', $db, $db/@modified-date)
  } else if ($rc?kind = 'collection') {
    for $child in db:dir($rc?db, $rc?path)
    let $name := string($child)
    where not($child/self::resource and $name = $res:DUMMY)
    return if ($child/self::dir) {
      res:collection($rc?db, res:join($rc?path, $name), $name)
    } else {
      res:resource($rc?db, res:join($rc?path, $name), $child)
    }
  } else {
    ()
  }
};

(:~
 : Returns all descendants of a collection (used for Depth: infinity).
 : @param  $rc  collection description
 : @return descendants
 :)
declare function res:descendants(
  $rc  as res:any
) as res:any* {
  for $child in res:children($rc)
  return ($child, res:descendants($child))
};

(:~
 : Creates a collection description.
 : @param  $db    database
 : @param  $path  database path (empty for the database itself)
 : @param  $name  display name
 : @return collection
 :)
declare function res:collection(
  $db    as xs:string,
  $path  as xs:string,
  $name  as xs:string
) as res:entry {
  res:entry('collection', $db, $path, $name, xs:dateTime(db:property($db, 'timestamp')))
};

(:~
 : Creates a resource description from a database listing entry.
 : @param  $db    database
 : @param  $path  database path
 : @param  $node  listing entry
 : @return resource
 :)
declare function res:resource(
  $db    as xs:string,
  $path  as xs:string,
  $node  as element(resource)
) as res:resource {
  res:resource('resource', $db, $path, res:name($path),
    $node/@modified-date, $node/@size, $node/@type, $node/@content-type)
};

(:~
 : Returns the content of a resource.
 : @param  $rc  resource description
 : @return content
 :)
declare function res:content(
  $rc  as res:resource
) as item() {
  switch ($rc?type)
    case 'xml' return db:get($rc?db, $rc?path)
    case 'value' return db:get-value($rc?db, $rc?path)
    default return db:get-binary($rc?db, $rc?path)
};
