/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.asanti.reader.parser;

import com.brightsparklabs.asanti.model.schema.type.AsnSchemaType;
import com.brightsparklabs.asanti.model.schema.typedefinition.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

/**
 * Logic for parsing a Type Definition from a module within an ASN.1 schema
 *
 * @author brightSPARK Labs
 */
public final class AsnSchemaTypeDefinitionParser
{

    // -------------------------------------------------------------------------
    // CLASS VARIABLES
    // -------------------------------------------------------------------------

    /** class logger */
    private static final Logger logger = LoggerFactory.getLogger(AsnSchemaTypeDefinitionParser.class
            .getName());

    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    /**
     * Private constructor. There should be no need to ever instantiate this static class.
     */
    private AsnSchemaTypeDefinitionParser()
    {
        throw new AssertionError();
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    /**
     * Parses a type definition from a module from an ASN.1 schema
     *
     * @param name
     *         the name of the defined type (i.e. the text on the left hand side of the {@code
     *         ::=})
     * @param value
     *         the value of the defined type (i.e. the text on the right hand side of the {@code
     *         ::=})
     *
     * @return an {@link AsnSchemaTypeDefinition} object representing the parsed type definition
     *
     * @throws ParseException
     *         if either of the parameters are {@code null}/empty or any errors occur while parsing
     *         the type
     */
/* TODO MJF
    public static ImmutableList<AsnSchemaTypeDefinition> parse(String name, String value)
            throws ParseException
*/
    public static AsnSchemaTypeDefinition parse(String name, String value)
            throws ParseException
    {
        // TODO MJF - not sure we still need to be returning a list of types.
        // any "generated" types are now scoped internally and are not Type Definitions

        logger.debug("Found type definition: {} = {}", name, value);
        if (name == null || name.trim().isEmpty())
        {
            throw new ParseException("A name must be supplied for a Type Definition", -1);
        }
        if (value == null || value.trim().isEmpty())
        {
            throw new ParseException("A value must be supplied for a Type Definition", -1);
        }

        if (name.equals("Invoke{InvokeId:InvokeIdSet, OPERATION:Operations}"))
        {
            // TODO MJF
            int breakpoint = 0;
        }

        // Get the underlying type
        AsnSchemaType type = AsnSchemaTypeParser.parse(value);
        logger.debug("\t{} is type {}", name, type.getPrimitiveType().getBuiltinType());
        // wrap it with the typedefinition name.
        return new AsnSchemaTypeDefinitionImpl(name, type);

    }

}
