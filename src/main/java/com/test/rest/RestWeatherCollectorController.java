package com.test.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.data.DataContainer;
import com.test.model.DataPoint;
import com.test.model.DataPointType;
import org.springframework.web.bind.annotation.*;
import com.test.exception.WeatherException;
import com.test.model.AirportData;
import com.test.service.CollectorService;
import com.test.service.QueryService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * A REST implementation of the WeatherCollector API. Accessible only to airport weather collection
 * sites via secure VPN.
 *
 * @author code test administrator
 */
@RestController
@RequestMapping("/collect")
//здесь и во втором контроллере я бы избавился от Response и возвращал бы только необходимую информацию
public class RestWeatherCollectorController {

    @Inject
    private CollectorService collectorService;
    @Inject
    private QueryService queryService;
    @Inject
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public Response ping() {
        return Response.ok("ready").build();
    }

    @PostMapping("/weather/{iata}/{pointType}")
    public Response updateWeather(@PathVariable String iataCode, @PathVariable String pointType, @RequestBody String datapointJson) {
        try {
            collectorService.addDataPoint(iataCode, DataPointType.valueOf(pointType.toUpperCase()), objectMapper.readValue(datapointJson, DataPoint.class));
            return Response.ok().build();
        } catch (WeatherException e) {
            return Response.status(422).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }


    @GetMapping("/airports")
    public Response getAirports()
    {
        return Response.ok(collectorService.getAirports()).build();
    }


    @GetMapping("/airport/{iata}")
    public Response getAirport(@PathVariable String iata) {
        AirportData airportData = queryService.findAirportData(iata);
        if (airportData == null){
                    return Response.status(404).build();
        }
        return Response.ok(airportData).build();
    }

    @PostMapping("/airport")
    public Response addAirport(@RequestParam("iata") String iata, @RequestParam("latitude") Integer latitude, @RequestParam("longitude") Integer longitude) {
        return Response.ok(DataContainer.addAirport(iata,latitude,longitude)).build();
    }

    @DeleteMapping("/airport/{iata}")
    public Response deleteAirport(@PathVariable("iata") String iata) {
        return Response.ok(DataContainer.delAirport(iata)).build();
    }

    @GetMapping("/exit")
    public Response exit() {
        System.exit(0);
        return Response.noContent().build();
    }
}