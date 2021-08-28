# SignPlugin

 This plugin helps automatic signing by loading signing credentials and setting it for android project gradle configurations.
 It provides two gradle tasks for loading and setting project's "android.signingConfigs":
 
   **1 - gradle task 'set-sign-config-env'**

   This task reads the four environment variables :
   
    
        - SIGNING_KEY_PASSWORD
        - SIGNING_STORE_PASSWORD
        - SIGNING_KEY_ALIAS
        - SIGNING_KEY_ALIAS_LEGACY
    
    
and loads keystore files from the path provided in variable: $keyStorePath
from project config 'sign-info'.


   **2 - gradle task 'set-sign-config-prop'**
   
This task reads credentials from properties file located in the following path:
 
_'[User home]/.gradle/signing_config.properties'`_

Properties file should contain the following keys :
       
       
    'LEGACY_RELEASE_STORE_FILE',
    'RELEASE_STORE_FILE',
    'RELEASE_STORE_PASSWORD',
    'LEGACY_RELEASE_KEY_ALIAS',
    'RELEASE_KEY_ALIAS',
    'RELEASE_KEY_PASSWORD'

The first two parameters are the path for keystore files.  


**Configuration parameters :**

Parameter values should be provided in 'sign_info' block

    [ in module build.gradle ]
        apply plugin: 'org.saba.sign'
        sign_info {
            keyStorePath = '/tmp/'
            releaseLegacy = true
            env = true
        }
- keyStorePath <String> : Keystore path used when signing in 'env = true' mode
- env          <Boolen> : Sign using credentials provided in environment variables.[Default value = true]
- prop         <Boolen> : Sign using credentials provided in properties file [Default value = false]
- releaseLegacy<Boolen> : Also sign in legacy mode [Default value = false]



       
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
  
   
  
  - Now you can test it by running any of the two tasks :
       
        gradlew set-sign-config-env
        gradlew set-sign-config-prop
       
  - The tasks are added to all module 'release' build variants when applying the plugin
   

  