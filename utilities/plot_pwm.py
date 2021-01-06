#!/usr/bin/env python3

import numpy as np
import sys
from weblogo import * 

### USAGE:
# ./plot_pwm.py PWM_AS_LIST [OUTFILE]
# ./plot_pwm.py [[1,9,1,1],[9,2,1,1],[1,1,1,12]] logo

def createlogo(counts, outfile):
    counts = counts[::-1]

    counts = np.array(counts)
    logo = LogoData.from_counts(seq.Alphabet("ACGT"), counts)
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
