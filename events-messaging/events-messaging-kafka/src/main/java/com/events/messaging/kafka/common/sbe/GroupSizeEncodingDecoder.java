package com.events.messaging.kafka.common.sbe;

import org.agrona.DirectBuffer;

public class GroupSizeEncodingDecoder
{
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final int ENCODED_LENGTH = 4;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private int offset;
    private DirectBuffer buffer;

    public GroupSizeEncodingDecoder wrap(final DirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;

        return this;
    }

    public DirectBuffer buffer()
    {
        return buffer;
    }

    public int offset()
    {
        return offset;
    }

    public int encodedLength()
    {
        return ENCODED_LENGTH;
    }

    public int sbeSchemaId()
    {
        return SCHEMA_ID;
    }

    public int sbeSchemaVersion()
    {
        return SCHEMA_VERSION;
    }

    public static int blockLengthEncodingOffset()
    {
        return 0;
    }

    public static int blockLengthEncodingLength()
    {
        return 2;
    }

    public static int blockLengthSinceVersion()
    {
        return 0;
    }

    public static int blockLengthNullValue()
    {
        return 65535;
    }

    public static int blockLengthMinValue()
    {
        return 0;
    }

    public static int blockLengthMaxValue()
    {
        return 65534;
    }

    public int blockLength()
    {
        return (buffer.getShort(offset + 0, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF);
    }


    public static int numInGroupEncodingOffset()
    {
        return 2;
    }

    public static int numInGroupEncodingLength()
    {
        return 2;
    }

    public static int numInGroupSinceVersion()
    {
        return 0;
    }

    public static int numInGroupNullValue()
    {
        return 65535;
    }

    public static int numInGroupMinValue()
    {
        return 0;
    }

    public static int numInGroupMaxValue()
    {
        return 65534;
    }

    public int numInGroup()
    {
        return (buffer.getShort(offset + 2, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF);
    }


    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        builder.append('(');
        builder.append("blockLength=");
        builder.append(blockLength());
        builder.append('|');
        builder.append("numInGroup=");
        builder.append(numInGroup());
        builder.append(')');

        return builder;
    }
}
