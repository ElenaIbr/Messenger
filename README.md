# Messenger

Hi everyone!
I would like to describe my mobile web app. 
This app is the solution for communication, or just messenger like Whats`app, Viber, Telegram and so on. 
I have been trying to realize main functionality and cover MVP requirements.
Of course, there is still a lot of work, but the app does its main function, 
giving opportunity to send and get messages from other users. What i used to achieve this purpose:
1. realasing application within single activity (1 activity and 7 fragments)
2. using Firebase tools (auth, database)
3. using FCM and Retrofit2 for push-notifications

Let's see how it works and looks like.
First, when user launches app he  sees auth fragment on his mobile screen. 
App asks for mobile number and send verification code to users mobile phone. 
After that user inputs auth code and can see main fragment (chat list). 

![](https://raw.githubusercontent.com/ElenaIbr/Messenger/master/auth_demo.gif)

What is going on in code. First off all, we have to make shure, that user is not logged. In onResume(), 
after Firebase instances initialization we call checkAuthorization() and check if there is current user UID. If there is UID, user can interact with his chat list.
If there is no cuurent  UID, user has to input his number and log in. 
In the same time, if user launches messenger on his device for the first time, app asks permissions for contact reading and after that saves contact list in CoroutineScope. We need this indormation to name others users, we have in database and contact list.

