#!/bin/bash

. release-common.sh

##
## Really build the distrib sdk

cd ../ceylon

log "Building distrib sdk"
docker pull ceylon/ceylon-build-sdk 2>&1 >> $LOG_FILE || fail "Docker pull"
docker run -t --rm -v /tmp/ceylon:/output ceylon/ceylon-build-sdk $CEYLON_RELEASE_VERSION 2>&1 >> $LOG_FILE || fail "Docker build SDK"

log "Creating Herd upload"
echo -n "Enter Herd user name: "
read HERD_USER
echo -n "Enter Herd password: "
read -s HERD_PASS
echo

COOKIES=`tempfile`

curl --data "username=$HERD_USER&password=$HERD_PASS&authenticityToken=$HERD_TOKEN" --cookie $COOKIES --cookie-jar $COOKIES https://herd.ceylon-lang.org/login
HERD_TOKEN=`curl --silent --cookie $COOKIES --cookie-jar $COOKIES https://herd.ceylon-lang.org/uploads | grep -m 1 authenticityToken | sed -E "s/.*name=\"authenticityToken\" value=\"([^\"]+)\".*/\\1/g"`
HERD_UPLOAD=`curl --silent -D - --data "authenticityToken=$HERD_TOKEN" --cookie $COOKIES https://herd.ceylon-lang.org/uploads/new|grep Location|perl -pe 's/^Location: //; s|//herd\.|//modules.|; s/\\r//g'`
rm $COOKIES

echo Token is $HERD_TOKEN 
echo URL is $HERD_UPLOAD/repo/

docker pull ceylon/ceylon-publish
docker run -t --rm -v /tmp/ceylon:/output ceylon/ceylon-publish $CEYLON_RELEASE_VERSION $HERD_UPLOAD/repo/ $HERD_USER $HERD_PASS
