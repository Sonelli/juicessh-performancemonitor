#!/bin/bash

. release-common.sh

log "Creating SDK release branch"
cd ../ceylon-sdk
git checkout -b version-$CEYLON_RELEASE_VERSION $CEYLON_BRANCHING_TAG 2>&1 >> $LOG_FILE || fail "Git checkout new SDK release branch"

log "Updating versions for release"
../ceylon/dist/dist/bin/ceylon version --set $CEYLON_RELEASE_VERSION --confirm=none --update-distribution 2>&1 >> $LOG_FILE || fail "Update SDK version"

log "Committing"
git commit -a -m "Fixed version for release $CEYLON_RELEASE_VERSION" 2>&1 >> $LOG_FILE  || fail "Git commit new SDK release branch"
log "Tagging"
git tag $CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "Git tag SDK new release branch"

log "Installing the SDK locally"
ant -Dceylon.home=../ceylon/dist/dist clean publish 2>&1 >> $LOG_FILE || fail "Build and publish SDK"

cd ../ceylon
