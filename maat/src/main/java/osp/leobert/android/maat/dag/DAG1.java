package main.java.util.rename;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p><b>Package:</b> main.java.util.rename </p>
 * <p><b>Classname:</b> DAG </p>
 * Created by leobert on 2020/9/23.
 */
class DAG1 {
    //记录两份网络节点,使用空间换时间,提升反向查询效率

    //网络初始化,x_k,y_k
    private Map<String, Map<String, String>> xMapMap;
    //网络初始化,y_k,x_k
    private Map<String, Map<String, String>> yMapMap;

    //回环路径
    private List<String> loopbackList;//回环地址
    //节点深度路径
    private List<List<String>> deepPathList;//深度路径

    private Map<String, String> jobMapStatus;//map任务状态

    enum TYPE {X, Y}


    public DAG1() {
        xMapMap = new HashMap<>();//X点集
        yMapMap = new HashMap<>();//Y点集
        loopbackList = new LinkedList<>();//回环地址
        deepPathList = new LinkedList<>();//深度路径
    }

    /**
     * 初始化任务
     */
    public void initJob() {
        jobMapStatus = new HashMap<>();
        Set<String> allPoint = getAllPoint();
        allPoint.forEach(job -> jobMapStatus.put(job, "0"));//0,1  0表示未执行,1表示执行
    }


    /**
     * 获取任务的转状态
     *
     * @param job
     * @return
     */
    public String getJobMapStatus(String job) {
        String status = jobMapStatus.get(job);
        return status;
    }

    /**
     * 更新任务的状态
     *
     * @param job    任务名称
     * @param status 任务状态
     */
    public void setJobMapStatus(String job, String status) {
        jobMapStatus.put(job, status);
    }


    public List<String> getLoopbackList() {
        return loopbackList;
    }

    public List<List<String>> getDeepPathList() {
        return deepPathList;
    }

    /**
     * 网络添加节点
     *
     * @param xPoint   x位
     * @param yPoint   y位
     * @param distance 位移
     */
    public void addPoint(String xPoint, String yPoint, String distance) {
        if (!xMapMap.containsKey(xPoint)) {//记录x的索引
            xMapMap.put(xPoint, new HashMap<>());
        }
        xMapMap.get(xPoint).put(yPoint, distance);
        if (!yMapMap.containsKey(yPoint)) {//记录y的索引
            yMapMap.put(yPoint, new HashMap<>());
        }
        yMapMap.get(yPoint).put(xPoint, distance);
    }

    /**
     * 根据坐标获取某点
     *
     * @param xPoint
     * @param yPoint
     * @return
     */
    public String getPoint(String xPoint, String yPoint) {
        Map<String, String> linePoints = xMapMap.get(xPoint);
        String point = linePoints.get(yPoint);
        return point;
    }

    public String getPoint(String point1, String point2, TYPE type) {
        if (type == TYPE.X) {
            Map<String, String> linePoints = xMapMap.get(point1);
            String point = linePoints.get(point2);
            return point;
        } else {
            Map<String, String> linePoints = yMapMap.get(point1);
            String point = linePoints.get(point2);
            return point;
        }
    }

    /**
     * 获取X轴的一列数据
     *
     * @param xPoint
     * @return
     */
    public List<Map<String, String>> getLinePoint(String xPoint) {
        List<Map<String, String>> linePointList = new ArrayList<Map<String, String>>();
        Map<String, String> linePoints = xMapMap.get(xPoint);
        if (linePoints != null) {
            for (Map.Entry<String, String> pointUnit : linePoints.entrySet()) {
                Map<String, String> pointMap = new HashMap<String, String>();
                pointMap.put("X", xPoint);
                pointMap.put("Y", pointUnit.getKey());
                pointMap.put("D", pointUnit.getValue());
                linePointList.add(pointMap);
            }
        }
        return linePointList;
    }

