# robo4j-rpi
Hardware abstractions for the raspberry pi.

These hardware abstractions make it easier to develop for the Raspberry Pi. They can be used stand-alone, or together with the Robo4J framework. 


There are three source folders:

1. src - the source code
2. test - junit tests
3. examples - examples that demonstrate how the devices can be used

There is an Eclipse .project file provided. The only thing required for development is to set up a User Library in Eclipse called PI4J, with the PI4J jars.


Notes on running the examples:

1. Make sure that you have the raspberry jars on your class path
2. Access to the hardware ports usually requires sudo access