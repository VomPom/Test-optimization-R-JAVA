package DCL;

import DCL.bean.MixLeavelData;
import DCL.util.ExcelUtils;
import DCL.util.RUtils;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;


/**
 *
 * Created by Julis on 17/8/5.
 * User:Julis 落叶挽歌
 * Date:17/8/5
 * Time:下午2:56
 */

public class MainControl {
    public static void main(String []args){
         mainJudge();
}
    /**
     * 主判断程序主要根据对vkb等相关参数
     */
    public static void mainJudge(){

         int b=4;//区组数
         int []ki={10};//每个区的数
         int []vi={2,3,3,3,3};  //  f1  代表每个因子的水平
         int []qi={0,0,1,0,0};  //定量定性判断:定性1 定量0 和vi的个数相对应


         int []viF2={};    //噪声因子的水平数目 f2 每个因子的水平
         int []viF3={};    //信号因子的水平数目 f3 每个因子的水平


         //可选参数
         double[]range=new double[]
                 {120,60,30,10,9,3,0.36,0.02,300,80};//大的在前面，小的在后面

         int l=0;       //随机效应
         int lamda=0;   //相遇数
         int cost=1;    //成本
         int z=0;       //是否可以综合噪声 0没综合
         int n=18;      //根据实验条件获取
         int j=2;       //交互作用的类型
         int regression_midle=4;//回归中心试验次数
         int regressionType=2;  //1代表 一次回归分析,2代表 2次分析,
         int t=0;               //数据特性   0 望目 －1望小 1望大


         int v=vi[0];//水平数   单因子的话 就一个,多因子 v1 v2 v3 f1
         int f1=vi.length;  //可控因子水平个数
         int f3=viF3.length;    //信号因子个数
         int f2=viF2.length+f3; //噪声因子个数
         int m=v/b;
         int f=f1+f2+f3;    //总因子数
         int k=minOfK(ki);//每个区的最小的容纳水平数








         if(qi.length!=vi.length){
             System.out.println("vi的个数必须和qi的个数一样");
             System.exit(0);
         }
         judeg_range(range);


         if(f1==1) {//单因子
             if (m >= 2) {
                System.out.println("链式法则");
                chainArea(n,b,v,k);

                /**
                 * 链式法则
                 *
                 * 1\产生随即表 ok?
                 * 2\方差分析
                 *
                 * ?????
                 * n和书上的n不一样代表的东西不一样1
                 */
            } else {
                if (k == v) {
                    CBD(b,v);
                } else {
                     //System.out.println("BIBD");
                    if (b >= v) {
                        int rInt=(b * k) / v;//处理的重复数
                        int rJudge = (b * k) % v;
                        if (rJudge == 0) {
                            lamda = (rInt * (k - 1)) % (v - 1);
//                            int temp = (rInt * (k - 1)) / (v - 1);
//                            System.out.println(temp);
//                            System.out.println("r="+rInt);
                            if (lamda == 0) {
//                                if (temp == 1) {
//                                    System.out.println("根据vbk设定试验方案");//根据文件夹 试验方案表
                                    blockDesignMatrix(v,k,b);
//                                } else {
//                                    CBD(b,v);
//                               }
                            } else {
                                CBD(b,v);
                            }
                        } else {
                            CBD(b,v);
                        }
                    } else {
                        CBD(b,v);
                    }
                }
            }
        }else if(f1 >= 2){//多因子
            if(f1>7) {
                System.out.println("筛选代码块");    //饱和设计
                saturationDesign(f1);
                return;
            }

            if(f2>0){
                System.out.println("参数设计");
                if(f3>0){
                    System.out.println("动态设计");
                    parameterDesign(f1,vi[0],f2,viF2[0]);//一般参数设计
                }else{
                    if(z==1){
                        System.out.println("综合噪声");
                        syntheticNoise(f1,v);//综合噪声
                    }else{
                        System.out.println("一般参数设计");
                        parameterDesign(f1,vi[0],f2,viF2[0]);//一般参数设计
                    }
                }
            }else{

                System.out.println("成本分析"); //人工处理
                System.out.println("时间和金钱成本");
                    if(maxOfQi(qi)>0){    //有定性因子
                        System.out.println("正交设计");
                        if(isEqualOfVi(vi)){    //因子水平数相等
                            System.out.println("等水平");
                                orthogonal(j,v,f1,cost);//标准正交表
                        }else{  //因子水平数不相等==>混合水平
                            System.out.println("混合水平");
                            mixLeavel(vi);  //混合水平
                        }
                    }else{
                        System.out.println("回归分析");
                        if(regressionType==1) {
                            regressionAnalysisOnce(regression_midle, f1, range);
                        }else if(regressionType==2){
                            regressionAnalysisTwice(regression_midle,f1);
                        }
                    }
                }
            }
        }

