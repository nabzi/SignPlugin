# SignPlugin

 This plugin helps automatic signing by loading signing credentials and setting it for android project gradle configurations.
 It provides two gradle tasks for loading and setting project's "android.signingConfigs":
 
   1 - gradle task 'set-sign-config-env'
   
        This task reads the four environment variables :
            - SIGNING_KEY_PASSWORD
            - SIGNING_STORE_PASSWORD
            - SIGNING_KEY_ALIAS
            - SIGNING_KEY_ALIAS_LEGACY
            
        and loads keystore files from the path provided in variable: $keyStorePath
        from project config 'sign-info'.
        
        When applying the plugin, you can set the configuration for keystore path by adding:
        
        sign_info {
              keyStorePath = '[path]'
         }
        
        in the gradle script.

   2 - gradle task 'set-sign-config-prop'
       
       This task reads credentials from this file : '[User home]/.gradle/signing_config.properties'
        
       The file should contain following keys :
                 'LEGACY_RELEASE_STORE_FILE','RELEASE_STORE_FILE','RELEASE_STORE_PASSWORD',
                 'LEGACY_RELEASE_KEY_ALIAS','RELEASE_KEY_ALIAS','RELEASE_KEY_PASSWORD'
       
       The first two parameters are the path for keystore files.
       
# Usage in android project : 

  - Add plugin jar file to /libraries folder
  - Add this:
        
        flatDir dirs: 'libraries'
        
    to project level build.gradle , 'buildscript>repositories' section 
  - Add this:
        
        classpath group: 'org.saba', name: 'SignPlugin', version: '1.0'
        
     to project level build.gradle , 'buildscript>dependencies' section 
  
  - Add  to module level build.gradle :
       
        apply plugin: 'org.saba.sign'
  
    If you want to use 'set-sign-config-env', Add this too:
  
        sign_info {
          keyStorePath = '/tmp/' //replace this with your keystore path
        }
  
 - Now you can run any of the two tasks :
       
       gradlew set-sign-config-env
       gradlew set-sign-config-prop
       
       
