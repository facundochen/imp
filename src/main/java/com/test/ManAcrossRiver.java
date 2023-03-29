package main.java.com.test;

import java.util.Scanner;

/**
 * 传教士和野人问题(Missionaries and Cannibals)
 * 问题：有N个传教士和N个野人来到河边渡河,河岸有一条船,每次至多可供k人乘渡。
 * 问传教士为了安全起见,应如何规划摆渡方案,使得任何时刻,
 * 河两岸以及船上的野人数目总是不超过传教士的数目(否则不安全,传教士有可能被野人吃掉)。
 * 即求解传教士和野人从左岸全部摆渡到右岸的过程中,
 * 任何时刻满足M(传教士数)≥C(野人数)和M+C≤k的摆渡方案。
 * 我们此处举例,只讨论N为3、k为2的乘渡问题,这样传教士和野人问题的描述就具体为如下:
 * 三个传教士与三个野人来到河边,有一条船可供一人或两人乘渡,问题是如何用这条船渡河才能
 * 使得河的任一岸上野人的数目总不超过传教士的数目(当然,如果某一岸上只有野人而没有传教士是允许的)。
 */
public class ManAcrossRiver {
    public class Man{
        int left_wild;//左岸野人数目
        int left_church;//左岸传教士数目
        int boat_wild;//船上野人数目
        int boat_church;//船上传教士数目
        int right_wild;//右岸野人数目
        int right_church;//右岸传教士数目
        Man next;//下一个状态指针
        Man back;//上一个状态指针

        Man(){
            left_wild=0;
            left_church=0;
            boat_wild=0;
            boat_church=0;
            right_wild=0;
            right_church=0;
            next=null;
            back=null;
        }

        public Man(int left_wild, int left_church, int boat_wild,int boat_church,
                   int right_wild, int right_church, Man next,Man back) {
            this.left_wild = left_wild;
            this.left_church = left_church;
            this.boat_wild = boat_wild;
            this.boat_church = boat_church;
            this.right_wild = right_wild;
            this.right_church = right_church;
            this.next = next;
            this.back = back;
        }
    }

    //这儿是复制结构体里面的数据
    private boolean copyNode(Man obj,Man source){
        obj.boat_church=source.boat_church;
        obj.boat_wild=source.boat_wild;
        obj.left_church=source.left_church;
        obj.left_wild=source.left_wild;
        obj.right_church=source.right_church;
        obj.right_wild=source.right_wild;
        obj.next=source.next;
        obj.back=source.back;
        return true;
    }

    //判断两个结构体里的数据是不是相同
    private boolean check(Man obj,Man source){
        if(obj.boat_church!=source.boat_church)
            return false;
        if(obj.boat_wild!=source.boat_wild)
            return false;
        if(obj.left_church!=source.left_church)
            return false;
        if(obj.left_wild!=source.left_wild)
            return false;
        if(obj.right_church!=source.right_church)
            return false;
        if(obj.right_wild!=source.right_wild)
            return false;
        return true;
    }

    //确定找出的一个状态是不是满足要求（野人>传教士）
    private boolean isNext(Man obj){
        if(obj.boat_church>0 || obj.boat_wild>0){
            if(obj.left_church>=0 && obj.left_wild>=0 && obj.right_church>=0 && obj.right_wild>=0)
                if(obj.left_church >= obj.left_wild || obj.left_church==0)
                    if(obj.right_church >= obj.right_wild || obj.right_church==0)
                        if(obj.boat_church >= obj.boat_wild || obj.boat_church==0)
                            if((obj.left_wild+obj.boat_wild <= obj.left_church+obj.boat_church ||
                                    (obj.left_church+obj.boat_church==0)) && (obj.right_wild+obj.boat_wild
                                    <= obj.right_church+obj.boat_church || obj.right_church+obj.boat_church==0))
                                return true;
        }
        else if(obj.boat_church==0 && obj.boat_wild==0 && obj.left_church==0 && obj.left_wild==0)
            return true;

        return false;
    }

