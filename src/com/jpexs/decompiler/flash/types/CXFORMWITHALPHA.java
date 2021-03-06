/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.annotations.Calculated;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;

/**
 * Defines a transform that can be applied to the color space of a graphic
 * object.
 *
 * @author JPEXS
 */
public class CXFORMWITHALPHA extends ColorTransform implements Serializable {

    /**
     * Has color addition values
     */
    public boolean hasAddTerms;
    /**
     * Has color multiply values
     */
    public boolean hasMultTerms;
    /**
     * Red multiply value
     */
    @Conditional("hasMultTerms")
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int redMultTerm;
    /**
     * Green multiply value
     */
    @Conditional("hasMultTerms")
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int greenMultTerm;
    /**
     * Blue multiply value
     */
    @Conditional("hasMultTerms")
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int blueMultTerm;
    /**
     * Alpha multiply value
     */
    @Conditional("hasMultTerms")
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int alphaMultTerm;
    /**
     * Red addition value
     */
    @Conditional("hasAddTerms")
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int redAddTerm;
    /**
     * Green addition value
     */
    @Conditional("hasAddTerms")
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int greenAddTerm;
    /**
     * Blue addition value
     */
    @Conditional("hasAddTerms")
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int blueAddTerm;
    /**
     * Alpha addition value
     */
    @Conditional("hasAddTerms")
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int alphaAddTerm;

    @Calculated
    @SWFType(value = BasicType.UB, count = 4)
    public int nbits;

    @Override
    public int getRedAdd() {
        return hasAddTerms ? redAddTerm : super.getRedAdd();
    }

    @Override
    public int getGreenAdd() {
        return hasAddTerms ? greenAddTerm : super.getGreenAdd();
    }

    @Override
    public int getBlueAdd() {
        return hasAddTerms ? blueAddTerm : super.getBlueAdd();
    }

    @Override
    public int getAlphaAdd() {
        return hasAddTerms ? alphaAddTerm : super.getAlphaAdd();
    }

    @Override
    public int getRedMulti() {
        return hasMultTerms ? redMultTerm : super.getRedMulti();
    }

    @Override
    public int getGreenMulti() {
        return hasMultTerms ? greenMultTerm : super.getGreenMulti();
    }

    @Override
    public int getBlueMulti() {
        return hasMultTerms ? blueMultTerm : super.getBlueMulti();
    }

    @Override
    public int getAlphaMulti() {
        return hasMultTerms ? alphaMultTerm : super.getAlphaMulti();
    }

}
