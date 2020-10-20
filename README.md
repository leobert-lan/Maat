# Maat
Maat是秩序之神，在之前参与开发的组件化项目JIMU中，一直被要求添加按序加载组件Component的功能，于是开始开发这个Android组件加载库，他可以单独使用，并不一定依赖于JIMU项目

## 最新版本：

version|Maat
---|---
最新版本|[![Download](https://api.bintray.com/packages/leobert-lan-oss/maven/maat/images/download.svg)](https://api.bintray.com/packages/leobert-lan-oss/maven/maat/_latestVersion)

## 解决的问题：
组件化场景（泛化到存在代码隔离的模块化场景）下，组件按序初始化的问题，有效解决“前置条件”，“线程灵活”。

> 组件化的基础是模块化，在做到模块化的同时，模块与模块在编写、编译期间也就达成了完全代码隔离，组件间的交互依靠 底层接口+服务发现（或者服务注册） 或者更加抽象为 “基于协议、隐藏实现”。
> 
> 这种场景下，想要用比较原始的、直面问题的方式解决组件按序初始化问题，例如使用反射+无分支遗漏的逻辑涵盖所有组件组合情况，会导致耦合激增。

## 功能特点

* 线程灵活
* 依赖缺失校验、依赖成环校验

## 集成：

```
implementation 'osp.leobert.android:maat:1.0.0'
```

考虑到jcenter审核需要时间，未被收录时可添加仓库：

```
allprojects {
    repositories {
        maven {
            url 'https://dl.bintray.com/leobert-lan-oss/maven/'
        }
    }
}
```

初始化：

```
 val maat = Maat.init(application = this, printChunkMax = 6,
            logger = object : Maat.Logger() {
            //日志功能开关
                override val enable: Boolean = true


                override fun log(msg: String, throws: Throwable?) {
                    Log.d("maat", msg, throws)
                }

            }, callback = Maat.Callback(onSuccess = {}, onFailure = { maat, job, throwable ->

            })
        )
```

printChunkMax 是打印信息输出时比较理想的单元长度，取所有任务名长度的最大值即可,输出会比较美观。例如：

```
   a     start d     b     c     
X\Y    ------------------------------
a     |0     0     1     1     1     
start |1     0     0     0     0     
d     |0     0     0     0     0     
b     |0     0     1     0     0     
c     |0     0     1     0     0     
```

添加任务：

```
 maat.append(object : JOB() {
            override val uniqueKey: String = "a"
            override val dependsOn: List<String> = emptyList()
            override val dispatcher: CoroutineDispatcher = Dispatchers.IO

            override fun init(maat: Maat) {
                Log.e(
                    "maat",
                    "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                )
                //test exception
//                throw NullPointerException("just a test")
            }

            override fun toString(): String {
                return uniqueKey
            }

        }).append(object : JOB() {
            override val uniqueKey: String = "b"
            override val dependsOn: List<String> = arrayListOf("a")
            override val dispatcher: CoroutineDispatcher = Dispatchers.Main /* + Job()*/

            override fun init(maat: Maat) {
                Log.e(
                    "maat",
                    "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                )
            }

            override fun toString(): String {
                return uniqueKey
            }

        }).append(object : JOB() {
            override val uniqueKey: String = "c"
            override val dependsOn: List<String> = arrayListOf("a")
            override val dispatcher: CoroutineDispatcher = Dispatchers.IO /* + Job()*/

            override fun init(maat: Maat) {
                Log.e(
                    "maat",
                    "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                )
            }

            override fun toString(): String {
                return uniqueKey
            }

        }).append(object : JOB() {
            override val uniqueKey: String = "d"
            override val dependsOn: List<String> = arrayListOf("a", "b", "c")
            override val dispatcher: CoroutineDispatcher = Dispatchers.Main

            override fun init(maat: Maat) {
                Log.e(
                    "maat",
                    "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                )
            }

            override fun toString(): String {
                return uniqueKey
            }

        })
```

开始初始化：

```
maat.start()
```

Maat中实现了单例，初始化后可以用

```
Maat.getDefault()
```
获取初始化的单例。**添加任务必须在初始化之后、start之前**

确信任务完成后不再需要Maat了可以释放实例：

```
Maat.release()
```

更多细节：
[分析与设计](https://juejin.im/post/6884492604370026503/#heading-8) 以及阅读源码

欢迎提供您宝贵的意见。