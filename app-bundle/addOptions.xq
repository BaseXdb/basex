copy $doc := doc('dist/BaseX.app/Contents/Info.plist')
modify
(insert node (<key>NSHighResolutionCapable</key>,<string>True</string>) as last into $doc/plist/dict
)
return $doc