    //传入一个刚开始的野人和传教士分布的结构体，再进行扩展,寻找下一种情况，直到全过河为止
    public void acrossRiver(Man obj,int num){
        boolean flag=false;
        Man now,tail;
        tail=obj;
        while(tail.left_church>0 || tail.left_wild>0){
            now=new Man();
            //寻找从左岸上船的各种情况
            for(int i=num;i>=0;i--){//船上野人数目
                for(int j=num-i;j>=0;j--){//船上传教士数目
                    copyNode(now,tail);
                    now.left_wild -= i;
                    now.left_church -= j;
                    now.boat_church += j;
                    now.boat_wild += i;
                    if(isNext(now)){
                        //判断符合条件并且和前一种运输状况不同
                        if(tail.back!=null && !check(now,tail.back)){
                            tail.next=now;
                            now.back=tail;
                            tail=tail.next;
                            flag=true;
                            break;
                        }else if(tail.back==null){
                            //如果刚开始就直接连在链表末尾，（下同）
                            tail.next=now;
                            now.back=tail;
                            tail=tail.next;
                            flag=true;
                            break;
                        }
                    }
                }
                if(flag){
                    flag=false;
                    break;
                }
            }
            //把船上的人都放到右岸，形成一个节点
            now=new Man();
            copyNode(now,tail);
            now.right_church += now.boat_church;
            now.right_wild += now.boat_wild;
            now.boat_church=0;
            now.boat_wild=0;
            tail.next=now;
            now.back=tail;
            tail=tail.next;

            //寻找从右岸上船的各种情况
            now=new Man();
            for(int i=0;i<=num;i++){
                for(int j=0;j<=num-i;j++){
                    copyNode(now,tail);
                    now.right_church -= i;
                    now.right_wild -= j;
                    now.boat_church += i;
                    now.boat_wild += j;
                    if(isNext(now)){
                        //判断符合条件并且和前一种状况不同
                        if(tail.back!=null && !check(now,tail.back)){
                            tail.next=now;
                            now.back=tail;
                            tail=tail.next;
                            flag=true;
                            break;
                        }else if(tail.back==null){
                            tail.next=now;
                            now.back=tail;
                            tail=tail.next;
                            flag=true;
                            break;
                        }
                    }
                }
                if(flag){
                    flag=false;
                    break;
                }
            }
            //把船上的人都放到左岸，形成一个节点
            now=new Man();
            copyNode(now,tail);
            now.left_church += now.boat_church;
            now.left_wild += now.boat_wild;
            now.boat_church=0;
            now.boat_wild=0;
            tail.next=now;
            now.back=tail;
            tail=tail.next;

            //如果左岸全没人了，跳出这层循环
            if(tail.left_church==0 && tail.left_wild==0){
                now=new Man();
                copyNode(now,tail);
                now.right_church += now.boat_church;
                now.right_wild += now.boat_wild;
                now.boat_church=0;
                now.boat_wild=0;
                tail.next=now;
                now.back=tail;
                tail=tail.next;
                break;
            }
        }

        //初始状态
        tail=obj;
        System.out.println(tail.left_church+" "+tail.left_wild+"-----"+
                tail.boat_church+" "+tail.boat_wild+"-----"+tail.right_church+" "+tail.right_wild);
        tail=tail.next;

        //遍历Man链表，显示运输过程
        flag=true;
        int count=0;
        while(tail!=null){
            count++;
            //划船过河
            if(flag){
                if(count%2==0)
                    flag=!flag;
                System.out.println(tail.left_church+" "+tail.left_wild+"---->"
                        +tail.boat_church+" "+tail.boat_wild+"---->"+tail.right_church+" "+tail.right_wild);
            }else{//返回划船
                if(count%2==0)
                    flag=!flag;
                System.out.println(tail.left_church+" "+tail.left_wild+"<----"
                        +tail.boat_church+" "+tail.boat_wild+"<----"+tail.right_church+" "+tail.right_wild);
            }
            tail=tail.next;
        }
    }

    public static void main(String[] args){
        ManAcrossRiver mar=new ManAcrossRiver();
        Man initMan = mar.new Man();
        Scanner scn=new Scanner(System.in);
        System.out.println("input the number of wildman or churchman:");
        int manCount=scn.nextInt();
        initMan.left_wild=initMan.left_church=manCount;
        System.out.println("input the number of once across by boat:");
        int boatMaxCount=scn.nextInt();
        mar.acrossRiver(initMan, boatMaxCount);
    }
}