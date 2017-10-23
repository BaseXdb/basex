(:~
 : Upload files.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Upploads files.
 : @param  $files  map with uploaded files
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path("/dba/file-upload")
  %rest:form-param("files", "{$files}")
function dba:file-upload(
  $files  as map(xs:string, xs:base64Binary)
) as element(rest:response) {
  cons:check(),

  (: save files :)
  let $dir := cons:current-dir()
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
