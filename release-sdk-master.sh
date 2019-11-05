#!/bin/bash

. release-common.sh

cd ../ceylon-sdk

log "Back to master"
git checkout master || fail "Git checkout SDK master"

log "Updating versions for master"
../ceylon/dist/dist/bin/ceylon version --set $CEYLON_NEXT_VERSION --confirm=none --update-distribution 2>&1 >> $LOG_FILE || fail "Update SDK version"

log "Committing"
git commit -a -m "Fixed version for master $CEYLON_NEXT_VERSION" 2>&1 >> $LOG_FILE  || fail "Git commit new SDK master branch"
