#!/bin/bash
rm *.class
javac *java
jar cfm modelv03_2016May19.jar manifest *class *java *jar
jar tf modelv03_2016May19.jar
