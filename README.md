# 基于Android的文字识别系统

本软件是一款运行在Android终端的文字识别软件，为本人2022年毕设项目，现将其开源。本项目亮点是使用SWT算法进行预处理，经过SWT算法将图片中的图像文本标记出来，再将其送入Tesseract识别引擎中，本方法对于自然场景图像有较大的降噪效果，最后的识别效果良好。

### 开发环境

- Android Stdio
- OpenCV 2.4.13 
- Boost

### 如何安装

1. 将项目导入Android Studio中等待加载完成
2. 修改CMakeLIst.txt文件中的内容

```cmake
set(opencvlibs "E:/opencv-2.4.13.6-android-sdk/OpenCV-android-sdk/sdk/native/libs")
#调用头文件的具体路径
include_directories(E:/opencv-2.4.13.6-android-sdk/OpenCV-android-sdk/sdk/native/jni/include)
```

将其修改为自己的OpenCV-android-sdk，OpenCV-android-sdk安装方法自行百度

3. 等待进度条加载完成，启动项目

### 功能特性

- 使用相册、相机导入图片
- 图片裁剪、图片压缩
- 使用SWT算法预处理
- 图片二值化
- 中文、英文识别 Tesseract识别引擎
- 识别记录保存、删除
- ......

### SWT算法介绍

