/*
 * Copyright © 2024, James R. Kane
 * All rights reserved.
 */
package org.ydnawarehouse

import htsjdk.samtools.*

import java.io.{BufferedInputStream, File}
import java.net.URI
import scala.util.Using

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
    val (inputURL, outputPath, targetBaseCount) = validateArguments(args)
    val progressFrequency = ProgressFrequency
    var totalBases = 0L

    Using.Manager { use =>
      val urlStream = use(new BufferedInputStream(URI.create(inputURL).toURL.openStream()))
      val samReaderFactory = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.LENIENT)
      val in = use(samReaderFactory.open(SamInputResource.of(urlStream)))
      val out = use(new SAMFileWriterFactory().makeSAMOrBAMWriter(in.getFileHeader, true, new File(outputPath)))

      val iterator = in.iterator()
      while (iterator.hasNext && totalBases < targetBaseCount) {
        totalBases += processRecord(iterator.next(), out)
        printProgress(totalBases, targetBaseCount, progressFrequency)
      }
    }.recover {
      case ex: Exception =>
        System.err.println(s"An error occurred: ${ex.getMessage}")
        ex.printStackTrace()
    }

    println(s"Sampling complete. Total sampled bases: $totalBases.")
  }

  private def validateArguments(args: Array[String]): (String, String, Long) = {
    require(args.length == 3,
      "Usage: SampleUBAM <input_ubam_url> <output_ubam_path> <target_base_count>\n" +
      "  <target_base_count>: Specify as plain number (bases), e.g., 1000000\n" +
      "                       or human-readable formats, e.g., 10Mb, 1Gb.")
    val inputURL = args(0)
    val outputPath = args(1)
    val targetBaseCount = parseTargetBaseCount(args(2))
    (inputURL, outputPath, targetBaseCount)
  }

  private val ProgressFrequency = 100_000
  private val KB = 1_000
  private val MB = 1_000_000
  private val GB = 1_000_000_000

  private var lastProgressTime = System.currentTimeMillis()
  private var lastBaseCount = 0L

  private def processRecord(record: SAMRecord, out: SAMFileWriter): Long = {
    out.addAlignment(record)
    record.getReadLength
  }

  private def printProgress(totalBases: Long, targetBaseCount: Long, progressFrequency: Int): Unit = {
    if (totalBases % progressFrequency == 0 || totalBases >= targetBaseCount) {
      val currentTime = System.currentTimeMillis()
      val elapsedTime = (currentTime - lastProgressTime) / 1000.0
      val basesTransferred = totalBases - lastBaseCount
      val transferRate = if (elapsedTime > 0) (basesTransferred / elapsedTime).toLong else 0L
      lastProgressTime = currentTime
      lastBaseCount = totalBases
      val progress = (totalBases.toDouble / targetBaseCount * 100).min(100).toInt
      println(f"Progress: $progress%% ${formatNumberAbbreviated(totalBases)} of ${formatNumberAbbreviated(targetBaseCount)} bases (${formatNumberAbbreviated(transferRate)} bps)")
    }
  }

  private def formatNumberAbbreviated(value: Long): String = {
    if (value >= 1_000_000_000) f"${value / GB}G"
    else if (value >= 1_000_000) f"${value / MB}M"
    else if (value >= 1_000) f"${value / KB}K"
    else value.toString
  }

  private def parseTargetBaseCount(input: String): Long = {
    if (input.matches("(?i)^\\d+Mb$")) {
      input.stripSuffix("Mb").stripSuffix("MB").toLong * MB
    } else if (input.matches("(?i)^\\d+Gb$")) {
      input.stripSuffix("Gb").stripSuffix("GB").toLong * GB
    } else if (input.matches("^\\d+$")) {
      input.toLong
    } else {
      throw new IllegalArgumentException("Invalid target base count format. Use plain number, XMb, or XGb.")
    }
  }
}