    /**
     * 根据类型获取某轴的一列数据
     *
     * @param point 索引点
     * @param type  类型
     * @return
     */
    public List<Map<String, String>> getLinePoint(String point, TYPE type) {
        List<Map<String, String>> linePointList = new ArrayList<Map<String, String>>();
        if (type == TYPE.X) {
            Map<String, String> linePoints = xMapMap.get(point);
            if (linePoints != null) {
                for (Map.Entry<String, String> pointUnit : linePoints.entrySet()) {
                    Map<String, String> pointMap = new HashMap<String, String>();
                    pointMap.put("X", point);
                    pointMap.put("Y", pointUnit.getKey());
                    pointMap.put("D", pointUnit.getValue());
                    linePointList.add(pointMap);
                }
            }
        } else {
            Map<String, String> linePoints = yMapMap.get(point);
            if (linePoints != null) {
                for (Map.Entry<String, String> pointUnit : linePoints.entrySet()) {
                    Map<String, String> pointMap = new HashMap<String, String>();
                    pointMap.put("X", pointUnit.getKey());
                    pointMap.put("Y", point);
                    pointMap.put("D", pointUnit.getValue());
                    linePointList.add(pointMap);
                }
            }
        }
        return linePointList;
    }


    /**
     * @param DAG        网络节点
     * @param startPoint 起始节点
     * @param pathList   当前任务的路径
     */
    public void recursive(DAG1 DAG, String startPoint, ArrayList<String> pathList) {
        if (pathList.contains(startPoint)) {
            DAG.getLoopbackList().add(pathList.toString() + "->" + startPoint);
            return;
        }
        pathList.add(startPoint);
        List<Map<String, String>> linePoint = DAG.getLinePoint(startPoint, TYPE.X);
        if (linePoint.size() == 0) {
            ArrayList<String> descList = new ArrayList<>(pathList.size());
            pathList.forEach(path -> descList.add(path));
            DAG.getDeepPathList().add(descList);
        }
        for (Map<String, String> map : linePoint) {
            recursive(DAG, map.get("Y"), pathList);
        }
        pathList.remove(startPoint);
    }


    /**
     * 获取所有的点,合并key
     *
     * @return
     */
    public Set<String> getAllPoint() {
        Set<String> allSet1 = xMapMap.keySet();
        Set<String> allSet2 = yMapMap.keySet();
        Set<String> allSet = new HashSet<>();
        allSet.addAll(allSet1);
        allSet.addAll(allSet2);
        return allSet;
    }


    /**
     * 显示路径
     */
    public void show() {
        int placeholder = 3;
        StringBuilder placeholderString = new StringBuilder();
        for (int i = 0; i < placeholder; i++) {
            placeholderString.append("-");
        }
        Set<String> allSet = getAllPoint();//获取所有的点,用于遍历
        System.out.print(String.format("%-" + placeholder + "s", ""));
        System.out.print(" ");
        for (String x : allSet) {
            System.out.print(String.format("%-" + placeholder + "s", x));
        }
        System.out.println();
        System.out.print(String.format("%-" + placeholder + "s", "X\\Y"));
        System.out.print(" ");
        for (String ignored : allSet) {
            System.out.print(placeholderString);
        }
        System.out.println();
        for (String x : allSet) {
            System.out.print(String.format("%-" + placeholder + "s|", x));
            for (String y : allSet) {
                Map<String, String> linePoints = xMapMap.get(x);
                String point = "0";
                if (linePoints != null && linePoints.get(y) != null) {
                    point = linePoints.get(y);
                }
                System.out.print(String.format("%-" + placeholder + "s", point));
            }
            System.out.println();
        }
    }

    static class Test {
        public static ConcurrentMap<String, List<String>> println = new ConcurrentHashMap<>();
        public static List<String> strings = new ArrayList<>();

