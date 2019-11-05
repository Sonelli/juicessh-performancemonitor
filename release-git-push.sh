#!/bin/bash

. release-common.sh

cd ../ceylon

#log "Pushing distrib master"
#git checkout master 2>&1 >> $LOG_FILE || fail "Git checkout master"
#git push origin 2>&1 >> $LOG_FILE  || fail "Git push master"

log "Pushing distrib version branch"
git checkout version-$CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "Git checkout new release branch"
git push --set-upstream origin version-$CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "Git push new release branch"
git push origin $CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "Git tag new release branch"

cd ../ceylon-sdk

#log "Pushing SDK master"
#git checkout master 2>&1 >> $LOG_FILE || fail "Git checkout SDK master branch"
#git push origin master 2>&1 >> $LOG_FILE || fail "Git push new SDK master branch"

log "Pushing SDK version branch"
git checkout version-$CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "Git checkout new SDK release branch"
git push --set-upstream origin version-$CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "Git push new SDK release branch"
git push origin $CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "Git tag SDK new release branch"

cd ../ceylon
