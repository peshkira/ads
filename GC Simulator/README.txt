This lab package contains a group communication simulator. You can find an 
example implementation and configuration of an unreliable protocol and an 
example TestProvider under the "src/net" directory. See the target "run.example" 
in the build.xml on how to invoke the simulator.

In order to implement and test a protocol, you have to create three artefacts:
*) A properties file containing the configuration parameters for the simulation.  
   A default configuration is already provided, but you may change the 
   parameters for development purposes.
*) A Java class (see example) implementing the group communication protocol.
*) A TestProvider (see example) to initialize and verify the simulation.

Put your protocol implementations, the config files, and the test providers in 
the "src/ads/gc" directory. You can already find some templates there. You may 
add additional classes, but don't change the class names of the main protocol 
and test provider classes.

If you are convinced that a specific protocol cannot be implemented, because of 
the used failure model, write your argumentation into "protocols.txt" in the 
directory of this README.txt.

Checking the results: If the simulation of your protocol and test provider leads 
to an incorrect result, the simulator will write the execution trace to a log 
file (trace-<timestamp>.log) that allows you to check for the error. However, it 
is up to your respective test provider implementation to discover protocol 
errors.

A note on the implementation: Your protocol implementation should work with 
other TestProviders corresponding to the same protocol (unordered, fifo, ...) as 
well.  Therefore, make sure you stick to the official API and do not use 
specifics of your protocol implementation while writing the test provider. In 
other words, your TestProvider should not depend on your implementation of the 
protocol and vice versa.

Submission
==========

To submit your solution, call "ant submission". This will create a file 
"gc_submission.zip" in the "build" directory. Double-check and submit this file.
