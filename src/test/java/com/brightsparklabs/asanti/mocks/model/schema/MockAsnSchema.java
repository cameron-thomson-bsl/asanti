/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */
package com.brightsparklabs.asanti.mocks.model.schema;

import com.brightsparklabs.asanti.common.OperationResult;
import com.brightsparklabs.asanti.model.schema.AsnBuiltinType;
import com.brightsparklabs.asanti.model.schema.AsnSchema;
import com.brightsparklabs.asanti.model.schema.DecodedTag;
import com.brightsparklabs.asanti.model.schema.type.AsnSchemaType;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * Utility class for obtaining mocked instances of {@link AsnSchema} which conform to the test ASN.1
 * schema defined in the {@linkplain README.md} file
 *
 * @author brightSPARK Labs
 */
public class MockAsnSchema
{
    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------

    /** the example schema defined in the {@linkplain README.md} file */
    public static final String TEST_SCHEMA_TEXT = new StringBuilder().append("Document-PDU\n")
            .append("    { joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) document(1) }\n")
            .append("DEFINITIONS")
            .append("    AUTOMATIC TAGS ::=")
            .append("BEGIN")
            .append("EXPORTS Header, Body;\n")
            .append("IMPORTS")
            .append("  People,")
            .append("  Person")
            .append("    FROM People-Protocol")
            .append("    { joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) };")
            .append("    Document ::= SEQUENCE")
            .append("    {\n")
            .append("        header  [1] Header,\n")
            .append("        body    [2] Body,\n")
            .append("        footer  [3] Footer,\n")
            .append("        dueDate [4] Date-Due,\n")
            .append("        version [5] SEQUENCE\n")
            .append("        {\n")
            .append("            majorVersion [0] INTEGER,\n")
            .append("            minorVersion [1] INTEGER\n")
            .append("        },\n")
            .append("        description [6] SET\n")
            .append("        {\n")
            .append("            numberLines [0] INTEGER,\n")
            .append("            summary     [1] OCTET STRING\n")
            .append("        } OPTIONAL\n")
            .append("    }\n")
            .append("    Header ::= SEQUENCE\n")
            .append("    {")
            .append("        published [0] PublishedMetadata")
            .append("    }\n")
            .append("    Body ::= SEQUENCE")
            .append("    {")
            .append("        lastModified [0] ModificationMetadata,")
            .append("        prefix       [1] Section-Note OPTIONAL,")
            .append("        content      [2] Section-Main,\n")
            .append("        suffix       [3] Section-Note OPTIONAL")
            .append("    }\n")
            .append("    Footer ::= SET")
            .append("    {")
            .append("        authors [0] People")
            .append("    }\n")
            .append("    PublishedMetadata ::= SEQUENCE")
            .append("    {")
            .append("        date    [1] GeneralizedTime,")
            .append("        country [2] OCTET STRING OPTIONAL")
            .append("    }\n")
            .append("    ModificationMetadata ::= SEQUENCE")
            .append("    {")
            .append("        date       [0] DATE,")
            .append("        modifiedBy [1] Person")
            .append("    }\n")
            .append("    Section-Note ::= SEQUENCE")
            .append("    {")
            .append("        text [1] OCTET STRING")
            .append("    }\n")
            .append("    Section-Main ::= SEQUENCE")
            .append("    {")
            .append("        text       [1] OCTET STRING OPTIONAL,")
            .append("        paragraphs [2] SEQUENCE OF Paragraph,")
            .append("        sections   [3] SET OF")
            .append("                       SET")
            .append("                       {")
            .append("                            number [1] INTEGER,")
            .append("                            text   [2] OCTET STRING")
            .append("                       }")
            .append("    }\n")
            .append("    Paragraph ::=  SEQUENCE")
            .append("    {")
            .append("        title        [1] OCTET STRING,")
            .append("        contributor  [2] Person OPTIONAL,")
            .append("        points       [3] SEQUENCE OF OCTET STRING")
            .append("    }\n")
            .append("    References ::= SEQUENCE (SIZE (1..50)) OF")
            .append("    SEQUENCE")
            .append("    {")
            .append("        title [1] OCTET STRING,")
            .append("        url   [2] OCTET STRING")
            .append("    }\n")
            .append("    Date-Due ::= INTEGER\n")
            .append("    {")
            .append("      tomorrow(0),\n")
            .append("      three-day(1),\n")
            .append("      week (2)\n")
            .append("    } DEFAULT week\n")
            .append("END")
            .append("\n")
            .append("People-Protocol\r\n\r\n")
            .append("\t\t{ joint-iso-itu-t internationalRA(23) set(42) set-vendors(9) example(99) modules(2) people(2) }\r\n")
            .append("DEFINITIONS\r\n")
            .append("\t\tAUTOMATIC TAGS ::=\r\n")
            .append("BEGIN\r\n")
            .append("\t\tDefaultAge INTEGER ::= 45\r\n")
            .append("\t\tPeople ::= SET OF Person\r\n")
            .append("\t\tPerson ::= SEQUENCE\r\n")
            .append("\t\t{\r\n")
            .append("\t\t\t\tfirstName \t[1]\t OCTET STRING,\r\n")
            .append("\t\t\t\tlastName \t [2]\t OCTET STRING,\r\n")
            .append("\t\t\t\ttitle\t\t   [3]\t ENUMERATED\r\n")
            .append("\t\t\t\t\t\t{ mr, mrs, ms, dr, rev } OPTIONAL,\r\n")
            .append("\t\t\t\tgender \t \tGender OPTIONAL,\r\n")
            .append("\t\t\t\tmaritalStatus CHOICE\n")
            .append("\t\t\t\t\t{ Married [0], Single [1] }\n")
            .append("\t\t}  \n")
            .append("\t\tGender ::= ENUMERATED   \r\n")
            .append("\t\t{")
            .append("\t\t\t\tmale(0), \r\n")
            .append("\t\t\t\tfemale(1)\t\t\r\n")
            .append("\t\t}\r\n")
            .append("END\r\n")
            .toString();

