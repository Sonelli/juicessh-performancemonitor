#! /bin/bash

# jdk-diff has produced two .diff files for each component. This script will
# copy the $NEW JDK files into the Ceylon source tree. 
# The source tree will be left in a non-compilable state, with the
# pristine JDK packages (using JDK package names) in the right place.




# source some common stuff used by this and the patch script
. $(dirname $0)/renames

# Get a copy of the new tree if it doesn't exist
if [ ! -e "$NEW" ]; then 
  hg clone "$NEW_URL" "$NEW"
fi

ln -s "$NEW" "compiler-java/$NEW"

git rm -r compiler-java/langtools/src/share/classes/
git rm -r langtools-classfile/src

## Copy packages
for d in javax/annotation/processing javax/lang/model javax/tools com/sun/source com/sun/tools/javac 
do
  mkdir -p $(dirname compiler-java/langtools/src/share/classes/$d)
  cp -r $NEW/src/share/classes/$d $(dirname compiler-java/langtools/src/share/classes/$d)
done

for d in com/sun/tools/classfile 
do
  mkdir -p $(dirname langtools-classfile/src/$d)
  cp -r $NEW/src/share/classes/$d $(dirname langtools-classfile/src/$d)
done

# COMMIT
git add compiler-java/langtools/src/share/classes/
git add langtools-classfile/src

echo "Now do a git status and make a commit. Then run jdk-patch.sh"



