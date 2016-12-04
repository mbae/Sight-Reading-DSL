from abjad import *
import random
import sys
from noteRelations import *
import random

content = []
with open("exportToPython.sr") as f:
	content = f.readlines()

content = [x.split() for x in content]
print content

# Setting up the staff
staff = Staff([])
measures = []

# Set the key signature of the whole piece
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
			measureNotes += numToKey(newNote) + str(int(4/currentDuration)) + " "
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
score.add_final_bar_line()
show(score)

systemtools.IOManager.save_last_pdf_as("firstoutput.pdf")