package io.github.adrianulbona.ve

import java.text.SimpleDateFormat
import java.util.Date

import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Minutes, StreamingContext}
import twitter4j.Place

/**
  * Created by adrianulbona on 21/11/2016.
  */
object VeChallenge {

  def main(args: Array[String]) {

    val spark = SparkSession.builder
      .master("local[*]")
      .appName("ve-challenge")
      .getOrCreate()

    val ssc = new StreamingContext(spark.sparkContext, Minutes(1))
    val stream = TwitterUtils.createStream(ssc, None, Seq("challenge"))

    val places: DStream[Place] = stream.map(status => Option(status.getPlace))
      .filter(optionPlace => optionPlace.isDefined)
      .map(place => place.get)

    places.map(place => place.getCountryCode)
      .countByValue()
      .foreachRDD(batch => printStats(batch.top(5)))

    ssc.start()
    ssc.awaitTermination()

    spark.stop()
  }

  def printStats(top5Countries: Array[(String, Long)]) {
    println(new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss").format(new Date()))
    top5Countries.foreach(println)
  }
}
