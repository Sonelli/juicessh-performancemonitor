#!/bin/bash

. release-common.sh

##
## Really build the distrib zip

log "Building distrib zip"
rm -rf /tmp/ceylon 2>&1 >> $LOG_FILE || fail "rm /tmp/ceylon"
mkdir /tmp/ceylon 2>&1 >> $LOG_FILE || fail "mkdir /tmp/ceylon"
docker pull ceylon/ceylon-build 2>&1 >> $LOG_FILE || fail "Docker pull"
docker run -t --rm -v /tmp/ceylon:/output ceylon/ceylon-build $CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "Docker build ZIP"

log "Publishing distrib zip"
scp /tmp/ceylon/ceylon-${CEYLON_RELEASE_VERSION}.zip ceylon-lang.org:/var/www/downloads.ceylonlang/cli/ 2>&1 >> $LOG_FILE || fail "Uploading distrib zip"

