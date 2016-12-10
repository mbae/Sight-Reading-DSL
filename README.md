# Sight-Reading-DSL

You have reached the Sight-Reading-DSL. This DSL is written in hopes to make generating random music for making practicing sight reading easier.

## Installation
You'll need a few things to get this running:
* Scala installed with sbt
* Python 2.x, where x > 7.
* The Abjad and PyPDF2 Python libraries installed

## First Steps
To generate music, you write a program in a separate file and call the following
```
./srg path/to/program name [numOfTimes]
```
in the root directory of this repository. `name` is the name of the output pdf, which is outputted in a separate "output" folder. The argument `numOfTimes` is an optional argument specifying how many variations of the program you would like. Sample programs are written with the hope of capturing all features of this language. To run the sample program, enter

```
./srgexample name [numOfTimes]
```

An example of what can be outputted is shown as example.pdf
