(:~
 : Put resources.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../../lib/html.xqm';
import module namespace utils = 'dba/utils' at '../../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Form for putting a new resource.
 : @param  $name    entered name
 : @param  $opts    chosen parsing options
 : @param  $path    database path
 : @param  $binary  store as binary
 : @param  $error   error string
 : @return page
 :)
declare
  %rest:GET
  %rest:POST
  %rest:path('/dba/db-put')
  %rest:query-param('name',   '{$name}')
  %rest:query-param('opts',   '{$opts}')
  %rest:query-param('path',   '{$path}')
  %rest:query-param('binary', '{$binary}')
  %rest:query-param('error',  '{$error}')
  %output:method('html')
  %output:html-version('5')
function dba:db-put(
  $name    as xs:string,
  $opts    as xs:string*,
  $path    as xs:string?,
  $binary  as xs:string?,
  $error   as xs:string?
) as element(html) {
  let $opts := if($opts = 'x') then $opts else ''
  return html:wrap({ 'header': ($dba:CAT, $name), 'error': $error },
    <tr>
      <td>
        <form method='post' enctype='multipart/form-data' autocomplete='off'>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, { 'name': $name }), ' » ',
            html:button('db-put-do', 'Put')
          }</h2>
          <!-- dummy value; prevents reset of options when nothing is selected -->
          <input type='hidden' name='opts' value='x'/>
          <input type='hidden' name='name' value='{ $name }'/>
          <table>
            <tr>
              <td>Input:</td>
              <td>{
                <input type='file' name='file' id='file'/>
              }</td>
            </tr>
            <tr>
              <td>Database Path:</td>
              <td>
                <input type='text' name='path' value='{ $path }'/>
              </td>
            </tr>
            <tr>
              <td>Binary Storage:</td>
              <td>{ html:checkbox('binary', 'true', $binary = 'true', '') }</td>
            </tr>
            <tr>
              <td colspan='2'>{
                <h3>Parsing Options</h3>,
                html:option('intparse', 'Use internal XML parser', $opts),
                html:option('dtd', 'Parse DTDs and entities', $opts),
                html:option('stripns', 'Strip namespaces', $opts),
                html:option('stripws', 'Strip whitespace', $opts),
                html:option('xinclude', 'Use XInclude', $opts)
              }</td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Puts a resource.
 : @param  $name    database
 : @param  $opts    chosen parsing options
 : @param  $path    database path
 : @param  $file    uploaded file
 : @param  $binary  store as binary file
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/db-put-do')
  %rest:form-param('name',   '{$name}')
  %rest:form-param('opts',   '{$opts}')
  %rest:form-param('path',   '{$path}')
  %rest:form-param('file',   '{$file}')
  %rest:form-param('binary', '{$binary}')
function dba:db-put-do(
  $name    as xs:string,
  $opts    as xs:string*,
  $path    as xs:string,
  $file    as map(*),
  $binary  as xs:string?
) as empty-sequence() {
  try {
    let $key := map:keys($file)
    let $path := if(not($path) or ends-with($path, '/')) then ($path || $key) else $path
    return if($key = '') then (
      error((), 'No input specified.')
    ) else (
      let $input := $file($key)
      return if($binary) then (
        db:put-binary($name, $input, $path)
      ) else (
        db:put($name, fetch:binary-doc($input), $path, map:merge(
          ('intparse', 'dtd', 'stripns', 'stripws', 'xinclude') ! map:entry(., $opts = .))
        )
      ),
      utils:redirect($dba:SUB,
        { 'name': $name, 'path': $path, 'info': 'Resource was added or updated.' }
      )
    )
  } catch * {
    utils:redirect('db-put', {
      'name': $name, 'opts': $opts, 'path': $path, 'binary': $binary, 'error': $err:description
    })
  }
};
