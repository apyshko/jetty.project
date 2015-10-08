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

package org.eclipse.jetty.webapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class WebAppContextTest
{
    @Test
    public void testConfigurationClassesFromDefault() throws Exception
    {
        Server server = new Server();
        //test if no classnames set, its the defaults
        WebAppContext wac = new WebAppContext();
        server.setHandler(wac);
        assertEquals(0,wac.getConfigurations().length);
        String[] classNames = wac.getConfigurationClasses();
        assertNotNull(classNames);
        assertEquals(0,classNames.length);

        wac.loadConfigurations();
        Assert.assertThat(
            Arrays.asList(wac.getConfigurations()).stream().map(c->{return c.getClass().getName();}).collect(Collectors.toList()).toArray(new String[0]),
            Matchers.arrayContaining(WebAppContext.DEFAULT_CONFIGURATION_CLASSES));
    }
    
    @Test
    public void testConfigurationClassesFromServerDefault() throws Exception
    {
        Server server = new Server();
        //test if no classnames set, its the defaults
        WebAppContext wac = new WebAppContext();
        server.setHandler(wac);

        String[] configs=
        {
            "org.eclipse.jetty.webapp.MetaInfConfiguration",
            "org.eclipse.jetty.webapp.WebXmlConfiguration",
            "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
            "org.eclipse.jetty.webapp.WebInfConfiguration"
        } ;
        
        String[] configs_sorted=
        {
            "org.eclipse.jetty.webapp.WebInfConfiguration",
            "org.eclipse.jetty.webapp.WebXmlConfiguration",
            "org.eclipse.jetty.webapp.MetaInfConfiguration",
            "org.eclipse.jetty.webapp.JettyWebXmlConfiguration"
        } ;
        
        server.setAttribute(Configuration.ATTR,configs);

        wac.loadConfigurations();
        Assert.assertThat(
            Arrays.asList(wac.getConfigurations()).stream().map(c->{return c.getClass().getName();}).collect(Collectors.toList()).toArray(new String[0]),
            Matchers.arrayContaining(configs_sorted));
        
    }
    
    @Test
    public void testConfigurationClassesOverride() throws Exception
    {
        Server server = new Server();
        //test if no classnames set, its the defaults
        WebAppContext wac = new WebAppContext();
        server.setHandler(wac);

        wac.setConfigurations(new Configuration[]
        {
            new org.eclipse.jetty.webapp.MetaInfConfiguration(),
            new org.eclipse.jetty.webapp.WebXmlConfiguration(),
            new org.eclipse.jetty.webapp.JettyWebXmlConfiguration(),
            new org.eclipse.jetty.webapp.WebInfConfiguration(),
            new org.eclipse.jetty.webapp.MetaInfConfiguration()
        });
        
        wac.addConfigurations(new org.eclipse.jetty.webapp.WebAppContextTest.OverrideWebXmlConfiguration());
        
        String[] configs_sorted=
        {
            "org.eclipse.jetty.webapp.WebInfConfiguration",
            "org.eclipse.jetty.webapp.WebAppContextTest$OverrideWebXmlConfiguration",
            "org.eclipse.jetty.webapp.MetaInfConfiguration",
            "org.eclipse.jetty.webapp.JettyWebXmlConfiguration"
        } ;
        
        wac.loadConfigurations();
        Assert.assertThat(
            Arrays.asList(wac.getConfigurations()).stream().map(c->{return c.getClass().getName();}).collect(Collectors.toList()).toArray(new String[0]),
            Matchers.arrayContaining(configs_sorted));
    }
    
    public static class OverrideWebXmlConfiguration extends WebXmlConfiguration
    {
        
    }
    
    
    @Test
    public void testConfigurationClasses() throws Exception
    {
        Server server = new Server();
        //test if no classnames set, its the defaults
        WebAppContext wac = new WebAppContext();
        server.setHandler(wac);

        wac.setConfigurations(new Configuration[]
        {
            new org.eclipse.jetty.webapp.MetaInfConfiguration(),
            new org.eclipse.jetty.webapp.WebXmlConfiguration(),
            new org.eclipse.jetty.webapp.JettyWebXmlConfiguration(),
            new org.eclipse.jetty.webapp.WebInfConfiguration()
        });
        
        String[] configs_sorted=
        {
            "org.eclipse.jetty.webapp.WebInfConfiguration",
            "org.eclipse.jetty.webapp.WebXmlConfiguration",
            "org.eclipse.jetty.webapp.MetaInfConfiguration",
            "org.eclipse.jetty.webapp.JettyWebXmlConfiguration"
        } ;
        
        wac.loadConfigurations();
        Assert.assertThat(
            Arrays.asList(wac.getConfigurations()).stream().map(c->{return c.getClass().getName();}).collect(Collectors.toList()).toArray(new String[0]),
            Matchers.arrayContaining(configs_sorted));
        
    }


    @Test
    public void testConfigurationInstances ()
    {
        Configuration[] configs = {new WebInfConfiguration()};
        WebAppContext wac = new WebAppContext();
        wac.setConfigurations(configs);
        assertTrue(Arrays.equals(configs, wac.getConfigurations()));

        //test that explicit config instances override any from server
        String[] classNames = {"x.y.z"};
        Server server = new Server();
        server.setAttribute(Configuration.ATTR, classNames);
        wac.setServer(server);
        assertTrue(Arrays.equals(configs,wac.getConfigurations()));
    }

    @Test
    public void testRealPathDoesNotExist() throws Exception
    {
        Server server = new Server(0);
        WebAppContext context = new WebAppContext(".", "/");
        server.setHandler(context);
        server.start();

        ServletContext ctx = context.getServletContext();
        assertNotNull(ctx.getRealPath("/doesnotexist"));
        assertNotNull(ctx.getRealPath("/doesnotexist/"));
    }

    /**
     * tests that the servlet context white list works
     *
     * @throws Exception on test failure
     */
    @Test
    public void testContextWhiteList() throws Exception
    {
        Server server = new Server(0);
        HandlerList handlers = new HandlerList();
        WebAppContext contextA = new WebAppContext(".", "/A");

        contextA.addServlet( ServletA.class, "/s");
        handlers.addHandler(contextA);
        WebAppContext contextB = new WebAppContext(".", "/B");

        contextB.addServlet(ServletB.class, "/s");
        contextB.setContextWhiteList(new String [] { "/doesnotexist", "/B/s" } );
        handlers.addHandler(contextB);

        server.setHandler(handlers);
        server.start();

        // context A should be able to get both A and B servlet contexts
        Assert.assertNotNull(contextA.getServletHandler().getServletContext().getContext("/A/s"));
        Assert.assertNotNull(contextA.getServletHandler().getServletContext().getContext("/B/s"));

        // context B has a contextWhiteList set and should only be able to get ones that are approved
        Assert.assertNull(contextB.getServletHandler().getServletContext().getContext("/A/s"));
        Assert.assertNotNull(contextB.getServletHandler().getServletContext().getContext("/B/s"));
    }


    @Test
    public void testAlias() throws Exception
    {
        File dir = File.createTempFile("dir",null);
        dir.delete();
        dir.mkdir();
        dir.deleteOnExit();

        File webinf = new File(dir,"WEB-INF");
        webinf.mkdir();

        File classes = new File(dir,"classes");
        classes.mkdir();

        File someclass = new File(classes,"SomeClass.class");
        someclass.createNewFile();

        WebAppContext context = new WebAppContext();
        context.setBaseResource(new ResourceCollection(dir.getAbsolutePath()));

        context.setResourceAlias("/WEB-INF/classes/", "/classes/");

        assertTrue(Resource.newResource(context.getServletContext().getResource("/WEB-INF/classes/SomeClass.class")).exists());
        assertTrue(Resource.newResource(context.getServletContext().getResource("/classes/SomeClass.class")).exists());

    }


    @Test
    public void testIsProtected() throws Exception
    {
        WebAppContext context = new WebAppContext();
        assertTrue(context.isProtectedTarget("/web-inf/lib/foo.jar"));
        assertTrue(context.isProtectedTarget("/meta-inf/readme.txt"));
        assertFalse(context.isProtectedTarget("/something-else/web-inf"));
    }
    
    
    @Test
    public void testNullPath() throws Exception
    {
        Server server = new Server(0);
        HandlerList handlers = new HandlerList();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        WebAppContext context = new WebAppContext();
        context.setBaseResource(Resource.newResource("./src/test/webapp"));
        context.setContextPath("/");
        server.setHandler(handlers);
        handlers.addHandler(contexts);
        contexts.addHandler(context);
        
        LocalConnector connector = new LocalConnector(server);
        server.addConnector(connector);
        
        server.start();
        try
        {
            String response = connector.getResponses("GET http://localhost:8080 HTTP/1.1\r\nHost: localhost:8080\r\nConnection: close\r\n\r\n");
            Assert.assertTrue(response.indexOf("200 OK")>=0);
        }
        finally
        {
            server.stop();
        }
    }
    
    
    class ServletA extends GenericServlet
    {
        @Override
        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
        {
            this.getServletContext().getContext("/A/s");
        }
    }

    class ServletB extends GenericServlet
    {
        @Override
        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
        {
            this.getServletContext().getContext("/B/s");
        }
    }
}
