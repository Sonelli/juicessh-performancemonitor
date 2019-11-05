#!/bin/bash

. release-common.sh

CEYLON_NEW_VERSION=$CEYLON_NEXT_VERSION
CEYLON_NEW_VERSION_MAJOR=$CEYLON_NEXT_VERSION_MAJOR
CEYLON_NEW_VERSION_MINOR=$CEYLON_NEXT_VERSION_MINOR
CEYLON_NEW_VERSION_RELEASE=$CEYLON_NEXT_VERSION_RELEASE
CEYLON_NEW_VERSION_QUALIFIER=$CEYLON_NEXT_VERSION_QUALIFIER
CEYLON_NEW_VERSION_PREFIXED_QUALIFIER=$CEYLON_NEXT_VERSION_PREFIXED_QUALIFIER
CEYLON_NEW_VERSION_OSGI_QUALIFIER=$CEYLON_NEXT_VERSION_OSGI_QUALIFIER
CEYLON_NEW_VERSION_NAME=$CEYLON_NEXT_VERSION_NAME

cd ../ceylon

log "Back to master"
git checkout master || fail "Git checkout master"

log "Replacing files"

replace common/src/org/eclipse/ceylon/common/Versions.java
replace language/src/ceylon/language/module.ceylon
replace language/src/ceylon/language/language.ceylon
replace language/test/process.ceylon
replace dist/samples/plugin/source/com/example/plugin/module.ceylon

perl -pi -e "s/ceylon\.version=.*/ceylon.version=$CEYLON_NEW_VERSION/" common-build.properties
perl -pi -e "s/ceylon\.osgi\.version=.*/ceylon.osgi.version=$CEYLON_NEW_VERSION_MAJOR.$CEYLON_NEW_VERSION_MINOR.$CEYLON_NEW_VERSION_RELEASE.osgi-$CEYLON_NEW_VERSION_OSGI_QUALIFIER/" common-build.properties

perl -pi -e "s/$CEYLON_RELEASE_VERSION_MAJOR\.$CEYLON_RELEASE_VERSION_MINOR\.$CEYLON_RELEASE_VERSION_RELEASE-SNAPSHOT _\"[^\"]+\"_/$CEYLON_NEW_VERSION _\"$CEYLON_NEW_VERSION_NAME\"_/" README.md dist/README.md
perl -pi -e "s/ceylon version $CEYLON_RELEASE_VERSION_MAJOR\.$CEYLON_RELEASE_VERSION_MINOR\.$CEYLON_RELEASE_VERSION_RELEASE-SNAPSHOT ([0-9a-f]+) \([^\)]+\)/ceylon version $CEYLON_NEW_VERSION \\1 \($CEYLON_NEW_VERSION_NAME\)/" README.md dist/README.md
perl -pi -e "s/$CEYLON_RELEASE_VERSION_MAJOR\.$CEYLON_RELEASE_VERSION_MINOR\.$CEYLON_RELEASE_VERSION_RELEASE-SNAPSHOT/$CEYLON_NEW_VERSION/" README.md dist/README.md
perl -pi -e "s/ceylon\.language-$CEYLON_RELEASE_VERSION_MAJOR\.$CEYLON_RELEASE_VERSION_MINOR\.$CEYLON_RELEASE_VERSION_RELEASE-SNAPSHOT/ceylon.language-$CEYLON_NEW_VERSION/" language/.classpath compiler-java/.classpath

while IFS='' read -r LINE || [[ -n "$LINE" ]]
do
    if [[ "$LINE" =~ '/*@NEW_VERSION_BINARY@*/' ]]
    then
        cat <<EOF
    public static final int V${CEYLON_RELEASE_VERSION_MAJOR}_${CEYLON_RELEASE_VERSION_MINOR}_${CEYLON_RELEASE_VERSION_RELEASE}_JVM_BINARY_MAJOR_VERSION = ${CEYLON_RELEASE_VERSION_JVM_BIN_MAJ};
    public static final int V${CEYLON_RELEASE_VERSION_MAJOR}_${CEYLON_RELEASE_VERSION_MINOR}_${CEYLON_RELEASE_VERSION_RELEASE}_JVM_BINARY_MINOR_VERSION = ${CEYLON_RELEASE_VERSION_JVM_BIN_MIN};

    public static final int V${CEYLON_RELEASE_VERSION_MAJOR}_${CEYLON_RELEASE_VERSION_MINOR}_${CEYLON_RELEASE_VERSION_RELEASE}_JS_BINARY_MAJOR_VERSION = ${CEYLON_RELEASE_VERSION_JS_BIN_MAJ};
    public static final int V${CEYLON_RELEASE_VERSION_MAJOR}_${CEYLON_RELEASE_VERSION_MINOR}_${CEYLON_RELEASE_VERSION_RELEASE}_JS_BINARY_MINOR_VERSION = ${CEYLON_RELEASE_VERSION_JS_BIN_MIN};

    /*@NEW_VERSION_BINARY@*/
