(:~
 : Add resources.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../../modules/cons.xqm';
import module namespace html = 'dba/html' at '../../modules/html.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Form for adding a new resource.
 : @param  $name    entered name
 : @param  $opts    chosen parsing options
 : @param  $path    database path
 : @param  $binary  store as binary
 : @param  $error   error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/db-add")
  %rest:query-param("name",   "{$name}")
  %rest:query-param("opts",   "{$opts}")
  %rest:query-param("path",   "{$path}")
  %rest:query-param("binary", "{$binary}")
  %rest:query-param("error",  "{$error}")
  %output:method("html")
function dba:db-add(
  $name    as xs:string,
  $opts    as xs:string*,
  $path    as xs:string?,
  $binary  as xs:string?,
  $error   as xs:string?
) as element(html) {
  cons:check(),
  let $opts := if($opts = 'x') then $opts else 'chop'
  return html:wrap(map { 'header': ($dba:CAT, $name), 'error': $error },
    <tr>
      <td>
        <form action="db-add" method="post" enctype="multipart/form-data" autocomplete="off">
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, map { 'name': $name }), ' » ',
            html:button('db-add', 'Add')
          }</h2>
          <!-- dummy value; prevents reset of options when nothing is selected -->
          <input type="hidden" name="opts" value="x"/>
          <input type="hidden" name="name" value="{ $name }"/>
          <table>
            <tr>
              <td>Input:</td>
              <td>{
                <input type="file" name="file" id="file"/>,
                html:focus('file')
              }</td>
            </tr>
            <tr>
              <td>Database Path:</td>
              <td>
                <input type="text" name="path" value="{ $path }"/>
              </td>
            </tr>
            <tr>
              <td>Binary Storage:</td>
              <td>{ html:checkbox('binary', 'true', $binary = 'true', '') }</td>
            </tr>
            <tr>
              <td colspan="2">{
                <h3>Parsing Options</h3>,
                html:option('intparse', 'Use internal XML parser', $opts),
                html:option('dtd', 'Parse DTDs and entities', $opts),
                html:option('stripns', 'Strip namespaces', $opts),
                html:option('chop', 'Chop whitespaces', $opts),
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
 : Adds a resource.
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
  %rest:path("/dba/db-add")
  %rest:form-param("name",   "{$name}")
  %rest:form-param("opts",   "{$opts}")
  %rest:form-param("path",   "{$path}")
  %rest:form-param("file",   "{$file}")
  %rest:form-param("binary", "{$binary}")
function dba:db-add-post(
  $name    as xs:string,
  $opts    as xs:string*,
  $path    as xs:string,
  $file    as map(*),
  $binary  as xs:string?
) as empty-sequence() {
  cons:check(),
  try {
    let $key := map:keys($file)
    let $path := if(not($path) or ends-with($path, '/')) then ($path || $key) else $path
    return if($key = '') then (
      error((), 'No input specified.')
    ) else if(db:exists($name, $path)) then (
      error((), 'Resource already exists: ' || $path)
    ) else (
      let $input := $file($key)
      return if($binary) then (
        db:store($name, $path, $input)
      ) else (
        db:add($name, fetch:xml-binary($input), $path, map:merge(
          ('intparse', 'dtd', 'stripns', 'chop', 'xinclude') ! map:entry(., $opts = .))
        )
      ),
      cons:redirect($dba:SUB,
        map { 'name': $name, 'path': $path, 'info': 'Resource was added.' }
      )
    )
  } catch * {
    cons:redirect('db-add', map {
      'name': $name, 'opts': $opts, 'path': $path, 'binary': $binary, 'error': $err:description
    })
  }
};
