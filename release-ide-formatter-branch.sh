#!/bin/bash

. release-common.sh

DESC=ceylon.formatter

replace_formatter() {
  log "Updating versions for release"
  ../ceylon/dist/dist/bin/ceylon version --set $CEYLON_NEW_VERSION --confirm=none --update-distribution 2>&1 >> $LOG_FILE || fail "Update $DESC version"
  
  perl -pi -e "s/(module=ceylon\\.formatter\/).*/\${1}${CEYLON_NEW_VERSION}/" script/ceylon/formatter/ceylon-format.plugin
  perl -pi -e "s/(module\\.ceylon\\.formatter\\.version=).*/\${1}${CEYLON_NEW_VERSION}/" build.properties 
  perl -pi -e "s/(ceylon plugin install ceylon\\.formatter\/).*/\${1}${CEYLON_NEW_VERSION}/" source/ceylon/formatter/module.ceylon 
  perl -pi -e "s/(import ceylon\\.(file|collection|test)) \".*\"/\${1} \"${CEYLON_NEW_VERSION}\"/" \
   source/ceylon/formatter/module.ceylon \
   source/source_gen/ceylon/formatter/module.ceylon \
   source/test/ceylon/formatter/module.ceylon
  perl -pi -e "s/(ceylon\\.formatter\/)\\S+/\${1}${CEYLON_NEW_VERSION}/" test-cli/test-cli
}

cd ../$DESC

log "Updating $DESC master branch"
git checkout master 2>&1 >> $LOG_FILE || fail "Git checkout $DESC master branch"

CEYLON_NEW_VERSION=$CEYLON_NEXT_VERSION

replace_formatter

log "Committing"
git commit -a -m "Fixed version for master $CEYLON_NEXT_VERSION" 2>&1 >> $LOG_FILE  || fail "Git commit $DESC master branch"

log "Creating $DESC release branch"
git checkout -b version-$CEYLON_RELEASE_VERSION $CEYLON_BRANCHING_TAG 2>&1 >> $LOG_FILE || fail "Git checkout new $DESC release branch"

CEYLON_NEW_VERSION=$CEYLON_RELEASE_VERSION

replace_formatter

log "Committing"
git commit -a -m "Fixed version for release $CEYLON_RELEASE_VERSION" 2>&1 >> $LOG_FILE  || fail "Git commit new $DESC release branch"
log "Tagging"
git tag $CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "Git tag $DESC new release branch"

cd ../ceylon
