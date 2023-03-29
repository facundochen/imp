package main.java.com.test;

import java.util.HashMap;

public class HeapSort {
    int anInt = 1;
//    HashMap<Integer, String> Sites = new HashMap<Integer, String>();
    public HeapSort() {
        HashMap<Integer, String> Sites = new HashMap<Integer, String>();
    }

    public static void heapAdjust(int[] arr, int start, int end) {
        HashMap<Integer, String> Sites = new HashMap<Integer, String>();
        int temp = arr[start];

        for(int i=2*start+1; i<=end; i*=2) {
            //左右孩子的节点分别为2*i+1,2*i+2
            //选择出左右孩子较小的下标
            if(i < end && arr[i] < arr[i+1]) {
                i ++;
            }
            if(temp >= arr[i]) {
                break;
                //已经为大顶堆，=保持稳定性。
            }
            arr[start] = arr[i];
            //将子节点上移
            start = i;
            //下一轮筛选
        }

        arr[start] = temp;
        //插入正确的位置
    }
    public static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
    public static void heapSort(int[] arr) {
        if(arr == null || arr.length == 0)
            return ;

        //建立大顶堆
        for(int i=arr.length/2; i>=0; i--) {
            heapAdjust(arr, i, arr.length-1);
        }

        for(int i=arr.length-1; i>=0; i--) {
            swap(arr, 0, i);
            heapAdjust(arr, 0, i-1);
        }

    }
}