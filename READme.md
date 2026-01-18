# Pathly

Pathly is an interactive mapping web application that allows a user to navigate around the vicinity of the Berkeley downtown area in Berkeley, California.
I implemented the back-end "smart" features of this program including rastering, dragging/zooming, shortest path finding, auto-complete search and turn-by-turn directions. 

Data structures and algorithms such as Heap, Trie, K-D Tree and A* search were implemented from scratch in Java to learn object-oriented programming.

## Shortest path and Turn-by-turn directions
![path demo](shortestpath.gif)

## Autocomplete
![autocomplete demo](autocomplete.gif)

## Search and highlight
![search demo](search.gif)


| File | Description                                                                                                                                                 |
|------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
|  [RasterAPIHandler](https://github.com/Yusprog/Pathly/tree/4c7cb475f5a882a06180d3bbb45a7dd72dbfec7f/bearmaps/proj2d/server/handler/impl)    | Renders map images given a user's requested area and level of zoom.                                                                                         |
|  [AugmentedStreetMapGraph](https://github.com/Yusprog/Pathly/tree/4aa2246c495034824df2a0bde4412ff4299fb142/bearmaps/proj2d)    | Graph representation of the contents of Berkeley Open Street Map data.                                                                                      |
|  [AStarSolver](https://github.com/Yusprog/Pathly/tree/4c7cb475f5a882a06180d3bbb45a7dd72dbfec7f/bearmaps/proj2c)    | The A* search algorithm to find the shortest path between two points in Berkeley.                                                                           |
|  [TrieSet](https://github.com/Yusprog/Pathly/tree/4c7cb475f5a882a06180d3bbb45a7dd72dbfec7f/bearmaps/proj2ab)    | A TrieSet backs the autocomplete search feature, matching a prefix to valid location names in Θ(k) time, where k in the number of words sharing the prefix. |
|  [KD Tree](https://github.com/Yusprog/Pathly/tree/4c7cb475f5a882a06180d3bbb45a7dd72dbfec7f/bearmaps/proj2ab)    | A K-Dimensional Tree backs the A* search algorithm, allowing efficient nearest neighbor lookup averaging O(log(n)) time.                                    |
|  [ArrayHeapMinPQ](https://github.com/Yusprog/Pathly/tree/4c7cb475f5a882a06180d3bbb45a7dd72dbfec7f/bearmaps/proj2ab)    | A min-heap priority queue backs the A* search algorithm.
|  [Router](https://github.com/Yusprog/Pathly/tree/4aa2246c495034824df2a0bde4412ff4299fb142/bearmaps/proj2d)    | Creates the list of directions corresponding to a route on the graph

