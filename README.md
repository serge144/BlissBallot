# BlissBallot
Repository for recruitment purposes


Author: Sérgio Pimentel

#### Application
The signed application file (.apk) can be found on the /app/release folder for a quick check to see how the application behaves on a real smartphone. 

#### Android Studio
The application contains many Logs over the code that helps understand what's being processed and what requests are being made to the API.
The Logs have all the same tag: "APP_DEBUG".

1. First load the project to Android Studio by either downloading the project or using Android Studio's "check out project from Version Control" option.
2. Once the project is set up, open the Logcat view.
  * Filter the console's output by "Debug"
  * On the same Logcat view, on the top right corner, select the dropdown menu and select "Edit Filter Configuration"
  * Create a tag with "APP_DEBUG"
3. Run the application with your AVD or plug in your smarthphone.
4. Check the Logcat's output for the application log.
 
###To do

1. Unfortunately, i built the application using Android Studio 3.2.1 which doesn't match with the Non-functional requirement specification (NREQ-02) which is 3.0.1. With this, newer versions of Android Studio requires newer versions of gradle, and as such, a minimum of gradle 4.6 is required. When setting to 4.1 the following message was "Minimum support gradle is 4.6, using 4.1" as mentioned in the third answer of this link: https://stackoverflow.com/questions/43077386/gradle-error-minimum-supported-gradle-version-is-3-3-current-version-is-3-2/43078202.

2. Although i implemented the FREQ-02 URL functionality, i don't think i fully understood the following sentence of the FREQ-02 (Regarding the URL format for the Questions List Screen): "Notice that this format contains a query parameter which should be used to fill the search box and trigger the search functionality. If the question_filter parameter is missing the user should simply be placed at the listing. If the question_filter parameter is present but has an empty value the the user should be placed at the filter variant with no input inserted but with the input box focused".
