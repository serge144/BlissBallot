# BlissBallot
Repository for recruitment purposes


Author: Sérgio Pimentel

#### Application
The signed application file (.apk) can be found on the /app/release folder for a quick check to see how the application behaves on a real smartphone. 

#### Android Studio
The application contains many Logs over the code that helps understand what's being processed and what requests are being made to the API.
The Logs have all the same tag: "APP_DEBUG".

1. First load the project to Android Studio by either downloading the project or using Android Studio's "check out project from Version Control" option.
2. Unfortunately, i built the application using Android Studio 3.2.1 which doesn't match with the Non-functional requirement specification (NREQ-02) which is 3.0.1. With this, newer versions of Android Studio requires newer versions of gradle, and as such, a minimum of gradle 4.6 is required. When setting to 4.1 the following message was "Minimum support gradle is 4.6, using 4.1" as mentioned in the third answer of this link: https://stackoverflow.com/questions/43077386/gradle-error-minimum-supported-gradle-version-is-3-3-current-version-is-3-2/43078202
3. Once the project is set up, open the Logcat view.
  * Filter the console's output by "Debug"
  * On the same Logcat view, on the top right corner, select the dropdown menu and select "Edit Filter Configuration"
  * Create a tag with "APP_DEBUG"
4. Run the application with your AVD or plug in your smarthphone.
5. Check the Logcat's output for the application log.
 
