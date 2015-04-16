/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.asanti.decoder.builtin;

import com.brightsparklabs.asanti.common.DecodeException;
import com.brightsparklabs.asanti.model.schema.AsnBuiltinType;
import com.brightsparklabs.asanti.validator.ValidationResult;
import com.brightsparklabs.asanti.validator.bytes.AsnByteValidator;

/**
 * Decoder for data of type {@link AsnBuiltinType#BmpString}
 *
 * @author brightSPARK Labs
 */
public class BmpStringDecoder extends AbstractBuiltinTypeDecoder<String>
{
    // -------------------------------------------------------------------------
    // INSTANCE VARIABLES
    // -------------------------------------------------------------------------

    /** singleton instance */
    private static BmpStringDecoder instance;

    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    /**
     * Default constructor.
     *
     * <p>This is private, use {@link #getInstance()} to obtain an instance</p>
     */
    private BmpStringDecoder() {}

    /**
     * Returns a singleton instance of this class
     *
     * @return a singleton instance of this class
     */
    public static BmpStringDecoder getInstance()
    {
        if (instance == null)
        {
            instance = new BmpStringDecoder();
        }
        return instance;
    }

    // -------------------------------------------------------------------------
    // IMPLEMENTATION: AbstractBuiltinTypeDecoder
    // -------------------------------------------------------------------------

    @Override
    public String decode(final byte[] bytes) throws DecodeException
    {
        final ValidationResult validationResult = AsnByteValidator.validateAsBmpString(bytes);
        DecodeException.throwIfHasFailures(validationResult);
        // TODO: ASN-107 implement
        return null;
    }
}