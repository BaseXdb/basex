(:~
 : DBA configuration.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace config = 'dba/config';

(:~ Session key. :)
declare variable $config:SESSION-KEY := 'dba';
(:~ Currently selected directory. :)
declare %private variable $config:DIRECTORY := 'dba-directory';
(:~ Name of currently opened file. :)
declare %private variable $config:FILE := 'dba-file';

(:~ DBA directory. :)
declare variable $config:DBA-DIRECTORY := (
  for $dir in db:option('dbpath') || '/.dba'
  return (
    if(file:exists($dir)) then () else file:create-dir($dir),
    file:path-to-native($dir)
  )
);

(:~ Permission values. :)
declare variable $config:PERMISSIONS := ('none', 'read', 'write', 'create', 'admin');
(:~ Indentation values. :)
declare variable $config:INDENTS := ('no', 'yes');

(:~ Maximum length of XML characters. :)
declare variable $config:MAXCHARS := 'maxchars';
(:~ Maximum number of table entries. :)
declare variable $config:MAXROWS := 'maxrows';
(:~ Query timeout. :)
declare variable $config:TIMEOUT := 'timeout';
(:~ Maximal memory consumption. :)
declare variable $config:MEMORY := 'memory';
(:~ Permission when running queries. :)
declare variable $config:PERMISSION := 'permission';
(:~ Show DBA log entries. :)
declare variable $config:IGNORE-LOGS := 'ignore-logs';
(:~ Indent results. :)
declare variable $config:INDENT := 'indent';

(:~ Options file. :)
declare %private variable $config:OPTIONS-FILE := $config:DBA-DIRECTORY || '.dba.xml';

(:~ Default options. :)
declare %basex:lazy %private variable $config:DEFAULTS := map {
  $config:MAXCHARS   : 1000000,
  $config:MAXROWS    : 1000,
  $config:TIMEOUT    : 30,
  $config:MEMORY     : 1000,
  $config:PERMISSION : 'admin',
  $config:IGNORE-LOGS: '',
  $config:INDENT     : 'no'
};

(:~ Currently assigned options. :)
declare %basex:lazy %private variable $config:OPTIONS := (
  if(file:exists($config:OPTIONS-FILE)) then (
    try {
      (: merge defaults with saved options :)
      let $options := fetch:doc($config:OPTIONS-FILE)/options
      return map:merge(
        map:for-each($config:DEFAULTS, fn($key, $value) {
          map:entry($key,
            let $option := $options/*[name() = $key]
            return if($option) then (
              typeswitch($value)
                case xs:numeric  return xs:integer($option)
                case xs:boolean  return xs:boolean($option)
                default          return xs:string($option)
            ) else (
              $value
            )
          )
        })
      )
    } catch * {
      (: use defaults if an error occurs while parsing the options :)
      $config:DEFAULTS
    }
  ) else (
    $config:DEFAULTS
  )
);

(:~
 : Returns the value of an option.
 : @param  $name  name of option
 : @return value
 :)
declare function config:get(
  $name  as xs:string
) as xs:anyAtomicType {
  $config:OPTIONS($name)
};

(:~
 : Saves options.
 : @param  $options  keys/values that have been changed
 :)
declare function config:save(
  $options  as map(*)
) as empty-sequence() {
  file:write($config:OPTIONS-FILE, element options {
    map:for-each($config:DEFAULTS, fn($key, $value) {
      element { $key } { ($options($key), $value)[1] }
    })
  })
};

(:~
 : Returns the current working directory.
 : @return directory
 :)
declare function config:directory() as xs:string {
  let $dir := session:get($config:DIRECTORY)
  return if(exists($dir) and file:exists($dir)) then (
    $dir
  ) else (
    $config:DBA-DIRECTORY
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
 : Returns the name of the currently opened file.
 : @return current file
 :)
declare function config:file(
) as xs:string? {
  session:get($config:FILE)
};

(:~
 : Assigns the name of the currently opened file.
 : @param  $value  value
 :)
declare function config:file(
  $value  as xs:string
) as empty-sequence() {
  session:set($config:FILE, $value)
};

(:~
 : Returns the names of all files.
 : @return list of files
 :)
declare function config:files() as xs:string* {
  let $limit := config:get($config:MAXCHARS)
  let $dir := config:directory()
  where file:is-dir($dir)
  for $file in file:children($dir)
  where file:is-file($file) and file:size($file) <= $limit
  return file:name($file)
};
