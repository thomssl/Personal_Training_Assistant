# Personal_Training_Assistant
Introduction

This application was created to help myself organize and program for my clients. Now I hope this can be used my others to help improve the efficiency and effectiveness of their work. The following are rules and practices that the app follows when deciding how to schedule and setup client sessions. I tried to leave as much of the practices up to the user but as you see below, some choices have already been made for you. I am sorry but some things cannot be allowed if I wanted to make this app easier for myself.

##Input Fields

All fields or boxes that the user inputs information must be checked to make sure the input data is valid. This means that some characters are not allowed such as:

-	Commas
-	Underscores
- Semicolons
- Double Quote Marks

##Sessions

Only 1 session is allowed per day per client. Allowing for more makes checking for session conflicts much more challenging. If you want to add another session for a client to a single date (although I would not recommend that for 90% of clients) and you don’t need to be tracked as rigorously (i.e. cardio)  you can add it in the logged sessions notes. If you need the extra session’s exercises to be tracked add that to the exercises completed in the logged session.

An exercises can only appear once per session. If the repeat exercise needs to be tracked (I’m not sure why someone would want to repeat and exercise later in a session though) you need to add it to the existing exercise entry and add the volume (If the weight or reps change then sorry, you cannot log that). If you don’t need to track the repeat exercise than add it to the session notes.

Max session duration is 120 minutes. This covers 99% of people and if you’re still with a client after 2 hours that is one long ass session. If you need more time, don’t worry about it just enter your session data as per usual. The overall time constraint is just a number that fits most people and eliminates the chance of a user inputting a ridiculously long session duration.

##Exercises

There are only 3 types of exercises:

  1)	Strength – Muscles driven (Strength or Hypertrophy exercises)
  2)	Mobility – Joint Driven
  3)	Stability – Joint Driven
  
This makes it easier to classify and limit over complication of the data to eliminate the chances of a user getting an error when reading or writing data. At first, I considered just having Strength exercises since most trainers focus primarily on the strength of their clients. However, after consultations with other trainers, it became clear that some exercises are not focused on strength but mobility and stability of a joint depending on the needs of a client. Follow this logic to categorize your exercises:

  •	If the exercise is driven by the strength or hypertrophy of a given muscle(s), choose strength
  •	If the exercise is driven by the range of motion or strength of a joint(s), choose mobility or stability

When characterizing an exercise, there is a primary focus and secondary focus(es). To simplify the data collection, an exercise can only have 1 primary focus, but it can have as many secondary focuses as needed within the collection of muscles and joints provided. For example, a Hip Thrust would have a primary focus of the glutes and possibly a secondary focus of the hamstrings and spinal erectors. However, be for warned that if you choose “Strength” as the exercise type, the choices for primary and secondary focuses are limited to muscles and the same if true if you choose “Mobility” or “Stability” with respect to joints.