    private static void judeg_range(double []range) {
        if(range.length%2!=0){
            System.out.println("Range输入个数有问题");
            System.exit(0);
        }
        for(int i=0;i<range.length-2;i+=2){
            if(range[i]<range[i+1]){
                System.out.println("Range输入必须保证前面大于后面，两个一组");
                System.exit(0);
            }
        }
    }

    /**
     *   赋闲列
     *
     * @param countOfF2 2水平因子个数
     * @param countOfF3 3水平因子个数
     */



    public static void idleColcum(int countOfF2,int countOfF3){
        Rengine engine=RUtils.loadR("idleColcum.R");
        String  order="myfunfx("+countOfF2+","+countOfF3+")";
        REXP rexp = engine.eval(order);
        RUtils.printRreturnData(rexp);
    }
    /**
     *
     * 二次回归分析
     * @param m 重复试验次数
     * @param f1 因子个数
     *
     */
    public static void regressionAnalysisTwice(int m,int f1){

        Rengine engine=RUtils.loadR("regressionAnalysisTwice.R");

        String  order="myfun("+m+","+f1+")";
        REXP rexp = engine.eval(order);

        RUtils.printREAL(rexp);

    }
    /**
     * 一次回归分析
     * @param m 重复试验次数
     * @param f1 因子个数
     * @param range 取值范围
     */
    public static void regressionAnalysisOnce(int m,int f1,double[]range){

        Rengine engine=RUtils.loadR("regressionAnalysis.R");
        engine.assign("c2",range);
        String  order="myfun("+m+","+f1+",c2)";
        REXP rexp = engine.eval(order);
       // System.out.println(order);

        RUtils.printRegressionAnalysisData(rexp,range);

    }
    /**
     * 综合噪声
     * @param f1
     * @param v
     */
    public static void syntheticNoise(int f1,int v){
        Rengine engine=RUtils.loadR("syntheticNoise.R");
        String  order="myfunzh("+f1+","+v+")";
        REXP rexp = engine.eval(order);

        RUtils.printRreturnData(rexp);
    }
    /**
     * 一般参数设计
     *
     */
    public static void  parameterDesign(int f1,int v1,int f2,int v2){

        Rengine engine=RUtils.loadR("parameterDesign.R");
        String order="myfuncs("+f1+","+v1+","+f2+","+v2+")";
        REXP rexp = engine.eval(order);

        RUtils.printRreturnData(rexp);
    }
    /**
     *  完全不平衡区组设计矩阵表
     * @param v
     * @param k
     * @param b
     */
    public static void blockDesignMatrix(int v,int k,int b)  {
        ExcelUtils.printVKBDesign(v,k,b);
    }
    /**
     * 正交设计
     * @param f1 可控因子个数
     * @param v 水平数
     * @param j 交互作用类型 分为 0 1 2 3
     */
    public static void orthogonal(int j,int v,int f1,int cost){
        Rengine engine=RUtils.loadR("Orthogonal.R");
        REXP rexp=null;

        if(cost==1){
            rexp=engine.eval("myfunzj("+j+","+v+","+f1+")");
        }else{
            rexp=engine.eval("myfunzj1("+j+","+v+","+f1+")");
        }
//        RUtils.printRreturnDataWithName(rexp);
        RUtils.printRreturnData(rexp);
    }
    /**
     * 饱和设计
     * @param f1
     */
    public static void saturationDesign(int f1){
        Rengine engine=RUtils.loadR("SaturationDesign.R");
        String order=null;
        if(f1<12){
            order="myfun12("+f1+")";
        }else if(f1>=12&&f1<20){
            order="myfun20("+f1+")";
        }else if(f1>=20&&f1<28){
            order="myfun28("+f1+")";
        }else if(f1>=28&&f1<32){
            order="myfun32("+f1+")";
        }else{
            System.out.println("大于32个水平");
        }
        REXP rexp = engine.eval(order);
        //System.out.println(rexp);
        RUtils.printRreturnData(rexp);
    }
    /**
     * 混合水平
     * @param vi 各个因子的水平数组
     */
    public static void mixLeavel(int []vi){
        int []leavelCount=leavelJudge(vi);//获取到各个水平的数量
        int twoLeavelsPostion=leavelCount[2];//二水平数组索引
        int threeLeavelsPostion=leavelCount[3];//三水平数组索引
        System.out.println(twoLeavelsPostion+"  "+threeLeavelsPostion);
        int TYPE_OF_MIXLEAVE=MixLeavelData.table[threeLeavelsPostion-1][twoLeavelsPostion-1];//获取判断的类型

        switch (TYPE_OF_MIXLEAVE){
            case 0:
                System.out.println("没有此方法");
                break;
            case 1:
                System.out.println("拟水平方法");
                pseudoHorizontal(vi);//拟水平法
                break;
            case 2:
                System.out.println("赋闲列方法");
                idleColcum(leavelCount[2],leavelCount[3]);//赋闲列方法
                break;
            case 3:
                System.out.println("原表不变");
                pseudo2137(leavelCount[2]+leavelCount[3],leavelCount[3]);
                break;
            default:
                System.out.println("混合设计不存在的选项");
                break;
        }

    }
    /**
     * 调用拟水平方法
     * @param vi
     */
    public static void pseudoHorizontal(int []vi){
        String order="";
        Rengine engine=RUtils.loadR("pesudoHorizontal.R");
        int leavelCount[]=leavelJudge(vi);
        if(leavelCount[2]!=0){
            order="myfundesnsp("+leavelCount[2]+",2,"+leavelCount[3]+",3)";
        }else{
            order="myfundesnsp("+leavelCount[3]+",3,"+leavelCount[4]+",4)";
        }
        REXP rexp = engine.eval(order);

        RUtils.printRreturnData(rexp);
    }
    /**
     * 调用 2137
     *
     * @param f1
     * @param f2
     */
    public static void pseudo2137(int f1,int f2){
        Rengine engine=RUtils.loadR("2137.R");
        // 直接调用无参的函数，将结果保存到一个对象中
        String order="myfundesnsp1("+f1+","+f2+")";
        REXP rexp = engine.eval(order);
        RUtils.printRreturnData(rexp);


    }
    /**
     * 调用CBD方法
     * @param b 区组数
     * @param v 因子个数
     */
    public static void CBD(int b,int v){
        Rengine engine=RUtils.loadR("CBD.R");
        // 直接调用无参的函数，将结果保存到一个对象中
        String order="myfunbd("+b+","+v+")";
        REXP rexp = engine.eval(order);
        RUtils.printRreturnData(rexp);
    }
    /**
     * 链式设计方案
     * @param n 可容纳的最大次数
     * @param b 区组数
     * @param v 水平数
     * @param k 最小的...
     */
    public static void chainArea(int n,int b,int v,int k) {

        int g=(b*k-v)/b; // 每个小组容纳的个数
        System.out.println("k="+k);
        System.out.println("b="+b);
        System.out.println("n="+n);
        System.out.println("v="+v);
        System.out.println("g="+g);
        if(g<=v/2&&g>1){

            int[][] number = new int[b][g];
            int num = 0;
            for (int i = 0; i < b; i++) {
                for (int j = 0; j < g; j++)
                    number[i][j] = ++num;
            }

            for (int j = 0; j < g; j++) {
                for (int i = 0; i < b; i++)
                    System.out.print(String.format("%-4d",number[i][j]));
                 System.out.println();
            }
            for (int j = 0; j < g; j++) {
                for (int i = 1; i < b; i++)
                    System.out.print(String.format("%-4d",number[i][j]));
                System.out.print(String.format("%-4d",number[0][j]));
                System.out.println();
            }
            int count=1;
            for(int i=g*b+1;i<=v;i++,count++){
                System.out.print(String.format("%-4d",i));
                if(count%b==0){
                    System.out.println();
                }
            }
        }else{
            System.out.println("不能做实验");
        }

    }
    /**
     * 判断 2 3 4 5 因子水平的个数
     * @param vi 因子水平数
     * @return
     */
    public static int []leavelJudge(int []vi){
        int []leavelJudge=new int[6];// 为方便记忆,设因子个数数组索引以水平数目作为索引
        for(int i=0;i<leavelJudge.length;i++)
            leavelJudge[i]=0;
        for(int i=0;i<vi.length;i++){
            switch (vi[i]){
                case 2:
                    leavelJudge[2]++;
                    break;
                case 3:
                    leavelJudge[3]++;
                    break;
                case 4:
                    leavelJudge[4]++;
                    break;
                case 5:
                    leavelJudge[5]++;
                    break;
            }

        }
        return leavelJudge;
    }
    /**
     * 找Ki的最小值
     * @param k
     * @return
     */
    public static int minOfK(int []k){
        int min=k[0];
        for(int i=0;i<k.length;i++)
            if (k[i]<min)
                min=k[i];
        return min;
    }

    /**
     * 找qi的最大值
     * @param k
     * @return
     */
    public static int maxOfQi(int []k){
        int max=k[0];
        for(int i=0;i<k.length;i++)
            if (k[i]>max)
                max=k[i];
        return max;
    }
    /**
     * 判断是否是等因子
     * @param vi
     * @return
     */
    public static boolean isEqualOfVi(int []vi){
        int temp=vi[0];
        boolean flag=true;
        for(int i=1;i<vi.length;i++){
            if(temp!=vi[i]){
                flag=false;
                return flag;
            }
        }
        return flag;
    }
}










