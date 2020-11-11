#!/bin/sh

if [ $# != 2 ]; then
cat <<- _EOF_
usage: $0 <source> <destination>

Imports localization files into the dest project.
_EOF_
exit 0
fi

src="${1%/}"
dst=$2
tmp="tmp-l10n"

rm -rf "$tmp"
mkdir "$tmp"

cp -R "$src/" "$tmp"

for f in "$tmp"/*; do
    if [ -d "$f" ]; then
        lang=$(basename "$f" | tr '[:upper:]' '[:lower:]')
        find "$f" -type 'd' -name values -print0 | xargs -0 -I{} mv {} "{}-$lang"
        cp -R "$f/" "$dst"
    fi
done

rm -rf "$tmp"
