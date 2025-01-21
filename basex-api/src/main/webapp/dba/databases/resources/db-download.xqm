(:~
 : Download resources.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/databases';

(:~
 : Downloads a resource.
 : @param  $name      database
 : @param  $resource  resource
 : @return rest response and file content
 :)
declare
  %rest:POST
  %rest:path('/dba/db-download')
  %rest:form-param('name',     '{$name}')
  %rest:form-param('resource', '{$resource}')
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
    return if ($type = 'xml') {
      db:get($name, $resource)
    } else if ($type = 'binary') {
      db:get-binary($name, $resource)
    } else {
      db:get-value($name, $resource)
    }
  } catch * {
    web:error(404, $err:description)
  }
};
