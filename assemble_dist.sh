#! /bin/bash

VERSION=$1

git checkout $1 || exit

BDIR=build/atg-xref-${VERSION}

[ -d ${BDIR} ] && rm -rf ${BDIR}

mkdir -p ${BDIR}/bin

(cd indexer; cake bin) && cp indexer/atg-xref-indexer ${BDIR}/bin/indexer
(cd webui; cake bin) && 
cp webui/atg-xref-webui ${BDIR}/bin/webui && 
cp -r webui/templates ${BDIR} &&
cp -r webui/public ${BDIR}

cp -r solr ${BDIR}
(cd ${BDIR}/solr; rm -rf work logs/* solr/data)
mkdir ${BDIR}/solr/logs

cp README.md ${BDIR}/
[ -x `which pandoc` ] && pandoc -o ${BDIR}/readme.html README.md

(cd build; tar cvzf ../atg-xref-${VERSION}.tgz atg-xref-${VERSION})
(cd build; zip -r ../atg-xref-${VERSION}.zip atg-xref-${VERSION})
