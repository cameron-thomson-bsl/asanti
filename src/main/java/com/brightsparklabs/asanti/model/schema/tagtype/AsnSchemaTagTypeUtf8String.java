package com.brightsparklabs.asanti.model.schema.tagtype;

import com.brightsparklabs.asanti.model.schema.AsnBuiltinType;
import com.brightsparklabs.asanti.model.schema.constraint.AsnSchemaConstraint;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaTagTypeVisitor;

/**
 * An {@code UTF8String} component/tag type from a within a module specification
 * within an ASN.1 schema.
 *
 * @author brightSPARK Labs
 */
public class AsnSchemaTagTypeUtf8String extends AbstractAsnSchemaTagType
{
    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    /**
     * Default constructor.
     *
     * @param constraint
     *            The constraint on the type. Use
     *            {@link AsnSchemaConstraint#NULL} if no constraint.
     *            <p>
     *            E.g. For {@code Utf8String (SIZE (1..50))} this would be
     *            {@code SIZE (1..50)}
     *
     * @throws NullPointerException
     *             if {@code name} is {@code null}
     *
     */
    public AsnSchemaTagTypeUtf8String(AsnSchemaConstraint constraint)
    {
        super(AsnBuiltinType.Utf8String, constraint);
    }

    // -------------------------------------------------------------------------
    // IMPLEMENTATION: OLDAsnSchemaTypeDefinition
    // -------------------------------------------------------------------------

    @Override
    public Object visit(AsnSchemaTagTypeVisitor<?> visitor)
    {
        return visitor.visit(this);
    }

}
