package main.java.com.test;

import java.util.Objects;

public class SelectSort {
int a,b ,c ;
    public static void selectSort(int[] arr) {
        if(arr == null || arr.length == 0)
            return ;
        int minIndex = 0;
        for(int i=0; i<arr.length-1; i++)
        { //只需要比较n-1次
            minIndex = i;
            for(int j=i+1; j<arr.length; j++)
            { //从i+1开始比较，因为minIndex默认为i了，i就没必要比了。
                if(arr[j] < arr[minIndex]) {
                    minIndex = j;
                }
            }
            if(minIndex != i) { //如果minIndex不为i，说明找到了更小的值，交换之。
                swap(arr, i, minIndex);
            }
        }

    }

    public static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectSort)) return false;
        SelectSort that = (SelectSort) o;
        return getA() == that.getA() &&
                getB() == that.getB() &&
                getC() == that.getC();
    }

    @Override
    public int hashCode() {

        return Objects.hash(getA(), getB(), getC());
    }

    @Override
    public String toString() {
        return "SelectSort{" +
                "a=" + a +
                ", b=" + b +
                ", c=" + c +
                '}';
    }
}