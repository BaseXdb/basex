(:~
 : Runs a query on a database resource.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)

(:~ Name of the database. :)
declare variable $name as xs:string external;
(:~ Path of the resource. :)
declare variable $resource as xs:string external;
(:~ Query. :)
declare variable $query as xs:string external;

let $type := db:type($name, $resource)
let $context := head(if ($type = 'xml') {
  db:get($name, $resource)
} else if ($type = 'binary') {
  db:get-binary($name, $resource)
} else {
  db:get-value($name, $resource)
})
return xquery:eval($query, { '': $context }, { 'pass': true() })
