package com.brightsparklabs.asanti.model.schema.tagtype;

import com.brightsparklabs.asanti.model.schema.AsnBuiltinType;
import com.brightsparklabs.asanti.model.schema.constraint.AsnSchemaConstraint;
import com.brightsparklabs.asanti.model.schema.typedefinition.AsnSchemaTagTypeVisitor;

/**
 * Created by Michael on 12/06/2015.
 */
public class AsnSchemaTagTypeSequenceOf extends AbstractAsnSchemaTagType
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
     *            E.g for
     *            {@code SEQUENCE (SIZE (1..100)) OF OCTET STRING (SIZE (256))}
     *            this would be {@code SIZE (256)}
     *
     * @throws NullPointerException
     *             if {@code name}, {@code elementTypeName} or
     *             {@code componentTypes} are {@code null}
     *
     * @throws IllegalArgumentException
     *             if {@code name} or {@code elementTypeName} is blank
     */
    public AsnSchemaTagTypeSequenceOf(AsnSchemaConstraint constraint)
    {
        super(AsnBuiltinType.SequenceOf, constraint);
    }

    // -------------------------------------------------------------------------
    // IMPLEMENTATION: AsnSchemaTypeDefinition
    // -------------------------------------------------------------------------

    @Override
    public Object visit(AsnSchemaTagTypeVisitor<?> visitor)
    {
        return visitor.visit(this);
    }

}
