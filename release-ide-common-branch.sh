#!/bin/bash

. release-common.sh

DESC=ceylon-ide-common

replace_ide_common() {
  log "Updating versions for release"
  ../ceylon/dist/dist/bin/ceylon version --set $CEYLON_NEW_VERSION --confirm=none --update-distribution 2>&1 >> $LOG_FILE || fail "Update $DESC version"
  
  perl -pi -e "s/(module\\.org\\.eclipse\\.ceylon\\.ide\\.common\\.version=).*/\${1}${CEYLON_NEW_VERSION}/" build.properties 
  perl -pi -e "s/(import (test\\.)?ceylon\\.(interop\\.java|test|file|collection|formatter|bootstrap)) \".*\"/\${1} \"${CEYLON_NEW_VERSION}\"/" \
   source/org/eclipse/ceylon/ide/common/module.ceylon \
   test-source/test/org/eclipse/ceylon/ide/common/module.ceylon
  perl -pi -e "s/(\"(Ceylon: (test\\.)?(ceylon\\.(interop\\.java|test|language|collection|bootstrap|file|formatter)|org\\.eclipse\\.ceylon\\.[^\/]+)))\/[^\"]*\"/\${1}\/${CEYLON_NEW_VERSION}\"/" \
   ceylon-ide-common.iml
}

cd ../$DESC

log "Updating $DESC master branch"
git checkout master 2>&1 >> $LOG_FILE || fail "Git checkout $DESC master branch"

CEYLON_NEW_VERSION=$CEYLON_NEXT_VERSION

replace_ide_common

log "Committing"
git commit -a -m "Fixed version for master $CEYLON_NEXT_VERSION" 2>&1 >> $LOG_FILE  || fail "Git commit $DESC master branch"

log "Creating $DESC release branch"
git checkout -b version-$CEYLON_RELEASE_VERSION $CEYLON_BRANCHING_TAG 2>&1 >> $LOG_FILE || fail "Git checkout new $DESC release branch"

CEYLON_NEW_VERSION=$CEYLON_RELEASE_VERSION

replace_ide_common

log "Committing"
git commit -a -m "Fixed version for release $CEYLON_RELEASE_VERSION" 2>&1 >> $LOG_FILE  || fail "Git commit new $DESC release branch"
log "Tagging"
git tag $CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "Git tag $DESC new release branch"

cd ../ceylon
