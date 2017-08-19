(:~
 : Download resources.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../../modules/cons.xqm';

(:~
 : Downloads a resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $file      file name (ignored)
 : @return rest response and file content
 :)
declare
  %rest:path("/dba/db-download")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
function dba:db-download(
  $name      as xs:string,
  $resource  as xs:string
) as item()+ {
  cons:check(),
  try {
    web:response-header(
      map { 'media-type': db:content-type($name, $resource) },
      map { 'Cache-Control': '',
            'Content-Disposition': 'attachment; filename=' || $resource }
    ),
    if(db:is-raw($name, $resource)) then (
      db:retrieve($name, $resource)
    ) else (
      db:open($name, $resource)
    )
  } catch * {
    <rest:response>
      <http:response status="400" message="{ $err:description }"/>
    </rest:response>
  }
};
