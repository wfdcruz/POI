/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.traceable.poi.services;

import com.traceable.poi.domain.Meeting;
import com.traceable.poi.domain.PointInterest;
import com.traceable.poi.domain.Position;
import com.traceable.poi.repositories.MeetingRepository;
import com.traceable.poi.repositories.PointInterestRepository;
import com.traceable.poi.repositories.PositionRepository;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.stereotype.Service;

/**
 *
 * @author Wesley
 */
@Service
public class PoiService {

    private static final Logger LOG = LoggerFactory.getLogger(PoiService.class);

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PointInterestRepository interestRepository;

    public List<Meeting> calculate() {
        List<Position> positions = positionRepository.findAll();
        List<PointInterest> interests = interestRepository.findAll();
        List<Meeting> generatedMeetings = generateMeetings(positions, interests);

        return generatedMeetings;
    }

    private List<Meeting> generateMeetings(List<Position> positions, List<PointInterest> interests) {
        List<Meeting> lst = new ArrayList<>();

        for (PointInterest inter : interests) {
            for (Position position : positions) {
                if (verifyMeeting(position, inter)) {
                    Meeting m = new Meeting();
                    m.setInterest(inter);
                    m.setPosition(position);
                    try {
                        lst.add(meetingRepository.save(m));
                    } catch (Exception e) {
                        LOG.error("Exception on save: {} ", e);
                        return new ArrayList<>();
                    }

                }
            }
        }
        return lst;
    }

    private boolean verifyMeeting(Position pos, PointInterest interests) {

        //double earthRadius = 3958.75;//miles
        double dLat = Math.toRadians(Math.abs(interests.getLatitude()) - Math.abs(pos.getLatitude()));
        double dLng = Math.toRadians(Math.abs(interests.getLongitude()) - Math.abs(pos.getLongitude()));
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(Math.abs(pos.getLatitude())))
                * Math.cos(Math.toRadians(interests.getLatitude()));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));


        Boolean b = c < interests.getRadius();
        return b;
    }

}
