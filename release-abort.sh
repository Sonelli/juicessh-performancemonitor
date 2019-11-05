#!/bin/bash

. release-common.sh

git_reset() {
  git checkout master
  git branch -D version-$CEYLON_RELEASE_VERSION
  git tag -d $CEYLON_RELEASE_VERSION
  git push origin :version-$CEYLON_RELEASE_VERSION
  git push origin :$CEYLON_RELEASE_VERSION
  git reset --hard $CEYLON_BRANCHING_TAG
}


cd ../ceylon
git_reset

cd ../ceylon-sdk
git_reset

cd ../ceylon-debian-repo
git_reset

cd ../ceylon-rpm-repo
git_reset

cd ../ceylon.formatter
git_reset

cd ../ceylon.tool.converter.java2ceylon
git_reset

cd ../ceylon-ide-common
git_reset

cd ../ceylon
