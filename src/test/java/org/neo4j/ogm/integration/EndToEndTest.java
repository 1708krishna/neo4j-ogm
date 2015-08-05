/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.domain.bike.Frame;
import org.neo4j.ogm.domain.bike.Saddle;
import org.neo4j.ogm.domain.bike.Wheel;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * @author Michal Bachman
 */
public class EndToEndTest {

    @ClassRule
    public static Neo4jIntegrationTestRule databaseServerRule = new Neo4jIntegrationTestRule();

    private Session session;

    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.bike");
        session = sessionFactory.openSession(databaseServerRule.url());
    }

    @After
    public void clearDatabase() {
        databaseServerRule.clearDatabase();
    }

    @Test
    public void canSimpleQueryDatabase() {
        Saddle expected = new Saddle();
        expected.setPrice(29.95);
        expected.setMaterial("Leather");
        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();
        Bike bike = new Bike();
        bike.setBrand("Huffy");
        bike.setWheels(Arrays.asList(frontWheel, backWheel));
        bike.setSaddle(expected);
        session.save(bike);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("material", "Leather");
        Saddle actual = session.queryForObject(Saddle.class, "MATCH (saddle:Saddle{material: {material}}) RETURN saddle", parameters);

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getMaterial(), actual.getMaterial());

        HashMap<String, Object> parameters2 = new HashMap<>();
        parameters2.put("brand", "Huffy");
        Bike actual2 = session.queryForObject(Bike.class, "MATCH (bike:Bike{brand: {brand}}) RETURN bike", parameters2);

        assertEquals(bike.getId(), actual2.getId());
        assertEquals(bike.getBrand(), actual2.getBrand());

    }


    @Test
    public void canSimpleScalarQueryDatabase() {
        Saddle expected = new Saddle();
        expected.setPrice(29.95);
        expected.setMaterial("Leather");
        session.save(expected);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("material", "Leather");
        int actual = session.queryForObject(Integer.class, "MATCH (saddle:Saddle{material: {material}}) RETURN COUNT(saddle)", parameters);

        assertEquals(1, actual);
    }

    @Test
    public void canComplexQueryDatabase() {
        Saddle saddle = new Saddle();
        saddle.setPrice(29.95);
        saddle.setMaterial("Leather");
        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();
        Bike bike = new Bike();
        bike.setBrand("Huffy");
        bike.setWheels(Arrays.asList(frontWheel, backWheel));
        bike.setSaddle(saddle);

        session.save(bike);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("brand", "Huffy");
        Bike actual = session.queryForObject(Bike.class, "MATCH (bike:Bike{brand:{brand}})-[rels]-() RETURN bike, COLLECT(DISTINCT rels) as rels", parameters);

        assertEquals(bike.getId(), actual.getId());
        assertEquals(bike.getBrand(), actual.getBrand());
        assertEquals(bike.getWheels().size(), actual.getWheels().size());
        assertNotNull(actual.getSaddle());
    }

    @Test
    public void canComplexExecute() {
        Saddle saddle = new Saddle();
        saddle.setPrice(29.95);
        saddle.setMaterial("Leather");
        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();
        Bike bike = new Bike();
        bike.setBrand("Huffy");
        bike.setWheels(Arrays.asList(frontWheel, backWheel));
        bike.setSaddle(saddle);

        session.save(bike);

        Saddle newSaddle = new Saddle();
        newSaddle.setPrice(19.95);
        newSaddle.setMaterial("Vinyl");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("brand", "Huffy");
        parameters.put("saddle", newSaddle);
        session.execute("MATCH (bike:Bike{brand:{brand}})-[r]-(s:Saddle) SET s = {saddle}", parameters);

        HashMap<String, Object> parameters2 = new HashMap<>();
        parameters2.put("brand", "Huffy");
        Bike actual = session.queryForObject(Bike.class, "MATCH (bike:Bike{brand:{brand}})-[rels]-() RETURN bike, COLLECT(DISTINCT rels) as rels", parameters2);

        assertEquals(bike.getId(), actual.getId());
        assertEquals(bike.getBrand(), actual.getBrand());
        assertEquals(bike.getWheels().size(), actual.getWheels().size());
        assertEquals(actual.getSaddle().getMaterial(), "Vinyl");
    }

    @Test
    public void canSaveNewObjectTreeToDatabase() {

        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();
        Bike bike = new Bike();

        bike.setFrame(new Frame());
        bike.setSaddle(new Saddle());
        bike.setWheels(Arrays.asList(frontWheel, backWheel));

        assertNull(frontWheel.getId());
        assertNull(backWheel.getId());
        assertNull(bike.getId());
        assertNull(bike.getFrame().getId());
        assertNull(bike.getSaddle().getId());

        session.save(bike);

        assertNotNull(frontWheel.getId());
        assertNotNull(backWheel.getId());
        assertNotNull(bike.getId());
        assertNotNull(bike.getFrame().getId());
        assertNotNull(bike.getSaddle().getId());

    }