笔画宽度变化（SWT）是一种文本区域检测算法，SWT可以从有噪声的图像中提取文本，通过提取具有一致性宽度的带状目标实现，由此得到的图像消除大量的噪声，并保留大量文本，再将其送入OCR引擎中，从而得到更可靠的光学字符识别结果[论文地址](https://www.microsoft.com/en-us/research/wp-content/uploads/2016/02/1509.pdf) 

算法流程为：

- 计算梯度图：使用Canny边缘检测得到图像图像边缘，这一步得到的每个像素的值都为该像素的梯度值
- 进行SWT操作：
  - 对于边缘像素p，其梯度方向θ（通过梯度图计算梯度方向）沿着梯度方向移动，直到遇到下一个边缘像素q(或跑到图像外)。如果边缘像素q的梯度指向与p相反的方向(θ - π)，那么该边缘大致平行于第一个边缘，这两个边缘像素相连得到这个笔画的一个ray（我喜欢称作横截面）。以像素为单位记录笔画宽度（ray所包含的像素个数），并将该值分配给这个ray上的所有像素
  - 如果边缘像素q没找到，或者像素q的方向与像素p的方向不是相反的，那这个ray就舍弃。
  - 两个平行边缘上的像素点p和q的梯度方向应该大致相反， ![[公式]](https://www.zhihu.com/equation?tex=dq+%3D+-dp+%C2%B1+%CF%80%2F6) ，![[公式]](https://www.zhihu.com/equation?tex=%CF%80%2F6) 是误差最大容忍值，当遇到存在拐角多的字符（比如"A", "X")，误差可以稍微增大一些，这时可能会找到其中的交叉像素，这取决于实际图像和边缘检测器的参数。

![image-20220608223735132](http://cdn.noteblogs.cn/image-20220608223735132.png)

- 找到候选字母：根据笔画宽度将这些像素分组为候选字母，将具有相似笔画宽度的像素分组，然后应用几个规则来区分候选字母。使用连通组件算法来实现图像的分组。主要应用以下规则：
  - 组件内笔画宽度的差异不得太大，这有助于剔除自然图像中常被误认为是文本的树叶。
  - 组件的长宽比必须在一个小值范围内，剔除长而窄的组件。
  - 组件直径与其笔画宽度的中值之间的比率小于指定阈值，这也有助于剔除细长组件。
  - 尺寸过大或过小的组件也将被忽略，这是通过限制组件的长度、宽度和像素数量来实现的。
  - 如果一个组件中包含的像素过少，则将其剔除，可以避免一些单线和散线将不同字母分组到一个组件中。
  - 组件的像素数量与组件边界框中的像素数量之比应在有限范围内，这将剔除覆盖大空间但有效像素数量较少的组件。

> 连通组件算法：连通组件算法是图像分析中的常用算法，本质上是扫描一幅图像的每个像素，对于像素值相同的分为相同的组。最终得到图像中的所有像素连通组件

- 文本行聚合：将方向相似、位置相近的候选字母分组得到一个文本区域中。可以形成一个单词，使用的规则如下：
  - 两个候选字母应该有相似的笔画宽度。为此，将两者笔画宽度的中值之间的比率限制在某个阈值以下。
  - 两个候选字母的高度之比和宽度之比均不得超过2.5，这是由于小写字母可能在大写字母的旁边。
  - 两个字母之间的距离不得超过更宽字母宽度的三倍。
  - 同一单词的字符应该具有相似的颜色，比较两个字母的平均颜色值。
  - 这对字母的像素数量与其边界框中的像素数量之比应在有限范围内。

### ![image-20220609101632253](http://cdn.noteblogs.cn/image-20220609101632253.png)

### Tessreact使用

Tessreact是一款优秀的开源OCR识别工具，可以在Windows、LInux平台上，无法直接使用在Android平台上。但是有其一个分支tess-two是运行Android上的。本系统使用tess-two 9.1.0版本，对图像进行文字识别。其使用方法很简单：

1. 导入依赖

```
implementation 'com.rmtheis:tess-two:9.1.0'
```

2. 使用代码

```java
TessBaseAPI tessBaseAPI = new TessBaseAPI();
tessBaseAPI.setDebug(true);
tessBaseAPI.init(path, language);
tessBaseAPI.setImage(result);                     //传入SWT处理之后的图像
String text = tessBaseAPI.getUTF8Text();            //得到结果
```

需要注意的是，在使用Tesseract时(tess-two)，需要将对应的语言包放到path路径下

### 项目文件

- assets文件夹：存放语言包资源文件
- cpp文件夹：存放C++语言代码
  - TextDetection.h：头文件
  - native-lib.cpp：实现SWT算法
  - CMakeLists.txt
- opencvdemo文件夹：存放整个源代码
  - MainActivity：主界面
  - ResActivity：识别结果界面
  - HistoryActivity：历史记录查看界面
  - HistoryDetailActivity：历史记录详细界面
  - SDUtils：操作存储工具类
  - BitmapUtils：Bitmap图片工具类
- databaseHelper文件夹：操作数据库相关

- utakephoto：使用相册、相机导入图片、裁剪图片相关。引用github项目`utakephoto`

# 实现细节

本系统分为四个模块：

- 图像获取模块：能够将用户需要识别的图片加载进内存，对于存在手机存储的图片用户可以使用相册进行载入，用户也可以调用相机即时拍照载入图片，同时可以对图片进行裁剪；
- 图像预处理模块：负责将上一个模块加载的原始图片中的文字区域提取出来，是下一个文字识别模块的前提，该模块首先需要将图片进行灰度化处理，然后将图片进行平滑处理，去掉图片的噪声，接着将图片二值化处理，最后进行边界获取，使用SWT算法确定图片中的文字区域；
- 文字识别模块：将上一个模块的文字区域中的文字提取出来，并且与字库进行比对，识别出其中的文字，并且能够将最终的识别结构导出到Word文档里面；
- 历史记录查看模块：用户可以查看以往的识别记录，以列表的方式展现给用户

### 图像获取模块

图像获取模块整体需要实现的是：从相册、相机导入图片；对图片进行裁剪；是否旋转、压缩；识别方式是中文还是英文，本系统使用TakePhotoManager类来管理这些配置项，也就是在主界面看到的七个复选框，当用户选择某项时，TakePhotoManager对应的配置项生效，然后当用户点击选择图片时使用相应的方式获取到图片的Uri后，在主界面进行的图片控件中进行显示

同时，由于图像获取模块是主界面模块，还需要完成一些资源的初始化工作。例如在启动软件时，检查tessdate文件夹是否存在，这个是后续Tesseract能否正确工作的必要前提，如果不存在那么将加载该文件夹；同时，还需要检查数据库，如果数据库没有创建则需要创建数据库。

```java
//使用多线程加载资源
class InitDataHandler extends Thread {
    @Override
    public void run() {
        super.run();
        String LANGUAGE_PATH1 = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;
        String LANGUAGE_PATH2 = tessdata + File.separator + ENGLISH_LANGUAGE_NAME;
        assets2SD(getApplicationContext(), LANGUAGE_PATH1, DEFAULT_LANGUAGE_NAME);
        assets2SD(getApplicationContext(), LANGUAGE_PATH2, ENGLISH_LANGUAGE_NAME);
        handler.sendEmptyMessage(0);
    }
}
//创建数据库
public void createDatabase() {
    MyHelper myHelper = new MyHelper(this);
    Log.i(TAG , "创建数据库");
    myHelper.getWritableDatabase();
}
```

### 图像预处理模块

图像预处理模块需要使用到SWT算法，使用JNI技术，用Java语言调用C++语言，首先需要完成的工作时，将Java中的Bitmap对象转为OpenCV中的Mat对象，使用两个通用的方法：

```c++
// 将Mat转为Bitmap
bool MatrixToBitmap(JNIEnv * env, cv::Mat & matrix, jobject obj_bitmap) {

    if (bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        cv::Mat tmp(bitmapInfo.height, bitmapInfo.width, CV_8UC4, bitmapPixels);
        switch (matrix.type()) {
            case CV_8UC1:   cv::cvtColor(matrix, tmp, cv::COLOR_GRAY2RGBA);     break;
            case CV_8UC3:   cv::cvtColor(matrix, tmp, cv::COLOR_RGB2RGBA);      break;
            case CV_8UC4:   matrix.copyTo(tmp);                                 break;
            default:        AndroidBitmap_unlockPixels(env, obj_bitmap);        return false;
        }
    } else {
        cv::Mat tmp(bitmapInfo.height, bitmapInfo.width, CV_8UC2, bitmapPixels);
        switch (matrix.type()) {
            case CV_8UC1:   cv::cvtColor(matrix, tmp, cv::COLOR_GRAY2BGR565);   break;
            case CV_8UC3:   cv::cvtColor(matrix, tmp, cv::COLOR_RGB2BGR565);    break;
            case CV_8UC4:   cv::cvtColor(matrix, tmp, cv::COLOR_RGBA2BGR565);   break;
            default:        AndroidBitmap_unlockPixels(env, obj_bitmap);        return false;
        }
    }
    AndroidBitmap_unlockPixels(env, obj_bitmap);                // Unlock
    return true;
}
// 将Bitmap转为Map
bool BitmapToMatrix(JNIEnv * env, jobject obj_bitmap, cv::Mat & matrix) {
    void * bitmapPixels;                                            // Save picture pixel data
    AndroidBitmapInfo bitmapInfo;                                   // Save picture parameters


    if (bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        cv::Mat tmp(bitmapInfo.height, bitmapInfo.width, CV_8UC4, bitmapPixels);    // Establish temporary mat
        tmp.copyTo(matrix);                                                         // Copy to target matrix
    } else {
        cv::Mat tmp(bitmapInfo.height, bitmapInfo.width, CV_8UC2, bitmapPixels);
        cv::cvtColor(tmp, matrix, cv::COLOR_BGR5652RGB);
    }

    //convert RGB to BGR
    cv::cvtColor(matrix,matrix,cv::COLOR_RGB2BGR);

    AndroidBitmap_unlockPixels(env, obj_bitmap);            // Unlock
    return true;
}
```

接下来，封装一个Java类NaturalSceneOCR用来与C++代码进行交互，该类有一个native方法JniBitmapUseSWT，传入六个参数：原图、灰度化处理后图像、边缘算法处理后图像、SWT操作处理后图像、找到候选字母处理图像、最后结果图像(也就是SWT算法的各个步骤，这个将其全部显示)。

当用户点击开始识别时，图像预预处理模块开始工作，同时开启一个线程专门用来预处理工作，将调用JniBitmapUseSWT本地方法，将SWT算法的各个步骤封装到对应参数中，最后显示在用户界面。

### 文字识别模块

