(:~
 : Download resources.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/databases';

(:~
 : Downloads a resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $file      file name (ignored)
 : @return rest response and file content
 :)
declare
  %rest:POST
  %rest:path('/dba/db-download')
  %rest:query-param('name',     '{$name}')
  %rest:query-param('resource', '{$resource}')
function dba:db-download(
  $name      as xs:string,
  $resource  as xs:string
) as item()+ {
  try {
    web:response-header(
      { 'media-type': db:content-type($name, $resource) },
      { 'Content-Disposition': 'attachment; filename=' || $resource }
    ),
    let $type := db:type($name, $resource)
    return if($type = 'xml') then (
      db:get($name, $resource)
    ) else if($type = 'binary') then (
      db:get-binary($name, $resource)
    ) else (
      db:get-value($name, $resource)
    )
  } catch * {
    <rest:response>
      <http:response status='400' message='{ $err:description }'/>
    </rest:response>
  }
};
