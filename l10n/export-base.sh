#!/bin/sh

if [ $# != 1 ]; then
cat <<- _EOF_
usage: $0 <source>

Exports base localization files from the source proj.
_EOF_
exit 0
fi

now=$(date +'%m-%d-%Y-%H%M')
output="base-$now"
zip -r "$output" "$1" -i "*/values/strings.xml"
echo "Created $output.zip"
