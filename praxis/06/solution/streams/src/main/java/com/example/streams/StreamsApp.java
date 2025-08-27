package com.example.streams;

import com.google.gson.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.time.Instant;
import java.util.Properties;

import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;

public class StreamsApp {
    public static void main(String[] args) {
        String broker =  "kafka:19092";
        String inTopic = "input-topic";
        String outTopic = "output-topic";
        String appId = "streams-example";

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // Make earliest reads convenient during demos
        props.put("auto.offset.reset", "earliest");

        StreamsBuilder b = new StreamsBuilder();
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();

        KStream<String, String> source = b.stream(inTopic, Consumed.with(Serdes.String(), Serdes.String()));

        KStream<String, String> transformed =
                source
                  .filter((key, value) -> {
                      try {
                          JsonObject obj = JsonParser.parseString(value).getAsJsonObject();
                          return obj.get("value").getAsInt() >= 50;
                      } catch (Exception e) {
                          return false; // drop malformed
                      }
                  })
                  .mapValues((key, value) -> {
                      try {
                          JsonObject obj = JsonParser.parseString(value).getAsJsonObject();
                          int v = obj.get("value").getAsInt();
                          obj.addProperty("double", v * 2);
                          obj.addProperty("processedAt", Instant.now().toString());
                          obj.addProperty("by", "kstreams-demo");
                          return gson.toJson(obj);
                      } catch (Exception e) {
                          return null;
                      }
                  })
                  .filter((k, v) -> v != null);

        transformed.to(outTopic, Produced.with(Serdes.String(), Serdes.String()));

        Topology topology = b.build();
        KafkaStreams streams = new KafkaStreams(topology, props);

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        streams.setUncaughtExceptionHandler((e) -> {
            e.printStackTrace();
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
        });

        streams.start();
        System.out.println("[streams] Started with topology:\n" + topology.describe());
    }
}
