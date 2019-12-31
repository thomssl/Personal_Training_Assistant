# Personal_Training_Assistant

## Introduction

This application was created to help myself organize and program for my clients. Now I hope this can be used my others to help improve the efficiency and effectiveness of their work. The following are rules and practices that the app follows when deciding how to schedule and setup client sessions. I tried to leave as much of the practices up to the user but as you see below, some choices have already been made for you. I am sorry but some things cannot be allowed if I wanted to make this app easier for myself.

## Input Fields

All fields or boxes that the user inputs information must be checked to make sure the input data is valid. This means that the input value must not be blank and some characters are not allowed such as:

-	Commas
-	Underscores
- Semicolons
- Double Quote Marks

## Clients

### Overview

Clients are used to conatin the information pertaining to a single client. They contain:

- id, unique to the client
- name, unique to the client
- schedule type, see schedule types below in classification
- days, list of days the client is scheduled if their schedule is constant (if not constant, it will be the number of weekly or monthly session allotted to the client)
- times, list of times the client is scheduled (correlating with their days) if their schedule is constant (if not constant, it will be 0)
- durations, list of durationss the client is scheduled (correlating with their days) if their schedule is constant (if not constant, it will be a single default session duration)
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

Enter client name and choose schedule type. Input fields will change depending on the schedule type. Enter all required input fields visible to complete Client creation. See "Input Feilds" to make sure name field input is valid.

## Joints

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

## Muscles

### Overview

Muscles are used to contain the information about a muscle:

id, unique to the muscle
name, unique to the muscle

### Creation

Enter name of muscle.  See "Input Feilds" to make sure name field input is valid. Checks if to make sure name is unique

### Deletion

Application will check if the Muscle is used to describe an exercise. If it is used, the deletion will be stopped and the user will be prompted to remove exercises that contain the muscle.

## Exercises

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

When characterizing an exercise, there is a primary focus and secondary focus(es). To simplify the data collection, an exercise can only have 1 primary focus, but it can have as many secondary focuses as needed within the collection of muscles and joints provided. For example, a Hip Thrust would have a primary focus of the glutes and possibly a secondary focus of the hamstrings and spinal erectors. However, be for warned that if you choose “Strength” as the exercise type, the choices for primary and secondary focuses are limited to muscles and the same if true if you choose “Mobility” or “Stability” with respect to joints.

### Creation

Enter exercise name and exercise type. Once type is chosen, the primary movers list will be filled with the appropriate data. Once a primary mover is selected, the secondary mover list will be filled will all the possible movers with the primary mover selected omitted (to remove redudant secondary mover information). See "Input Feilds" to make sure name field input is valid. Checks to make sure name is unique.

### Deletion

Application will check if the Exercise is used to describe a session. If it is used, the session log entry will have the exercise removed and the information for that exercise added to the notes section

## Sessions

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
