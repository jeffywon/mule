/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transformer.simple;

import java.text.MessageFormat;

import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.util.AttributeEvaluator;

public abstract class AbstractAddVariablePropertyTransformer extends AbstractMessageTransformer
{
    private AttributeEvaluator keyEvaluator;
    private AttributeEvaluator valueEvaluator;

    public AbstractAddVariablePropertyTransformer()
    {
        registerSourceType(DataTypeFactory.OBJECT);
        setReturnDataType(DataTypeFactory.OBJECT);
    }

    @Override
    public void initialise() throws InitialisationException
    {
        super.initialise();
        keyEvaluator.initialize(muleContext.getExpressionManager());
        valueEvaluator.initialize(muleContext.getExpressionManager());
    }

    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException
    {
        Object keyValue = keyEvaluator.resolveValue(message);
        String key = (keyValue == null ? null : keyValue.toString());
        if (key == null)
        {
            logger.error("Setting Null variable keys is not supported, this entry is being ignored");
        }
        else
        {
            Object value = valueEvaluator.resolveValue(message);
            if (value == null)
            {
                logger.info(MessageFormat.format(
                        "Variable with key '{0}', not found on message using '{1}'. Since the value was marked optional, nothing was set on the message for this variable",
                        key, valueEvaluator.getRawValue()));
            }
            else
            {
                message.setProperty(key, value, getScope());
            }
        }
        return message;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        AbstractAddVariablePropertyTransformer clone = (AbstractAddVariablePropertyTransformer) super.clone();
        clone.setKey(this.keyEvaluator.getRawValue());
        clone.setValue(this.valueEvaluator.getRawValue());
        return clone;
    }

    public void setKey(String key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("Key must not be null");
        }
        this.keyEvaluator = new AttributeEvaluator(key);
    }

    public void setValue(String value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Value must not be null");
        }
        this.valueEvaluator = new AttributeEvaluator(value);
    }
    
    abstract protected PropertyScope getScope();

}