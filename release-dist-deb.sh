#!/bin/bash

. release-common.sh

##
## Really build the distrib deb

log "Updating deb repo"
cd ../ceylon-debian-repo

git checkout master 2>&1 >> $LOG_FILE || fail "checking out ceylon-debian-repo master"
./new-version.sh $CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "checking out ceylon-debian-repo master"

log "Building distrib deb"
docker pull ceylon/ceylon-package-deb 2>&1 >> $LOG_FILE || fail "Docker pull"
docker run -t --rm -v /tmp/ceylon:/output ceylon/ceylon-package-deb $CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "Docker build deb"

log "Publishing distrib deb"
scp /tmp/ceylon/ceylon-${CEYLON_RELEASE_VERSION}_${CEYLON_RELEASE_VERSION}-0_all.deb ceylon-lang.org:/var/www/downloads.ceylonlang/cli/ 2>&1 >> $LOG_FILE || fail "Uploading distrib deb"

log "Building distrib repo"
docker pull ceylon/ceylon-repo-deb 2>&1 >> $LOG_FILE || fail "Docker pull deb repo"
docker run -ti --rm -v /tmp/ceylon:/output -v ~/.gnupg:/gnupg ceylon/ceylon-repo-deb ${CEYLON_RELEASE_VERSION} 2>&1 | tee $LOG_FILE || fail "Docker build deb repo"
rsync -rv /tmp/ceylon/{db,dists,pool} ceylon-lang.org:/var/www/downloads.ceylonlang/apt/ 2>&1 >> $LOG_FILE || fail "Publish deb repo"
