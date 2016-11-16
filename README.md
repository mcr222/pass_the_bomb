# Introduction
This code is the prototipe/base for a new android app game. The **Bomb game**.

#Main idea
The **Bomb game** is a game app that intends to avoid the over use of the phone 
when in social reunions: meeting friends or family, at a restaurant, bar or party.

It could be interesting to test it in other events, like in a classroom or a 
talk (to limit the phone usage).

The game allows using the phone but it limits its usage, as the people
more prone to look at their phones will lose more points.


# Game instructions

## How does the game works? 

*Note:* **Active bomb** -> when the player has the bomb and it is discounting points
from the point counter. **Inactive bomb** -> when the player has the bomb but it
is not discounting points.

    1. As many inactive bombs as you chose will be randomly distributed among the players. 
    2. Once a player has an inactive bomb, the bomb will be activated when the player
    starts using his/her phone (phone is unlocked).
    3. The longer you have a bomb, the more points you lose.  
    4. There are two options to stop the points countdown:
        a. Pass/unload the bomb to a Bluetooth-distance player who is using his/her phone.
        Unloading the bomb will only happen if the person at a bluetooth-distance
        has his/her phone unlocked. Therefore, if you are not using your phone then
        you won't receive any bombs.
        b. Don’t use your cell for 15 minutes (bomb will be deactivated, but player
        will still have it). 
    5. The game is over when the score of one of the players reaches zero. This
    person will have to be penalized however the players decided at the beggining
    of the game (for example: she/he has to pay for another round of drinks).

## Do you want to start a new game with your friends? Follow the next steps! 

    1. All the players must go to the “new game” window. 

    2. One of you should set the number of bombs and the initial number of points you start the game. 

    3. The same person will invite all the players to the game by means of Bluetooth. 
    From the detected devices through Bluetooth this person will select all the 
    players to join the game. Therefore, all players must have Bluetooth activated
    and should be active until the game is finished. 

## Cheaters

Each game has its own unique ID, so cheaters can be caught if their game ID is 
not the same as the rest. If a player turns off their phone/app/bluetooth then
the ID will disapear, and starting a new game won't produce the same ID.

# How to develop code (good practices)

1. Use Android Studio
2. Look at the Issues in the Gitlab page.
3. Open issues for any problem you encounter.
4. Branch the code and do merge request.
5. Test the code that you implement (in a real phone if possible)
6. **Comment** all the code that you develop