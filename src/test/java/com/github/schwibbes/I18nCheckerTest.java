package com.github.schwibbes;


import org.apache.maven.plugin.testing.MojoRule;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;

public class I18nCheckerTest
{
    @Rule
    public MojoRule rule = new MojoRule()
    {
        @Override
        protected void before() throws Throwable
        {
        }

        @Override
        protected void after()
        {
        }
    };

    @Test
    public void testSomething()
            throws Exception
    {
        File pom = rule.getTestFile("src/test/resources/basic-test/pom.xml" );

        I18nChecker underTest = ( I18nChecker ) rule.lookupConfiguredMojo( pom, "18nCheck" );
        assertNotNull( underTest );
        underTest.execute();

        File outputDirectory = ( File ) rule.getVariableValueFromObject( underTest, "baseFilePath" );
        assertNotNull( outputDirectory );
        assertTrue( outputDirectory.exists() );
    }

}

