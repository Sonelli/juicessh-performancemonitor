#! /bin/bash

# Creates a couple of .diff files of our changes since the 
# last JDK javac rebase.
# Running this script will:
#
# * Clone the OLD mercurial repo defined in `renames`
# * Create a couple of of .diff files
# * Revert the git working tree (so start clean!)
#
# It will not make any git commits

#set -x

# source some common stuff used by this and the patch script
. $(dirname $0)/renames

DIFF_FLAGS=-aurwB

# run from the git repo root
cd $(git rev-parse --show-toplevel)

# Get a copy of the old tree if it doesn't exist
if [ ! -e "$OLD" ]; then 
  hg clone "$OLD_URL" "$OLD"
fi

# In ceylon-compiler
cd compiler-java

# Add a symlink to the old javac source tree
# (so that the diff is done on sibling directories, since patch needs that)
ln -s "../$OLD" "$OLD"

# Rename packages to where the JDK expects them
renamePackage langtools/src/share/classes \
  org.eclipse.ceylon.javax.annotation.processing \
  javax.annotation.processing \
  renames

renamePackage langtools/src/share/classes \
  org.eclipse.ceylon.javax.lang.model \
  javax.lang.model \
  renames
  
renamePackage langtools/src/share/classes \
  org.eclipse.ceylon.javax.tools \
  javax.tools \
  renames
  
renamePackage langtools/src/share/classes \
  org.eclipse.ceylon.langtools.source \
  com.sun.source \
  renames
  
renamePackage langtools/src/share/classes \
  org.eclipse.ceylon.langtools.tools.javac \
  com.sun.tools.javac \
  renames

echo "Creating diff for compiler-java"
diff $DIFF_FLAGS "${OLD}" langtools | grep -v '^Only in langtools-7' > ../compiler-java.diff

# So we got a patch file, but we've also screwed up the working tree
# (it's now using javac package names). Use git to revert
git checkout -- .
# This will also delete the link we created
git clean -f -d

cd ..

# Now the same in langtools-classfile
cd langtools-classfile

# Add a symlink to the old javac source tree
# (so that the diff is done on sibling directories, since patch needs that)
ln -s "../$OLD/src/share/classes" "src-old"

renamePackage ./src \
  org.eclipse.ceylon.langtools.classfile \
  com.sun.tools.classfile \
  renames
  
echo "Creating diff for langtools-classfile"
diff $DIFF_FLAGS "src-old" src | grep -v '^Only in src-old' > ../langtools-classfile.diff

git checkout -- .
# This will also delete the link we created
git clean -f -d

cd ..

echo "Now run jdk-copy.sh"

#set +x
