#!/usr/bin/env python3

import sys
import numpy
import matplotlib.pyplot as plt
import matplotlib.cm as cm
import re


### Check params
if len(sys.argv) < 2 or "help" in sys.argv[1] :
    print("usage: ./combine_profiles.py a.csv b.csv c.csv ...")

comb_profile = {}

for f in sys.argv:
    with open(f) as fp:
        for line in fp: 
            if not line.startswith("#") and not line.startswith("kmer"):
                parts = line.split("\t")

                values = [float(x)/readc for x in parts[1:]]
                kmer = parts[0]

                if kmer in comb_profile:
                    comb_profile[kmer] = [values[i] + comb_profile[kmer][i] for i in range(len(values))]
                else: 
                    comb_profile[kmer] = values

            else: 
                if line.startswith("# reads used:"):
                    parts = line.split(" ")
                    readc = int(parts[3])

print(comb_profile)
