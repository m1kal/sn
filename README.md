# sn

Snake game written in Clojurescript.

There are two ways computer can play this game:
* Algorithm - the snake tries to get to the reward, but does not go directly into obstacles.
* AI - q-learning reinforcement learning is used to teach the snake to take the best route. The state contains the following information
  * obstacle directly ahead
  * obstacles on the sides
  * direction to the reward (left/right/ahead)
  * previous move

The aim of the project was to learn practical uses of Reagent and to understand basic concepts of reinforcement learning.

## Usage

* Create an empty figwheel project `lein new figwheel -- --reagent`.
* Copy the contents of `src` to the project root folder
* Run `lein figwheel` or `lein cljsbuild once`

## Missing features
* Nice graphics
* Sounds
* More complex state allowing the snake to avoid traps
* Use values of all pixels as current game state. Q-table becomes too large, but the value can be predicted by a machine learning system.
