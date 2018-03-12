DNAmod is a tool for retrieving DNA sequences for genomic coordinates.

# Prebuild Genomes

We offer some prebuilt genomes so you do not have to create them yourself. Download both the `chrs.gz` and the `dna.zip` files for the genomes you want

## mm10

[mm10.chrs.gz](https://drive.google.com/open?id=1eyKnIxM135AWBilU7j4Ia6wUU9ca_993)
[mm10.dna.zip](https://drive.google.com/open?id=1EpnRBGMcuvnuHlQz9ZFAHDoHtGJxxSva)

# Genome Locations

Genome files must be located in either of the following locations:

`<home directory>/app_home/res/genomes/<genome name>`
`res/genomes/<genome name>`

where `<home directory>' is the path to the user's home directory. 

`<home directory>/app_home/res/genomes/<genome name>` allows data to be shared between apps, otherwise each app must have a local copy of each data set. In the case of genomes, this is around 700 MB per genome so it may be beneficial to use the shared directory.

# Graphical User Interface
DNAmod has a graphical user interface to make retrieving DNA sequences straightforward and quick.

![](images/dnamod.png)

1. Start the application by running either **`dna.bat`** on Windows, **`dna.command`** on a Mac, or **`dna.sh`** on Linux.
2. To create random DNA sequences, chose the Random DNA button and specify the length and quantity of sequences desired.
3. To create DNA sequences for a known list of coordinates, load an appropriate BED file and use the **Sequences** button to obtain sequences.

## Extracting DNA

1. Choose **File** -> **Open** to open a Text or Excel file containing genomic coordinates.
2. Make sure to turn **Header text columns** off when importing from a text/Excel file.
3. Genomic coordinates should be of the form `chr1:1-100` and in a column called **DNA Location**.
4. Choose **Extract DNA**.
5. Select the genome you want to use and whether you want to extend the coordinates. By default, UPPERCASE sequences will be output, but you can choose lowercase and whether to indicate 'N's and poor quality bases.
6. Choose OK to generate sequences.

## Reverse Complement DNA

1. Choose **File** -> **Open** to open a Text, Excel or FASTA file containing genomic coordinates.
2. Text/Excel files must contain the columns **DNA Location** and **DNA sequence**. Make sure to turn **Header text columns** off when importing from a text/Excel file.
3. Choose **Reverse Complement**.
4. A new table containing the reverse complemented sequences will be generated.

## Generating random DNA

1. Choose **Random DNA**.
2. Choose a genome.
3. Choose the length and the number of sequences you want to generate.
4. Select the genome you want to use and whether you want to extend the coordinates. By default, UPPERCASE sequences will be output, but you can choose lowercase and whether to indicate 'N's and poor quality bases.
5. Choose OK to generate sequences.

## Encoding a genome

1. Assemble gzipped FASTA files for each chromosome in your genome in a common directory.
2. Click **DNA** -> **Encode DNA**.
3. Choose a name for your genome, e.g. **grch38**.
4. Select the directory containing all of your gzipped FASTA files.
5. Choose **OK**.
6. Encoding the genome can take several minutes. Once complete the genome files will be located in the `res/genomes/<genome name>` directory where the application is running. 

The output will consist of:

`<genome name>.chrs.gz` - contains the chromosome sizes.

`<genome name>.dna.zip` - contains the encoded DNA for every chromosome.


# Command line

DNAmod is supplied with a command line variant of the tool that can be integrated into data pipelines.

## Encoding a genome
DNAmod uses genomes encoded in a 2bit/1bit format. You can download pre-encoded, ready to use use, genomes from this site for **hg19/grch37**, **grch38**, and **mm10/grcm38**. To encode your own genome, you can DNAmod in the command line mode with the following options:

```
java -Xmx1G -jar edu.columbia.rdf.matcalc.toolbox.dna.app.jar \
encode \
--genome=hg19 \
--genome-dir=/path/to/fasta/files/
```
1. **`encode`** tells the application to run the encoding tool.
2. **`--genome`** specifies the name of the genome.
3. **`--genome-dir`** specifies a directory that must contain gzipped fasta files (e.g. **`chr1.fa.gz`**), such as those obtainable from the **UCSC** where each file contains one chromosome. The chromosomes will be named by their fasta heading.
4. Once run, `<genome>.chrs.gz` and `<genome>.dna.zip` will be created containing all of the chromosome information found in the `genome-dir` directory.

## Generating DNA sequences for an existing set of locations

```
java -Xmx1G -jar edu.columbia.rdf.matcalc.toolbox.dna.app.jar \
seq \
--genome=hg19 \
--file=Peaks_CB4_BCL6_RK040_vs_Input_RK063_p12.bed \
--genome-dir=/path/to/genome/zip/files/ \
--zip-dir=/path/to/encoded/zip/ \
--length=10000 \
--n=10000 \
> seq.fasta
```
1. **`seq`** tells the application to run the sequence tool.
2. **`--genome`** specifies the name of the genome.
2. **`--file`** - a BED file listing locations of interest.
3. **`--genome-dir`** - a directory that must contain gzipped tables listing chromosome sizes.
4. **`--zip-dir`** - a directory that must contain the encoded genome in a zip file (the output of **`encode`**).
4. **`--length`** - the length of the sequence to be created.
4. **`--n`** - the number of sequences to generate.
5. Once run, **`seq.fasta`** will be created containing **`n`** random sequences each of length **`length`**.

## Generating random DNA sequences

```
java -Xmx1G -jar edu.columbia.rdf.matcalc.toolbox.dna.app.jar \
random \
--genome=hg19 \
--genome-dir=/path/to/genome/zip/files/ \
--zip-dir=/path/to/encoded/zip/ \
--length=10000 \
--n=10000 \
> seq.fasta
```
1. **`random`** tells the application to run the random tool.
2. **`--genome`** - the name of the genome.
3. **`--genome-dir`** - a directory that must contain gzipped tables listing chromosome sizes.
4. **`--zip-dir`** - a directory that must contain the encoded genome in a zip file (the output of **`encode`**).
4. **`--length`** - the length of the sequence to be created.
4. **`--n`** - the number of sequences to generate.
5. Once run, **`seq.fasta`** will be created containing **`n`** random sequences each of length **`length`**.
