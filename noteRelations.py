import os
import sys
import random
import copy
from abjad import *
# To predict next note, just look at previous note for now (first-order markov process)

allKeysSharp = ["a", "as", "b", "c", "cs", "d", "ds", "e", "f", "fs", "g", "gs"]
allKeysFlat = ["a", "bf", "b", "c", "df", "d", "ef", "e", "f", "gf", "g", "af"]
sharpKeys = ["c", "g", "d", "a", "e", "b", "fs", "cs"]
flattedKeys = ["f", "bf", "ef", "af", "df", "gf", "cf"]
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

def numToKey(n, key):
	""" Converts a number between 0-87 into the proper abjad note
	"""
	baseNote = None
	if key in flattedKeys:
		baseNote = allKeysFlat[n % 12]
	else:
		baseNote = allKeysSharp[n % 12]
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

	startingIndex = None
	if key in flattedKeys:
		startingIndex = allKeysFlat.index(key)
	else:
		startingIndex = allKeysSharp.index(key)
	
	# Get the notes below
	i = 0
	allPossibleScaleNotes = []
	currentIndex = startingIndex
	while currentIndex >= 0:
		allPossibleScaleNotes.append(currentIndex)
		currentIndex -= descendingIntervals[i]
		i = (i + 1) % 7

	# Get the notes above
	i = 1
	currentIndex = startingIndex + ascendingIntervals[0]
	while currentIndex <= 87:
		allPossibleScaleNotes.append(currentIndex)
		currentIndex += ascendingIntervals[i]
		i = (i + 1) % 7
	return allPossibleScaleNotes

def predictNextNote(n, key, quality, sizeOfInterval, lowerBound, upperBound):
	""" Given a previous note and the current key, output the next note
		based on probability weights of each possible note.

		n: previous note from 0-87
		key: the key we are in (A, C#, etc.)
		quality: major, minor, etc.
	"""
	notesWithinOctave = [x for x in generateNotesInScale(key, quality) if (abs(n-x) < sizeOfInterval and x >= lowerBound and x <= upperBound)]
	return notesWithinOctave[random.randrange(0,len(notesWithinOctave))]



def createScore(content, name):
	"""
		content is a set of rules from exportToPython.sr file. This
		function creates and saves a piece of sheet music to the
		output folder with the given name.
	"""
	# Setting up the staff
	staff = Staff([])
	measures = []



	# Set the key signature of the whole piece
	currentKey = "c"
	currentQuality = "major"
	if len(content) != 0:
		currentKey, currentQuality = dictOfKeys[content[0][0]]

	key_signature = KeySignature(currentKey, currentQuality)
	attach(key_signature, staff)

	# Variables for algorithm
	lowerBound = 40
	upperBound = 70
	currentNote = random.randrange(42,52)
	currentDuration = 1 # quarter note; 0.5 represents an eighth note; Could be random.randrange(1,3)

	# print(Measure(2,4, []))
	for section in content:
		# if we get an empty line, don't do anything
		if len(section) == 0:
			break
		currentKey, currentQuality = dictOfKeys[section[0]]
		for i in range(int(section[4])): # For each bar
			duration = int(section[2]) / float(section[3]) * 4.0 # Gives us the duration in quarter notes
			measures.append(Measure((int(section[2]),int(section[3])), []))
			measureNotes = ""
			while(True): # Until we fill this measure up
				newNote = predictNextNote(currentNote, currentKey, currentQuality, random.randrange(4,int(14 * currentDuration)), lowerBound, upperBound)
				measureNotes += numToKey(newNote, currentKey) + str(int(4/currentDuration)) + " "
				duration -= currentDuration
				if duration <= 0:
					break
				currentNote = newNote
				currentDuration = min(1/float(random.randrange(1,3)),duration) # Take min to prevent overflowing
				
			measureNotes = measureNotes[:-1] # Removes last whitespace
			measures[-1].extend(measureNotes)

	staff.extend(measures)
	# Transferring staff to score and adding final bar
	score = Score([])
	score.append(staff)
	if len(content) != 0:
		score.add_final_bar_line()

	# command = indicatortools.LilyPondCommand('break', 'after')
	# attach(command, score[0])
	# show(score)
	if not os.path.exists("output"):
	    os.makedirs("output")
	persist(score).as_pdf("output/" + name)