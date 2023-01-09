(:~
 : Upload files.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/files';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace util = 'dba/util' at '../lib/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Uploads files.
 : @param  $files  map with uploaded files
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/file-upload')
  %rest:form-param('files', '{$files}')
function dba:file-upload(
  $files  as map(xs:string, xs:base64Binary)
) as element(rest:response) {
  (: save files :)
  let $dir := config:directory()
  return try {
    (: Parse all XQuery files; reject files that cannot be parsed :)
    map:for-each($files, function($file, $content) {
      if(matches($file, '\.xqm?$')) then (
        prof:void(xquery:parse(
          convert:binary-to-string($content),
          map { 'plan': false(), 'pass': true(), 'base-uri': $dir || $file }
        ))
      ) else ()
    }),
    map:for-each($files, function($file, $content) {
      file:write-binary($dir || $file, $content)
    }),
    web:redirect($dba:CAT, map { 'info': util:info(map:keys($files), 'file', 'uploaded') })
  } catch * {
    web:redirect($dba:CAT, map { 'error': 'Upload failed: ' || $err:description })
  }
};
