package com.service.fusionweather.controller;

import com.service.fusionweather.util.KafkaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.service.fusionweather.entity.CurrentWeatherSummary;
import com.service.fusionweather.entity.ForecastWeatherSummary;
import com.service.fusionweather.entity.FusionWeatherSummary;

import io.vertx.core.json.Json;

@Component
public class FusionweatherImplDelegate {
  private static final Logger LOGGER = LoggerFactory.getLogger(FusionweatherImplDelegate.class);

  @Autowired
  RestTemplate restTemplate;

  @Value("${KAFKA_ENABLED:false}")
  private boolean kafkaEnabled = false;

  @Value("${KAFKA_TOPIC:abc}")
  private String kafkaTopic = "";

  @Autowired
  private KafkaUtil kafkaUtil;

  public FusionWeatherSummary showFusionWeather(String city, String user) {
    FusionWeatherSummary summary = new FusionWeatherSummary();
    summary.setCurrentWeather(achieveCurrentWeatherSummary(city, user));
    summary.setForecastWeather(achieveForecastWeatherSummary(city));

    return summary;
  }

  private CurrentWeatherSummary achieveCurrentWeatherSummary(String city, String user) {
    /*final String url = !StringUtils.isEmpty(user) && user.equalsIgnoreCase("beta") ?
        "http://127.0.0.1:13090/weather/show?city=" + city :
        "http://127.0.0.1:13093/weather/show?city=" + city;*/
    final String url = !StringUtils.isEmpty(user) && user.equalsIgnoreCase("beta") ?
        "http://weather/weather/show?city=" + city :
        "http://weather/weather/show?city=" + city;
    CurrentWeatherSummary su;
    try {
      Object s = restTemplate.getForObject(url, Object.class, new Object());
      su = Json.decodeValue(Json.encode(s), CurrentWeatherSummary.class);

      if(kafkaEnabled) {
        kafkaUtil.send(kafkaTopic, "Message from fusionweather: get current weather summary success");
      }
    } catch (Exception e) {
      LOGGER.error("FusionWeatherDataDelegate>> Failed to achieve the current weather summary", e);
      su = new CurrentWeatherSummary();

      if(kafkaEnabled) {
        kafkaUtil.send(kafkaTopic, "Message from fusionweather: get current weather summary failed");
      }
    }
    return su;
  }

  private ForecastWeatherSummary achieveForecastWeatherSummary(String city) {
    // final String url = "http://127.0.0.1:13091/forecast/show?city=" + city;
    final String url = "http://forecast/forecast/show?city=" + city;
    // final String url = "cse://forecast/forecast/show?city=" + city;
    ForecastWeatherSummary su;
    try {
      Object s = restTemplate.getForObject(url, Object.class, new Object());
      su = Json.decodeValue(Json.encode(s), ForecastWeatherSummary.class);

      if(kafkaEnabled) {
        kafkaUtil.send(kafkaTopic, "Message from fusionweather: get forecast weather summary success");
      }
    } catch (Exception e) {
      LOGGER.error("FusionWeatherDataDelegate>> Failed to achieve the forecast weather summary", e);
      su = new ForecastWeatherSummary();

      if(kafkaEnabled) {
        kafkaUtil.send(kafkaTopic, "Message from fusionweather: get forecast weather summary failed");
      }
    }
    return su;
  }
}
