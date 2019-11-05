#!/bin/bash

. release-common.sh

cd ../ceylon

log "Switching to new branch for $CEYLON_RELEASE_VERSION"

#FIXME: check working tree clean

CEYLON_NEW_VERSION=$CEYLON_RELEASE_VERSION
CEYLON_NEW_VERSION_MAJOR=$CEYLON_RELEASE_VERSION_MAJOR
CEYLON_NEW_VERSION_MINOR=$CEYLON_RELEASE_VERSION_MINOR
CEYLON_NEW_VERSION_RELEASE=$CEYLON_RELEASE_VERSION_RELEASE
CEYLON_NEW_VERSION_QUALIFIER=$CEYLON_RELEASE_VERSION_QUALIFIER
CEYLON_NEW_VERSION_PREFIXED_QUALIFIER=$CEYLON_RELEASE_VERSION_PREFIXED_QUALIFIER
CEYLON_NEW_VERSION_OSGI_QUALIFIER=$CEYLON_RELEASE_VERSION_OSGI_QUALIFIER
CEYLON_NEW_VERSION_NAME=$CEYLON_RELEASE_VERSION_NAME

git checkout -b version-$CEYLON_RELEASE_VERSION $CEYLON_BRANCHING_TAG 2>&1 >> $LOG_FILE || fail "Git checkout new release branch"

log "Replacing files"

replace common/src/org/eclipse/ceylon/common/Versions.java
replace language/src/ceylon/language/module.ceylon
replace language/src/ceylon/language/language.ceylon
replace language/test/process.ceylon
replace dist/samples/plugin/source/com/example/plugin/module.ceylon

perl -pi -e "s/ceylon\.version=.*/ceylon.version=$CEYLON_NEW_VERSION/" common-build.properties
perl -pi -e "s/ceylon\.osgi\.version=.*/ceylon.osgi.version=$CEYLON_NEW_VERSION_MAJOR.$CEYLON_NEW_VERSION_MINOR.$CEYLON_NEW_VERSION_RELEASE.osgi-$CEYLON_NEW_VERSION_OSGI_QUALIFIER/" common-build.properties

perl -pi -e "s/$CEYLON_RELEASE_VERSION_MAJOR\.$CEYLON_RELEASE_VERSION_MINOR\.$CEYLON_RELEASE_VERSION_RELEASE-SNAPSHOT _\"[^\"]+\"_/$CEYLON_NEW_VERSION _\"$CEYLON_NEW_VERSION_NAME\"_/" README.md dist/README.md
perl -pi -e "s/ceylon version $CEYLON_RELEASE_VERSION_MAJOR\.$CEYLON_RELEASE_VERSION_MINOR\.$CEYLON_RELEASE_VERSION_RELEASE-SNAPSHOT ([0-9a-f]+) \([^\)]+\)/ceylon version $CEYLON_NEW_VERSION \\1 \($CEYLON_NEW_VERSION_NAME\)/" README.md dist/README.md
perl -pi -e "s/$CEYLON_RELEASE_VERSION_MAJOR\.$CEYLON_RELEASE_VERSION_MINOR\.$CEYLON_RELEASE_VERSION_RELEASE-SNAPSHOT/$CEYLON_NEW_VERSION/" README.md dist/README.md
perl -pi -e "s/ceylon\.language-$CEYLON_RELEASE_VERSION_MAJOR\.$CEYLON_RELEASE_VERSION_MINOR\.$CEYLON_RELEASE_VERSION_RELEASE-SNAPSHOT/ceylon.language-$CEYLON_NEW_VERSION/" language/.classpath compiler-java/.classpath

find compiler-java/test/src/ -name '*.ceylon' -or -name '*.src' -or -name '*.properties' | xargs perl -pi -e "s/${CEYLON_RELEASE_VERSION_MAJOR}\.${CEYLON_RELEASE_VERSION_MINOR}\.${CEYLON_RELEASE_VERSION_RELEASE}-SNAPSHOT/$CEYLON_RELEASE_VERSION_MAJOR.$CEYLON_RELEASE_VERSION_MINOR.$CEYLON_RELEASE_VERSION_RELEASE/g"

log "Committing"
git commit -a -m "Fixed version for release $CEYLON_RELEASE_VERSION" 2>&1 >> $LOG_FILE  || fail "Git commit new release branch"
log "Tagging"
git tag $CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "Git tag new release branch"

log "Building local distrib"
ant clean dist 2>&1 >> $LOG_FILE || fail "Ant clean dist"
