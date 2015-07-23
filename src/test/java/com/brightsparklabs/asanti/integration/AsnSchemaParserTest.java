package com.brightsparklabs.asanti.integration;

import com.brightsparklabs.asanti.Asanti;
import com.brightsparklabs.asanti.common.DecodeException;
import com.brightsparklabs.asanti.common.OperationResult;
import com.brightsparklabs.asanti.model.data.DecodedAsnData;
import com.brightsparklabs.asanti.model.schema.AsnBuiltinType;
import com.brightsparklabs.asanti.model.schema.AsnSchema;
import com.brightsparklabs.asanti.model.schema.DecodedTag;
import com.brightsparklabs.asanti.model.schema.primitive.AsnPrimitiveType;
import com.brightsparklabs.asanti.reader.AsnSchemaReader;
import com.brightsparklabs.asanti.reader.parser.AsnSchemaParser;
import com.brightsparklabs.asanti.validator.ValidatorImpl;
import com.brightsparklabs.asanti.validator.failure.DecodedTagValidationFailure;
import com.brightsparklabs.asanti.validator.result.ValidationResult;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Testing end-to-end parsing, no mocks. Mostly I have just made this as a way to exercise the
 * parser(s) while making changes
 *
 * @author brightSPARK Labs
 */
public class AsnSchemaParserTest
{
    /** class logger */
    private static final Logger logger = LoggerFactory.getLogger(AsnSchemaParserTest.class);

    private static final String NO_ROOT_STRUCTURE = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "    MyInt ::= INTEGER\n" +
            "END";

    private static final String HUMAN_SIMPLE = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       name UTF8String,\n" +
            "       age INTEGER\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_SIMPLE3 = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       name UTF8String,\n" +
            "       age PersonAge\n" +
            "   }\n" +
            "   PersonAge ::= INTEGER\n" +
            "END";

    private static final String HUMAN_SIMPLE_ENUMERATED = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       pickOne ENUMERATED\n" +
            "       {\n" +
            "           optA(0),\n" +
            "           optB(1)\n" +
            "       }\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_SIMPLE_CHOICE = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       payload Payload\n" +
            "   }\n" +
            "   Payload ::= CHOICE\n" +
            "   {\n" +
            "       optA [0] TypeA,\n" +
            "       optB [1] TypeB\n" +
            "   }\n" +
            "   TypeA ::= SEQUENCE\n" +
            "   {\n" +
            "       name UTF8String,\n" +
            "       age INTEGER\n" +
            "   }\n" +
            "   TypeB ::= SEQUENCE\n" +
            "   {\n" +
            "       number INTEGER,\n" +
            "       also INTEGER\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_SIMPLExx = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       age [0] INTEGER,\n" +
            "       gender ENUMERATED{male(0),female(1)}" +
            "   }\n" +
            "END";

    private static final String HUMAN_SIMPLE2 = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       age INTEGER(1..100),\n" +
            "       name UTF8String(SIZE( 1..10))\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_SIMPLE_SET = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SET\n" +
            "   {\n" +
            "       age INTEGER (1..100),\n" +
            "       name UTF8String\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_NESTED = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       name SEQUENCE\n" +
            "       {\n" +
            "           first UTF8String,\n" +
            "           last  UTF8String\n" +
            "       },\n" +
            "       age INTEGER (1..100)\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_USING_TYPEDEF = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       age [0] PersonAge (1..15) OPTIONAL\n" +
            "   }\n" +
            "   PersonAge ::= INTEGER (1..200)\n" +
            "END";

    private static final String HUMAN_USING_TYPEDEF_INDIRECT = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       age [0] PersonAge (0..150) OPTIONAL\n" +
            "   }\n" +
            "   PersonAge ::= ShortInt (0..200)\n" +
            "   ShortInt ::= Int32 (0..32767)\n" +
            "   Int32 ::= INTEGER (0..65536)\n" +
            "END";

    private static final String HUMAN_USING_TYPEDEF_SEQUENCE = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       age [0]  PersonAge (1..15) OPTIONAL,\n" +
            "       name [1] PersonName" +
            "   }\n" +
            "   PersonName ::= SEQUENCE\n" +
            "   {\n" +
            "       first UTF8String,\n" +
            "       last  UTF8String\n" +
            "   }\n" +
            "   PersonAge ::= INTEGER (1..200)\n" +
            "END";

    private static final String HUMAN_SEQUENCEOF_PRIMITIVE = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       age SEQUENCE OF INTEGER (1..100)\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_SEQUENCEOF_SEQUENCE = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE OF SEQUENCE\n" +
            "   {\n" +
            "       age INTEGER (1..100),\n" +
            "       name UTF8String\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_SEQUENCEOF_SEQUENCE2 = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       age INTEGER (1..100),\n" +
            "       name UTF8String,\n" +
            "       friends SEQUENCE OF Name\n" +
            "   }\n" +
            "   Name ::= SEQUENCE\n" +
            "   {\n" +
            "       first UTF8String,\n" +
            "       last  UTF8String\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_SEQUENCEOF_SEQUENCE3 = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       age [0] INTEGER (1..100),\n" +
            "       name [1] UTF8String,\n" +
            "       friends [2] SEQUENCE OF SEQUENCE\n" +
            "       {\n" +
            "           age [0] INTEGER (1..100),\n" +
            "           name [1] UTF8String\n" +
            "       }\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_SEQUENCEOF_SEQUENCE4 = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       age INTEGER (1..100),\n" +
            "       name UTF8String,\n" +
            "       friends SEQUENCE OF Friend\n" +
            "   }\n" +
            "   Friend ::= SEQUENCE {\n" +
            "           age INTEGER (1..100),\n" +
            "           name UTF8String\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_USING_TYPEDEF_SETOF = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       faveNumbers FaveNumbers,\n" +
            "       name PersonName,\n" +
            "       bitString BIT STRING (SIZE (4))" +
            "   }\n" +
            "   PersonName ::= SEQUENCE\n" +
            "   {\n" +
            "       first NameType,\n" +
            "       last  NameType\n" +
            "   }\n" +
            "   FaveNumbers ::= SET OF INTEGER\n" +
            "   NameType ::= UTF8String\n" +
            "END";

    private static final String HUMAN_BROKEN_MISSING_TYPEDEF = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "IMPORTS\n" +
            ";\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       age [0] PersonAge (1..150) OPTIONAL\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_BROKEN_MISSING_IMPORT = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "IMPORTS\n" +
            "    PersonAge\n" +
            "    FROM MissingModule { joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) missing(5) }\n"
            +
            ";\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       age [0] PersonAge (1..150) OPTIONAL\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_BROKEN_MISSING_IMPORTED_TYPEDEF = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            "IMPORTS\n" +
            "    PersonAge\n" +
            "    FROM OtherModule { joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) missing(5) }\n"
            +
            ";\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       age [0] PersonAge (1..150) OPTIONAL\n" +
            "   }\n" +
            "END\n" +
            "OtherModule\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) missing(5) }\n"
            +
            "DEFINITIONS\n" +
            "AUTOMATIC TAGS ::=\n" +
            "BEGIN\n" +
            ";\n" +
            "   Foo ::= SEQUENCE\n" +
            "   {\n" +
            "       bar UTF8String\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_DUPLICATE_CHOICE = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS IMPLICIT TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       payload Payload\n" +
            "   }\n" +
            "   Payload ::= CHOICE\n" +
            "   {\n" +
            "       optA INTEGER,\n" +
            "       optB INTEGER\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_DUPLICATE_SET = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS IMPLICIT TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       payload Payload\n" +
            "   }\n" +
            "   Payload ::= SET\n" +
            "   {\n" +
            "       optA INTEGER,\n" +
            "       optB INTEGER\n" +
            "   }\n" +
            "END";

