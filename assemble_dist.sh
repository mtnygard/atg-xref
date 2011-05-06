#! /bin/bash

VERSION=0.1.1
BDIR=build/atg-xref-${VERSION}

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

(cd build; tar cvzf ../atg-xref-dist.tgz atg-xref-${VERSION})
(cd build; zip -r ../atg-xref-dist.zip atg-xref-${VERSION})