        public static void main(String[] args) throws InterruptedException {
            DAG1 DAG = new DAG1();

            DAG.addPoint("1", "2", "1");
            DAG.addPoint("1", "3", "1");
            DAG.addPoint("1", "4", "1");
            DAG.addPoint("1", "5", "1");
            DAG.addPoint("1", "13", "1");
            DAG.addPoint("2", "7", "1");
            DAG.addPoint("3", "7", "1");
            DAG.addPoint("13", "7", "1");
            DAG.addPoint("4", "8", "1");
            DAG.addPoint("4", "9", "1");
            DAG.addPoint("5", "6", "1");
            DAG.addPoint("6", "9", "1");
            DAG.addPoint("7", "10", "1");
            DAG.addPoint("8", "10", "1");
            DAG.addPoint("9", "11", "1");
            DAG.addPoint("10", "12", "1");
            DAG.addPoint("11", "12", "1");

            DAG.addPoint("6", "1", "1");
//        DAG.addPoint("4", "6", "1");//回环的两条数据
            DAG.initJob();//初始化所有节点任务

            DAG.show();
            //获取起点,如图起点为1
            String startPoint = "1";
            DAG.recursive(DAG, startPoint, new ArrayList<>()); //递归计算所有节点的路径
            if (DAG.getLoopbackList().size() != 0) {
                System.out.println("出现回环地址,回环地址的路径为:" + DAG.getLoopbackList());
                return;
            }

            //开始计算任务
            for (List<String> pathJobList : DAG.getDeepPathList()) {
                new Thread(() -> {
                    System.out.println("路线:" + pathJobList);
                    for (int i = 0; i < pathJobList.size(); i++) {
                        String job = pathJobList.get(i);
                        List<String> linePoint = new ArrayList<>();//获取任务前置条件
                        DAG.getLinePoint(job, TYPE.Y).forEach(jobLine -> linePoint.add(jobLine.get("X")));
//                    System.out.println("任务" + job + "的前置条件:" + linePoint);
                        execJob(job, linePoint, DAG);//执行任务

                    }
                }).start();
            }
            Thread.sleep(1000);//这里使用的休眠进行下一次判断,也可以使用锁同步数据,使用锁实现较为复杂
            for (Map.Entry<String, List<String>> entry : println.entrySet()) {
                System.out.println(entry);
            }
            System.out.println("执行顺序:" + strings);


        }

        /**
         * 执行任务
         *
         * @param job
         */
        private static void execJob(String job, List<String> linePoint, DAG1 DAG) {
            List<String> tmp = new ArrayList<>();
            while (true) {
                for (String precondition : linePoint) {
                    String jobMapStatus;
                    synchronized (String.class) {
                        jobMapStatus = DAG.getJobMapStatus(precondition);
                    }
                    if (!"1".equals(jobMapStatus)) {
                        tmp.add(precondition);
                    }//未执行
                }
                if (tmp.size() == 0) {
                    break;
                }
                linePoint.clear();
                tmp.forEach(jobTmp -> linePoint.add(jobTmp));
                tmp.clear();
//            try {Thread.sleep(100);} catch (InterruptedException e) { e.printStackTrace();}
            }
            String jobMapStatus;
            Boolean status = false;//是否需要执行任务
            synchronized (String.class) {
                jobMapStatus = DAG.getJobMapStatus(job);
                if ("1".equals(jobMapStatus)) {
                    return;
                }//任务已经完成
                if ("0".equals(jobMapStatus)) {
                    DAG.setJobMapStatus(job, "-1");
                    status = true;
                }//立即执行
            }
            if (status) {
                synchronized (String.class) {
                    if (!println.containsKey(Thread.currentThread().getName())) {
                        println.put(Thread.currentThread().getName(), new ArrayList<>());
                    }
                    println.get(Thread.currentThread().getName()).add(job);
                    strings.add(job);
                    DAG.setJobMapStatus(job, "1");
                }
            }
        }
    }
}


