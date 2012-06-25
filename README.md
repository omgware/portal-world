portal-world
===============

Current revision: 0.1

Author: Simone Autore (aka Sippolo)


This demo is just a raw implementation of portals, simple joints and basic physics using Box2D, and is developed with the awesome LibGDX java game framework: http://libgdx.badlogicgames.com/


- Run Instruction:

Make sure you have jre6+ installed on your machine, then just run the PortalWorld.jar

Download Builds: you can find the zip file of the build in the Downloads section on this repository, just make sure you extract all the files before executing the jar/bat, otherwise it may not run at all.


- IDE Integration:

You should be able to import the project directly into Eclipse and compile it without issues.


- Android platform

Following LibGDX instructions on their website, you can easily make an Android project attached to this one.


- Gameplay Instructions and Commands

There's no real goal in this test demo.
Your character is the red circle/ball you see on screen.
Imagine that you're a sort of space ship, with limited fuel (which recharge automagically while you're not using it, you can see it appearing on the bottom of the screen), and you can trigger movements clicking with the mouse near the circle and dragging the mouse outwards to move towards that direction.
You can also double click to trigger an instant rush (which will burn a big fuel quantity instantly).
Also, while within the interactive range of your ball, the further the mouse is from the ball, the faster it moves.
When you're near little boxes (squares, imagine them as little cargos), if you click on them you can attach them to yourself, and travel around together, then if you quickly click and slide in any direction, you'll throw the attached box away.
The slim vertical rectangles are portals, anything (allowed inside the source code) that will go through them will be teleported on an exit portal on the same stage.

Have fun!