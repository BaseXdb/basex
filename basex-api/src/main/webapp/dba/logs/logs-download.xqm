(:~
 : Download log file.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/logs-download';

import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~
 : Downloads database logs.
 : @param  $names  names (dates) of log files
 : @return single or zipped file
 :)
declare
  %rest:POST
  %rest:path('/dba/logs-download')
  %rest:form-param('name', '{$names}')
function dba:logs-download(
  $names  as xs:string*
) as item()+ {
  (: an archive of several logs is named after the first and the last date :)
  utils:download(
    $names ! (db:option('dbpath') || '/.logs/' || . || '.log'),
    string-join(sort($names)[position() = (1, last())], '_')
  )
};