    private static final String HUMAN_DUPLICATE_SEQUENCE = "Test-Protocol\n" +
            "{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\n"
            +
            "DEFINITIONS IMPLICIT TAGS ::=\n" +
            "BEGIN\n" +
            "   Human ::= SEQUENCE\n" +
            "   {\n" +
            "       payload Payload\n" +
            "   }\n" +
            "   Payload ::= SEQUENCE\n" +
            "   {\n" +
            "       optA INTEGER OPTIONAL,\n" +
            "       optB INTEGER\n" +
            "   }\n" +
            "END";

    // TODO ASN-123 - rationalise these.  Determine if we want many small examples, or one more
    // comprehensive example (using the AsantiSample schema).  The small examples were useful
    // during the refactoring (ASN-126), not sure how useful they will be beyond that.
    // Also, consolidate this and AsantiTest in to one, as they are both
    // doing end-to-end testing.

    // -------------------------------------------------------------------------
    // TESTS
    // -------------------------------------------------------------------------
    @Test
    public void testAsantiSample() throws Exception
    {
        // loads the AsantiSample test schema and checks all the tag decoding.
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/AsantiSample.asn"), Charsets.UTF_8);
        AsnSchema instance = AsnSchemaReader.read(schemaData);

        ImmutableMap<String, String> tags = ImmutableMap.<String, String>builder()
                .put("", "/Document")
                .put("0[1]", "/Document/header")
                .put("0[1]/0[0]", "/Document/header/published")
                .put("0[1]/0[0]/1[1]", "/Document/header/published/date")
                .put("0[1]/0[0]/2[2]", "/Document/header/published/country")
                .put("1[2]", "/Document/body")
                .put("1[2]/0[0]", "/Document/body/lastModified")
                .put("1[2]/0[0]/0[0]", "/Document/body/lastModified/date")
                .put("1[2]/0[0]/1[1]/0[1]", "/Document/body/lastModified/modifiedBy/firstName")
                .put("1[2]/0[0]/1[1]/1[2]", "/Document/body/lastModified/modifiedBy/lastName")
                .put("1[2]/0[0]/1[1]/2[3]", "/Document/body/lastModified/modifiedBy/title")
                .put("1[2]/1[1]/0[1]", "/Document/body/prefix/text")
                .put("1[2]/2[2]/0[1]", "/Document/body/content/text")
                .put("1[2]/2[2]/1[2]/0[UNIVERSAL 16]/0[1]",
                        "/Document/body/content/paragraphs[0]/title")
                .put("1[2]/2[2]/1[2]/1[UNIVERSAL 16]/0[1]",
                        "/Document/body/content/paragraphs[1]/title")
                .put("1[2]/2[2]/1[2]/0[UNIVERSAL 16]/1[2]/0[1]",
                        "/Document/body/content/paragraphs[0]/contributor/firstName")
                .put("1[2]/2[2]/1[2]/0[UNIVERSAL 16]/1[2]/1[2]",
                        "/Document/body/content/paragraphs[0]/contributor/lastName")
                .put("1[2]/2[2]/1[2]/0[UNIVERSAL 16]/1[2]/2[3]",
                        "/Document/body/content/paragraphs[0]/contributor/title")
                .put("1[2]/2[2]/1[2]/99[UNIVERSAL 16]/0[1]",
                        "/Document/body/content/paragraphs[99]/title")
                .put("1[2]/2[2]/1[2]/99[UNIVERSAL 16]/1[2]/0[1]",
                        "/Document/body/content/paragraphs[99]/contributor/firstName")
                .put("1[2]/2[2]/1[2]/99[UNIVERSAL 16]/1[2]/1[2]",
                        "/Document/body/content/paragraphs[99]/contributor/lastName")
                .put("1[2]/2[2]/1[2]/99[UNIVERSAL 16]/1[2]/2[3]",
                        "/Document/body/content/paragraphs[99]/contributor/title")
                .put("1[2]/2[2]/1[2]/0[UNIVERSAL 16]/3[3]",
                        "/Document/body/content/paragraphs[0]/points")
                .put("1[2]/2[2]/1[2]/0[UNIVERSAL 16]/3[3]/0[UNIVERSAL 4]",
                        "/Document/body/content/paragraphs[0]/points[0]")
                .put("1[2]/2[2]/1[2]/11[UNIVERSAL 16]/0[1]",
                        "/Document/body/content/paragraphs[11]/title")
                .put("1[2]/2[2]/1[2]/11[UNIVERSAL 16]/3[3]/44[UNIVERSAL 4]",
                        "/Document/body/content/paragraphs[11]/points[44]")

                .put("1[2]/3[3]/0[1]", "/Document/body/suffix/text")

                .put("2[3]", "/Document/footer")
                .put("2[3]/0[0]", "/Document/footer/authors")
                .put("2[3]/0[0]/0[UNIVERSAL 16]/0[1]", "/Document/footer/authors[0]/firstName")
                .put("2[3]/0[0]/0[UNIVERSAL 16]/1[2]", "/Document/footer/authors[0]/lastName")
                .put("3[4]", "/Document/dueDate")
                .put("4[5]", "/Document/version")
                .put("4[5]/0[0]", "/Document/version/majorVersion")
                .put("4[5]/1[1]", "/Document/version/minorVersion")
                .put("5[6]", "/Document/description")
                .put("5[6]/0[0]", "/Document/description/numberLines")
                .put("5[6]/1[1]", "/Document/description/summary")
                .build();

        final ImmutableSet<OperationResult<DecodedTag>> decodedTags
                = instance.getDecodedTags(tags.keySet(), "Document");

        for (OperationResult<DecodedTag> decodedTag : decodedTags)
        {
            assertTrue(decodedTag.wasSuccessful());

            final DecodedTag output = decodedTag.getOutput();
            assertEquals(tags.get(output.getRawTag()), output.getTag());
        }

        final ImmutableMap<String, String> tagsBad = ImmutableMap.<String, String>builder()
                .put("0[1]/0[0]/99[99]/98[98]", "/Document/header/published/99[99]/98[98]")
                .put("99[99]/98[98]", "/Document/99[99]/98[98]")
                .build();

        final ImmutableSet<OperationResult<DecodedTag>> decodedTagsBad = instance.getDecodedTags(
                tagsBad.keySet(),
                "Document");

        for (OperationResult<DecodedTag> decodedTag : decodedTagsBad)
        {
            assertFalse(decodedTag.wasSuccessful());

            final DecodedTag output = decodedTag.getOutput();
            assertEquals(tagsBad.get(output.getRawTag()), output.getTag());
        }
    }

    @Test
    public void testParse_NoRoot() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Root_MyInt.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData
                = Resources.asByteSource(getClass().getResource("/Root_MyInt.ber"));
        String topLevelType = "MyInt";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        debugPdus(pdus);