EOF
    elif [[ "$LINE" =~ '/*@NEW_VERSION_JVM_VERSIONS@*/' ]]
    then
        cat <<EOF
            new VersionDetails("${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}", V${CEYLON_RELEASE_VERSION_MAJOR}_${CEYLON_RELEASE_VERSION_MINOR}_${CEYLON_RELEASE_VERSION_RELEASE}_JVM_BINARY_MAJOR_VERSION, V${CEYLON_RELEASE_VERSION_MAJOR}_${CEYLON_RELEASE_VERSION_MINOR}_${CEYLON_RELEASE_VERSION_RELEASE}_JVM_BINARY_MINOR_VERSION),
            /*@NEW_VERSION_JVM_VERSIONS@*/
EOF
    elif [[ "$LINE" =~ '/*@NEW_VERSION_JS_VERSIONS@*/' ]]
    then
        cat <<EOF
            new VersionDetails("${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}", V${CEYLON_RELEASE_VERSION_MAJOR}_${CEYLON_RELEASE_VERSION_MINOR}_${CEYLON_RELEASE_VERSION_RELEASE}_JS_BINARY_MAJOR_VERSION, V${CEYLON_RELEASE_VERSION_MAJOR}_${CEYLON_RELEASE_VERSION_MINOR}_${CEYLON_RELEASE_VERSION_RELEASE}_JS_BINARY_MINOR_VERSION),
            /*@NEW_VERSION_JS_VERSIONS@*/
EOF
    else
        echo "$LINE"
    fi
done < common/src/org/eclipse/ceylon/common/Versions.java > common/src/org/eclipse/ceylon/common/Versions.java2  || fail "Replace Versions.java"
mv common/src/org/eclipse/ceylon/common/Versions.java2 common/src/org/eclipse/ceylon/common/Versions.java  || fail "Move Versions.java"

while IFS='' read -r LINE || [[ -n "$LINE" ]]
do
    if [[ "$LINE" =~ '/*@NEW_VERSION@*/' ]]
    then
        cat <<EOF
                        } else if ("${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}".equals(depv)) {
                            depv = Versions.CEYLON_VERSION_NUMBER;
                        } /*@NEW_VERSION@*/
EOF
    else
        echo "$LINE"
    fi
done < compiler-js/src/main/java/org/eclipse/ceylon/compiler/js/loader/JsModuleSourceMapper.java > compiler-js/src/main/java/org/eclipse/ceylon/compiler/js/loader/JsModuleSourceMapper.java2  || fail "Replace JsModulesourceMapper"
mv compiler-js/src/main/java/org/eclipse/ceylon/compiler/js/loader/JsModuleSourceMapper.java2 compiler-js/src/main/java/org/eclipse/ceylon/compiler/js/loader/JsModuleSourceMapper.java  || fail "Move JsModulesourceMapper"

while IFS='' read -r LINE || [[ -n "$LINE" ]]
do
    if [[ "$LINE" =~ '<!-- @NEW_VERSION@ -->' ]]
    then
        cat <<EOF
        <!-- used for auto-upgrading at runtime for ${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE} -->
        <replace module="ceylon.bootstrap" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="ceylon.bootstrap" version="\${uv}" />
        </replace>
        <replace module="ceylon.language" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="ceylon.language" version="\${uv}" />
        </replace>
        <replace module="ceylon.runtime" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="ceylon.runtime" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.cli" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.cli" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.common" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.common" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.compiler.java" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.compiler.java" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.compiler.js" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.compiler.js" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.java.main" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.java.main" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.langtools.classfile" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.langtools.classfile" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.model" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.model" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.module-loader" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.module-loader" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.module-resolver" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.module-resolver" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.module-resolver-aether" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.module-resolver-aether" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.module-resolver-javascript" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.module-resolver-javascript" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.module-resolver-webdav" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.module-resolver-webdav" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.tool.provider" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.tool.provider" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.tools" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.tools" version="\${uv}" />
        </replace>
        <replace module="org.eclipse.ceylon.typechecker" version="${CEYLON_RELEASE_VERSION_MAJOR}.${CEYLON_RELEASE_VERSION_MINOR}.${CEYLON_RELEASE_VERSION_RELEASE}">
            <with module="org.eclipse.ceylon.typechecker" version="\${uv}" />
        </replace>

        <!-- @NEW_VERSION@ -->
EOF
    else
        echo "$LINE"
    fi
done < cmr/api/src/main/resources/org/eclipse/ceylon/cmr/api/dist-overrides.xml > cmr/api/src/main/resources/org/eclipse/ceylon/cmr/api/dist-overrides.xml2  || fail "Replace dist-overrides"
mv cmr/api/src/main/resources/org/eclipse/ceylon/cmr/api/dist-overrides.xml2 cmr/api/src/main/resources/org/eclipse/ceylon/cmr/api/dist-overrides.xml  || fail "Move dist-overrides"

find compiler-java/test/src/ -name '*.ceylon' -or -name '*.src' -or -name '*.properties' | xargs perl -pi -e "s/${CEYLON_RELEASE_VERSION_MAJOR}\.${CEYLON_RELEASE_VERSION_MINOR}\.${CEYLON_RELEASE_VERSION_RELEASE}-SNAPSHOT/${CEYLON_NEW_VERSION_MAJOR}.${CEYLON_NEW_VERSION_MINOR}.${CEYLON_NEW_VERSION_RELEASE}/g"

log "Committing"
git commit -a -m "Updated versions for work on $CEYLON_NEW_VERSION" 2>&1 >> $LOG_FILE  || fail "Git commit on master"
