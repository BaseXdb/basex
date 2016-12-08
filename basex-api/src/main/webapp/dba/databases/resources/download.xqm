(:~
 : Download resources.
 :
 : @author Christian Grün, BaseX Team, 2014-16
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
  %rest:path("/dba/download")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
function dba:download(
  $name      as xs:string,
  $resource  as xs:string
) as item()+ {
  cons:check(),
  try {
    web:response-header(
      map { 'media-type': db:content-type($name, $resource) },
      map { 'Content-Disposition': 'attachment; filename=' || $resource }
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

(:~
 : Downloads a database backup.
 : @param  $backup  name of backup file (ignored)
 : @return zip file
 :)
declare
  %rest:path("/dba/backup/{$backup}")
  %output:media-type("application/octet-stream")
function dba:download(
  $backup  as xs:string
) {
  cons:check(),
  file:read-binary(db:system()/globaloptions/dbpath || '/' || $backup)
};