        // TODO ASN-151, we can't decode this at the root level because it is not a constructed type.
/*
        AsnSchema schema = AsnSchemaParser.parse(NO_ROOT_STRUCTURE);

        String tag = "/";
        logger.info("get tag " + tag);
        ImmutableSet<String> tags = ImmutableSet.of(tag);
        ImmutableSet<OperationResult<DecodedTag>> results = schema.getDecodedTags(tags, "MyInt");
        assertEquals(1, results.size());

        if (results.asList().get(0).wasSuccessful())
        {
            DecodedTag actualTag = results.asList().get(0).getOutput();
            logger.info(actualTag.getTag() + " : " + actualTag.getType().getBuiltinType());
        }
*/
    }

    @Test
    public void testParse_HumanSimple() throws Exception
    {
        AsnSchema schema = AsnSchemaParser.parse(HUMAN_SIMPLE);

        final ByteSource berData
                = Resources.asByteSource(getClass().getResource("/Human_Simple.ber"));

        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        DecodedAsnData pdu = pdus.get(0);
        String tag = "/Human/age";
        BigInteger age = pdu.<BigInteger>getDecodedObject(tag).get();
        logger.info(tag + " : " + age);
        assertEquals(new BigInteger("32"), age);

        tag = "/Human/name";
        String name = pdu.<String>getDecodedObject(tag).get();
        logger.info("{} : {}", tag, name);
        assertEquals("Adam", name);
    }

    @Test
    public void testParse_HumanSimple2() throws Exception
    {
        AsnSchema schema = AsnSchemaParser.parse(HUMAN_SIMPLE2);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_Simple2.ber"));
        String topLevelType = "Human";
        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        DecodedAsnData pdu = pdus.get(0);

        assertEquals(0, pdu.getUnmappedTags().size());

        String tag = "/Human/age";
        BigInteger age = pdu.<BigInteger>getDecodedObject(tag).get();
        logger.info(tag + " : " + age);
        assertEquals(new BigInteger("32"), age);

        tag = "/Human/name";
        String name = pdu.<String>getDecodedObject(tag).get();
        logger.info(tag + " : " + name);
        assertEquals("Adam", name);
    }

    @Test
    public void testParse_HumanEnumerated() throws Exception
    {
        AsnSchema schema = AsnSchemaParser.parse(HUMAN_SIMPLE_ENUMERATED);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_SimpleEnumerated.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        DecodedAsnData pdu = pdus.get(0);

        assertEquals(0, pdu.getUnmappedTags().size());

        String tag = "/Human/pickOne";
        assertEquals(AsnPrimitiveType.ENUMERATED, pdu.getType(tag).getPrimitiveType());

        byte[] bytes = pdu.getBytes(tag).get();
        assertEquals(1, bytes[0]);
    }

    @Test
    public void testParse_HumanSimpleChoice() throws Exception
    {
        AsnSchema schema = AsnSchemaParser.parse(HUMAN_SIMPLE_CHOICE);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_SimpleChoice.ber"));
        String topLevelType = "Human";
        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        DecodedAsnData pdu = pdus.get(0);
        String tag = "/Human/payload/optA/age";
        BigInteger age = pdu.<BigInteger>getDecodedObject(tag).get();
        assertEquals(new BigInteger("32"), age);

        tag = "/Human/payload/optA/name";
        String name = pdu.<String>getDecodedObject(tag).get();
        assertEquals("Adam", name);
    }

    @Test
    public void testParse_HumanSimpleSet() throws Exception
    {
        AsnSchema schema = AsnSchemaParser.parse(HUMAN_SIMPLE_SET);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_SimpleSet.ber"));
        String topLevelType = "Human";
        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        DecodedAsnData pdu = pdus.get(0);

        assertEquals(0, pdu.getUnmappedTags().size());

        String tag = "/Human/age";
        BigInteger age = pdu.<BigInteger>getDecodedObject(tag).get();
        logger.info(tag + " : " + age);
        assertEquals(new BigInteger("32"), age);

        tag = "/Human/name";
        String name = pdu.<String>getDecodedObject(tag).get();
        logger.info(tag + " : " + name);
        assertEquals("Adam", name);
    }

    @Test
    public void testParse_HumanNested() throws Exception
    {
        AsnSchema schema = AsnSchemaParser.parse(HUMAN_NESTED);

        final ByteSource berData
                = Resources.asByteSource(getClass().getResource("/Human_Nested.ber"));
        String topLevelType = "Human";
        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        DecodedAsnData pdu = pdus.get(0);

        assertEquals(0, pdu.getUnmappedTags().size());

        String tag = "/Human/name/first";
        String first = pdu.<String>getDecodedObject(tag).get();
        logger.info(tag + " : " + first);
        assertEquals("Adam", first);

        tag = "/Human/name/last";
        String last = pdu.<String>getDecodedObject(tag).get();
        logger.info(tag + " : " + last);
        assertEquals("Smith", last);

        tag = "/Human/age";
        BigInteger age = pdu.<BigInteger>getDecodedObject(tag).get();
        logger.info(tag + " : " + age);
        assertEquals(new BigInteger("32"), age);

        final ValidatorImpl validator = new ValidatorImpl();
        final ValidationResult validationresult = validator.validate(pdu);

        assertFalse(validationresult.hasFailures());
    }

    @Test
    public void testParse_HumanUsingTypeDef() throws Exception
    {
        AsnSchema schema = AsnSchemaParser.parse(HUMAN_USING_TYPEDEF);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_Typedef.ber"));
        String topLevelType = "Human";
        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        DecodedAsnData pdu = pdus.get(0);

        assertEquals(0, pdu.getUnmappedTags().size());

        String tag = "/Human/age";
        BigInteger age = pdu.<BigInteger>getDecodedObject(tag).get();
        logger.info(tag + " : " + age);
        assertEquals(new BigInteger("32"), age);

        final ValidatorImpl validator = new ValidatorImpl();
        final ValidationResult validationresult = validator.validate(pdu);

        assertTrue(validationresult.hasFailures());

        ImmutableSet<DecodedTagValidationFailure> failures = validationresult.getFailures();
        assertEquals(1, failures.size());

        for (DecodedTagValidationFailure fail : failures)
        {
            assertEquals("/Human/age", fail.getTag());

            logger.info("Tag: " + fail.getTag() +
                    " reason: " + fail.getFailureReason() +
                    " type: " + fail.getFailureType());
        }
    }

    @Test
    public void testParse_HumanUsingTypeDefIndirect() throws Exception
    {

        AsnSchema schema = AsnSchemaParser.parse(HUMAN_USING_TYPEDEF_INDIRECT);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_Typedef.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        debugPdus(pdus);

        DecodedAsnData pdu = pdus.get(0);

        assertEquals(0, pdu.getUnmappedTags().size());

        String tag = "/Human/age";
        BigInteger age = pdu.<BigInteger>getDecodedObject(tag).get();
        logger.info(tag + " : " + age);

        final ValidatorImpl validator = new ValidatorImpl();
        final ValidationResult validationresult = validator.validate(pdu);

        // dump any failures so we can see what went wrong
        for (DecodedTagValidationFailure fail : validationresult.getFailures())
        {
            logger.info("Validation Failure for : " + fail.getTag() +
                    " reason: " + fail.getFailureReason() +
                    " type: " + fail.getFailureType());
        }

        assertFalse(validationresult.hasFailures());
    }

    @Test
    public void testParse_HumanUsingTypeDefSequence() throws Exception
    {

        AsnSchema schema = AsnSchemaParser.parse(HUMAN_USING_TYPEDEF_SEQUENCE);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_TypedefSequence.ber"));
        String topLevelType = "Human";
        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        debugPdus(pdus);

        DecodedAsnData pdu = pdus.get(0);

        assertEquals(0, pdu.getUnmappedTags().size());

        String tag = "/Human/age";

        AsnBuiltinType builtinType = pdu.getType(tag).getBuiltinType();
        assertEquals(AsnBuiltinType.Integer, builtinType);

        BigInteger age = pdu.<BigInteger>getDecodedObject(tag).get();
        logger.info(tag + " : " + age);
        assertEquals(new BigInteger("32"), age);

        tag = "/Human/name/first";
        String first = pdu.<String>getDecodedObject(tag).get();
        logger.info(tag + " : " + first);
        assertEquals("Adam", first);

        tag = "/Human/name/last";
        String last = pdu.<String>getDecodedObject(tag).get();
        logger.info(tag + " : " + last);
        assertEquals("Smith", last);

/*
        final ValidatorImpl validator = new ValidatorImpl();
        final ValidationResult validationresult = validator.validate(pdu);

        assertTrue(validationresult.hasFailures());

        ImmutableSet<DecodedTagValidationFailure> failures = validationresult.getFailures();
        assertEquals(1, failures.size());

        for (DecodedTagValidationFailure fail : failures)
        {
            assertEquals("/Human/age", fail.getTag());

            logger.info("Tag: " + fail.getTag() +
                    " reason: " + fail.getFailureReason() +
                    " type: " + fail.getFailureType());
        }
*/
    }

    @Test
    public void testParse_HumanSequenceOfPrimitive() throws Exception
    {
        AsnSchema schema = AsnSchemaParser.parse(HUMAN_SEQUENCEOF_PRIMITIVE);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_SequenceOfPrimitive.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        debugPdus(pdus);

        String tag = "/Human/age[0]";
        assertEquals(new BigInteger("1"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());
        tag = "/Human/age[1]";
        assertEquals(new BigInteger("2"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());
        tag = "/Human/age[2]";
        assertEquals(new BigInteger("3"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());

        final ValidatorImpl validator = new ValidatorImpl();
        final ValidationResult validationresult = validator.validate(pdus.get(0));
        assertFalse(validationresult.hasFailures());
    }

    @Test
    public void testParse_HumanSequenceOfSequence3() throws Exception
    {
        AsnSchema schema = AsnSchemaParser.parse(HUMAN_SEQUENCEOF_SEQUENCE3);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_SequenceOfSequence3.ber"));
        String topLevelType = "Human";
        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        DecodedAsnData pdu = pdus.get(0);

        assertEquals(0, pdu.getUnmappedTags().size());

        debugPdus(pdus);

        String tag = "/Human/age";
        BigInteger age = pdu.<BigInteger>getDecodedObject(tag).get();
        assertEquals(32, age.intValue());

        tag = "/Human/name";
        String name = pdu.<String>getDecodedObject(tag).get();
        assertEquals("Adam", name);

        tag = "/Human/friends[0]/name";
        name = pdu.<String>getDecodedObject(tag).get();
        assertEquals("Finn", name);

        tag = "/Human/friends[0]/age";
        age = pdu.<BigInteger>getDecodedObject(tag).get();
        assertEquals(5, age.intValue());

        tag = "/Human/friends[1]/name";
        name = pdu.<String>getDecodedObject(tag).get();
        assertEquals("Fatty", name);

        tag = "/Human/friends[1]/age";
        age = pdu.<BigInteger>getDecodedObject(tag).get();
        assertEquals(3, age.intValue());

        final ValidatorImpl validator = new ValidatorImpl();
        final ValidationResult validationresult = validator.validate(pdus.get(0));
        assertFalse(validationresult.hasFailures());
    }

    @Test
    public void testParse_HumanSequenceOfSequence4() throws Exception
    {

        AsnSchema schema = AsnSchemaParser.parse(HUMAN_SEQUENCEOF_SEQUENCE4);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_SequenceOfSequence3.ber"));
        String topLevelType = "Human";
        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        DecodedAsnData pdu = pdus.get(0);

        assertEquals(0, pdu.getUnmappedTags().size());

        debugPdus(pdus);

        String tag = "/Human/age";
        BigInteger age = pdu.<BigInteger>getDecodedObject(tag).get();
        assertEquals(32, age.intValue());

        tag = "/Human/name";
        String name = pdu.<String>getDecodedObject(tag).get();
        assertEquals("Adam", name);

        tag = "/Human/friends[0]/name";
        name = pdu.<String>getDecodedObject(tag).get();
        assertEquals("Finn", name);

        tag = "/Human/friends[0]/age";
        age = pdu.<BigInteger>getDecodedObject(tag).get();
        assertEquals(5, age.intValue());

        tag = "/Human/friends[1]/name";
        name = pdu.<String>getDecodedObject(tag).get();
        assertEquals("Fatty", name);

        tag = "/Human/friends[1]/age";
        age = pdu.<BigInteger>getDecodedObject(tag).get();
        assertEquals(3, age.intValue());

        final ValidatorImpl validator = new ValidatorImpl();
        final ValidationResult validationresult = validator.validate(pdus.get(0));
        assertFalse(validationresult.hasFailures());
    }

    @Test
    public void testParse_HumanSequenceOfSequence2() throws Exception
    {

        AsnSchema schema = AsnSchemaParser.parse(HUMAN_SEQUENCEOF_SEQUENCE2);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_SequenceOfSequence2.ber"));
        String topLevelType = "Human";
        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        DecodedAsnData pdu = pdus.get(0);

        assertEquals(0, pdu.getUnmappedTags().size());

        for (String tag : pdu.getTags())
        {
            logger.info("\t{} => {}", tag, pdu.getHexString(tag));
        }
        for (String tag : pdu.getUnmappedTags())
        {
            logger.info("\t{} => {}", tag, pdu.getHexString(tag));
        }

        final ValidatorImpl validator = new ValidatorImpl();
        final ValidationResult validationresult = validator.validate(pdus.get(0));
        assertFalse(validationresult.hasFailures());
    }

    @Test
    public void testParse_HumanUsingTypeDefSetOf() throws Exception
    {
        AsnSchema schema = AsnSchemaParser.parse(HUMAN_USING_TYPEDEF_SETOF);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_TypedefSetOf.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        DecodedAsnData pdu = pdus.get(0);

        assertEquals(0, pdu.getUnmappedTags().size());

        debugPdus(pdus);

        String tag = "/Human/faveNumbers";
        BigInteger fave = pdu.<BigInteger>getDecodedObject(tag + "[0]").get();
        assertEquals(new BigInteger("1"), fave);
        fave = pdu.<BigInteger>getDecodedObject(tag + "[1]").get();
        assertEquals(new BigInteger("2"), fave);
        fave = pdu.<BigInteger>getDecodedObject(tag + "[2]").get();
        assertEquals(new BigInteger("3"), fave);

        final ValidatorImpl validator = new ValidatorImpl();
        final ValidationResult validationresult = validator.validate(pdus.get(0));
        assertFalse(validationresult.hasFailures());
    }

    @Test
    public void testParse_ImplicitTagging() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_ImplicitTagging.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_ImplicitTagging.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);
        debugPdus((pdus));

        assertEquals("A", pdus.get(0).getDecodedObject("/Human/lastName").get());
        assertEquals("U", pdus.get(0).getDecodedObject("/Human/firstName").get());

        final ValidatorImpl validator = new ValidatorImpl();
        final ValidationResult validationresult = validator.validate(pdus.get(0));
        assertFalse(validationresult.hasFailures());
    }

    @Test
    public void testParse_ImplicitTagging2() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_ImplicitTagging2.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_ImplicitTagging2.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);
        debugPdus((pdus));
        String tag = "/Human/payload/lastName";
        assertEquals("Smith", pdus.get(0).<String>getDecodedObject(tag).get());
        tag = "/Human/payload/firstName";
        assertEquals("Adam", pdus.get(0).<String>getDecodedObject(tag).get());

        final ValidatorImpl validator = new ValidatorImpl();
        final ValidationResult validationresult = validator.validate(pdus.get(0));
        assertFalse(validationresult.hasFailures());
    }

    @Test
    public void testParse_ImplicitTagging3() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_ImplicitTagging3.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_ImplicitTagging3.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);
        debugPdus((pdus));
        String tag = "/Human/payload/a";
        assertEquals(new BigInteger("10"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());
        tag = "/Human/payload/b/i";
        assertEquals(new BigInteger("1"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());
        tag = "/Human/payload/c";
        assertEquals("U", pdus.get(0).<String>getDecodedObject(tag).get());
    }

    @Test
    public void test_ReuseTypeWithOptional() throws Exception
    {
        // This tests our decoding session management by re-using a Type Definition and
        // having optional components.
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_ReuseWithOptional.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_ReuseWithOptional.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);
        debugPdus(pdus);

        String tag = "/Human/a/a";
        assertEquals(new BigInteger("10"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());
        tag = "/Human/a/b";
        assertEquals("U", pdus.get(0).<String>getDecodedObject(tag).get());
        tag = "/Human/a/c";
        assertEquals("V", pdus.get(0).<String>getDecodedObject(tag).get());
        tag = "/Human/b/b";
        assertEquals("A", pdus.get(0).<String>getDecodedObject(tag).get());
        tag = "/Human/b/c";
        assertEquals("B", pdus.get(0).<String>getDecodedObject(tag).get());
    }

    @Test
    public void testParse_NonUniqueTags() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_NonUniqueTags.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_NonUniqueTags.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);
        debugPdus((pdus));

        String tag = "/Human/c";
        assertEquals("A", pdus.get(0).<String>getDecodedObject(tag).get());
        tag = "/Human/b";
        assertEquals(new BigInteger("10"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());
        tag = "/Human/a";
        assertEquals("U", pdus.get(0).<String>getDecodedObject(tag).get());
    }

    @Test
    public void testParse_NonUniqueTagsImplicit() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_NonUniqueTagsImplicit.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_NonUniqueTagsImplicit.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);
        debugPdus((pdus));

        String tag = "/Human/c";
        assertEquals("A", pdus.get(0).<String>getDecodedObject(tag).get());
        tag = "/Human/b";
        assertEquals(new BigInteger("10"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());
        tag = "/Human/a";
        assertEquals("U", pdus.get(0).<String>getDecodedObject(tag).get());
    }

    @Test
    public void testParse_NonUniqueTagsOptional() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_NonUniqueTagsOptional.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_NonUniqueTagsOptional_allpresent.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);
        debugPdus((pdus));

        String tag = "/Human/c";
        assertEquals("A", pdus.get(0).<String>getDecodedObject(tag).get());
        tag = "/Human/b";
        assertEquals(new BigInteger("10"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());
        tag = "/Human/a";
        assertEquals("U", pdus.get(0).<String>getDecodedObject(tag).get());
    }

    @Test
    public void testParse_NonUniqueTagsOptional_missing() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_NonUniqueTagsOptional.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_NonUniqueTagsOptional_noOptional.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);
        debugPdus((pdus));

        String tag = "/Human/b";
        assertEquals(new BigInteger("10"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());
        tag = "/Human/a";
        assertEquals("U", pdus.get(0).<String>getDecodedObject(tag).get());
    }

    @Test
    public void testParse_SetOfChoice() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_SetOfChoice.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_SetOfChoice.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);

            debugPdus((pdus));

            String tag = "/Human/payload/name";
            assertEquals("Adam", pdus.get(0).<String>getDecodedObject(tag).get());
            tag
                    = "/Human/payload/supplementary-Services-Information/non-Standard-Supplementary-Services[0]/simpleIndication";
            Optional<byte[]> bytes = pdus.get(0).getBytes(tag);
            assertEquals(0, bytes.get()[0]);
        }
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_SetOfChoice_2items.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);

            debugPdus((pdus));

            String tag = "/Human/payload/name";
            assertEquals("Adam", pdus.get(0).<String>getDecodedObject(tag).get());
            tag
                    = "/Human/payload/supplementary-Services-Information/non-Standard-Supplementary-Services[0]/simpleIndication";
            Optional<byte[]> bytes = pdus.get(0).getBytes(tag);
            assertEquals(0, bytes.get()[0]);
            tag
                    = "/Human/payload/supplementary-Services-Information/non-Standard-Supplementary-Services[1]/simpleIndication";
            bytes = pdus.get(0).getBytes(tag);
            assertEquals(1, bytes.get()[0]);
        }

    }

    @Test
    public void testParse_SetOfUnTaggedChoice() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_SetOfUnTaggedChoice.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_SetOfUnTaggedChoice.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        debugPdus((pdus));

        String tag = "/Human/name[0]/a";
        assertEquals(new BigInteger("10"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());
        tag = "/Human/name[1]/b";
        assertEquals("U", pdus.get(0).<String>getDecodedObject(tag).get());
    }

    @Test
    public void testParse_SetOfSetOfUnTaggedChoice() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_SetOfSetOfUnTaggedChoice.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_SetOfSetOfUnTaggedChoice.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);

        debugPdus((pdus));

        String tag = "/Human/name[0][0]/a";
        assertEquals(new BigInteger("10"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());
        tag = "/Human/name[0][1]/b";
        assertEquals("U", pdus.get(0).<String>getDecodedObject(tag).get());
        tag = "/Human/name[1][0]/a";
        assertEquals(new BigInteger("11"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());
        tag = "/Human/name[1][1]/b";
        assertEquals("V", pdus.get(0).<String>getDecodedObject(tag).get());
    }

    @Test
    public void testParse_SequenceOf() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_SequenceOf.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_SequenceOf_optA.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);

            debugPdus((pdus));

            String tag = "/Human/selection/optA/ints[0]";
            assertEquals(new BigInteger("1"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());
            tag = "/Human/selection/optA/ints[1]";
            assertEquals(new BigInteger("2"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_SequenceOf_optB.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);

            debugPdus((pdus));

            String tag = "/Human/selection/optB/namesInline[0]/first";
            assertEquals("Adam", pdus.get(0).<String>getDecodedObject(tag).get());
            tag = "/Human/selection/optB/namesInline[0]/last";
            assertEquals("Smith", pdus.get(0).<String>getDecodedObject(tag).get());

            tag = "/Human/selection/optB/namesInline[1]/first";
            assertEquals("Michael", pdus.get(0).<String>getDecodedObject(tag).get());
            tag = "/Human/selection/optB/namesInline[1]/last";
            assertEquals("Brown", pdus.get(0).<String>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_SequenceOf_optC.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);

            debugPdus((pdus));

            String tag = "/Human/selection/optC/names[0]/first";
            assertEquals("Adam", pdus.get(0).<String>getDecodedObject(tag).get());
            tag = "/Human/selection/optC/names[0]/last";
            assertEquals("Smith", pdus.get(0).<String>getDecodedObject(tag).get());

            tag = "/Human/selection/optC/names[1]/first";
            assertEquals("Michael", pdus.get(0).<String>getDecodedObject(tag).get());
            tag = "/Human/selection/optC/names[1]/last";
            assertEquals("Brown", pdus.get(0).<String>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
    }

    @Test
    public void testParse_SetOf() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_SetOf.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_SetOf_optA.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);

            debugPdus((pdus));

            String tag = "/Human/selection/optA/ints[0]";
            assertEquals(new BigInteger("1"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());
            tag = "/Human/selection/optA/ints[1]";
            assertEquals(new BigInteger("2"), pdus.get(0).<BigInteger>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_SetOf_optB.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);

            debugPdus((pdus));

            String tag = "/Human/selection/optB/namesInline[0]/first";
            assertEquals("Adam", pdus.get(0).<String>getDecodedObject(tag).get());
            tag = "/Human/selection/optB/namesInline[0]/last";
            assertEquals("Smith", pdus.get(0).<String>getDecodedObject(tag).get());

            tag = "/Human/selection/optB/namesInline[1]/first";
            assertEquals("Michael", pdus.get(0).<String>getDecodedObject(tag).get());
            tag = "/Human/selection/optB/namesInline[1]/last";
            assertEquals("Brown", pdus.get(0).<String>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_SetOf_optC.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);

            debugPdus((pdus));

            String tag = "/Human/selection/optC/names[0]/first";
            assertEquals("Adam", pdus.get(0).<String>getDecodedObject(tag).get());
            tag = "/Human/selection/optC/names[0]/last";
            assertEquals("Smith", pdus.get(0).<String>getDecodedObject(tag).get());

            tag = "/Human/selection/optC/names[1]/first";
            assertEquals("Michael", pdus.get(0).<String>getDecodedObject(tag).get());
            tag = "/Human/selection/optC/names[1]/last";
            assertEquals("Brown", pdus.get(0).<String>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
    }

    @Test
    public void testParse_ChoiceImplicit() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_ChoiceImplicit.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_ChoiceImplicit_milliSeconds.ber"));
        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);
        debugPdus(pdus);

        DecodedAsnData pdu = pdus.get(0);
        String tag = "/Human/payload/name";
        assertEquals("Adam", pdu.<String>getDecodedObject(tag).get());

        tag = "/Human/payload/open/milliSeconds";
        assertEquals(new BigInteger("100"), pdu.<BigInteger>getDecodedObject(tag).get());

        final ValidatorImpl validator = new ValidatorImpl();
        final ValidationResult validationresult = validator.validate(pdus.get(0));
        assertFalse(validationresult.hasFailures());
    }

    @Test
    public void testParse_Choice_ZZZ() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_Choice_ZZZ.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_Choice_ZZZ.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);
            debugPdus(pdus);

            DecodedAsnData pdu = pdus.get(0);
            String tag = "/Human/payload/age/dob";
            Optional<byte[]> actual = pdu.getDecodedObject(tag);
            assertEquals("1973", new String(actual.get(), Charsets.UTF_8));

            tag = "/Human/payload/name";
            assertEquals("Fred", pdu.<String>getDecodedObject(tag).get());

            tag = "/Human/payload/cin/iri-to-CC/cc[0]";
            actual = pdu.getDecodedObject(tag);
            assertEquals("123", new String(actual.get(), Charsets.UTF_8));

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_Choice_ZZZ_2.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);
            debugPdus(pdus);

            DecodedAsnData pdu = pdus.get(0);
            String tag = "/Human/payload/age/dob";
            Optional<byte[]> actual = pdu.getDecodedObject(tag);
            assertEquals("1973", new String(actual.get(), Charsets.UTF_8));

            tag = "/Human/payload/name";
            assertEquals("Fred", pdu.<String>getDecodedObject(tag).get());

            tag = "/Human/payload/cin/iri-to-CC/cc[0]";
            actual = pdu.getDecodedObject(tag);
            assertEquals("123", new String(actual.get(), Charsets.UTF_8));

            tag = "/Human/payload/cin/iri-to-CC/cc[1]";
            actual = pdu.getDecodedObject(tag);
            assertEquals("456", new String(actual.get(), Charsets.UTF_8));

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
    }

    @Test
    public void testParse_ChoicePassthrough_basic() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_Choice_basic.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_Choice_basic_roundYears.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);
            debugPdus(pdus);

            DecodedAsnData pdu = pdus.get(0);
            String tag = "/Human/payload/age/roundYears";
            assertEquals(new BigInteger("42"), pdu.<BigInteger>getDecodedObject(tag).get());

            tag = "/Human/payload/name";
            assertEquals("Fred", pdu.<String>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_Choice_basic_ymd.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);
            debugPdus(pdus);

            DecodedAsnData pdu = pdus.get(0);
            String tag = "/Human/payload/age/ymd/years";
            assertEquals(new BigInteger("42"), pdu.<BigInteger>getDecodedObject(tag).get());
            tag = "/Human/payload/age/ymd/months";
            assertEquals(new BigInteger("2"), pdu.<BigInteger>getDecodedObject(tag).get());
            tag = "/Human/payload/age/ymd/days";
            assertEquals(new BigInteger("22"), pdu.<BigInteger>getDecodedObject(tag).get());

            tag = "/Human/payload/name";
            assertEquals("Fred", pdu.<String>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_Choice_basic_dob.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);
            debugPdus(pdus);

            DecodedAsnData pdu = pdus.get(0);
            String tag = "/Human/payload/age/dob";
            Optional<byte[]> actual = pdu.getDecodedObject(tag);
            assertEquals("1973", new String(actual.get(), Charsets.UTF_8));

            tag = "/Human/payload/name";
            assertEquals("Fred", pdu.<String>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
    }

    @Test
    public void testParse_ChoicePassthrough() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_Choice2.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_Choice2_typeA.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);
            debugPdus(pdus);

            DecodedAsnData pdu = pdus.get(0);
            String tag = "/Human/payload/iRIsContent/typeA/mid/other";
            assertEquals(new BigInteger("10"), pdu.<BigInteger>getDecodedObject(tag).get());

            tag = "/Human/payload/iRIsContent/typeA/mid/stuff";
            assertEquals("U", pdu.<String>getDecodedObject(tag).get());

            tag = "/Human/payload/name";
            assertEquals("payload", pdu.<String>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_Choice2_int.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);
            debugPdus(pdus);

            DecodedAsnData pdu = pdus.get(0);
            String tag = "/Human/payload/iRIsContent/int";
            assertEquals(new BigInteger("10"), pdu.<BigInteger>getDecodedObject(tag).get());

            tag = "/Human/payload/name";
            assertEquals("payload", pdu.<String>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_Choice2_sofA.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);
            debugPdus(pdus);

            DecodedAsnData pdu = pdus.get(0);
            String tag = "/Human/payload/iRIsContent/sequenceOfA[0]/mid/other";
            assertEquals(new BigInteger("10"), pdu.<BigInteger>getDecodedObject(tag).get());

            tag = "/Human/payload/iRIsContent/sequenceOfA[0]/mid/stuff";
            assertEquals("U", pdu.<String>getDecodedObject(tag).get());

            tag = "/Human/payload/name";
            assertEquals("payload", pdu.<String>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_Choice2_sofA_2_mid_entries.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);
            debugPdus(pdus);

            DecodedAsnData pdu = pdus.get(0);
            String tag = "/Human/payload/iRIsContent/sequenceOfA[0]/mid/other";
            assertEquals(new BigInteger("10"), pdu.<BigInteger>getDecodedObject(tag).get());

            tag = "/Human/payload/iRIsContent/sequenceOfA[0]/mid/stuff";
            assertEquals("U", pdu.<String>getDecodedObject(tag).get());
            tag = "/Human/payload/name";
            assertEquals("payload", pdu.<String>getDecodedObject(tag).get());

            tag = "/Human/payload/iRIsContent/sequenceOfA[1]/mid/other";
            assertEquals(new BigInteger("11"), pdu.<BigInteger>getDecodedObject(tag).get());
            tag = "/Human/payload/iRIsContent/sequenceOfA[1]/mid/stuff";
            assertEquals("V", pdu.<String>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_Choice2_setOfA.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);
            debugPdus(pdus);

            DecodedAsnData pdu = pdus.get(0);
            String tag = "/Human/payload/iRIsContent/setOfA[0]/mid/other";
            assertEquals(new BigInteger("10"), pdu.<BigInteger>getDecodedObject(tag).get());

            tag = "/Human/payload/iRIsContent/setOfA[0]/mid/stuff";
            assertEquals("U", pdu.<String>getDecodedObject(tag).get());

            tag = "/Human/payload/name";
            assertEquals("payload", pdu.<String>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
        {
            final ByteSource berData = Resources.asByteSource(getClass().getResource(
                    "/Human_Choice2_setOfA_2entries.ber"));
            String topLevelType = "Human";

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);
            debugPdus(pdus);

            DecodedAsnData pdu = pdus.get(0);
            String tag = "/Human/payload/iRIsContent/setOfA[0]/mid/other";
            assertEquals(new BigInteger("10"), pdu.<BigInteger>getDecodedObject(tag).get());

            tag = "/Human/payload/iRIsContent/setOfA[0]/mid/stuff";
            assertEquals("U", pdu.<String>getDecodedObject(tag).get());
            tag = "/Human/payload/name";
            assertEquals("payload", pdu.<String>getDecodedObject(tag).get());

            tag = "/Human/payload/iRIsContent/setOfA[1]/mid/other";
            assertEquals(new BigInteger("11"), pdu.<BigInteger>getDecodedObject(tag).get());
            tag = "/Human/payload/iRIsContent/setOfA[1]/mid/stuff";
            assertEquals("V", pdu.<String>getDecodedObject(tag).get());

            final ValidatorImpl validator = new ValidatorImpl();
            final ValidationResult validationresult = validator.validate(pdus.get(0));
            assertFalse(validationresult.hasFailures());
        }
    }

    @Test
    public void testParse_ChoiceDuplicate() throws Exception
    {
        try
        {
            final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                    "/ChoiceDuplicate.asn"), Charsets.UTF_8);
            AsnSchemaReader.read(schemaData);
            fail("Should have thrown an IOException because of duplicate tags");
        }
        catch (IOException e)
        {
        }
    }

    @Test
    public void testParse_Choice3() throws Exception
    {
        final CharSource schemaData = Resources.asCharSource(getClass().getResource(
                "/Human_Choice3.asn"), Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData = Resources.asByteSource(getClass().getResource(
                "/Human_Choice3_typeB.ber"));

        String topLevelType = "Human";

        final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                schema,
                topLevelType);
        debugPdus(pdus);

        DecodedAsnData pdu = pdus.get(0);
        String tag = "/Human/payload/iRIsContent/typeB/other";
        assertEquals(new BigInteger("10"), pdu.<BigInteger>getDecodedObject(tag).get());
        tag = "/Human/payload/iRIsContent/typeB/stuff";
        assertEquals("U", pdu.<String>getDecodedObject(tag).get());

        final ValidatorImpl validator = new ValidatorImpl();
        final ValidationResult validationresult = validator.validate(pdus.get(0));
        assertFalse(validationresult.hasFailures());
    }

    @Test
    public void testParse_EtsiV122() throws Exception
    {
        //fail("for quick runs");

        long start = System.currentTimeMillis();

        // TODO ASN-137, ASN-141 prevent us from being able to parse the EIFv122.asn schema

        final CharSource schemaData = Resources.asCharSource(getClass().getResource("/EIFv122.asn"),
                Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData = Resources.asByteSource(getClass().getResource("/test.ber"));

        final ByteSource berData5 = Resources.asByteSource(getClass().getResource("/test5.ber"));

        long mid = System.currentTimeMillis();

        String topLevelType = "PS-PDU";

        long mid2 = System.currentTimeMillis();

        {

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);

            logger.debug("Results of /test.ber");

            debugPdus(pdus);

            assertEquals(3, pdus.size());
            assertEquals(0, pdus.get(0).getUnmappedTags().size());
            assertEquals(0, pdus.get(1).getUnmappedTags().size());
            assertEquals(0, pdus.get(2).getUnmappedTags().size());

            String tag = "/PS-PDU/pSHeader/communicationIdentifier/communicationIdentityNumber";

            BigInteger number = pdus.get(0).<BigInteger>getDecodedObject(tag).get();
            assertEquals(new BigInteger("622697890"), number);

            tag = "/PS-PDU/pSHeader/sequenceNumber";
            number = pdus.get(0).<BigInteger>getDecodedObject(tag).get();
            assertEquals(new BigInteger("0"), number);

            tag = "/PS-PDU/pSHeader/authorizationCountryCode";
            String str = pdus.get(0).<String>getDecodedObject(tag).get();
            assertEquals("AU", str);

            tag = "/PS-PDU/pSHeader/communicationIdentifier/deliveryCountryCode";
            str = pdus.get(1).<String>getDecodedObject(tag).get();
            assertEquals("AU", str);

            tag
                    = "/PS-PDU/pSHeader/communicationIdentifier/networkIdentifier/networkElementIdentifier";
            Optional<byte[]> bytes = pdus.get(1).getDecodedObject(tag);
            str = new String(bytes.get(), Charsets.UTF_8);
            assertEquals("BAEProd2", str);

            tag = "/PS-PDU/pSHeader/sequenceNumber";
            number = pdus.get(2).<BigInteger>getDecodedObject(tag).get();
            assertEquals(new BigInteger("8"), number);
        }

        {

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData5,
                    schema,
                    topLevelType);

            logger.debug("Results of /test5.ber");

            debugPdus(pdus);

            String tag = "/PS-PDU/pSHeader/communicationIdentifier/communicationIdentityNumber";

            Optional<BigInteger> number = pdus.get(0).getDecodedObject(tag);
            assertEquals(new BigInteger("622697903"), number.get());

            tag = "/PS-PDU/pSHeader/sequenceNumber";
            number = pdus.get(0).getDecodedObject(tag);
            assertEquals(new BigInteger("0"), number.get());

            tag = "/PS-PDU/pSHeader/authorizationCountryCode";
            Optional<String> str = pdus.get(0).getDecodedObject(tag);
            assertEquals("AU", str.get());

            tag = "/PS-PDU/pSHeader/communicationIdentifier/deliveryCountryCode";
            str = pdus.get(1).getDecodedObject(tag);
            assertEquals("AU", str.get());

            tag
                    = "/PS-PDU/pSHeader/communicationIdentifier/networkIdentifier/networkElementIdentifier";
            Optional<byte[]> bytes = pdus.get(1).getDecodedObject(tag);
            String octetToString = new String(bytes.get(), Charsets.UTF_8);
            assertEquals("BAEProd2", octetToString);

            tag = "/PS-PDU/pSHeader/communicationIdentifier/cINExtension/iri-to-CC/cc[0]";
            bytes = pdus.get(1).getDecodedObject(tag);
            octetToString = new String(bytes.get(), Charsets.UTF_8);
            assertEquals("3030", octetToString);

            assertEquals(15, pdus.size());
            for (int i = 0; i < 14; i++)
            {
                assertEquals(0, pdus.get(i).getUnmappedTags().size());
            }
        }

        long end = System.currentTimeMillis();

        logger.error("Duration {}ms, just schema {}ms, files {}ms",
                (end - mid2),
                (mid - start),
                (mid2 - mid));

        //        {
        //            String berFilename = getClass().getResource("/mock/SSI_CC.etsi").getFile();
        //            final File berFile = new File(berFilename);
        //            String topLevelType = "PS-PDU";
        //
        //            final ImmutableList<DecodedAsnData> pdus = AsnDecoder.getDecodedTags(berFile,
        //                    schema,
        //                    topLevelType);
        //
        //            logger.debug("Results of /mock/SSI_CC.etsi");
        //            debugPdus(pdus);
        ///*
        //            for (int i = 0; i < pdus.size(); i++)
        //            {
        //
        //
        //                final ValidatorImpl validator = new ValidatorImpl();
        //                final ValidationResult validationresult = validator.validate(pdu);
        //                // TODO - we should get a validation failure where we can't determine the type of a tag
        //                //assertTrue(validationresult.hasFailures());
        //
        //                ImmutableSet<DecodedTagValidationFailure> failures = validationresult.getFailures();
        //                //assertEquals(1, failures.size());
        //                logger.warn("Validation failures: {}", failures.size());
        //
        //                for (DecodedTagValidationFailure fail : failures)
        //                {
        //
        //                    logger.info("Tag: " + fail.getTag() +
        //                            " reason: " + fail.getFailureReason() +
        //                            " type: " + fail.getFailureType());
        //                }
        //
        //            }
        //*/
        //
        //            String tag = "/PS-PDU/pSHeader/communicationIdentifier/communicationIdentityNumber";
        //
        //            BigInteger number = (BigInteger)pdus.get(0).getDecodedObject(tag);
        //            logger.info("communicationIdentityNumber: " + number);
        //
        //            String str = (String)pdus.get(0).getDecodedObject("/PS-PDU/pSHeader/authorizationCountryCode");
        //            logger.info("authorizationCountryCode: " + str);
        //
        //            str = (String)pdus.get(1).getDecodedObject("/PS-PDU/pSHeader/communicationIdentifier/deliveryCountryCode");
        //            logger.info("deliveryCountryCode: {}", str);
        //
        //            byte [] bytes = (byte [])pdus.get(1).getDecodedObject("/PS-PDU/pSHeader/communicationIdentifier/networkIdentifier/networkElementIdentifier");
        //            String s = new String(bytes, Charsets.UTF_8);
        //            logger.info("networkElementIdentifier: {} - from Octet String", s);
        //
        //
        //            try
        //            {
        //
        //            }
        //            catch (Exception e)
        //            {
        //
        //            }
        //        }
