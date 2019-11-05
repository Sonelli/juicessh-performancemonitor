#!/bin/bash

. release-common.sh

##
## Really build the distrib rpm

log "Updating rpm repo"
cd ../ceylon-rpm-repo

git checkout master 2>&1 >> $LOG_FILE || fail "checking out ceylon-rpm-repo master"
./new-version.sh $CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "checking out ceylon-rpm-repo master"

log "Building distrib rpm"
docker pull ceylon/ceylon-package-rpm 2>&1 >> $LOG_FILE || fail "Docker pull"
docker run -t --rm -v /tmp/ceylon:/output ceylon/ceylon-package-rpm $CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "Docker build rpm"

log "Publishing distrib rpm"
scp /tmp/ceylon/ceylon-${CEYLON_RELEASE_VERSION}-${CEYLON_RELEASE_VERSION}-0.noarch.rpm ceylon-lang.org:/var/www/downloads.ceylonlang/cli/ 2>&1 >> $LOG_FILE || fail "Uploading distrib rpm"

log "Building distrib repo"
docker pull ceylon/ceylon-repo-rpm 2>&1 >> $LOG_FILE || fail "Docker pull rpm repo"
docker run -ti --rm -v /tmp/ceylon:/output -v ~/.gnupg:/gnupg ceylon/ceylon-repo-rpm ${CEYLON_RELEASE_VERSION} 2>&1 | tee $LOG_FILE || fail "Docker build rpm repo"
rsync -rv /tmp/ceylon/{*.noarch.rpm,repodata} ceylon-lang.org:/var/www/downloads.ceylonlang/rpm/ 2>&1 >> $LOG_FILE || fail "Publish rpm repo"
