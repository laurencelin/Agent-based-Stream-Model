# Agent-based-Stream-Model

This program is coded in JAVA 8 or later with parallel computation design. 
An agent (named as "parcel" in the code) represents a small amount of water and solute (NO3-N). This small amount is user-defined. 
Generally, the smaller the amount of water and solute a parcel represents, the higher accuracy of the model and higher computational resource the program demands.

Detailed model description and application is in Lin, Davis, Cohen, and Edmonds 2016 (https://doi.org/10.1016/j.ecolmodel.2016.05.018)

--------------------------------------------------------------
<Installation>
Assuming JAVA 8 or later has already been installed, run command "sh makefile.sh" in the model folder. This command removes exisiting .class files and recompiles the .java files and produces model.jar, which is an executable binary.

An short example shows how to pass parameters to the model through command line (bash/shell).

Advanced user should look into the modelv3.java for further fine model setup.

