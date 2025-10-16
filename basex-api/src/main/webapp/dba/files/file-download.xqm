(:~
 : Download file.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/files';

import module namespace config = 'dba/config' at '../lib/config.xqm';

(:~ Top category :)
declare variable $dba:CAT := '../files';

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
  return try {
    web:response-header(
      { 'media-type': 'application/octet-stream' },
      { 'Content-Length': file:size($path) }
    ),
    file:read-binary($path)
  } catch * {
    web:redirect($dba:CAT, { 'error': 'Download failed: ' || $err:description })
  }
};
