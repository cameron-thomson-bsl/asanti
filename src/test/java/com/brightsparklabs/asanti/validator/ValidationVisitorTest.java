/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */
package com.brightsparklabs.asanti.validator;

import com.brightsparklabs.asanti.model.schema.constraint.AsnSchemaConstraint;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaComponentType;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaNamedTag;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaTypeDefinition;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaTypeDefinitionChoice;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaTypeDefinitionEnumerated;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaTypeDefinitionIa5String;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaTypeDefinitionInteger;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaTypeDefinitionOctetString;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaTypeDefinitionSequence;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaTypeDefinitionSequenceOf;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaTypeDefinitionSet;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaTypeDefinitionSetOf;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaTypeDefinitionUtf8String;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaTypeDefinitionVisibleString;
import com.brightsparklabs.asanti.validator.rule.PrimitiveValidationRule;
import com.brightsparklabs.asanti.validator.rule.ValidationRule;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ValidationVisitor}
 *
 * @author brightSPARK Labs
 */
public class ValidationVisitorTest
{
    // -------------------------------------------------------------------------
    // FIXTURES
    // -------------------------------------------------------------------------

    /** instance under test */
    private static final ValidationVisitor instance = new ValidationVisitor();

    // -------------------------------------------------------------------------
    // TESTS
    // -------------------------------------------------------------------------

    @Test
    public void testVisitAsnSchemaTypeDefinitionNull()
    {
        final AsnSchemaTypeDefinition.Null visitable = AsnSchemaTypeDefinition.NULL;
        assertEquals(ValidationRule.NULL, instance.visit(visitable));
    }

    @Test
    public void testVisitAsnSchemaTypeDefinitionChoice()
    {
        final AsnSchemaTypeDefinitionChoice visitable =
                new AsnSchemaTypeDefinitionChoice("TEST_NAME",
                        ImmutableList.<AsnSchemaComponentType>of(),
                        AsnSchemaConstraint.NULL);
        assertEquals(ValidationRule.NULL, instance.visit(visitable));
    }

    @Test
    public void testVisitAsnSchemaTypeDefinitionEnumerated()
    {
        final AsnSchemaTypeDefinitionEnumerated visitable =
                new AsnSchemaTypeDefinitionEnumerated("TEST_NAME", ImmutableList.<AsnSchemaNamedTag>of());
        assertEquals(ValidationRule.NULL, instance.visit(visitable));
    }

    @Test
    public void testVisitAsnSchemaTypeDefinitionIA5String()
    {
        final AsnSchemaTypeDefinitionIa5String visitable =
                new AsnSchemaTypeDefinitionIa5String("TEST_NAME", AsnSchemaConstraint.NULL);
        final Object result = instance.visit(visitable);
        assertTrue(result instanceof PrimitiveValidationRule);
    }

    @Test
    public void testVisitAsnSchemaTypeDefinitionInteger()
    {
        final AsnSchemaTypeDefinitionInteger visitable =
                new AsnSchemaTypeDefinitionInteger("TEST_NAME", ImmutableList.<AsnSchemaNamedTag>of(), AsnSchemaConstraint.NULL);
        final Object result = instance.visit(visitable);
        assertTrue(result instanceof PrimitiveValidationRule);
    }

    @Test
    public void testVisitAsnSchemaTypeDefinitionOctetString()
    {
        final AsnSchemaTypeDefinitionOctetString visitable =
                new AsnSchemaTypeDefinitionOctetString("TEST_NAME", AsnSchemaConstraint.NULL);
        final Object result = instance.visit(visitable);
        assertTrue(result instanceof PrimitiveValidationRule);
    }

    @Test
    public void testVisitAsnSchemaTypeDefinitionSequence()
    {
        final AsnSchemaTypeDefinitionSequence visitable =
                new AsnSchemaTypeDefinitionSequence("TEST_NAME",
                        ImmutableList.<AsnSchemaComponentType>of(),
                        AsnSchemaConstraint.NULL);
        assertEquals(ValidationRule.NULL, instance.visit(visitable));
    }

    @Test
    public void testVisitAsnSchemaTypeDefinitionSequenceOf()
    {
        final AsnSchemaTypeDefinitionSequenceOf visitable =
                new AsnSchemaTypeDefinitionSequenceOf("TEST_NAME", "TEST_TYPE", AsnSchemaConstraint.NULL);
        assertEquals(ValidationRule.NULL, instance.visit(visitable));
    }

    @Test
    public void testVisitAsnSchemaTypeDefinitionSet()
    {
        final AsnSchemaTypeDefinitionSet visitable =
                new AsnSchemaTypeDefinitionSet("TEST_NAME",
                        ImmutableList.<AsnSchemaComponentType>of(),
                        AsnSchemaConstraint.NULL);
        assertEquals(ValidationRule.NULL, instance.visit(visitable));
    }

    @Test
    public void testVisitAsnSchemaTypeDefinitionSetOf()
    {
        final AsnSchemaTypeDefinitionSetOf visitable =
                new AsnSchemaTypeDefinitionSetOf("TEST_NAME", "TEST_TYPE", AsnSchemaConstraint.NULL);
        assertEquals(ValidationRule.NULL, instance.visit(visitable));
    }

    @Test
    public void testVisitAsnSchemaTypeDefinitionUTF8String()
    {
        final AsnSchemaTypeDefinitionUtf8String visitable =
                new AsnSchemaTypeDefinitionUtf8String("TEST_NAME", AsnSchemaConstraint.NULL);
        final Object result = instance.visit(visitable);
        assertTrue(result instanceof PrimitiveValidationRule);
    }

    @Test
    public void testVisitAsnSchemaTypeDefinitionVisibleString()
    {
        final AsnSchemaTypeDefinitionVisibleString visitable =
                new AsnSchemaTypeDefinitionVisibleString("TEST_NAME", AsnSchemaConstraint.NULL);
        final Object result = instance.visit(visitable);
        assertTrue(result instanceof PrimitiveValidationRule);
    }
}