//
//  ========================================================================
//  Copyright (c) 1995-2015 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//


package org.eclipse.jetty.gcloud.session;

import static org.junit.Assert.fail;
import org.eclipse.jetty.server.session.AbstractStopSessionManagerPreserveSessionTest;
import org.eclipse.jetty.server.session.AbstractTestServer;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * StopSessionManagerPreserveSessionTest
 *
 *
 */
public class StopSessionManagerPreserveSessionTest extends AbstractStopSessionManagerPreserveSessionTest
{
    static GCloudSessionTestSupport _testSupport;

    @BeforeClass
    public static void setup () throws Exception
    {
        String projectId = System.getProperty("test.projectId", null);
        String port = System.getProperty("test.port","0");
        _testSupport = new GCloudSessionTestSupport(projectId,
                                                    Integer.parseInt(port),
                                                    null);
        _testSupport.setUp();
    }

    @AfterClass
    public static void teardown () throws Exception
    {
        _testSupport.tearDown();
    }

    /** 
     * @see org.eclipse.jetty.server.session.AbstractStopSessionManagerPreserveSessionTest#checkSessionPersisted(boolean)
     */
    @Override
    public void checkSessionPersisted(boolean expected)
    {
        try
        {
            _testSupport.assertSessions(1);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }

    }

    /** 
     * @see org.eclipse.jetty.server.session.AbstractStopSessionManagerPreserveSessionTest#createServer(int)
     */
    @Override
    public AbstractTestServer createServer(int port)
    {
        return  new GCloudTestServer(port, _testSupport.getConfiguration());
    }

    /** 
     * @see org.eclipse.jetty.server.session.AbstractStopSessionManagerPreserveSessionTest#configureSessionManagement(org.eclipse.jetty.servlet.ServletContextHandler)
     */
    @Override
    public void configureSessionManagement(ServletContextHandler context)
    {
        
    }

    @Test
    @Override
    public void testStopSessionManagerPreserveSession() throws Exception
    {
        super.testStopSessionManagerPreserveSession();
    }

    
}