//    @Test
//    public void tourDeFrance() throws InterruptedException {
//
//        long now = -System.currentTimeMillis();
//
//        create1000bikes(1);
//
//        now += System.currentTimeMillis();
//
//        System.out.println("Number of separate requests: 1000");
//        System.out.println("Number of threads: 1");
//        System.out.println("Number of new objects to create per request: 5");
//        System.out.println("Number of relationships to create per request: 2");
//        System.out.println("Average number of requests per second to HTTP TX endpoint (avg. throughput) : " + (int) (1000000.0 / now));
//    }
//
//    @Test
//    public void multiThreadedTourDeFrance() throws InterruptedException {
//
//        final int NUM_THREADS=4;
//
//        long now = -System.currentTimeMillis();
//
//        create1000bikes(NUM_THREADS);
//
//        now += System.currentTimeMillis();
//
//        System.out.println("Number of separate requests: 1000");
//        System.out.println("Number of threads: " + NUM_THREADS);
//        System.out.println("Number of new objects to create per request: 5");
//        System.out.println("Number of relationships to create per request: 2");
//        System.out.println("Average number of requests per second to HTTP TX endpoint (avg. throughput) : " + (int) (1000000.0 / now));
//
//    }
//
//    @Test
//    public void testLoadCollectDistinct() throws InterruptedException {
//        create1000bikes(4);
//
//        long now = -System.currentTimeMillis();
//        session.clear();
//        session.query(Bike.class, "MATCH (n:Bike) WITH n MATCH p=(n)-[*0..2]-(m) RETURN collect(distinct p)", Utils.map());
//        now += System.currentTimeMillis();
//
//        System.out.println("time taken to load 1000 bikes using collect: " + now);
//    }
//
//    @Test
//    public void testLoadDistinct() throws InterruptedException {
//        create1000bikes(4);
//
//        long now = -System.currentTimeMillis();
//        session.clear();
//        session.query(Bike.class, "MATCH (n:Bike) WITH n MATCH p=(n)-[*0..2]-(m) RETURN distinct p", Utils.map());
//        now += System.currentTimeMillis();
//
//        System.out.println("time taken to load 1000 bikes not using collect: " + now);
//    }
//
//    @Test
//    public void testLoad() throws InterruptedException {
//        create1000bikes(4);
//
//        long now = -System.currentTimeMillis();
//        session.clear();
//        session.query(Bike.class, "MATCH (n:Bike) WITH n MATCH p=(n)-[*0..2]-(m) RETURN p", Utils.map());
//        now += System.currentTimeMillis();
//
//        System.out.println("time taken to load 1000 bikes not using collect or distinct: " + now);
//    }
//
//
//    @Test
//    public void testLoadNotUsingCollectWithoutDistinct() throws InterruptedException {
//        create1000bikes(4);
//
//        long now = -System.currentTimeMillis();
//        session.clear();
//        session.query(Bike.class, "MATCH (n:Bike) WITH n MATCH p=(n)-[*0..2]-(m) RETURN collect(p)", Utils.map());
//        now += System.currentTimeMillis();
//
//        System.out.println("time taken to load 1000 bikes not using collect or distinct: " + now);
//    }
//
//    private void create1000bikes(final int NUM_THREADS) throws InterruptedException {
//        List<Thread> threads = new ArrayList<>();
//        final int NUM_INSERTS = 1000 / NUM_THREADS;
//        for (int i = 0; i < NUM_THREADS; i++) {
//            Thread thread = new Thread( new Runnable() {
//
//                @Override
//                public void run() {
//                    Wheel frontWheel = new Wheel();
//                    Wheel backWheel = new Wheel();
//                    Bike bike = new Bike();
//
//                    bike.setFrame(new Frame());
//                    bike.setSaddle(new Saddle());
//                    bike.setWheels(Arrays.asList(frontWheel, backWheel));
//
//                    for (int i = 0; i < NUM_INSERTS; i++) {
//                        frontWheel.setId(null);
//                        backWheel.setId(null);
//                        bike.setId(null);
//                        bike.getFrame().setId(null);
//                        bike.getSaddle().setId(null);
//                        session.save(bike);
//                    }
//                }
//            });
//            threads.add(thread);
//            thread.start();
//        }
//
//        for (int i = 0; i < NUM_THREADS; i++) {
//            threads.get(i).join();
//        }
//
//    }

}
