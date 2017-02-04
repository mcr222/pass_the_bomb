# Introduction
This code is the prototipe/base for a new android app game. The **Bomb game**.

# Main idea
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

# Competition analysis
This are some apps that revolve around the same idea of not looking at your phone.
There are a lot of them, but all of them focus on oneself.

http://www.inc.com/jeremy-goldman/6-apps-to-stop-your-smartphone-addiction.html

The most similar to our approach is *Flipd* one: http://flipdapp.co/
It is similar because it is thought for use in a classroom, but it take a harsher
approach of blocking the phone. Good thing is it has around:  10,000 - 50,000 installs,
which means that our market is considerable.

There is actually another similar one, and that is *bSociable* (https://www.anti-apps.com/). 
It allows to get points for not using the phone, but it doesn't allow you to look at 
your phone at all (not even lock it as it must run in foreground). And it does
not have the sociable interaction of passing the bomb. Additionally it is only for
iPhone (https://itunes.apple.com/au/app/bsociable/id1002964913?mt=8).

**Thus, we haven't found anything close to similar to our approach, so there is** 
**market and we are creating something innovative.**

# Making money
First of all all people involved in the project might be aware that we could potentially
not win anything, or just very little. So everyone must be aware of the time he/she is
willing to invest and their limits.

Secondly, approaches to monetize the app:
1. Add advertisements
2. Add additional bombs with different behaviors that can spice up the game. 
As the bomb is shared among users, the price can be split up between players 
(which makes it insignificant for players, but allows us to make some profit)
