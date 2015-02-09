(:~
 : Query resources.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/databases';

import module namespace G = 'dba/global' at '../../modules/global.xqm';
import module namespace web = 'dba/web' at '../../modules/web.xqm';

(:~
 : Runs a query on a document and returns the result as string.
 : @param  $name      name of database
 : @param  $resource  resource
 : @param  $query     query
 : @return result string
 :)
declare
  %rest:POST
  %rest:path("dba/query-resource")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
  %rest:query-param("query",    "{$query}")
  %output:method("text")
function _:query-resource(
  $name      as xs:string,
  $resource  as xs:string,
  $query     as xs:string
) as xs:string {
  web:check(),
  let $limit := $G:MAX-CHARS
  let $query := if($query) then $query else '.'
  return web:query($query, "'': db:open($name, $resource)", map {
    'name': $name, 'resource': $resource
  })
};
