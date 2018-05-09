/*
 * Copyright (c) 2018, MyCat_Plus and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese 
 * opensource volunteers. you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address 
 * https://code.google.com/p/opencloudb/.
 *
 */

package io.mycat.route.function.custom;

import io.mycat.route.function.AbstractPartitionAlgorithm;

/**
 * Created by bingyuan@guoxiaomei.com on 2018/4/3.
 */
public class SubstringHashMod extends AbstractPartitionAlgorithm {

    private static final long serialVersionUID = 1L;

    private int               length;

    private int               startIndex;

    public SubstringHashMod() {
        name = "SubstringColumnValueHashCodeMod";
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public Integer calculate(String columnValue) {
        int len = columnValue.length();
        boolean canCalculate = (len >= (startIndex + length) && startIndex >= 0 && length <= len);
        if (!canCalculate) {
            return 0;
        }
        String substring = columnValue.substring(startIndex, len - length - 1);
        int node = substring.hashCode() % partitionNum;
        return node;
    }

}