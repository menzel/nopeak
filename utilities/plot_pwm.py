#!/usr/bin/env python3

import numpy as np
import sys
from weblogolib import * 
from corebio.seq import Seq, SeqList, Alphabet, unambiguous_dna_alphabet
import math

def createlogo(counts, outfile):
    counts = counts[::-1]

    counts = np.array(counts)
    logo = LogoData.from_counts(unambiguous_dna_alphabet, counts)
    options = LogoOptions()
    options.fineprint = ""

    if len(counts) > 23:
        options.logo_start = 9
        options.logo_end = 19
    format = LogoFormat(logo, options)

    jpg = pdf_formatter(logo, format)

    with open(outfile, "wb") as out:
        out.write(jpg) 

profile = eval(sys.argv[1])

outfile = "logo.pdf"
if len(sys.argv) >= 3:
    outfile = sys.argv[2]  + ".pdf"

createlogo(profile, outfile)
print("pdf saved as " + outfile) 
