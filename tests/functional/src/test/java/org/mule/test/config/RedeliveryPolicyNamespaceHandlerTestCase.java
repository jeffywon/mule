/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.config;

import org.junit.Test;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.source.MessageSource;
import org.mule.construct.Flow;
import org.mule.processor.AbstractRedeliveryPolicy;
import org.mule.processor.IdempotentRedeliveryPolicy;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.util.store.SimpleMemoryObjectStore;

import java.io.Serializable;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for all object stores that can be configured on an {@link org.mule.routing.IdempotentMessageFilter}.
 */
public class RedeliveryPolicyNamespaceHandlerTestCase extends FunctionalTestCase
{
    public RedeliveryPolicyNamespaceHandlerTestCase()
    {
        // we just test the wiring of the objects, no need to start the MuleContext
        setStartContext(false);
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/test/config/redelivery-policy-config.xml";
    }

    @Test
    public void testInMemoryObjectStore() throws Exception
    {
        IdempotentRedeliveryPolicy filter = redeliveryPolicyFromFlow("inMemoryStore");

        assertNotNull(filter.getTheFailedMessageProcessor());
        assertEquals(12, filter.getMaxRedeliveryCount());
        assertNull(filter.getIdExpression());
    }

    @Test
    public void testSimpleTextFileStore() throws Exception
    {
        IdempotentRedeliveryPolicy filter = redeliveryPolicyFromFlow("simpleTextFileStore");
        assertEquals("#[message:id]", filter.getIdExpression());
        assertNotNull(filter.getTheFailedMessageProcessor());
        assertEquals(5, filter.getMaxRedeliveryCount());
    }

    @Test
    public void testCustomObjectStore() throws Exception
    {
        IdempotentRedeliveryPolicy filter = redeliveryPolicyFromFlow("customObjectStore");
        assertNotNull(filter.getTheFailedMessageProcessor());
        assertEquals(5, filter.getMaxRedeliveryCount());
        assertNull(filter.getIdExpression());
    }

    private IdempotentRedeliveryPolicy redeliveryPolicyFromFlow(String flowName) throws Exception
    {
        FlowConstruct flow = getFlowConstruct(flowName);
        assertTrue(flow instanceof Flow);

        MessageSource source = ((Flow) flow).getMessageSource();
        assertTrue(source instanceof InboundEndpoint);
        AbstractRedeliveryPolicy redeliveryPolicy = ((InboundEndpoint)source).getRedeliveryPolicy();
        assertTrue(redeliveryPolicy instanceof IdempotentRedeliveryPolicy);
        return (IdempotentRedeliveryPolicy) redeliveryPolicy;
    }

    public static class CustomObjectStore extends SimpleMemoryObjectStore<Serializable>
    {
        private String customProperty;

        public String getCustomProperty()
        {
            return customProperty;
        }

        public void setCustomProperty(String value)
        {
            customProperty = value;
        }
    }
}
