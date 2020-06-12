# Multiplayer Mastermind

*Authors:
[Paolo Baldini](https://github.com/Mandrab),
[Ylenia Battistini](https://github.com/yleniaBattistini)*

The project, developed for the *Concurrent and Distributed Programming* course at University of Bologna, is about the development of a multiplayer version of the Mastermind game.<br/>
The system is based on the *actor* concept and use for his implementation the **Akka** framework.<br/>
Being a study-project, it uses some functionality not required for the development of the system (e.g., both *classic* and *typed* actors, receptionist, etc.), but added for a better comprehension of the framework/paradigm.

## The game

The game is a variant of the famous game *Mastermind*, where a player should guess a secret code created by a code-maker.<br/>
This version of the game is however a multiplayer one, where a player should guess all the adversarys' codes. The whole system is administrated by an *Arbiter*, who take some decisions such as the turn assignment, the ban of a player (if he think to have won, when instead he doesn't), etc.

<img align="left" src="res/base_play.gif" alt="Basic play">
<img align="right" src="res/human_player.gif" alt="Play with human">

## The Adversary Algorithm

Due to a possible high number of player and a length of codes that can vary, a solution like the one used in the *Five-guess algorithm* (who use *minimax* technique) was not feasible in terms of memory allocation. Due to that a new algorithm was implemented. This one does not focus on resolve the code in the minimun number of moves, but on the use of a lazy calculation for the next code to try. In fact, there is no need that a code must be discovered in a determined number of turn, but there is the necessity to use less memory as possible.

## Actors communication

In this paragraph, we don't desire to accurately describe how messages are passed, in which order, or the fault techniques adopted. We are only interested in show a macro-structure of the message-passing between the actors and the GUIs frames. That will be done through use of the following image.

<img align="left" src="res/message-schema.png" alt="Message passing">