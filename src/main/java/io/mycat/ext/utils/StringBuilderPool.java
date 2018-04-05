/*
 * GuoXiaoMei.com Inc.
 * Copyright (c) 2017-2018 All Rights Reserved.
 */
package io.mycat.ext.utils;

/**
 * 线程安全的StringBuilderPool
 * @author jeff.cao
 * @version 0.0.1, 2018年3月29日 下午7:57:54
 */
public class StringBuilderPool extends ThreadLocal<StringBuilder> {

    @Override
    protected StringBuilder initialValue() {
        return new StringBuilder();
    }

    @Override
    public StringBuilder get() {
        StringBuilder sb = super.get();
        sb.setLength(0);
        return sb;
    }

}
