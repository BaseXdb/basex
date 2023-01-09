(:~
 : Download file.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/files';

import module namespace config = 'dba/config' at '../lib/config.xqm';

(:~
 : Downloads a file.
 : @param  $name  name of file
 : @return binary data
 :)
declare
  %rest:GET
  %rest:path('/dba/file/{$name}')
function dba:file-download(
  $name  as xs:string
) as item()+ {
  let $path := config:directory() || $name
  return (
    web:response-header(
      map { 'media-type': 'application/octet-stream' },
      map { 'Content-Length': file:size($path) }
    ),
    file:read-binary($path)
  )
};
