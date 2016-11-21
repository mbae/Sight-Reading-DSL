from abjad import *
import random
import sys
# sys.path.append('/home/matthew/cs111/sight-reading-project')
# import importlib
# importlib.import_module("noteRelations")
import noteRelations

import random

# To predict next note, just look at previous note for now (first-order markov process)

allKeys = ["a", "bf", "b", "c", "cs", "d", "ef", "e", "f", "fs", "g", "gs"]
majorIntervalsAscending = [2,2,1,2,2,2,1]
majorIntervalsDescending = majorIntervalsAscending[::-1]
minorIntervalsAscending = [2,1,2,2,1,2,2]
minorIntervalsDescending = minorIntervalsAscending[::-1]
possibleKeysInScala = "A,B,C,D,E,F,G,Bb,Eb,Ab,Db,Gb,Cb,F#,C#,a,e,b,c#,g#,ab,d#,eb,a#,bb,f,c,g,d".split(",")
dictOfKeys = {}

# Problem: Doesn't handle all possible enharmonics
for e in possibleKeysInScala:
	if e[0].isupper():
		if len(e) > 1:
			if e[1] == "b":
				dictOfKeys[e] = (e[0].lower() + "f","major")
			else:
				dictOfKeys[e] = (e[0].lower() + "s","major")
		else:
			dictOfKeys[e] = (e[0].lower(), "major")
	else:
		if len(e) > 1:
			if e[1] == "b":
				dictOfKeys[e] = (e[0].lower() + "f","minor")
			else:
				dictOfKeys[e] = (e[0].lower() + "s","minor")
		else:
			dictOfKeys[e] = (e[0].lower(), "minor")

def numToKey(n):
	""" Converts a number between 0-87 into the proper abjad note
	"""
	baseNote = allKeys[n % 12]
	return baseNote + getOctave(n)

# 0-2 -> ,,,
# 3-14 -> ,, 0
# 15-26 -> , 1
# 27-38 -> no modifier 2
# 39-50 -> ' (middle C)
# 51-62 -> ''
# 63-74 -> '''
# 75-86 -> ''''
# 87 -> '''''
def getOctave(n):
	""" returns the octave modifier according to the rules above 
	"""
	if n < 3:
		return ",,,"
	elif n < 39:
		return "," * (2 - (n-3)/12)
	return "'" * ((n-27)/12)

def generateNotesInScale(key, quality):
	ascendingIntervals = []
	descendingIntervals = []
	if quality == "major":
		ascendingIntervals = majorIntervalsAscending
		descendingIntervals = majorIntervalsDescending
	else:
		ascendingIntervals = minorIntervalsAscending
		descendingIntervals = minorIntervalsDescending

	startingIndex = allKeys.index(key)
	
	# Get the notes below
	i = 0
	allPossibleScaleNotes = []
	currentIndex = startingIndex
	while currentIndex >= 0:
		allPossibleScaleNotes.append(currentIndex)
		currentIndex -= descendingIntervals[i]
		i = (i + 1) % 7
	allPossibleScaleNotes.sort() # for test purposes

	# Get the notes above
	i = 1
	currentIndex = startingIndex + ascendingIntervals[0]
	while currentIndex <= 87:
		allPossibleScaleNotes.append(currentIndex)
		currentIndex += ascendingIntervals[i]
		i = (i + 1) % 7
	return allPossibleScaleNotes

# bflatscale = generateNotesInScale("bf", "major")
# print [numToKey(x) for x in bflatscale if abs(5-x) < 5]
def predictNextNote(n, key, quality, sizeOfInterval, lowerBound, upperBound):
	""" Given a previous note and the current key, output the next note
		based on probability weights of each possible note.

		n: previous note from 0-87
		key: the key we are in (A, C#, etc.)
		quality: major, minor, etc.
	"""
	notesWithinOctave = [x for x in generateNotesInScale(key, quality) if (abs(n-x) < sizeOfInterval and x >= lowerBound and x <= upperBound)]
	return notesWithinOctave[random.randrange(0,len(notesWithinOctave))]

# print numToKey(predictNextNote(5, "d", "major", 5, 0))

##############################################################################

content = []
with open("exportToPython.sr") as f:
	content = f.readlines()

content = [x.split() for x in content]
lowerBound = 40
upperBound = 70
print content
currentNote = random.randrange(40,55)
staffNotes = ""
for section in content:
	numOfNotes = int(section[1])*4
	currentKey, currentQuality = dictOfKeys[section[0]]
	for i in range(numOfNotes):
		newNote = predictNextNote(currentNote, currentKey, currentQuality, random.randrange(4,13), lowerBound, upperBound)
		staffNotes += numToKey(newNote) + "4 "
		currentNote = newNote

staffNotes = staffNotes[:-1]
# print staffNotes

staff = Staff(staffNotes)
key_signature = KeySignature('A', 'major')
attach(key_signature, staff)
show(staff)
systemtools.IOManager.save_last_pdf_as("firstoutput.pdf")