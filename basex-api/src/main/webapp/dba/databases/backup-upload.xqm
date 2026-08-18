(:~
 : Upload backups.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/backup-upload';

import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';

(:~
 : Uploads backups.
 : @param  $files  map with uploaded files
 : @param  $name   database the backups are uploaded for (empty string: the general data)
 : @return form or redirection
 :)
(: kept out of the databases dispatcher: an upload must not wait for the database locks :)
declare
  %rest:POST
  %rest:path('/dba/backup-upload')
  %rest:form-param('files', '{$files}')
  %rest:form-param('name',  '{$name}', '')
function dba:file-upload(
  $files  as item()?,
  $name   as xs:string
) as element(rest:response) {
  let $dir := db:option('dbpath') || '/'
  let $files := $files[. instance of map(*)] otherwise {}
  (: the panel the upload was started from decides where the backups belong :)
  let $params := { 'name': $name }
  return try {
    (: reject backups with invalid content :)
    map:for-each($files, fn($file, $content) {
      let $db := replace($file, $utils:BACKUP-ZIP-REGEX, '$1')
      let $entries := archive:entries($content) ! data()
      where not(if ($db) {
        every $entry in $entries satisfies starts-with($entry, $db || '/') and
        $entries = $db || '/inf.basex'
      } else {
        every $entry in $entries satisfies matches($entry, '\.(xml|basex)')
      })
      return error((), 'Invalid backup file: ' || $file)
    }),
    (: reject the backup of another database: it would be invisible in the panel it was uploaded
       from. Without a selected database there is nothing to contradict, and a backup of a
       database that no longer exists is what a recovery starts from :)
    map:for-each($files, fn($file, $content) {
      let $db := replace($file, $utils:BACKUP-ZIP-REGEX, '$1')
      where $name and $db != $name
      return error((), `Backup "{ $file }" does not belong to database "{ $name }".`)
    }),
    map:for-each($files, fn($file, $content) {
      file:write-binary($dir || $file, $content)
    }),
    web:redirect($dba:CAT, map:put($params, 'info',
      utils:info(map:keys($files), 'backup', 'uploaded')))
  } catch * {
    web:redirect($dba:CAT, map:put($params, 'error', $err:description))
  }
};
