package org.saba

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property

import java.nio.file.Paths


/**
 * This plugin helps automatic signing by loading signing credentials and setting it for android project gradle configurations.
 * It provides two gradle tasks for loading and setting project's "android.signingConfigs":
 *
 *     1 - gradle task 'set-sign-config-env'
 *          This task reads the four environment variables :
 *              - SIGNING_KEY_PASSWORD
 *              - SIGNING_STORE_PASSWORD
 *              - SIGNING_KEY_ALIAS
 *              - SIGNING_KEY_ALIAS_LEGACY
 *          and loads keystore files from the path provided in variable: $keyStorePath
 *          from project config 'sign-info'.
 *          When applying the plugin, you can set the configuration for keystore path by adding:
 *          sign_info {*                 keyStorePath = '[path]'
 *}*          in the gradle script.
 *
 *     2 - gradle task 'set-sign-config-prop'
 *          This task reads credentials from this file : '[User home]/.gradle/signing_config.properties'
 *          The file should contain following keys :
 *                   'LEGACY_RELEASE_STORE_FILE','RELEASE_STORE_FILE','RELEASE_STORE_PASSWORD',
 *                   'LEGACY_RELEASE_KEY_ALIAS','RELEASE_KEY_ALIAS','RELEASE_KEY_PASSWORD'
 *          The first two parameters are the path for keystore files.
 *
 *     For signing legacy release, add :
 *          releaseLegacy = true
 *     to  sign_info {} block
 * */
class SignPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def extension = project.extensions.create('sign_info', SignPluginExtension)
        def setSignConfigEnvTask = project.tasks.create('set-sign-config') {
            project.afterEvaluate {
                println "SignPlugin ***** KeyStore path--->  ${extension.keyStorePath.get()}  'has release legacy--->'  ${extension.releaseLegacy.get()} "
                if(extension.keyStorePath.get() == "") {
                    println "SignPlugin ***** signing by credentials from properties file"
                    setSignConfigProp(extension.releaseLegacy.get(), project)
                }else {
                    println "SignPlugin ***** signing by credentials from environment variables"
                    setSignConfigEnv(extension.keyStorePath.get(), extension.releaseLegacy.get(), project)
                }
            }
        }
    }

    void setSignConfigProp(Boolean releaseLegacy, Project project) {
        Properties props = new Properties()
        def userHome = Paths.get(System.getProperty('user.home'))
        println 'userHome:' + userHome
        def propFile = project.file(userHome.resolve('.gradle/signing_config.properties'))
        println 'propFile' + propFile
        if (!propFile.canRead()) {
            println 'Can not read signing.properties file'
            project.android.buildTypes.release.signingConfig = null
        }
        props.load(new FileInputStream(propFile))
        if (props != null &&
                props.containsKey('RELEASE_STORE_FILE') &&
                props.containsKey('RELEASE_STORE_PASSWORD') &&
                props.containsKey('RELEASE_KEY_ALIAS') &&
                props.containsKey('RELEASE_KEY_PASSWORD')) {
            def releaseStoreFile = project.file(props['RELEASE_STORE_FILE'])
            def releaseLegacyStoreFile = null
            def legacyKeyAlias = ""
            if (releaseLegacy) {
                if (!props.containsKey('LEGACY_RELEASE_KEY_ALIAS') ||
                        !props.containsKey('LEGACY_RELEASE_STORE_FILE')) {
                    println 'signing.properties found but some entries are missing'
                    project.android.buildTypes.release.signingConfig = null
                    return
                }
                releaseLegacyStoreFile = project.file(props['LEGACY_RELEASE_STORE_FILE'])
                legacyKeyAlias = props['LEGACY_RELEASE_KEY_ALIAS'] as String
            }
            println 'releaseStoreFile:' + releaseStoreFile + ' releaseLegacyStoreFile:' + releaseLegacyStoreFile
            addSignCredentials(project,
                    releaseStoreFile,
                    releaseLegacyStoreFile,
                    props['RELEASE_STORE_PASSWORD'] as String,
                    props['RELEASE_KEY_PASSWORD'] as String,
                    props['RELEASE_KEY_ALIAS'] as String,
                    legacyKeyAlias,
                    releaseLegacy
            )
        } else {
            println 'signing.properties found but some entries are missing'
            project.android.buildTypes.release.signingConfig = null
        }
    }

    void setSignConfigEnv(String keyStorePath, Boolean releaseLegacy, Project project) {
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

        if (keyPassword == null || storePassword == null || keyAlias == null || (releaseLegacy && keyAliasLegacy == null)) {
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
                keyAliasLegacy,
                releaseLegacy
        )
    }

    def addSignCredentials(Project project,
                           File releaseKey,
                           File legacyReleaseKey,
                           String storePassword,
                           String keyPassword,
                           String keyAlias,
                           String keyAliasLegacy,
                           Boolean releaseLegacy
    ) {
        project.android.signingConfigs.release.storeFile = releaseKey
        project.android.signingConfigs.release.storePassword = storePassword
        project.android.signingConfigs.release.keyAlias = keyAlias
        project.android.signingConfigs.release.keyPassword = keyPassword
        if (releaseLegacy) {
            project.android.signingConfigs.releaseLegacy.storeFile = legacyReleaseKey
            project.android.signingConfigs.releaseLegacy.storePassword = storePassword
            project.android.signingConfigs.releaseLegacy.keyAlias = keyAliasLegacy
            project.android.signingConfigs.releaseLegacy.keyPassword = keyPassword
        }
        println(" set signing config successfully")
    }

}

abstract class SignPluginExtension {
    abstract Property<String> getKeyStorePath()
    abstract Property<Boolean> getReleaseLegacy()

    SignPluginExtension() {
        keyStorePath.convention("")
        releaseLegacy.convention(false)
    }
}

