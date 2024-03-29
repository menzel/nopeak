# NoPeak: Binding Motif Discovery from ChIP-Seq Data without Peak calling


Binding motif discovery through Chromatin immunoprecipitation with high-throughput DNA sequencing (ChIP-Seq) data is an important tool for understanding regulatory processes. Identifying binding motifs from ChIP-Seq data is based on peak calling where regions with an enriched read count are classified as peaks and subsequently the surrounding regions are analyzed using a motif finder. Yet, only part of the high signals corresponds with biological effects. High signals are also caused by experimental noise and common? binding characteristics. Peak calling relies on correctly chosen parameters to filter noise from signal and is likely to classify weak binding motifs and co-factors as noise. Additionally, motif finding using a tool that is detached from the original data source introduces another source of error.


The NoPeak Software uses the integration profile of k-mers based on mapped reads. Instead of finding peaks across the genome we create read profiles for each k-mer. The profiles have a distinct shape by which they are filtered and scored. Selected k-mers are then combined directly to sequence logos. This method also offers the possibility to directly included experiment specific background data to remove background noise.



# Usage


## Prepare Reads

* Mapping with Bowtie 2: bowtie2 --sensitive -p 4 -x hg19 -U ENCFF000XBO.fastq -S cebpb.sam
(Take a look at the Bowtie 2 manual for further reference)

* Create a .bed-file: 
    * samtools view -bS cebpb.sam > cebpb.bam
    * bamToBed -i cebpb.bam | sort -k1,1 -k2n > cebpb.bed 


## Create profiles:

java -jar NoPeak.jar PROFILE --reads reads.bed --genome /hg19 [-k 8]

* Where reads.bed is the file with the mapped reads that should be analyzed, e.g.:

    > chr1	807531	807581	sample	1	-
    > chr1	809492	809542	sample	1	-
    > chr1	1379054	1379104	sample	1	-
    > ... 

* /hg19 is a directory with the (human) genome as .fasta-files (chr1.fa, chr2.fa, ... chrY.fa) 
(You can get it here for example: hgdownload.cse.ucsc.edu/goldenPath/hg19/bigZips/chromFa.tar.gz)

* -k k-mer length: Can set the length of the k-mers to find. The default value is 8 bases.


## Export k-mers and create sequence logos  

* Example:
java -jar NoPeak.jar LOGO --signal reads.csv [--control control.csv] --fraglen 100 --strict [--export-kmers file]

* reads.csv and control.csv contain the profiles that were created using NoPeak in a previous PROFILE run:

    > GCCATGAC        130     131     122     121     119     122 ...
    > GCCATGAA        170     187     182     203     168     178 ...
    > ...

* Controls are optional but recommended.

* fragment length: Estimated fragment length. You can use the included estimate_fraglen.jar tool or any other estimation tool.  

* It is recommended to use the --strict parameter to get only high scoring k-mers. If you deciede to do otherwise, review the shapes of k-mer profiles using the .csv from before and the plot_profile.py script (described below) with the top k-mers to verify that the shapes are well.

* --export-kers export k-mers with scores in an intermediate step. Scores are the profile heights. Absolute height if there are no controls, with controls the scores are relative values.

# Utilities

### plot_profile.py

Plots a given profile produced by NoPeak PROFILE. Which profile to plot is set by a regex (e.g. AA(G|C)T+). 

### plot_n_highest_profiles.py

Plots the n highest profiles by relative height without any 

### combine_profiles.py

Combines two profile files as produced by NoPeak PROFILE by adding values for each k-mer. Prints the resulting profile to stdout.
Use with caution because the profiles are only normalized over read count and could have problems with profiles that have different fragment lengths.

### plot_pwm.py
Plots a PWM as sequence logo (using Weblogo https://weblogo.berkeley.edu/logo.cgi) from a given list output by NoPeak.


# Rust version
There is a much faster version written in Rust: https://github.com/menzel/nopeak_rust
However, until now only the core algorithm is implemented and thus it lacks several features.
