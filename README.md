# TopDownGame
A top down shooter being used to learn various concepts in Java.
This is a little game made by Benjamin Correia.

# Supporting software
JOGL:
  Availible at: 
    http://jogamp.org/deployment/jogamp-current/archive/jogamp-all-platforms.7z

# How to play
Game Instructions:

How to play:
1.  Hit 'l' (lower case L) with the main window focused
	- mouse on the main window controls direction of fire
	- left click is shoot
	- 'w','a','s', and 'd' controls movement
	- hit 'q' to toggle weapons


Creating a map:
1.  left click to place down triangles (every third click will add the triangle to the map geometry)
	- If the clock is close enough to a previous vertex, the previous vertex will be referenced (even works on the same triangle)
2.  hit 'k' to enable pathing graph; left click now places points; every even click will in addition to placing a point, place an edge conecting n to n-1
	- If the click is close enough to a previous point, a new point will not be created and the previous point will be referenced
	- Ensure every node is fully connected (i.e. you can get from any node to any other node)
3.  hit 'l' to start the game
	- 'w','a','s', and 'd' controls movement
	- hit 'q' to toggle weapons

Saving/Loading a map:
1.  In the mapping/graphing mode hit 'm' to open the "save/load" dialog.
2.  Type the exact name of the map folder in the space provided.
3.  Hit "save" or "load" button depending on your need.
	-  Hitting "save" will overwrite any map with the same name.

# Videos
Current state of the game:
	https://youtu.be/UF0g8ZcNzSk

Mapping demo and gameplay:
	https://youtu.be/JbbWrBtPMYM

Dijkstra Test and gameplay:
	https://youtu.be/TLSb709ZjD0
	
Multi-threading and gameplay:
	https://youtu.be/tio0EqF15Qk
