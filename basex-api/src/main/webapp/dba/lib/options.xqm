(:~
 : Global options.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace options = 'dba/options';

(:~ DBA directory. :)
declare variable $options:DBA-DIRECTORY := (
  for $dir in db:option('dbpath') || '/.dba'
  return (
    if(file:exists($dir)) then () else file:create-dir($dir),
    file:path-to-native($dir)
  )
);

(:~ Permission values. :)
declare variable $options:PERMISSIONS := ('none', 'read', 'write', 'create', 'admin');
(:~ Indentation values. :)
declare variable $options:INDENTS := ('no', 'yes');

(:~ Maximum length of XML characters. :)
declare variable $options:MAXCHARS := 'maxchars';
(:~ Maximum number of table entries. :)
declare variable $options:MAXROWS := 'maxrows';
(:~ Query timeout. :)
declare variable $options:TIMEOUT := 'timeout';
(:~ Maximal memory consumption. :)
declare variable $options:MEMORY := 'memory';
(:~ Permission when running queries. :)
declare variable $options:PERMISSION := 'permission';
(:~ Show DBA log entries. :)
declare variable $options:IGNORE-LOGS := 'ignore-logs';
(:~ Indent results. :)
declare variable $options:INDENT := 'indent';

(:~ Options file. :)
declare %private variable $options:FILE := $options:DBA-DIRECTORY || '.dba.xml';

(:~ Default options. :)
declare %basex:lazy %private variable $options:DEFAULTS := map {
  $options:MAXCHARS   : 200000,
  $options:MAXROWS    : 200,
  $options:TIMEOUT    : 30,
  $options:MEMORY     : 500,
  $options:PERMISSION : 'admin',
  $options:IGNORE-LOGS: '',
  $options:INDENT     : 'no'
};

(:~ Currently assigned options. :)
declare %basex:lazy %private variable $options:OPTIONS := (
  if(file:exists($options:FILE)) then (
    try {
      (: merge defaults with saved options :)
      let $options := fetch:doc($options:FILE)/options
      return map:merge(
        map:for-each($options:DEFAULTS, function($key, $value) {
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
      $options:DEFAULTS
    }
  ) else (
    $options:DEFAULTS
  )
);

(:~
 : Returns the value of an option.
 : @param  $name  name of option
 : @return value
 :)
declare function options:get(
  $name  as xs:string
) as xs:anyAtomicType {
  $options:OPTIONS($name)
};

(:~
 : Saves options.
 : @param  $options  keys/values that have been changed
 :)
declare function options:save(
  $options  as map(*)
) as empty-sequence() {
  file:write($options:FILE, element options {
    map:for-each($options:DEFAULTS, function($key, $value) {
      element { $key } { ($options($key), $value)[1] }
    })
  })
};