/*
        {
            String berFilename = getClass().getResource("/mock/SSI_IRI.etsi").getFile();
            final File berFile = new File(berFilename);
            String topLevelType = "PS-PDU";

            final ImmutableList<DecodedAsnData> pdus = AsnDecoder.getDecodedTags(berFile,
                    schema,
                    topLevelType);

            for (int i = 0; i < pdus.size(); i++)
            {

                logger.info("Parsing PDU[{}]", i);
                final DecodedAsnData pdu = pdus.get(i);
                for (String tag : pdu.getTags())
                {
                    logger.info("\t{} => {} as {}",
                            tag,
                            pdu.getHexString(tag),
                            pdu.getType(tag).getBuiltinType());
                }
                for (String tag : pdu.getUnmappedTags())
                {
                    logger.info("\t? {} => {}", tag, pdu.getHexString(tag));
                }
            }
        }
*/
        /*
        BigInteger sequenceNumber = (BigInteger)pdus.get(0).getDecodedObject("/PS-PDU/pSHeader/sequenceNumber");
        logger.info("sequenceNumber[0]: " + sequenceNumber);
        BigInteger communicationIdentityNumber = (BigInteger)pdus.get(0).getDecodedObject("/PS-PDU/pSHeader/communicationIdentifier/communicationIdentityNumber");
        logger.info("communicationIdentityNumber[0]: " + communicationIdentityNumber);

        communicationIdentityNumber = (BigInteger)pdus.get(1).getDecodedObject("/PS-PDU/pSHeader/communicationIdentifier/communicationIdentityNumber");
        logger.info("communicationIdentityNumber[1]: {}", communicationIdentityNumber);

        sequenceNumber = (BigInteger)pdus.get(2).getDecodedObject("/PS-PDU/pSHeader/sequenceNumber");
        logger.info("sequenceNumber[2]: " + sequenceNumber);
*/

    }

    @Test
    public void testParse_Duplicates() throws Exception
    {
        try
        {
            AsnSchemaParser.parse(HUMAN_DUPLICATE_CHOICE);
            fail("Should have thrown parse exception");
        }
        catch (ParseException e)
        {
        }

        try
        {
            AsnSchemaParser.parse(HUMAN_DUPLICATE_SET);
            fail("Should have thrown parse exception");
        }
        catch (ParseException e)
        {
        }
        try
        {
            AsnSchemaParser.parse(HUMAN_DUPLICATE_SEQUENCE);
            fail("Should have thrown parse exception");
        }
        catch (ParseException e)
        {
        }

    }

    @Test
    public void testParse_HumanUsingTypeDefBroken() throws Exception
    {
        try
        {
            AsnSchemaParser.parse(HUMAN_BROKEN_MISSING_TYPEDEF);
            fail("Should have thrown parse exception");
        }
        catch (ParseException e)
        {
        }
        try
        {
            AsnSchemaParser.parse(HUMAN_BROKEN_MISSING_IMPORT);
            fail("Should have thrown parse exception");
        }
        catch (ParseException e)
        {
        }
        try
        {
            AsnSchemaParser.parse(HUMAN_BROKEN_MISSING_IMPORTED_TYPEDEF);
            fail("Should have thrown parse exception");
        }
        catch (ParseException e)
        {
        }
    }

    @Test
    public void testPerformance() throws Exception
    {
        long start = System.currentTimeMillis();

        // TODO ASN-137, ASN-141 prevent us from being able to parse the EIFv122.asn schema
        final CharSource schemaData = Resources.asCharSource(getClass().getResource("/EIFv122.asn"),
                Charsets.UTF_8);
        AsnSchema schema = AsnSchemaReader.read(schemaData);

        final ByteSource berData = Resources.asByteSource(getClass().getResource("/test.ber"));

        final ByteSource berData5 = Resources.asByteSource(getClass().getResource("/test5.ber"));
        String topLevelType = "PS-PDU";

        for (int z = 0; z < 5; ++z)
        {

            final ImmutableList<DecodedAsnData> pdus = Asanti.decodeAsnData(berData,
                    schema,
                    topLevelType);
            assertEquals(3, pdus.size());
            for (int i = 0; i < 3; i++)
            {
                assertEquals(0, pdus.get(i).getUnmappedTags().size());
            }

            final ImmutableList<DecodedAsnData> pdus2 = Asanti.decodeAsnData(berData5,
                    schema,
                    topLevelType);
            assertEquals(15, pdus2.size());
            for (int i = 0; i < 14; i++)
            {
                assertEquals(0, pdus2.get(i).getUnmappedTags().size());
            }
        }

        long end = System.currentTimeMillis();

        long duration = end - start;
        logger.error("Duration {}ms", duration);

        // On a Michael's dev machine this takes less than 1500ms with logging set to INFO
        // and less than 2000ms set to DEBUG.
        long threshold = 5000;
        if (logger.isDebugEnabled())
        {
            threshold *= 2;
        }

        assertThat(duration, lessThan(threshold));
    }

    /**
     * Do a dump of all the data, both Mapped and Unmapped in all DecodedAsnData Defaults to using
     * {@link DecodedAsnData#getHexString} format.
     *
     * @param pdus
     *         the input DecodedAsnData objects
     */
    public static void debugPdus(Iterable<DecodedAsnData> pdus)
    {
        debugPdus(pdus, true);
    }

    /**
     * @param pdus
     *         the input DecodedAsnData objects
     * @param printHexString
     *         determines whether to use {@link DecodedAsnData#getHexString} (if true) or {@link
     *         DecodedAsnData#getPrintableString} (if false)
     */
    public static void debugPdus(Iterable<DecodedAsnData> pdus, boolean printHexString)
    {
        int i = 0;
        for (DecodedAsnData pdu : pdus)
        {
            logger.info("Parsing PDU[{}]", i);
            for (String t : pdu.getTags())
            {
                try
                {
                    logger.info("\t{} => {} as {}",
                            t,
                            (printHexString ?
                                    pdu.getHexString(t).get() :
                                    pdu.getPrintableString(t).get()),
                            pdu.getType(t).getBuiltinType());
                }
                catch (DecodeException e)
                {
                    logger.info("\t\tDecodeException {}", e.getMessage());
                }
            }

            for (String t : pdu.getUnmappedTags())
            {
                logger.info("\t?{} => {}", t, pdu.getHexString(t));
            }

            i++;
        }
    }
}