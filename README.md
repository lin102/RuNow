
MOBILE CARTOGRAPHY PROJECT -- RUNow
Group Members : Lin Che, Zhenyu Wei, Yingwen Deng, Vreni Preußler

RUNow is a Java-based running application for Android. It is designed to help the user to track and record runs and give them information about various aspects of the run like distance, pace, duration and consumed calories. RUNow also saves completed runs records and allows the user to look back at the recorded data from database and also can delete any record or the whole table. Therefore, it is designed for a various group of users from all age groups with Android devices, interested in running or fitness in general who want to keep track of their runs and personal performance development (concerning the user privacy the data will be only stored in local). It aims to allow the user to run more frequently, better and healthier and safer.
A more detailed description of RUNow, it’s structure and functionality are explained in the following.

LOGO ICON
The symbol is a running person which represent the core theme of this app.
The green color schema shows a healthy running life style in the nature.
                                                                                  

Splash screen
The starting page shows the app logo and the name in the middle of the page. It introduces the colour concept of the app and leads directly to the main page of the application. At the bottom of the page, copyright is remarked.


MAIN ACTIVITY
At the top of the main activity, the switch button —“Theme” for different themes - day and night mode is placed on the left corner which user can choose freely while running. Next to it in the right upper corner, a button called “History” that leads to another page with the records of runs which user can manipulate.
The main activity of the application consists mostly of a map fragment that shows the current location on a Google Maps Interface while running. During the run a multi-coloured line shows the track depending on the pace of runners.The line symbolises the pace of the run in three colours (red – yellow – green) which is corresponding to three different ranges of pace, red represents slow and green represent fast.
Below the map there is a dashboard with the information that are gathered while the user is running. The app is controlled via three buttons at the bottom of the page.
Via “start” button the new running activity is started. While the user is running, certain variables are gathered and displayed below the map fragment in respective TextViews, like distance, pace, duration of the run and burned calories. The logo turns into a “pause” button as soon as an activity is started and stops the tracking of the run and the calculation of the variables momentarily until the run is continued via the same button means “resume” now. The “stop” button ends the recording of the run and adds the tracked run to the run history(the local database), along with all calculated variables. 

THEME SWITCH
In this application we provide the day mode and night mode for better user experience during different lighting situation by toggling the switch. With the day mode, the UI is themed in a lighter colour tone of matching colour scheme as well as a customised styled map which is less detailed but actually tailored to the usage of runners during running. For example, it enhances the streets/parks and marked out the public transportation stations/highways which could be useful information for runners. With the night mode, the UI is themed in a darker colour tone as well as the google map with matching colour scheme to the entire design. 

RECORD PAGE
The button with “memory” icon on the top right corner in the main activity of the app leads to the record page. Via the “BACK” button, which is left of the “trash” button, the user is redirected to the main page. And via the “trash” button, you can clear all of the records (the whole table) at once. In addition, it also enables users to delete single entries with the “delete” button on the beginning of each row. On the record page, the run entries with time and calculated data including the ID for each entires, distances and consumed calories are listed in the main TextView.

POINTS OF INTEREST (Hidden surprise)
In this running application, we also include an internal function of pop-out points of interest with placeholders when the users approach some certain feature objects while running.
The places are: 1. Alte Mensa, 2. SLUB and 3. Hülsse-Bau. 
The threshold is set to 30m which means when runner approach the above places within 30m the Icon will pop up as a hidden surprise of this app.
This internal function of exploring points of interest spices up the running experience in a surprising way.
 
 
