package io.mycat.route.function;

import io.mycat.config.model.rule.RuleAlgorithm;

/**
 * 哈希值取模
 * 根据分片列的哈希值对分片个数取模，哈希算法为Wang/Jenkins
 * 用法和简单取模相似，规定分片个数和分片列即可。
 * 
 * modify by jeff.cao
 * 原始的代码判断count是否2的n次方,如果是进行取模
 * 如果是2的N次方则 m%count == m&(count-1)
 * 
 * 位运算加if判断 比直接mod没有优势,直接改为mod
 *public static void main(String[] args) {
        int size = 80000000;
        int mod = 4;
        int x = 0;
        boolean w = true;
        long st = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            if(w)
             x = i  &(mod-1);
        }
        long end = System.currentTimeMillis();
        System.out.println(end - st);
        st = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            x = i % mod;
        }
        end = System.currentTimeMillis();
        System.out.println(end - st);
    }
 * @author Hash Zhang
 */
public class PartitionByHashMod extends AbstractPartitionAlgorithm implements RuleAlgorithm {

    private static final long serialVersionUID = 1L;

    public PartitionByHashMod() {
        name = "ColumnValueHashCodeMod";
    }

    /**
     * Using Wang/Jenkins Hash
     *
     * @param key
     * @return hash value
     */
    protected int hash(int key) {
        key = (~key) + (key << 21); // key = (key << 21) - key - 1;
        key = key ^ (key >> 24);
        key = (key + (key << 3)) + (key << 8); // key * 265
        key = key ^ (key >> 14);
        key = (key + (key << 2)) + (key << 4); // key * 21
        key = key ^ (key >> 28);
        key = key + (key << 31);
        return key;
    }

    @Override
    public Integer calculate(String columnValue) {
        int val = hash(columnValue.hashCode());
        if (val < 0) {
            val = -val;
        }
        return val % partitionNum;
    }
}
