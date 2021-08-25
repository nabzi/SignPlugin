package org.saba

import org.gradle.api.Plugin
import org.gradle.api.Project

import java.nio.file.Paths

class SignPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def extension = project.extensions.create('sign_info', SignPluginExtension)
        project.tasks.create('set-sign-config-env') {
            doLast {
                println "KeyStore path--->" + "${extension.path} "
                setSignConfigEnv(extension.path, project)
            }
        }
        project.tasks.create('set-sign-config-prop') {
            doLast {
                println "KeyStore path--->" + "${extension.path} "
                setSignConfigProp( project)
            }
        }
    }

    void setSignConfigProp(Project project) {
        Properties props = new Properties()
        def userHome = Paths.get(System.getProperty('user.home'))
        println 'userHome:' + userHome
        def propFile = project.file(userHome.resolve('.gradle/signing_config.properties'))
        println 'propFile' + propFile
        if (propFile.canRead()) {
            props.load(new FileInputStream(propFile))
            if (props != null && props.containsKey('LEGACY_RELEASE_STORE_FILE') && props.containsKey('RELEASE_STORE_FILE')
                    && props.containsKey('RELEASE_STORE_PASSWORD') && props.containsKey('LEGACY_RELEASE_KEY_ALIAS') && props.containsKey('RELEASE_KEY_ALIAS') && props.containsKey('RELEASE_KEY_PASSWORD')) {
                def releaseStoreFile = project.file(props['RELEASE_STORE_FILE'])
                def releaseLegacyStoreFile = project.file(props['LEGACY_RELEASE_STORE_FILE'])
                println 'releaseStoreFile:' + releaseStoreFile + ' releaseLegacyStoreFile:' + releaseLegacyStoreFile
                addSignCredentials(project,
                        releaseStoreFile,
                        releaseLegacyStoreFile,
                        props['RELEASE_STORE_PASSWORD'] as String,
                        props['RELEASE_KEY_PASSWORD'] as String,
                        props['RELEASE_KEY_ALIAS'] as String,
                        props['LEGACY_RELEASE_KEY_ALIAS'] as String)
                return
            } else {
                println 'signing.properties found but some entries are missing'
                project.android.buildTypes.release.signingConfig = null
            }
        }
    }

    void setSignConfigEnv(String keyStorePath, Project project) {
        /*
        * Automates generation of Release APK
        * ./gradlew assembleRelease
        * */
        if (keyStorePath == null) {
            println 'keystore path can not be null'
            return
        }
       
        def keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        def storePassword = System.getenv("SIGNING_STORE_PASSWORD")

        def keyAlias = System.getenv("SIGNING_KEY_ALIAS")
        def keyAliasLegacy = System.getenv("SIGNING_KEY_ALIAS_LEGACY")

        println 'keyPassword' + keyPassword
        println 'storePassword' + storePassword
        println 'keyAlias' + keyAlias
        println 'keyAliasLegacy' + keyAliasLegacy

        if (keyPassword == null || storePassword == null || keyAlias == null || keyAliasLegacy == null) {
            println 'signing credentials not found in environment variables'
            project.android.buildTypes.release.signingConfig = null
            return
        }
        def allFilesFromDir = new File(keyStorePath).listFiles()
        if (allFilesFromDir == null) {
            println 'signFile is not available in path ' + keyStorePath
            return
        }
        println 'allFilesFromDir:' + allFilesFromDir.length
        def signFile = allFilesFromDir.first()
        println 'signFile.canRead:' + signFile.canRead()
        println 'signFile.exists:' + signFile.exists()
        addSignCredentials(
                project,
                signFile,
                signFile,
                storePassword,
                keyPassword,
                keyAlias,
                keyAliasLegacy
        )
    }
    def addSignCredentials(Project project,
                           File releaseKey,
                           File legacyReleaseKey,
                           String storePassword,
                           String keyPassword,
                           String keyAlias,
                           String keyAliasLegacy) {
        project.android.signingConfigs.release.storeFile = releaseKey
        project.android.signingConfigs.release.storePassword = storePassword
        project.android.signingConfigs.release.keyAlias = keyAlias
        project.android.signingConfigs.release.keyPassword = keyPassword

        project.android.signingConfigs.releaseLegacy.storeFile = legacyReleaseKey
        project.android.signingConfigs.releaseLegacy.storePassword = storePassword
        project.android.signingConfigs.releaseLegacy.keyAlias = keyAliasLegacy
        project.android.signingConfigs.releaseLegacy.keyPassword = keyPassword

        println(" set signing config successfully")
    }
}

class SignPluginExtension {
    def path = ""
}

