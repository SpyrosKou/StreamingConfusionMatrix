package com.streaming.model

/**
 * Created at 2021-04-25 on 12:12 
 *
 * @author Spyros Koukas
 */
final class Observation(val actualLabel: String, val observation: Map[String, Long]) {

  /**
   * Construct with empty Observation Map
   * @param actualLabel
   */
  def this(actualLabel: String) {
    this(actualLabel, Map.empty)
  }

}
