(:~
 : Download resources.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/databases';

import module namespace web = 'dba/web' at '../../modules/web.xqm';

(:~
 : Downloads a resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $file      file name (ignored)
 : @return rest response and file content
 :)
declare
  %rest:path("dba/download/{$file}")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
function _:download(
  $name      as xs:string,
  $resource  as xs:string,
  $file      as xs:string
) as item()+ {
  web:check(),
  try {
    let $options := map { 'n': $name, 'r': $resource }
    let $raw := web:eval("db:is-raw($n, $r)", $options)
    return (
      <rest:response>
        <output:serialization-parameters>
          <output:method value='{ if($raw) then "raw" else "xml" }'/>
          <output:media-type value='{ web:eval("db:content-type($n, $r)", $options) }'/>
        </output:serialization-parameters>
      </rest:response>,
      web:eval(if($raw) then "db:retrieve($n, $r)" else "db:open($n, $r)", $options)
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
  %rest:path("dba/backup/{$backup}")
  %output:method("raw")
  %output:media-type("application/octet-stream")
function _:download(
  $backup  as xs:string
) {
  web:check(),
  web:eval("file:read-binary(db:system()/globaloptions/dbpath || '/' || $b)",
    map { 'b': $backup }
  )
};
