(:~
 : Upload files.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/files';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

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
  $files  as item()?
) as element(rest:response) {
  (: save files :)
  let $dir := config:files-dir()
  return if ($files instance of xs:string) then web:redirect($dba:CAT) else
    
  try {
    (: Parse all XQuery files; reject files that cannot be parsed :)
    map:for-each($files, fn($name, $content) {
      if(matches($name, '\.xq(m|l|y|u|uery)?$')) then (
        void(utils:query-parse(convert:binary-to-string($content), $dir || $name))
      )
    }),
    map:for-each($files, fn($name, $content) {
      file:write-binary($dir || $name, $content)
    }),
    web:redirect($dba:CAT, { 'info': utils:info(map:keys($files), 'file', 'uploaded') })
  } catch * {
    web:redirect($dba:CAT, { 'error': 'Upload failed: ' || $err:description })
  }
};
