# Training Assistant

## Introduction

This application was created to help myself organize and program for my clients. Now I hope this can be used my others to help improve the efficiency and effectiveness of their work. The following are rules and practices that the app follows when deciding how to schedule and setup client sessions. I tried to leave as much of the practices up to the user but as you see below, some choices have already been made for you. I am sorry but some things cannot be allowed if I wanted to make this app easier for myself.

## Input Fields

All fields or boxes that the user inputs information must be checked to make sure the input data is valid. This means that the input value must not be blank and some characters are not allowed such as:

- Commas
- Underscores
- Semicolons
- Apostrophes
- Double Quote Marks


## Client

### Overview

Clients are used to contain the information pertaining to a single client. They contain:

- id, unique to the client
- name, unique to the client
- schedule type, see schedule types below in classification
- days, list of days the client is scheduled if their schedule is constant (if not constant, it will be the number of weekly or monthly session allotted to the client)
- times, list of times the client is scheduled (correlating with their days) if their schedule is constant (if not constant, it will be 0)
- durations, list of durations the client is scheduled (correlating with their days) if their schedule is constant (if not constant, it will be a single default session duration)
- start date, will be zero if "No Schedule" type is chosen
- end date, will be zero if "No Schedule" type is chosen

### Classification

There are only 4 types of schedules:

1. Weekly Constant – client has a regularly defined schedule
2. Weekly Variable – client has no regular schedule but a weekly quota
3. Monthly Variable – client has no regular schedule but a monthly quota
4. No Schedule - client has no regular schedule (days, times, start date and end date = 0)

This classification will guide how sessions are created and how the client will be tracked and edited.

### Creation

Enter client name and choose schedule type. Input fields will change depending on the schedule type. Enter all required input fields visible to complete Client creation. See "Input Fields" to make sure name field input is valid.


## Joint

Static list of joints:
1. Ankle
2. Hip
3. Knee
4. Lumbar Spine
5. Neck
6. SI (Sacroiliac)
7. Shoulder
8. Thoracic Spine
9. Wrist


## Muscle

### Overview

Muscles are used to hold the information about a muscle:

- id, unique to the muscle
- name, unique to the muscle

### Creation

Enter name of muscle.  See "Input Fields" to make sure name field input is valid. Checks if to make sure name is unique

### Deletion

Application will check if the Muscle is used to describe an exercise. If it is used, the deletion will be stopped and the user will be prompted to remove exercises that contain the muscle.


## Exercise

### Overview

Exercises are used inside a Session to describe the movements performed (or to be performed) during the session. They contain:

- id, unique to the exercise
- name, unique to the exercise
- exercise type, see types below in classification
- primary mover, Muscle or Joint as the primary focus of the exercise
- secondary movers, list of Muscle(s) or Joint(s) as the secondary focus(es) of the exercise  

### Classification

There are only 3 types of exercises:

1. Strength – Muscles driven (Strength or Hypertrophy exercises)
2. Mobility – Joint Driven
3. Stability – Joint Driven
  
This makes it easier to classify and limit over complication of the data to eliminate the chances of a user getting an error when reading or writing data. At first, I considered just having Strength exercises since most trainers focus primarily on the strength of their clients. However, after consultations with other trainers, it became clear that some exercises are not focused on strength but mobility and stability of a joint depending on the needs of a client. Follow this logic to categorize your exercises:

- If the exercise is driven by the strength or hypertrophy of a given muscle(s), choose strength
- If the exercise is driven by the range of motion or strength of a joint(s), choose mobility or stability

When characterizing an exercise, there is a primary focus and secondary focus(es). To simplify the data collection, an exercise can only have 1 primary focus, but it can have as many secondary focuses as needed within the collection of muscles and joints provided. For example, a Hip Thrust would have a primary focus of the Gluteus Maxiumus and possibly a secondary focus of the Hamstrings and Spinal Erectors. However, be for warned that if you choose “Strength” as the exercise type, the choices for primary and secondary focuses are limited to muscles and the same if true if you choose “Mobility” or “Stability” with respect to joints.

### Creation

Enter exercise name and exercise type. Once type is chosen, the primary movers list will be filled with the appropriate data. Once a primary mover is selected, the secondary mover list will be filled will all the possible movers with the primary mover selected omitted (to remove redundant secondary mover information). See "Input Fields" to make sure name field input is valid. Checks to make sure name is unique.

### Deletion

Application will check if the Exercise is used to describe a session. If it is used, the session log entry will have the exercise removed and the information for that exercise added to the notes section


## Session

### Overview

Sessions are the base unit for a day's schedule. They contain:

- client ID of the session holder
- name of the client holder
- the date and time of the session
- List of exercises as well as their sets, reps, resistance and order in the session
- notes about the session
- duration of the session

Sessions are used to populate a day in the schedule and provide data about a client's history.

### Limits

Only 1 session is allowed per day per client. Allowing for more makes checking for session conflicts much more challenging. If you want to add another session for a client to a single date (although I would not recommend that for 90% of clients) and you don’t need to be tracked as rigorously (i.e. cardio)  you can add it in the logged sessions notes. If you need the extra session’s exercises to be tracked add that to the exercises completed in the logged session.

A exercises can only appear once per session. If the repeat exercise needs to be tracked (I’m not sure why someone would want to repeat and exercise later in a session though) you need to add it to the existing exercise entry and add the volume (If the weight or reps change then sorry, you cannot log that). If you don’t need to track the repeat exercise than add it to the session notes.

