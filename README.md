
# SampleUBAM

## Overview

The `SampleUBAM` is utility program designed to sample reads from an unaligned BAM (UBAM) file up to a specified target number of bases. It reads input UBAM data from a given URL, processes the records, and writes them to a specified output file in SAM or BAM format.

This tool is useful for subsampling genomic sequencing data from large UBAM files, ensuring that a specified base count is achieved when sampling reads. The program operates efficiently while providing progress updates during execution.

## Features

- Subsamples reads from an input UBAM file up to a target number of bases.
- Accepts input UBAM data from a URL.
- Outputs the sampled reads to a specified file in SAM or BAM format.

## Usage

```
Usage: SampleUBAM <input_ubam_url> <output_ubam_path> <target_base_count>
```

### Parameters:
1. **`<input_ubam_url>`**  
   The URL pointing to the UBAM file to be sampled.

2. **`<output_ubam_path>`**  
   The local file path where the sampled output will be written (BAM or SAM format).

3. **`<target_base_count>`**  
   The maximum number of bases to sample. Reads are added until this base count is reached.

### Example: Retrieve the first 90Gigabase from a 381GB file
```
java -cp your_program.jar org.ydnawarehouse.SampleUBAM \
     https://s3-us-west-2.amazonaws.com/human-pangenomics/working/HPRC/HG00117/raw_data/PacBio_HiFi/m84081_231110_195735_s1.hifi_reads.bc2071.bam \
     /mnt/md0/HumanPangenomics/PacBio/HG00117/HG00117.bam \
     93Gb
```

### Alternative Example: Using SBT
```
sbt "runMain org.ydnawarehouse.SampleUBAM https://s3-us-west-2.amazonaws.com/human-pangenomics/working/HPRC/HG00117/raw_data/PacBio_HiFi/m84081_231110_195735_s1.hifi_reads.bc2071.bam /mnt/md0/HumanPangenomics/PacBio/HG00117/HG00117.bam 93Gb" 2> /dev/null
```

The above command subsamples the UBAM data from the specified URL to create an output BAM file of at most 1,000,000 bases.

## Key Details

- **Input Handling:**  
  - The program uses `BufferedInputStream` to efficiently read data from the provided URL.
  - The HTSJDK library is employed for reading and writing SAM/BAM formats.

- **Output File:**  
  - Automatically adapts to the output file format based on its file extension (SAM or BAM).

- **Progress Reporting:**  
  - Periodically reports progress during sampling, updating every 100,000 bases or when sampling is completed.

- **Error Handling:**  
  - Gracefully captures and reports any exceptions during execution.

- **Performance:**  
  - Designed to handle large UBAM files with an efficient streaming approach.
  - Stops processing as soon as the target base count is reached, minimizing unnecessary computations.

## System Requirements

- **Java:** JDK 21 or newer.
- **Scala:** Version 3.3 or compatible.
- **Dependencies:** HTSJDK Library (SAM/BAM API).

To ensure compatibility, ensure that all necessary libraries (such as HTSJDK) are bundled with your application during compilation.

## Limitations

- The program does not allow for partial read lengths; only complete reads are counted towards the target base count.
- If the target base count is set higher than the number of bases available in the input UBAM, all available reads will be written without exceeding the input data size.

## Compilation and Execution

1. **Compile:**
   - Ensure you have the required dependencies, including HTSJDK.
   - Use your preferred Scala build tool (e.g., `sbt` or `.scala` compiler).

2. **Run:**
   - Execute the compiled JAR using Java, passing the required parameters (input URL, output path, and target count).

## Acknowledgements

- **HTSJDK:** This program leverages HTSJDK for handling SAM and BAM file formats.
- **Author:** James R. Kane, 2024.
- **License:** All rights reserved.

---

For any further inquiries or bug reports, please contact the program's maintainer.
