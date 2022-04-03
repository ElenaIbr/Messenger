# Messenger

[Download APK](https://github.com/ElenaIbr/Messenger/raw/master/app-debug.apk)

This mobile web app is the software solution for communication, or just messenger like Whats`app, Viber, Telegram and so on. 
I have been trying to realize main functionality and cover MVP requirements.
Of course, there is still a lot of work, but the app does its main function, 
giving opportunity to send and get messages from other users. What i have used to achieve this purpose:
1. realasing application within single activity (1 activity and 7 fragments)
2. using Firebase tools (auth, realdatabase)
3. using FCM and Retrofit2 for push-notifications

Let's see how it works and looks like.
First, when new (or unlogged) user launches app he sees auth fragment on his mobile screen. 
App asks for mobile number and send verification code to users mobile phone. 
After that user inputs auth code and can see main fragment (chat list). 

<img width="220" alt="Снимок экрана 2022-04-03 в 16 04 29" src="https://user-images.githubusercontent.com/87421176/161432689-98cabcfe-4118-49fd-bfca-30042d66c843.jpg"> <img width="220" alt="Снимок экрана 2022-04-03 в 16 04 29" src="https://user-images.githubusercontent.com/87421176/161432690-d7f4671d-66bb-4f38-804c-12854a945303.jpg"> <img width="220" alt="Снимок экрана 2022-04-03 в 16 04 29" src="https://user-images.githubusercontent.com/87421176/161432692-adf35b35-c5a8-4dd0-97a7-aeb5597878a3.jpg">

<img width="220" alt="Снимок экрана 2022-04-03 в 18 34 44" src="https://user-images.githubusercontent.com/87421176/161438354-2ed32549-1de2-4cf2-a496-02d0a85b4ff3.jpg"> <img width="220" alt="Снимок экрана 2022-04-03 в 16 04 29" src="https://user-images.githubusercontent.com/87421176/161432694-fe0ef623-245a-4b86-9261-89bc87f29ad0.jpg">

What is going on in code. First of all, we have to make sure, that user is not logged. After Firebase instances initialization we call checkAuthorization() and check if there is current user UID. If there is UID, user can interact with his chat list.
If there is no current  UID, user has to input his number and log in. 
In the same time, if user launches messenger on his device for the first time, app asks permissions for contacts reading. After that in CoroutineScope app compares user`s contacts with realtime database data and saves matches in List. We need this information to name other users, we have in database and contact list.

Below we can look at main UI. I used bottomnavigationview with three items (Contacts, Chats and Profile). For contact list i used FirebaseRecyclerOptions.Builder and its setQuery(), for chat list - usual adapter.

Actual version:

![](https://raw.githubusercontent.com/ElenaIbr/Messenger/master/demo_2.gif)

And finally, users communication from two different physical devices, using real phone number. Time on devices was synchronized.

Actual version:

| ![](https://raw.githubusercontent.com/ElenaIbr/Messenger/master/demo_4.gif) | 
![](https://raw.githubusercontent.com/ElenaIbr/Messenger/master/demo_3.gif) |


Database structure, nodes

![](https://raw.githubusercontent.com/ElenaIbr/Messenger/master/dataabase.jpg)

## Tech

Application was created in Android Studio 4.2.1.

    defaultConfig {
        applicationId "com.example.messengerapplication"
        minSdkVersion 21
        targetSdkVersion 30
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

## Help

If you have got any questions please let me know ibraeva.elen@gmail.com



