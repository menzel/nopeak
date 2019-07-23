# NoPeak: Binding Motif Discovery from ChIP-Seq Data without Peak calling


Binding motif discovery through Chromatin immunoprecipitation with high-throughput DNA sequencing (ChIP-Seq) data is an important tool for understanding regulatory processes. Identifying binding motifs from ChIP-Seq data is based on peak calling where regions with an enriched read count are classified as peaks and subsequently the surrounding regions are analyzed using a motif finder. Yet, only part of the high signals corresponds with biological effects. High signals are also caused by experimental noise and common? binding characteristics. Peak calling relies on correctly chosen parameters to filter noise from signal and is likely to classify weak binding motifs and co-factors as noise. Additionally, motif finding using a tool that is detached from the original data source introduces another source of error.


The NoPeak Software uses the integration profile of k-mers based on mapped reads. Instead of finding peaks across the genome we create read profiles for each k-mer. The profiles have a distinct shape by which they are filtered and scored. Selected k-mers are then combined directly to sequence logos. This method also offers the possibility to directly included experiment specific background data to remove background noise.



# Usage


## Prepare Reads

* Mapping with bowtie2: bowtie2 --end-to-end --sensitive -p 4 -x hg19 -U ENCFF000XBO.fastq -S cebpb.sam
(Take a look at the bowtie2 manual for further reference)

* Create a .bed-file:

    * samtools view -bS cebpb.sam > cebpb.bam
    * bamToBed -i cebpb.bam > tmp.bed
    * sort -k1,1 -k2n tmp.bed > cebpb.bed 


## Create profiles:

java -jar NoPeak.jar PROFILE reads.bed hg19.fa 8 4

* Where reads.bed is the file with the mapped reads that should be analyzed, e.g.:

> chr1	807531	807581	sample	1	-
> chr1	809492	809542	sample	1	-
> chr1	1379054	1379104	sample	1	-
> ... 

* k-mer length: Sets the length of the k-mers to find. The default value is 8 bases.

* threads: Number of computation threads to run, should be smaller than the available thread count, at most 24.

## Create motif 

* Example:
java -jar noPeak.jar LOGO reads.csv control.csv 100
* reads.csv and control.csv contain the profiles that were created using NoPeak in a previous PROFILE run:

> GCCATGAC        130     131     122     121     119     122 ...
> GCCATGAA        170     187     182     203     168     178 ...
> ...

* fragment length: Estimated fragment length. You can use the included estimate_fraglen.jar tool or any other estimation tool.  
