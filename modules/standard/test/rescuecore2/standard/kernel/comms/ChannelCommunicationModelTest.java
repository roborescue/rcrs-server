package rescuecore2.standard.kernel.comms;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import rescuecore2.config.Config;
import rescuecore2.messages.Command;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.messages.AKSubscribe;
import rescuecore2.standard.messages.AKSpeak;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Iterator;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

public class ChannelCommunicationModelTest {
    private final static byte[] TEST_BYTES = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};
    private final static byte[] TEST_BYTES_2 = {0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16};

    private final static Collection<AKSpeak> NO_MESSAGES = new HashSet<AKSpeak>();

    private ChannelCommunicationModel model;
    private Civilian civ1;
    private Civilian civ2;
    private Civilian civ3;
    private Civilian civ4;
    private FireBrigade fb1;
    private FireBrigade fb2;
    private FireStation fs1;
    private FireStation fs2;
    private PoliceForce pf1;
    private PoliceForce pf2;
    private PoliceOffice po1;
    private PoliceOffice po2;
    private AmbulanceTeam at1;
    private AmbulanceTeam at2;
    private AmbulanceCentre ac1;
    private AmbulanceCentre ac2;
    private Road road;

    private Collection<Command> commands;
    private Collection<AKSpeak> expected;
    private Collection<AKSpeak> civ1Expected;
    private Collection<AKSpeak> civ2Expected;
    private Collection<AKSpeak> civ3Expected;
    private Collection<AKSpeak> civ4Expected;

    private ByteArrayOutputStream logBytes;

    @Before
    public void setup() {
        model = new ChannelCommunicationModel();
        commands = new ArrayList<Command>();
        expected = new ArrayList<AKSpeak>();
        civ1Expected = new ArrayList<AKSpeak>();
        civ2Expected = new ArrayList<AKSpeak>();
        civ3Expected = new ArrayList<AKSpeak>();
        civ4Expected = new ArrayList<AKSpeak>();
        logBytes = new ByteArrayOutputStream();
        PrintStream buffer = new PrintStream(logBytes);
        System.setOut(buffer);
    }

    @After
    public void cleanup() {
        System.err.println(new String(logBytes.toByteArray()));
    }

    @Test
    public void testName() {
        assertEquals("Channel communication model", model.toString());
    }

    @Test
    public void testSubscribeBasics() {
        model.initialise(createRadioConfig(), createWorldModel());
        commands.add(new AKSubscribe(civ1.getID(), 1, 0));
        AKSpeak speak1 = new AKSpeak(civ1.getID(), 1, 0, TEST_BYTES);
        commands.add(speak1);
        civ1Expected.add(speak1);
        model.process(1, commands);
        checkHearing(civ1, civ1Expected);
        checkHearing(civ2, civ2Expected);
    }

    @Test
    public void testSubscribeMultipleChannels() {
        model.initialise(createRadioConfig(), createWorldModel());
        commands.add(new AKSubscribe(civ1.getID(), 1, 0));
        commands.add(new AKSubscribe(civ2.getID(), 1, 1));
        commands.add(new AKSubscribe(civ3.getID(), 1, 0, 1));
        AKSpeak speak1 = new AKSpeak(civ1.getID(), 1, 0, TEST_BYTES);
        AKSpeak speak2 = new AKSpeak(civ1.getID(), 1, 1, TEST_BYTES);
        AKSpeak speak3 = new AKSpeak(civ1.getID(), 1, 2, TEST_BYTES);
        commands.add(speak1);
        commands.add(speak2);
        commands.add(speak3);
        civ1Expected.add(speak1);
        civ2Expected.add(speak2);
        civ3Expected.add(speak1);
        civ3Expected.add(speak2);
        model.process(1, commands);
        checkHearing(civ1, civ1Expected);
        checkHearing(civ2, civ2Expected);
        checkHearing(civ3, civ3Expected);
        checkHearing(civ4, civ4Expected);
    }

    @Test
    public void testSubscribeMultipleTimesteps() {
        model.initialise(createRadioConfig(), createWorldModel());
        AKSpeak speak1 = new AKSpeak(civ1.getID(), 2, 0, TEST_BYTES);
        AKSpeak speak2 = new AKSpeak(civ1.getID(), 3, 0, TEST_BYTES);
        commands.add(new AKSubscribe(civ1.getID(), 1, 0));
        commands.add(new AKSubscribe(civ2.getID(), 1, 0));
        model.process(1, commands);
        commands.clear();
        commands.add(speak1);
        model.process(2, commands);
        civ1Expected.add(speak1);
        civ2Expected.add(speak1);
        checkHearing(civ1, civ1Expected);
        checkHearing(civ2, civ2Expected);
        commands.clear();
        civ1Expected.clear();
        civ2Expected.clear();
        commands.add(speak2);
        civ1Expected.add(speak2);
        civ2Expected.add(speak2);
        model.process(3, commands);
        checkHearing(civ1, civ1Expected);
        checkHearing(civ2, civ2Expected);
    }

    @Test
    public void testVoiceDoesNotNeedSubscribe() {
        model.initialise(createVoiceConfig(), createWorldModel());
        AKSpeak speak1 = new AKSpeak(civ1.getID(), 1, 0, TEST_BYTES);
        AKSpeak speak2 = new AKSpeak(civ1.getID(), 1, 0, TEST_BYTES);
        commands.add(speak1);
        commands.add(speak2);
        civ1Expected.add(speak1);
        civ1Expected.add(speak2);
        model.process(1, commands);
        checkHearing(civ1, civ1Expected);
        checkHearing(civ2, civ1Expected);
        checkHearing(civ3, civ1Expected);
        checkHearing(civ4, civ1Expected);
    }

    @Test
    public void testVoiceWorksWithSubscribe() {
        model.initialise(createCombinedConfig(), createWorldModel());
        AKSpeak speak1 = new AKSpeak(civ1.getID(), 1, 0, TEST_BYTES);
        AKSpeak speak2 = new AKSpeak(civ1.getID(), 1, 0, TEST_BYTES);
        commands.add(new AKSubscribe(civ2.getID(), 1, 0));
        commands.add(new AKSubscribe(civ3.getID(), 1, 1));
        commands.add(new AKSubscribe(civ4.getID(), 1, 0, 1));
        commands.add(speak1);
        commands.add(speak2);
        civ1Expected.add(speak1);
        civ1Expected.add(speak2);
        model.process(1, commands);
        checkHearing(civ1, civ1Expected);
        checkHearing(civ2, civ1Expected);
        checkHearing(civ3, civ1Expected);
        checkHearing(civ4, civ1Expected);
    }

    @Test
    public void testVoiceDistance() {
        model.initialise(createVoiceConfig(), createWorldModel());
        civ1.setX(0);
        civ1.setY(0);
        civ2.setX(10000);
        civ2.setY(0);
        civ3.setX(20000);
        civ3.setY(0);
        civ4.setX(-10000);
        civ4.setY(-10000);
        AKSpeak speak1 = new AKSpeak(civ1.getID(), 1, 0, TEST_BYTES);
        AKSpeak speak2 = new AKSpeak(civ2.getID(), 1, 0, TEST_BYTES);
        AKSpeak speak3 = new AKSpeak(civ3.getID(), 1, 0, TEST_BYTES);
        AKSpeak speak4 = new AKSpeak(civ4.getID(), 1, 0, TEST_BYTES);
        commands.add(speak1);
        commands.add(speak2);
        commands.add(speak3);
        commands.add(speak4);
        civ1Expected.add(speak1);
        civ1Expected.add(speak2);
        civ2Expected.add(speak1);
        civ2Expected.add(speak2);
        civ2Expected.add(speak3);
        civ3Expected.add(speak2);
        civ3Expected.add(speak3);
        civ4Expected.add(speak4);
        model.process(1, commands);
        checkHearing(civ1, civ1Expected);
        checkHearing(civ2, civ2Expected);
        checkHearing(civ3, civ3Expected);
        checkHearing(civ4, civ4Expected);
    }

    @Test
    public void testInvalidConfig() {
        model.initialise(createInvalidChannelTypeConfig(), createWorldModel());
        // Check that a warning went to the log
        assertLogContains("Unrecognised channel type");
        // Check that no channels exist
        assertEquals(0, model.getAllChannels().size());
    }

    @Test
    public void testPartlyInvalidConfig() {
        model.initialise(createInvalidChannelTypeConfigWithSomeValid(), createWorldModel());
        // Check that a warning went to the log
        assertLogContains("Unrecognised channel type");
        // Check that one channel was still created
        assertEquals(1, model.getAllChannels().size());
    }

    @Test(expected=rescuecore2.config.NoSuchConfigOptionException.class)
    public void testMissingChannelsConfig() {
        model.initialise(createMissingChannelsConfig(), createWorldModel());
    }

    @Test
    public void testExtraChannelsConfig() {
        model.initialise(createExtraChannelsConfig(), createWorldModel());
        // Check that only one channel was created
        assertEquals(1, model.getAllChannels().size());
    }

    @Test
    public void testIncorrectChannel() {
        model.initialise(createRadioConfig(), createWorldModel());
        commands.add(new AKSpeak(civ1.getID(), 1, 10, TEST_BYTES));
        model.process(1, commands);
        assertLogContains("Unrecognised channel: 10");
        // Check that no-one heard the message
        checkHearing(civ1, NO_MESSAGES);
        checkHearing(civ2, NO_MESSAGES);
        checkHearing(civ3, NO_MESSAGES);
        checkHearing(civ4, NO_MESSAGES);
    }

    @Test
    public void testSubscribeNonExistantEntity() {
        model.initialise(createRadioConfig(), createWorldModel());
        commands.add(new AKSubscribe(new EntityID(-5), 1, 0));
        commands.add(new AKSpeak(civ1.getID(), 1, 1, TEST_BYTES));
        model.process(1, commands);
        assertLogContains("Couldn't find entity -5");
        // Check that no-one heard any messages on channel 0
        checkHearing(civ1, NO_MESSAGES);
        checkHearing(civ2, NO_MESSAGES);
        checkHearing(civ3, NO_MESSAGES);
        checkHearing(civ4, NO_MESSAGES);
    }

    @Test
    public void testSubscriptionLimits() {
        model.initialise(createRadioConfig(), createWorldModel());        
        commands.add(new AKSubscribe(civ1.getID(), 1, 0, 1));
        commands.add(new AKSubscribe(civ2.getID(), 1, 0, 1, 2));
        commands.add(new AKSubscribe(fb1.getID(), 1, 0, 1));
        commands.add(new AKSubscribe(fb2.getID(), 1, 0, 1, 2));
        commands.add(new AKSubscribe(pf1.getID(), 1, 0, 1));
        commands.add(new AKSubscribe(pf2.getID(), 1, 0, 1, 2));
        commands.add(new AKSubscribe(at1.getID(), 1, 0, 1));
        commands.add(new AKSubscribe(at2.getID(), 1, 0, 1, 2));
        commands.add(new AKSubscribe(fs1.getID(), 1, 0, 1, 2));
        commands.add(new AKSubscribe(fs2.getID(), 1, 0, 1, 2, 3));
        commands.add(new AKSubscribe(po1.getID(), 1, 0, 1, 2));
        commands.add(new AKSubscribe(po2.getID(), 1, 0, 1, 2, 3));
        commands.add(new AKSubscribe(ac1.getID(), 1, 0, 1, 2));
        commands.add(new AKSubscribe(ac2.getID(), 1, 0, 1, 2, 3));
        AKSpeak speak = new AKSpeak(fb1.getID(), 1, 0, TEST_BYTES);
        commands.add(speak);
        expected.add(speak);
        model.process(1, commands);
        assertLogContains("Agent " + civ2.getID() + " tried to subscribe to 3 channels but only 2 allowed");
        assertLogContains("Agent " + fb2.getID() + " tried to subscribe to 3 channels but only 2 allowed");
        assertLogContains("Agent " + pf2.getID() + " tried to subscribe to 3 channels but only 2 allowed");
        assertLogContains("Agent " + at2.getID() + " tried to subscribe to 3 channels but only 2 allowed");
        assertLogContains("Agent " + fs2.getID() + " tried to subscribe to 4 channels but only 3 allowed");
        assertLogContains("Agent " + po2.getID() + " tried to subscribe to 4 channels but only 3 allowed");
        assertLogContains("Agent " + ac2.getID() + " tried to subscribe to 4 channels but only 3 allowed");
        checkHearing(civ1, expected);
        checkHearing(fb1, expected);
        checkHearing(fs1, expected);
        checkHearing(pf1, expected);
        checkHearing(po1, expected);
        checkHearing(at1, expected);
        checkHearing(ac1, expected);
        checkHearing(civ2, NO_MESSAGES);
        checkHearing(fb2, NO_MESSAGES);
        checkHearing(fs2, NO_MESSAGES);
        checkHearing(pf2, NO_MESSAGES);
        checkHearing(po2, NO_MESSAGES);
        checkHearing(at2, NO_MESSAGES);
        checkHearing(ac2, NO_MESSAGES);
    }

    @Test
    public void testSubscriptionRemovesOldChannels() {
        model.initialise(createRadioConfig(), createWorldModel());        
        // Subscribe to channel 0
        commands.add(new AKSubscribe(civ1.getID(), 1, 0));
        AKSpeak speak1 = new AKSpeak(civ1.getID(), 1, 0, TEST_BYTES);
        AKSpeak speak2 = new AKSpeak(civ1.getID(), 1, 1, TEST_BYTES_2);
        AKSpeak speak3 = new AKSpeak(civ1.getID(), 2, 0, TEST_BYTES);
        AKSpeak speak4 = new AKSpeak(civ1.getID(), 2, 1, TEST_BYTES_2);
        commands.add(speak1);
        commands.add(speak2);
        expected.add(speak1);
        model.process(1, commands);
        checkHearing(civ1, expected);
        // Now subscribe to channel 1
        commands.clear();
        expected.clear();
        commands.add(new AKSubscribe(civ1.getID(), 2, 1));
        commands.add(speak3);
        commands.add(speak4);
        expected.add(speak4);
        model.process(2, commands);
        checkHearing(civ1, expected);
    }

    @Test
    public void testBadSubscriptionKeepsOKChannels() {
        model.initialise(createRadioConfig(), createWorldModel());        
        // Subscribe to channel 0
        commands.add(new AKSubscribe(civ1.getID(), 1, 0));
        AKSpeak speak1 = new AKSpeak(civ1.getID(), 1, 0, TEST_BYTES);
        AKSpeak speak2 = new AKSpeak(civ1.getID(), 1, 1, TEST_BYTES_2);
        AKSpeak speak3 = new AKSpeak(civ1.getID(), 2, 0, TEST_BYTES);
        AKSpeak speak4 = new AKSpeak(civ1.getID(), 2, 1, TEST_BYTES_2);
        commands.add(speak1);
        commands.add(speak2);
        expected.add(speak1);
        model.process(1, commands);
        checkHearing(civ1, expected);
        // Now subscribe to channels 1 and 100
        commands.clear();
        expected.clear();
        commands.add(new AKSubscribe(civ1.getID(), 2, 1, 100));
        commands.add(speak3);
        commands.add(speak4);
        expected.add(speak4);
        model.process(2, commands);
        assertLogContains("Agent " + civ1.getID() + " tried to subscribe to non-existant channel 100");
        checkHearing(civ1, expected);
    }

    @Test
    public void testSubscribeFromNonAgent() {
        model.initialise(createRadioConfig(), createWorldModel());        
        // Subscribe to channel 0
        commands.add(new AKSubscribe(civ1.getID(), 1, 0));
        commands.add(new AKSubscribe(road.getID(), 1, 0));
        AKSpeak speak = new AKSpeak(civ1.getID(), 1, 0, TEST_BYTES);
        commands.add(speak);
        expected.add(speak);
        model.process(1, commands);
        checkHearing(civ1, expected);
        assertLogContains("I don't know how to handle subscriptions for this entity: " + road.toString());
    }

    private Config createRadioConfig() {
        Config config = new Config();
        config.setValue("comms.channels.count", "4");
        config.setValue("comms.channels.0.type", "radio");
        config.setValue("comms.channels.0.bandwidth", "128");
        config.setValue("comms.channels.1.type", "radio");
        config.setValue("comms.channels.1.bandwidth", "128");
        config.setValue("comms.channels.2.type", "radio");
        config.setValue("comms.channels.2.bandwidth", "128");
        config.setValue("comms.channels.3.type", "radio");
        config.setValue("comms.channels.3.bandwidth", "128");

        config.setValue("comms.channels.max.platoon", "2");
        config.setValue("comms.channels.max.centre", "3");
        return config;
    }

    private Config createVoiceConfig() {
        Config config = new Config();
        config.setValue("comms.channels.count", "1");
        config.setValue("comms.channels.0.type", "voice");
        config.setValue("comms.channels.0.range", "10000");
        config.setValue("comms.channels.0.messages.size", "100");
        config.setValue("comms.channels.0.messages.max", "2");
        return config;
    }

    private Config createCombinedConfig() {
        Config config = new Config();
        config.setValue("comms.channels.count", "4");
        config.setValue("comms.channels.0.type", "voice");
        config.setValue("comms.channels.0.range", "10000");
        config.setValue("comms.channels.0.messages.size", "100");
        config.setValue("comms.channels.0.messages.max", "2");

        config.setValue("comms.channels.1.type", "radio");
        config.setValue("comms.channels.1.bandwidth", "128");
        config.setValue("comms.channels.2.type", "radio");
        config.setValue("comms.channels.2.bandwidth", "128");
        config.setValue("comms.channels.3.type", "radio");
        config.setValue("comms.channels.3.bandwidth", "128");

        config.setValue("comms.channels.max.platoon", "2");
        config.setValue("comms.channels.max.centre", "2");
        return config;
    }

    private Config createInvalidChannelTypeConfig() {
        Config config = new Config();
        config.setValue("comms.channels.count", "1");
        config.setValue("comms.channels.0.type", "blah");
        return config;
    }

    private Config createInvalidChannelTypeConfigWithSomeValid() {
        Config config = new Config();
        config.setValue("comms.channels.count", "2");
        config.setValue("comms.channels.0.type", "blah");
        config.setValue("comms.channels.1.type", "radio");
        config.setValue("comms.channels.1.bandwidth", "128");
        return config;
    }

    private Config createMissingChannelsConfig() {
        Config config = new Config();
        config.setValue("comms.channels.count", "2");
        config.setValue("comms.channels.0.type", "radio");
        config.setValue("comms.channels.0.bandwidth", "128");
        return config;
    }

    private Config createExtraChannelsConfig() {
        Config config = new Config();
        config.setValue("comms.channels.count", "1");
        config.setValue("comms.channels.0.type", "radio");
        config.setValue("comms.channels.0.bandwidth", "128");
        config.setValue("comms.channels.1.type", "radio");
        config.setValue("comms.channels.1.bandwidth", "128");
        return config;
    }

    private WorldModel<? extends Entity> createWorldModel() {
        StandardWorldModel world = new StandardWorldModel();
        civ1 = new Civilian(new EntityID(1));
        civ2 = new Civilian(new EntityID(2));
        civ3 = new Civilian(new EntityID(3));
        civ4 = new Civilian(new EntityID(4));
        fb1 = new FireBrigade(new EntityID(5));
        fb2 = new FireBrigade(new EntityID(6));
        pf1 = new PoliceForce(new EntityID(7));
        pf2 = new PoliceForce(new EntityID(8));
        at1 = new AmbulanceTeam(new EntityID(9));
        at2 = new AmbulanceTeam(new EntityID(10));
        fs1 = new FireStation(new EntityID(11));
        fs2 = new FireStation(new EntityID(12));
        po1 = new PoliceOffice(new EntityID(13));
        po2 = new PoliceOffice(new EntityID(14));
        ac1 = new AmbulanceCentre(new EntityID(15));
        ac2 = new AmbulanceCentre(new EntityID(16));
        road = new Road(new EntityID(17));
        civ1.setX(0);
        civ1.setY(0);
        civ2.setX(0);
        civ2.setY(0);
        civ3.setX(0);
        civ3.setY(0);
        civ4.setX(0);
        civ4.setY(0);
        world.addEntity(civ1);
        world.addEntity(civ2);
        world.addEntity(civ3);
        world.addEntity(civ4);
        world.addEntity(fb1);
        world.addEntity(fb2);
        world.addEntity(pf1);
        world.addEntity(pf2);
        world.addEntity(at1);
        world.addEntity(at2);
        world.addEntity(fs1);
        world.addEntity(fs2);
        world.addEntity(po1);
        world.addEntity(po2);
        world.addEntity(ac1);
        world.addEntity(ac2);
        world.addEntity(road);
        return world;
    }

    private void checkHearing(Entity agent, Collection<AKSpeak> expected) {
        Collection<Command> hearing = model.getHearing(agent);
        System.err.println("Expected: " + expected);
        System.err.println("Heard   : " + hearing);
        assertEquals(expected.size(), hearing.size());
        Collection<AKSpeak> remaining = new HashSet<AKSpeak>(expected);
        for (Command next : hearing) {
            AKSpeak speak = (AKSpeak)next;
            assertTrue(find(speak, remaining));
        }
    }

    private boolean find(AKSpeak speak, Collection<AKSpeak> possible) {
        for (Iterator<AKSpeak> it = possible.iterator(); it.hasNext(); ) {
            AKSpeak next = it.next();
            if (next.getAgentID().equals(speak.getAgentID())
                && next.getChannel() == speak.getChannel()
                && Arrays.equals(next.getContent(), speak.getContent())
                && next.getTime() == speak.getTime()) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    private void assertLogContains(String s) {
        System.err.println("Looking for " + s);
        String test = new String(logBytes.toByteArray());
        if (!test.contains(s)) {
            System.err.println("Couldn't find '" + s + "'");
        }
        assertTrue(test.contains(s));
    }
}