    // -------------------------------------------------------------------------
    // INSTANCE VARIABLES
    // -------------------------------------------------------------------------

    /** singleton instance */
    private static AsnSchema instance;

    private static Set<String> rawTags = Sets.newLinkedHashSet();

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    /**
     * Returns a singleton instance of this class
     *
     * @return a singleton instance
     */
    public static AsnSchema getInstance() throws Exception
    {
        if (instance != null)
        {
            return instance;
        }

        instance = mock(AsnSchema.class);

        ImmutableSet<OperationResult<DecodedTag>> results
                = ImmutableSet.<OperationResult<DecodedTag>>builder()
                .add(configureGetDecodedTag("/1/0/1",
                        "/Document/header/published/date",
                        true,
                        AsnBuiltinType.Date))
                .add(configureGetDecodedTag("/2/0/0",
                        "/Document/body/lastModified/date",
                        true,
                        AsnBuiltinType.Date))
                .add(configureGetDecodedTag("/2/1/1",
                        "/Document/body/prefix/text",
                        true,
                        AsnBuiltinType.OctetString))
                .add(configureGetDecodedTag("/2/2/1",
                        "/Document/body/content/text",
                        true,
                        AsnBuiltinType.OctetString))
                .add(configureGetDecodedTag("/3/0/1",
                                "/Document/footer/author/firstName",
                                true,
                                AsnBuiltinType.OctetString))
                .add(configureGetDecodedTag("/2/2/99",
                        "/Document/body/content/99",
                        false,
                        AsnBuiltinType.Null))
                .add(configureGetDecodedTag("/99/1/1",
                        "/Document/99/1/1",
                        false,
                        AsnBuiltinType.Null))
                .build();



        //        public ImmutableSet<OperationResult<DecodedTag>> getDecodedTags(ImmutableSet<String> rawTags,
        //            String topLevelTypeName);
        String topLevelTypeName = "Document";
            when(instance.getDecodedTags(ImmutableSet.copyOf(rawTags), topLevelTypeName)).thenReturn(
                    results);

        ImmutableSet<String> emptyTags = ImmutableSet.of();
        ImmutableSet<OperationResult<DecodedTag>> emptyResults = ImmutableSet.of();

        when(instance.getDecodedTags(ImmutableSet.copyOf(rawTags), topLevelTypeName)).thenReturn(results);
        when(instance.getDecodedTags(emptyTags, topLevelTypeName)).thenReturn(emptyResults);

        return instance;
    }

    // -------------------------------------------------------------------------
    // INTERNAL CLASS: NonEmptyByteArrayMatcher
    // -------------------------------------------------------------------------

    /**
     * Configures the {@code getDecodedTag()} method on the supplied instance to use the mocked
     * values supplied
     *
     * @param instance
     *         instance to configure
     * @param rawTag
     *         the raw tag to return
     * @param topLevelTypeName
     *         the top level type name
     * @param decodedTagPath
     *         the value to return for {@link DecodedTag#getTag()}
     * @param isFullyDecoded
     *         the value to return for {@link OperationResult#wasSuccessful()}
     */
    private static OperationResult<DecodedTag> configureGetDecodedTag(String rawTag,
            String decodedTagPath, boolean isFullyDecoded, AsnBuiltinType builtinType)
    {
        final DecodedTag decodedTag = mock(DecodedTag.class);

        final AsnSchemaType type = mock(AsnSchemaType.class);
        when(type.getBuiltinType()).thenReturn(builtinType);

        when(decodedTag.getTag()).thenReturn(decodedTagPath);
        when(decodedTag.getRawTag()).thenReturn(rawTag);
        when(decodedTag.getType()).thenReturn(type);
        when(decodedTag.getType()).thenReturn(type);
        when(decodedTag.isFullyDecoded()).thenReturn(isFullyDecoded);

        OperationResult<DecodedTag> result = mock(OperationResult.class);

        when(result.getOutput()).thenReturn(decodedTag);
        when(result.wasSuccessful()).thenReturn(isFullyDecoded);
        when(result.getFailureReason()).thenReturn("mock failure reason");

        rawTags.add(rawTag);

        return result;

    }
    //    private static void configureGetDecodedTag(AsnSchema instance, String rawTag,
    //            String topLevelTypeName, String decodedTagPath, boolean isFullyDecoded,
    //            AsnBuiltinType builtinType)
    //    {
    //        final DecodedTag decodedTag = mock(DecodedTag.class);
    //
    //        final AsnSchemaType type = mock(AsnSchemaType.class);
    //
    //        when(type.getBuiltinType()).thenReturn(builtinType);
    //
    //        when(decodedTag.getTag()).thenReturn(decodedTagPath);
    //        when(decodedTag.getRawTag()).thenReturn(rawTag);
    //        //when(decodedTag.getType()).thenReturn(AsnSchemaType.NULL);
    //        when(decodedTag.getType()).thenReturn(type);
    //        when(decodedTag.isFullyDecoded()).thenReturn(isFullyDecoded);
    //
    //        // TODO MJF - figure out how to deal with the session stuff here.  We also want unit tests
    //        // to test the new method of tagging etc...
    //        when(instance.getDecodedTags(rawTag, topLevelTypeName)).thenReturn(isFullyDecoded ?
    //                OperationResult.createSuccessfulInstance(decodedTag) :
    //                OperationResult.createUnsuccessfulInstance(decodedTag, "Mock Failure"));
    //    }
}