Max session duration is 120 minutes. This covers 99% of people and if you’re still with a client after 2 hours that is one long ass session. If you need more time, don’t worry about it just enter your session data as per usual. The overall time constraint is just a number that fits most people and eliminates the chance of a user inputting a ridiculously long session duration.


## Operation

### Daily Schedule

Select a date in the calendar to have the list to the right populated with all sessions found on that date. Any client with a constant schedule will have their scheduled future sessions shown (as long as you have not made a change to that scheduled session) even though nothing has been added to the session yet. All other session present have been added from a variable or non-schedule client and are one time events

The list of session can be interacted with to add, edit or remove sessions from the selected date. Any sessions that conflict with another session will be red. Clicking the add button in the bottom right corner opens a prompt to add a session to the list. You will be given a list of clients that are able to add a session on the chosen date and be able to choose a time and duration. The clients available are chosen based upon their schedule type:

- Constant Schedule - If the client has cancelled on a regular session, they are able to add a makeup session
- Variable Schedule - If the client has available weekly/monthly sessions or they have makeup sessions available
- No Schedule - All clients are available to add sessions

Click any session in the list to proceed with editing that session in another window. If you need to cancel a session found in the list, hold down the session and a prompt will appear to confirm the action.

### Session Editor

When a session found in your daily schedule is clicked you enter this window and can edit all attributes of the session except for the client information. If the session chosen has data already associated with it, that data will be loaded to the appropriate field and be made accessible for you to edit. The various buttons provide you the ability to change the following attributes (by button name):

- Change Duration - dialog to input a new duration (must be within 1-120mins)
- Change Date - dialog to input a new date from a calendar
- Change Time - dialog to input a new time from a clock
- Add Exercise - dialog to choose a new Exercise to add. See below for more info
- Confirm - confirms any changes made to the session

When attempting to add a new exercise to a session, the name input field allows you to search by exercise name, primary mover type and exercise type. When you see the desired exercise in the list click it to make sure you have a valid exercise chosen (the application will reject any name not associated with a logged exercise).

You can also edit or delete an exercise from the session by clicking or holding down on an exercise in the exercise list. Editing will allow you to change all the the attributes except for the exercise name chosen. When attempting to remove an exercise, you will be prompted to confirm before proceeding.

Notes can also be added/edited for the session. These notes cannot be tracked within 'Client Stats' but they can be useful to remember things about the session or client. The notes will also contain a reference to any exercise removed from the library that was once performed during this session.

### Clients

Clients are displayed in list format and each client row shows the important information about the client. To edit the client, you can click the row and a windows will appear with the current client info displayed. See 'Add/Edit Client' for more details. To add a new client, click the add button in the bottom right corner. A window will appear with default values selected. Add client information to confirm a new client and follow the rules about Clients. See 'Add/Edit Client' and 'Client' for more information. To remove a client, long hold the client's row and a prompt will ask you to confirm deletion of a client.

### Add/Edit Client

When you click the add button or click a row within the clients list, you will be sent to window to add or edit a client. The operations are very similar so I figured it would be easier for me if I put them together. When creating or editing a client with a normal schedule, only your normal schedule (ie clients with normal schedules) will be checked for conflicts. All sessions outside of your normal schedule clients will not be checked, hence you might find conflicts with variable sessions in your schedule. You will have to deal with those but the Schedule section will turn conflicting session red to denote a conflict. When creating or editing a non-constant schedule client, no conflicts are checked except for the clients name. However, the client is still checked to make sure the number of variables sessions falls within acceptable values (1-7 for weekly, 1-28 monthly) and/or the default duration is valid (1-120mins).

### Exercises

Exercises are displayed in list format and each exercise row shows the important information about the exercise. To edit the exercise, you can click the row and a windows will appear with the current exercise info displayed. See 'Add/Edit Exercise' for more details. To add a new exercise, click the add button in the bottom right corner. A window will appear with default values selected. Add exercise information to confirm a new exercise and follow the rules about Exercises. See 'Add/Edit Exercise' and 'Exercise' for more information. To remove an exercise, long hold the exercise's row and a prompt will ask you to confirm deletion of a exercise. See 'Exercise Deletion' for more information

### Add/Edit Exercise

When you click the add button or click a row within the exercises list, you will be sent to a window to add or edit an exercise. The operations are very similar so I figured it would be easier for me if I put them together. When creating or editing an exercise, you will be required to input a name (text box), exercise type (dropdown list), primary movers (list, single select) and secondary movers (list, multiple select). For rules regarding exercise creation see 'Exercise Creation'.

### Muscles

Muscles are displayed in list format and each muscle row shows the name of the muscle. To edit the muscle, you can click the row and a dialog will appear with the current muscle name displayed. To add a new exercise, click the add button in the bottom right corner. A dialog will appear with a blank name text box (follow the rules about 'Muscle Creation' and 'Input Fields). To remove a muscle, long hold the muscle's row and a prompt will ask you to confirm deletion of a muscle. See 'Muscle Deletion' for more information.

### Settings

The settings available are:

- 24 Hour Clock (on/off) - allows the user to choose time choices and displays to be in 24 hour clock mode rather than am/pm
- Default Session Duration - Value automatically inserted into session duration input fields or used if the duration is 0mins

## Database Structure

<img src="https://i.imgur.com/Gp0M9XK.jpg" style="display: block;margin-left: auto;margin-right: auto;width: 50%;">