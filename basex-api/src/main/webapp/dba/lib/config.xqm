(:~
 : DBA configuration.
 :
 : @author Christian Grün, BaseX Team 2005-21, BSD License
 :)
module namespace config = 'dba/config';

import module namespace options = 'dba/options' at 'options.xqm';

(:~ Session key. :)
declare variable $config:SESSION-KEY := 'dba';
(:~ Current directory. :)
declare %private variable $config:DIRECTORY := 'dba-directory';
(:~ Current query. :)
declare %private variable $config:QUERY := 'dba-query';

(:~
 : Returns the current working directory.
 : @return directory
 :)
declare function config:directory() as xs:string {
  let $dir := session:get($config:DIRECTORY)
  return if(exists($dir) and file:exists($dir)) then (
    $dir
  ) else (
    $options:DBA-DIRECTORY
  )
};

(:~
 : Assigns a working directory.
 : @param  $value  value
 :)
declare function config:directory(
  $value  as xs:string
) as empty-sequence() {
  session:set($config:DIRECTORY, $value)
};

(:~
 : Returns the name of the current query.
 : @return current query
 :)
declare function config:query(
) as xs:string? {
  session:get($config:QUERY)
};

(:~
 : Assigns the name of the current query.
 : @param  $value  value
 :)
declare function config:query(
  $value  as xs:string
) as empty-sequence() {
  session:set($config:QUERY, $value)
};

(:~
 : Returns the names of all files.
 : @return list of files
 :)
declare function config:query-files() as xs:string* {
  let $dir := config:directory()
  where file:exists($dir)
  return file:list($dir)[matches(., '\.xqm?$')]
};
