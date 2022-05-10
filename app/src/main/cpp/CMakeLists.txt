# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

#该变量为真时会创建完整版本的Makefile
set(CMAKE_VERBOSE_MAKEFILE on)

#定义变量ocvlibs使后面的命令可以使用定位具体的库文件
#set(opencvlibs "E:/opencv-2.4.13.6-android-sdk/OpenCV-android-sdk/sdk/native/libs")
set(opencvlibs "/Users/guopuwen/Downloads/OpenCV-android-sdk/sdk/native/libs")

#调用头文件的具体路径
include_directories(/Users/guopuwen/Downloads/OpenCV-android-sdk/sdk/native/jni/include)
include_directories( ${MY_BOOST_INC_DIR}
        )
#增加我们的动态库
add_library(libopencv_java SHARED IMPORTED)
#建立链接
set_target_properties(libopencv_java PROPERTIES IMPORTED_LOCATION
        "${opencvlibs}/${ANDROID_ABI}/libopencv_java.so")

#-----------------------------------------
add_library (boost_system SHARED IMPORTED)

set_target_properties( boost_system PROPERTIES IMPORTED_LOCATION
        ${MY_BOOST_LIBS_DIR}/${ANDROID_ABI}/libboost_system.so
        )

#-----------------------------------------
add_library( boost_chrono SHARED IMPORTED)

set_target_properties(boost_chrono PROPERTIES IMPORTED_LOCATION
        ${MY_BOOST_LIBS_DIR}/${ANDROID_ABI}/libboost_chrono.so
        )

# Declares and names the project.

project("opencvdemo")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             native-lib.cpp )

#add_library( # Sets the name of the library.
#        textdetection-lib
#
#        # Sets the library as a shared library.
#        SHARED
#
#        # Provides a relative path to your source file(s).
#        TextDetection.cpp  )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib
        jnigraphics
#        textdetection-lib
        libopencv_java
        boost_system
        boost_chrono

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )