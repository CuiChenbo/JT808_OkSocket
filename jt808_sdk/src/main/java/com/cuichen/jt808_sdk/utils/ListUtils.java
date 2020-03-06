package com.cuichen.jt808_sdk.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * List Utils
 */
public final class ListUtils {

    /**
     * get size of list
     * 
     * <pre>
     * getSize(null)   =   0;
     * getSize({})     =   0;
     * getSize({1})    =   1;
     * </pre>
     * 
     * @param <V>
     * @param sourceList
     * @return if list is null or empty, return 0, else return {@link List#size()}.
     */
    public static <V> int getSize(List<V> sourceList) {
        return sourceList == null ? 0 : sourceList.size();
    }

    /**
     * is null or its size is 0
     *
     * <pre>
     * isEmpty(null)   =   true;
     * isEmpty({})     =   true;
     * isEmpty({1})    =   false;
     * </pre>
     *
     * @param <V>
     * @param sourceList
     * @return if list is null or its size is 0, return true, else return false.
     */
    public static <V> boolean isEmpty(List<V> sourceList) {
        return (sourceList == null || sourceList.size() == 0);
    }

    /**
     * 拆分集合
     * 用于批处理，在批量操作数据库时候数据太大的时候最好还是先拆分
     *
     * @param <T>
     * @param resList 要拆分的集合
     * @param count   每个集合的元素个数
     * @return 返回拆分后的各个集合
     */
    public static <T> List<List<T>> split(List<T> resList, int count) {
        if (resList == null || count < 1) {
            return null;
        }
        List<List<T>> ret = new ArrayList<List<T>>();
        int size = resList.size();
        if (size <= count) { //数据量不足count指定的大小
            ret.add(resList);
        } else {
            int pre = size / count;
            int last = size % count;
            //前面pre个集合，每个大小都是count个元素
            for (int i = 0; i < pre; i++) {
                List<T> itemList = new ArrayList<T>();
                for (int j = 0; j < count; j++) {
                    itemList.add(resList.get(i * count + j));
                }
                ret.add(itemList);
            }
            //last的进行处理
            if (last > 0) {
                List<T> itemList = new ArrayList<T>();
                for (int i = 0; i < last; i++) {
                    itemList.add(resList.get(pre * count + i));
                }
                ret.add(itemList);
            }
        }
        return ret;
    }

    /**
     * 注！！！！！！→ 集合中的元素必须重写equals方法自行判断元素是否相同
     * 哈希地址相同 返回true
     * 如果两个参数都为空，则返回true
     * 如果有一项为空，则返回false
     * 如果数据长度不相同，则返回false
     * 集合1包含集合2中的所有元素，并且集合2也包含集合1中的所有元素 则返回true
     * 注！！！！！！→ 集合中的元素必须重写equals方法自行判断元素是否相同
     * @param l0
     * @param l1
     * @return
     */
    public static boolean isListEqual(List l0, List l1){
        if (l0 == l1)
            return true;
        if (l0 == null && l1 == null)
            return true;
        if (l0 == null || l1 == null)
            return false;
        if (l0.size() != l1.size())
            return false;
       if (l0.containsAll(l1) && l1.containsAll(l0)){
           return true;
       }
       return false;
    }
    
//    public static void main(String[] a){
//        List<String> list = new ArrayList();
//        for (int i = 0; i < 20 ; i++) {
//            list.add("_"+i);
//        }
//        List<List<String>> lists = split(list,3);
//        lists.size();
//    }
}
