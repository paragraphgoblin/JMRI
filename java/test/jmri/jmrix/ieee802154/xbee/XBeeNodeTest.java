package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * XBeeNodeTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeNode class
 *
 * @author	Paul Bender
 */
public class XBeeNodeTest{

    @Test
    public void testCtor() {
        XBeeNode m = new XBeeNode();
        Assert.assertNotNull("exists", m);
    }

    @Test
    public void testCtorWithParamters() {
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        XBeeNode node = new XBeeNode(pan,uad,gad);
        Assert.assertNotNull("exists", node);
        Assert.assertEquals("Node PAN address high byte", pan[0], node.getPANAddress()[0]);
        Assert.assertEquals("Node PAN address low byte", pan[1], node.getPANAddress()[1]);
        Assert.assertEquals("Node user address high byte", uad[0], node.getUserAddress()[0]);
        Assert.assertEquals("Node user address low byte", uad[1], node.getUserAddress()[1]);
        for (int i = 0; i < gad.length; i++) {
            Assert.assertEquals("Node global address byte " + i, gad[i], node.getGlobalAddress()[i]);
        }
    }

    @Test
    public void testSetPANAddress() {
        // test the code to set the User address
        XBeeNode node = new XBeeNode();
        byte pan[] = {(byte) 0x00, (byte) 0x01};
        node.setPANAddress(pan);
        Assert.assertEquals("Node PAN address high byte", pan[0], node.getPANAddress()[0]);
        Assert.assertEquals("Node PAN address low byte", pan[1], node.getPANAddress()[1]);
    }

    @Test
    public void testSetUserAddress() {
        // test the code to set the User address
        XBeeNode node = new XBeeNode();
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        Assert.assertEquals("Node user address high byte", uad[0], node.getUserAddress()[0]);
        Assert.assertEquals("Node user address low byte", uad[1], node.getUserAddress()[1]);
    }

    @Test
    public void testSetGlobalAddress() {
        // test the code to set the User address
        XBeeNode node = new XBeeNode();
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        for (int i = 0; i < gad.length; i++) {
            Assert.assertEquals("Node global address byte " + i, gad[i], node.getGlobalAddress()[i]);
        }
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
