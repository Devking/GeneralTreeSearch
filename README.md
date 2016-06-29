# General Tree Search Agent

This repository contains code for the General Tree Search Agent originally created by Wells Santo, at the NYU Tandon School of Engineering. In compliance with university regulations, the intellectual property rights of this code belong to New York University. However, I have provided my code here, free to be used and developed on, so long as credit has been given to myself and to the university.

# Directory Information

The current directory corresponds to the `src` directory located within the GVG-AI framework. You do not have to copy this entire directory in order to use the General Tree Search algorithm, but for ease of development while using version control, you may choose to use this repository in place of the original `src` directory.

The files/directories that are relevant to the GTS agent are as follows:

- `GTSRunner.java`: This tells GVG-AI to run the GTS agent. Currently, this is not different from the original `Test.java` code. Previously, this was modified such that you would pass in a text file to initialize the parameters of the agent.

- `/controllers/GeneralTreeSearch`: This directory contains the code for the Agent itself.

# Some Notes

- `/controllers/GeneralTreeSearch/BREAKDOWN.md`: This file describes more in depth the operation of the controller. The use of "TSDL" text files is a mere formality at the moment to load parameters easily on Agent initialization. This is not technically necessary (as you can specify the parameters manually), and the files themselves should be changed in the future to a better format, such as JSON.

- `Overview.pdf`: The overview slide deck I showed in the lab on 29 June 2016.
