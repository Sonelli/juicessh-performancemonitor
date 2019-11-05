#! /bin/bash

# jdk-diff.sh has produced two .diff files for each component, and
# jdk-copy.sh has copied the $NEW sources into the right source directories
#
# We now:
#
# 1. Apply the patches produced by jdk-diff.sh
# 2. Rename the javac packages to the ceylon package names


# source some common stuff used by this and the patch script
. $(dirname $0)/renames

# Get a copy of the new tree if it doesn't exist
if [ ! -e "$NEW" ]; then 
  echo "There is no $NEW, jdk-copy.sh should have created this. Something's wrong."
  echo "Exiting."
  exit 1
fi

# Apply the diff 
echo "Patching compiler-java"
patch -p1 -d compiler-java/langtools -N < compiler-java.diff

echo "Patching langtools-classfile"
patch -p1 -d langtools-classfile/src -N < langtools-classfile.diff

echo "Package renaming"
# Now do the reverse rename within the JDK tree
cd compiler-java
renamePackage langtools/src/share/classes \
  javax.annotation.processing \
  org.eclipse.ceylon.javax.annotation.processing \
  rev_renames

renamePackage langtools/src/share/classes \
  javax.lang.model \
  org.eclipse.ceylon.javax.lang.model \
  rev_renames
  
renamePackage langtools/src/share/classes \
  javax.tools \
  org.eclipse.ceylon.javax.tools \
  rev_renames
  
renamePackage langtools/src/share/classes \
  com.sun.source \
  org.eclipse.ceylon.langtools.source \
  rev_renames
  
renamePackage langtools/src/share/classes \
  com.sun.tools.javac \
  org.eclipse.ceylon.langtools.tools.javac \
  rev_renames

cd ..

cd langtools-classfile
renamePackage ./src \
  com.sun.tools.classfile \
  org.eclipse.ceylon.langtools.classfile \
  rev_renames

cd ..

echo "Now the tedious bit: You need to review all the rejected patches."
echo "Look for .rej files in the source trees."
echo "Many of these will be just imports"
echo "Good luck."
