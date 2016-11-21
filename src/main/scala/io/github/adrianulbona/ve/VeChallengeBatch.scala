package io.github.adrianulbona.ve

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Minutes, StreamingContext}
import twitter4j.{GeoLocation, Place, Status}

/**
  * Created by adrianulbona on 21/11/2016.
  */
object VeChallengeBatch {

  case class Location(latitude: Double, longitude: Double)

  case class Tweet(time: Long, text: String, user: String, isRetweet: Boolean, country: String, location: Location)

  def main(args: Array[String]) {

    val spark = SparkSession.builder
      .master("local[*]")
      .appName("ve-challenge")
      .getOrCreate()

    import spark.sqlContext.implicits._

    spark.read.parquet("tweets").groupBy($"country").count().toDF("country", "count").orderBy($"count".desc).show(6)

    spark.stop()
  }
}
