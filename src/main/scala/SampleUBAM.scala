package org.ydnawarehouse

import htsjdk.samtools.*

import java.io.{BufferedInputStream, File}
import java.net.URI

object SampleUBAM {
  def main(args: Array[String]): Unit = {
    if (args.length != 3) {
      println("Usage: SampleUBAM <input_ubam_url> <output_ubam_path> <target_base_count>")
      System.exit(1)
    }

    val inputURL = args(0)
    val outputPath = args(1)
    val targetBaseCount = args(2).toLong

    var totalBases = 0L
    val progressFrequency = 100000 // Update progress every 100,000 bases

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
          val progress = (totalBases.toDouble / targetBaseCount * 100).min(100).toInt // Limit to 100%
          println(f"Progress: $progress%d%% ${totalBases}%,d/${targetBaseCount}%,d bases)")
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
}