package fr.diod.searchAdherants.hashFind;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Assert;

/**
 * Unit test for simple App.
 */
public class HashNameTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public HashNameTest( String testName ) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( HashNameTest.class );
    }

    public void testHashNameNoTransform() {
    	String name = "aeiou";
		Assert.assertEquals(name, HashName.cleanName(name));
    }

    public void testHashNameE() {
    	String src  = "aéèëiou";
    	String dest = "aeeeiou";
		Assert.assertEquals(dest, HashName.cleanName(src));
    }
    
    public void testHashNameI() {
    	String src = "aïiou";
    	String dest = "aiiou";
		Assert.assertEquals(dest, HashName.cleanName(src));
    }
    
    public void testHashNameWord() {
    	String src = "aei|&o&u¨^^$qqq";
    	String dest = "aeiouqqq";
		Assert.assertEquals(dest, HashName.cleanName(src));
    }
    
    public void testHashNameDate() {
    	String src = "10/05/1978";
    	String dest = "10051978";
		Assert.assertEquals(dest, HashName.cleanName(src));
    }
}
