/*
 * Copyright © 2024, James R. Kane
 * All rights reserved.
 */
package org.ydnawarehouse

import htsjdk.samtools.*

import java.io.{BufferedInputStream, File}
import java.net.URI

/**
 * The `SampleUBAM` object provides functionality to sample reads from an input
 * unaligned BAM (UBAM) file up to a specified target number of bases and write
 * them to an output file. It reads the UBAM data from a given URL, processes the
 * records, and writes them to a specified output path in SAM or BAM format.
 *
 * Upon successful sampling, the program outputs the total number of sampled bases.
 */
object SampleUBAM {
  def main(args: Array[String]): Unit = {
    if (args.length != 3) {
      println("Usage: SampleUBAM <input_ubam_url> <output_ubam_path> <target_base_count>")
      println("  <target_base_count>: Specify as plain number (bases), e.g., 1000000")
      println("                       or human-readable formats, e.g., 10Mb, 1Gb.")
      System.exit(1)
    }

    val inputURL = args(0)
    val outputPath = args(1)
    val targetBaseCount = parseTargetBaseCount(args(2))

    var totalBases = 0L
    val progressFrequency = 100000 // Update progress every 100,000 bases
    var lastProgressTime = System.currentTimeMillis()
    var lastBaseCount = 0L

    // Use try-finally to ensure resources are safely closed
    var in: SamReader = null
    var out: SAMFileWriter = null

    try {
      // Open URL as input stream
      val urlStream = new BufferedInputStream(URI.create(inputURL).toURL.openStream())

      // Create SamReaderFactory with validation disabled to prevent premature exceptions
      val samReaderFactory = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.LENIENT)
      in = samReaderFactory.open(SamInputResource.of(urlStream))
      out = new SAMFileWriterFactory().makeSAMOrBAMWriter(in.getFileHeader, true, new File(outputPath))

      // Iterate over records and limit by base count
      val iterator = in.iterator()
      while (iterator.hasNext && totalBases < targetBaseCount) {
        val record = iterator.next()

        // Add the record to the output
        out.addAlignment(record)
        totalBases += record.getReadLength

        // Print progress at intervals
        if (totalBases % progressFrequency == 0 || totalBases >= targetBaseCount) {
          val currentTime = System.currentTimeMillis()
          val elapsedTime = (currentTime - lastProgressTime) / 1000.0 // seconds
          val basesTransferred = totalBases - lastBaseCount
          val transferRate = if (elapsedTime > 0) (basesTransferred / elapsedTime).toLong else 0L // bases per second
          lastProgressTime = currentTime
          lastBaseCount = totalBases
          val progress = (totalBases.toDouble / targetBaseCount * 100).min(100).toInt // Limit to 100%
          println(f"Progress: $progress%d%% ${totalBases}%,d/${targetBaseCount}%,d bases, $transferRate%,d bps)")
        }
      }
    } catch {
      case ex: Exception =>
        System.err.println(s"An error occurred: ${ex.getMessage}")
        ex.printStackTrace()
    } finally {
      // Close resources
      if (in != null) in.close()
      if (out != null) out.close()
    }

    println(s"Sampling complete. Total sampled bases: $totalBases.")
  }

  private def parseTargetBaseCount(input: String): Long = {
    if (input.matches("(?i)^\\d+Mb$")) {
      input.stripSuffix("Mb").stripSuffix("MB").toLong * 1000000
    } else if (input.matches("(?i)^\\d+Gb$")) {
      input.stripSuffix("Gb").stripSuffix("GB").toLong * 1000000000
    } else if (input.matches("^\\d+$")) {
      input.toLong
    } else {
      throw new IllegalArgumentException("Invalid target base count format. Use plain number, XMb, or XGb.")
    }
  }
}