# SignPlugin

 This plugin helps automatic signing by loading signing credentials and setting it for android project gradle configurations.
 It provides a gradle task for loading and setting project's "android.signingConfigs":
 **gradle task 'set-sign-config'**
    This tasks works in two modes : 
    
**1 - available keyStore path** 

If keystore path parameter is available in variable: $keyStorePath
from project config 'sign-info', it reads the four environment variables : 
    
        - SIGNING_KEY_PASSWORD
        - SIGNING_STORE_PASSWORD
        - SIGNING_KEY_ALIAS
        - SIGNING_KEY_ALIAS_LEGACY
    
    
and loads keystore files from the path provided .


   **2 - unavailable keyStore path**
   
If no keyStorePath value is set, it reads credentials from properties file located in the following path:
 
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
        signInfo {
            keyStorePath = '/tmp/'
            releaseLegacy = true
        }
- keyStorePath <String> : Keystore path used when signing using credentials from environment variables
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
  
   
  
  - The task is executed after project configuration phase (afterEvaluate phase)

  