(:~
 : Download file.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
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
  let $path := config:files-dir() || $name
  return (
    web:response-header(
      { 'media-type': 'application/octet-stream' },
      { 'Content-Length': file:size($path) }
    ),
    file:read-binary($path)
  )
};
