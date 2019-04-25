# Pentago-Swap-AI-Agent
AI agent for a deterministic board game with no hidden information

# About The Game
About the GamePentago Swap is a variant of the not so popular game Pentago.  The game is perfectly deterministicwith no hidden information which makes it ideal for developing an AI agent.  The game is playedon a 6x6 board with two players:  the white player, who always plays first, and the black player.Each player plays one after the other by placing a piece, then swapping two quadrants of the board.The goal of the game is to end up with 5 pieces in a row.

IMAGE OF GAME

# How Run the Game

First clone the repo to your local machine with:

```$ git clone https://github.com/vaquierm/Pentago-Swap-AI-Agent.git```

Open the repository with an IDE such as IntelliJ or eclipse.

Run the [```ServerGUI.java```](https://github.com/vaquierm/Pentago-Swap-AI-Agent/blob/master/src/boardgame/ServerGUI.java) file in the ```src/boardgame/``` directory.

A window will appear. To start the board game server, click on **Launch -> Lauch Server**

Then launch two clients that will play against each other from the same **Launch** tab.

Player 0 is white and Player 1 is black.

Have fun trying to beat the Agent 😉

# Strategy
The strategy taken by this AI agent consists of a simple Monte-Carlo method with an Upper Confidence Tree (UCT) as the tree policy. It does however have some filtering capabilities to eliminate certain moves from its set of possible moves because they would be likely to lead to an inevitable defeat. When the move to play is chosen at the end of the turn after running many default policy simulations, it is picked based on maximizing the win rate. The default poicy used is not purely random. If a move that could lead to a win exists, it is played, otherwise, a random move is played.

The steps to chose the optimal move can be broken down into two major steps.

1. Pre exploration filtering

Used to make sure that the options of moves to chose from are the least *dangerous*.

2. Monte-Carlo Tree Search

Used to pick the best move to play among the set of least *dangerous* moves.

## Pre-Exploration Filtering

The  pre-exploration  filtering  is  used  to  eliminate  some  obvious *bad moves* which  could  lead  toan  inevitable  defeat  from  the  set  of  considered  moves.   A *bad move* has  one  of  the  followingcharacteristics:

1. Never play a move which leads the the opponent winning.
2. Never play a move which gives the opportunity to the opponent to win in one move.
3. Never play a move which allows the opponent to play a move which leads to the inevitable defeat of the agent two moves down.
3. Never play a move which allows the opponent to play a move which leads to the inevitable defeat of the agent four moves down.

In some board states where the Agent is not performing so well, it can be impossible to filter out all these moves, which will lead to the inevitable loss of the agent if the opponent plays optimally. This this filter only filters out moves in the above order of danger.

IMAGES of those states.

While the exploration occurs, the agent can find moves to play which would lead to a guaranteed victory. It can search up to four moves ahead in the game tree to find such moves that will guarantee its victory.

## Monte-Carlo Tree Search

After findnig the least dangerous moves to play, a monte carlo tree seach with UCT is used. The main improvement is the default policy. Instead of playing all moves randomly, it can detect of any of the availible moves can lead to a win. If such move exists, it is played, otherwise, a random move is played. This makes the rollout phase of the algorithm much more realistic.
