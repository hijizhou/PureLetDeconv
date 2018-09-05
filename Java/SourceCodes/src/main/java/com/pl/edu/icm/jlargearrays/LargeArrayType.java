/* ***** BEGIN LICENSE BLOCK *****
 * JLargeArrays
 * Copyright (C) 2013 onward University of Warsaw, ICM
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ***** END LICENSE BLOCK ***** */
package com.pl.edu.icm.jlargearrays;

/**
 * Supported types of large arrays.
 *
 * @author Piotr Wendykier (p.wendykier@icm.edu.pl)
 */
public enum LargeArrayType
{

    LOGIC,
    BYTE
    {

        @Override
        public boolean isNumericType()
        {
            return true;
        }

        @Override
        public boolean isIntegerNumericType()
        {
            return true;
        }

    },
    UNSIGNED_BYTE
    {

        @Override
        public boolean isNumericType()
        {
            return true;
        }

        @Override
        public boolean isIntegerNumericType()
        {
            return true;
        }

    },
    SHORT
    {
        @Override
        public long sizeOf()
        {
            return 2;
        }

        @Override
        public boolean isNumericType()
        {
            return true;
        }

        @Override
        public boolean isIntegerNumericType()
        {
            return true;
        }

    },
    INT
    {
        @Override
        public long sizeOf()
        {
            return 4;
        }

        @Override
        public boolean isNumericType()
        {
            return true;
        }

        @Override
        public boolean isIntegerNumericType()
        {
            return true;
        }

    },
    LONG
    {
        @Override
        public long sizeOf()
        {
            return 8;
        }

        @Override
        public boolean isNumericType()
        {
            return true;
        }

        @Override
        public boolean isIntegerNumericType()
        {
            return true;
        }

    },
    FLOAT
    {
        @Override
        public long sizeOf()
        {
            return 4;
        }

        @Override
        public boolean isNumericType()
        {
            return true;
        }

        @Override
        public boolean isRealNumericType()
        {
            return true;
        }

    },
    DOUBLE
    {
        @Override
        public long sizeOf()
        {
            return 8;
        }

        @Override
        public boolean isNumericType()
        {
            return true;
        }

        @Override
        public boolean isRealNumericType()
        {
            return true;
        }

    },
    COMPLEX_FLOAT
    {
        @Override
        public long sizeOf()
        {
            return 4;
        }

        @Override
        public boolean isNumericType()
        {
            return true;
        }

        @Override
        public boolean isComplexNumericType()
        {
            return true;
        }

    },
    COMPLEX_DOUBLE
    {
        @Override
        public long sizeOf()
        {
            return 8;
        }

        @Override
        public boolean isNumericType()
        {
            return true;
        }

        @Override
        public boolean isComplexNumericType()
        {
            return true;
        }

    },
    STRING,
    OBJECT;

    /**
     * Returns the size (in bytes) of a given LargeArray type.
     * <p>
     * @return size (in bytes) of a given LargeArray type
     */
    public long sizeOf()
    {
        return 1;

    }

    /**
     * Returns true if a given LargeArray type is numeric, false otherwise
     * <p>
     * @return true if a given LargeArray type is numeric, false otherwise
     */
    public boolean isNumericType()
    {
        return false;
    }

    /**
     * Returns true if a given LargeArray type is integer numeric, false otherwise
     * <p>
     * @return true if a given LargeArray type is integer numeric, false otherwise
     */
    public boolean isIntegerNumericType()
    {
        return false;
    }

    /**
     * Returns true if a given LargeArray type is real numeric, false otherwise
     * <p>
     * @return true if a given LargeArray type is real numeric, false otherwise
     */
    public boolean isRealNumericType()
    {
        return false;
    }

    /**
     * Returns true if a given LargeArray type is complex numeric, false otherwise
     * <p>
     * @return true if a given LargeArray type is complex numeric, false otherwise
     */
    public boolean isComplexNumericType()
    {
        return false;
    }

}
