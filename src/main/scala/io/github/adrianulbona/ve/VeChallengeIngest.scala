package io.github.adrianulbona.ve

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}
import twitter4j.{GeoLocation, Place, Status}

/**
  * Created by adrianulbona on 21/11/2016.
  */
object VeChallengeIngest {

  case class Location(latitude: Double, longitude: Double)

  case class Tweet(time: Long, text: String, user: String, isRetweet: Boolean, country: String, location: Location)

  def main(args: Array[String]) {

    val spark = SparkSession.builder
      .master("local[*]")
      .appName("ve-challenge")
      .getOrCreate()

    import spark.sqlContext.implicits._

    val ssc = new StreamingContext(spark.sparkContext, Minutes(2))
    val stream = TwitterUtils.createStream(ssc, None, Seq("challenge"))

    stream.map(extract).map(normalize).foreachRDD((batch, time) => {
      val batchDF: DataFrame = batch.toDF.cache
      batchDF.groupBy($"country").count().toDF("country", "count").orderBy($"count".desc).show(6)
      batchDF.coalesce(1).write.parquet("tweets/batch=" + time.milliseconds)
      batchDF.unpersist()
    })

    ssc.start()
    ssc.awaitTermination()

    spark.stop()
  }

  def extract(status: Status): (Long, String, String, Boolean, Option[Place], Option[GeoLocation]) = {
    (status.getCreatedAt.getTime,
      status.getText,
      status.getUser.getName,
      status.isRetweet,
      Option(status.getPlace),
      Option(status.getGeoLocation))
  }

  def normalize(extract: (Long, String, String, Boolean, Option[Place], Option[GeoLocation])): Tweet = extract match {
    case (time: Long, text: String, user: String, isRetweet: Boolean, Some(place: Place), Some(geoLoc: GeoLocation)) =>
      Tweet(time, text, user, isRetweet, place.getCountryCode, Location(geoLoc.getLatitude, geoLoc.getLongitude))
    case (time: Long, text: String, user: String, isRetweet: Boolean, Some(place: Place), None) =>
      Tweet(time, text, user, isRetweet, place.getCountryCode, Location(Double.NaN, Double.NaN))
    case (time: Long, text: String, user: String, isRetweet: Boolean, None, Some(geoLoc: GeoLocation)) =>
      Tweet(time, text, user, isRetweet, "unknown", Location(geoLoc.getLatitude, geoLoc.getLongitude))
    case (time: Long, text: String, user: String, isRetweet: Boolean, None, None) =>
      Tweet(time, text, user, isRetweet, "unknown", Location(Double.NaN, Double.NaN))
  }
}
