(:~
 : Download file.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/files';

import module namespace session = 'dba/session' at '../modules/session.xqm';

(:~
 : Downloads a file.
 : @param  $name  name of file
 : @return binary data
 :)
declare
  %rest:GET
  %rest:path("/dba/file/{$name}")
function dba:files(
  $name  as xs:string
) as item()+ {
  web:response-header(map { 'media-type': 'application/octet-stream' }),
  file:read-binary(session:directory() || $name)
};
