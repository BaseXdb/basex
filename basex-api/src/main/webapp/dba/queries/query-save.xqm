(:~
 : Save query.
 :
 : @author Christian Grün, BaseX Team 2005-21, BSD License
 :)
module namespace dba = 'dba/queries';

import module namespace config = 'dba/config' at '../lib/config.xqm';

(:~
 : Saves a query file and returns the list of stored queries.
 : @param  $name   name of query file
 : @param  $query  query string
 : @return names of stored queries
 :)
declare
  %rest:POST('{$query}')
  %rest:path('/dba/query-save')
  %rest:query-param('name', '{$name}')
  %output:method('text')
function dba:query-save(
  $name   as xs:string,
  $query  as xs:string
) as xs:string {
  let $path := config:directory() || $name
  return (
    try {
      prof:void(xquery:parse($query, map {
        'plan': false(), 'pass': true(), 'base-uri': $path
      }))
    } catch * {
      error($err:code, 'Query was not stored: ' || $err:description, $err:value)
    },
    config:query($name),
    file:write-text($path, $query),
    string-join(config:query-files(), '/')
  )
};
