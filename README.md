# MyGraduationDesign
本科毕业设计，要求是做一个基于Android的文字识别系统的设计与实现



# 系统调用相机并裁剪最后显示

主要参照：https://blog.csdn.net/qq_41885673/article/details/1209246475

# 固定图片中文识别

关键代码

```java
File dataPath = this.getExternalFilesDir(null);
String pathName = dataPath.getPath();
Log.i("dataPath", pathName);
TessBaseAPI tessBaseAPI = new TessBaseAPI();
tessBaseAPI.setDebug(true);
tessBaseAPI.init(pathName, "chi_sim");

BitmapFactory.Options options = new BitmapFactory.Options();
options.inSampleSize = 4; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
String picPath = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + File.separator + "11.jpg";

File file = new File(picPath);

Bitmap bitmap = BitmapFactory.decodeFile(picPath, options);
tessBaseAPI.setImage(bitmap);
String text = tessBaseAPI.getUTF8Text();
Log.i("text", text);

ori_pic.setImageBitmap(bitmap);
pre_pic.setImageBitmap(bitmap);
res.setText(text);
```

# 新增测试用图