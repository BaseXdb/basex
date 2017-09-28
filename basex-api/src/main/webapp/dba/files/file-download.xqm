(:~
 : Download file.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

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
  cons:check(),
  web:response-header(map { }, map { 'Cache-Control': '' }),
  file:read-binary($cons:DBA-DIR || $name)
};
