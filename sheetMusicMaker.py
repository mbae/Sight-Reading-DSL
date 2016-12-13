# import os
# import sys
# import random
# from abjad import *
import subprocess
import webbrowser

from noteRelations import *
from PyPDF2 import PdfFileMerger

content = []
with open("exportToPython.sr") as f:
	content = f.readlines()
content = [x.split() for x in content]
name = sys.argv[1] + ".pdf"
numberOfCopies = int(sys.argv[2])
pdfs = [str(i) + name for i in range(numberOfCopies)]
for pdf in pdfs:
	createScore(content, pdf)

merger = PdfFileMerger()
for pdf in pdfs:
    merger.append("output/" + pdf)

merger.write("output/" + name)



# Cleanup the lilypond files
thisDir = "output"
files = os.listdir(thisDir)
for file in files:
    if file.endswith(".ly") or file in pdfs:
        os.remove(os.path.join(thisDir,file))


# webbrowser.open(r'file:///home/matthew/cs111/Sight-Reading-DSL/output/' + name)