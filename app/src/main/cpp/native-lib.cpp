#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <assert.h>

#include "boost/graph/adjacency_list.hpp"
#include "boost/graph/graph_traits.hpp"
#include "boost/graph/connected_components.hpp"
#include "boost/unordered_map.hpp"
#include "boost/graph/floyd_warshall_shortest.hpp"
#include "boost/numeric/ublas/matrix.hpp"
#include "boost/numeric/ublas/io.hpp"

#include "boost/chrono.hpp"
#include "boost/lexical_cast.hpp"
#include "boost/version_ndk.hpp"

#include <cassert>
#include <cmath>
#include <iostream>
#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <math.h>
#include <time.h>
#include <utility>
#include <algorithm>
#include <vector>
#include <android/log.h>


#define  LOG    "opencvDemo" // 这个是自定义的LOG的标识
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG,__VA_ARGS__) // 定义LOGD类型
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG,__VA_ARGS__) // 定义LOGI类型
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG,__VA_ARGS__) // 定义LOGF类型

using namespace std;
using namespace cv;
using std::string;
//extern "C" JNIEXPORT jstring JNICALL
//Java_com_example_opencvdemo_MainActivity_stringFromJNI(
//        JNIEnv* env,
//        jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
//}

#include "TextDetection.h"

#define PI 3.14159265


bool BitmapToMatrix(JNIEnv * env, jobject obj_bitmap, cv::Mat & matrix) {
    void * bitmapPixels;                                            // Save picture pixel data
    AndroidBitmapInfo bitmapInfo;                                   // Save picture parameters

    assert( AndroidBitmap_getInfo(env, obj_bitmap, &bitmapInfo) >= 0);        // Get picture parameters
    assert( bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGBA_8888
                  || bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGB_565 );          // Only ARGB? 8888 and RGB? 565 are supported
    assert( AndroidBitmap_lockPixels(env, obj_bitmap, &bitmapPixels) >= 0 );  // Get picture pixels (lock memory block)
    assert( bitmapPixels );

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

bool MatrixToBitmap(JNIEnv * env, cv::Mat & matrix, jobject obj_bitmap) {
    void * bitmapPixels;                                            // Save picture pixel data
    AndroidBitmapInfo bitmapInfo;                                   // Save picture parameters

    assert( AndroidBitmap_getInfo(env, obj_bitmap, &bitmapInfo) >= 0);        // Get picture parameters
    assert( bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGBA_8888
                  || bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGB_565 );          // Only ARGB? 8888 and RGB? 565 are supported
    assert( matrix.dims == 2
                  && bitmapInfo.height == (uint32_t)matrix.rows
                  && bitmapInfo.width == (uint32_t)matrix.cols );                   // It must be a 2-dimensional matrix with the same length and width
    assert( matrix.type() == CV_8UC1 || matrix.type() == CV_8UC3 || matrix.type() == CV_8UC4 );
    assert( AndroidBitmap_lockPixels(env, obj_bitmap, &bitmapPixels) >= 0 );  // Get picture pixels (lock memory block)
    assert( bitmapPixels );

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

extern "C" JNIEXPORT void JNICALL
Java_com_example_opencvdemo_NaturalSceneOCR_JniBitmapUseSWT(
        JNIEnv * env, jobject /* this */,
        jobject obj_bitmap,
        jobject grayImage,
        jobject edgeImage,
        jobject saveSWT,
        jobject components,
        jobject result

){
    cv::Mat matBitmap;
    bool ret = BitmapToMatrix(env, obj_bitmap, matBitmap);          // Bitmap to cv::Mat
    if (ret == false) {
        LOGI("false-------------1");
        return;
    }

    // opencv processing of mat
    std::vector<cv::Mat> res = DetectText::textDetection(matBitmap, 1);
    MatrixToBitmap(env, res[0], grayImage);
    MatrixToBitmap(env, res[1], edgeImage);
    MatrixToBitmap(env, res[2], saveSWT);
    MatrixToBitmap(env, res[3], components);
    MatrixToBitmap(env, res[4], result);

}

namespace DetectText {

    const Scalar BLUE (255, 0, 0);
    const Scalar GREEN(0, 255, 0);
    const Scalar RED  (0, 0, 255);

    std::vector<SWTPointPair2i > findBoundingBoxes( std::vector<std::vector<SWTPoint2d> > & components,
                                                    std::vector<Chain> & chains,
                                                    std::vector<SWTPointPair2d > & compBB,
                                                    Mat& output) {
        std::vector<SWTPointPair2i > bb;
        bb.reserve(chains.size());
        for (auto& chainit : chains) {
            int minx = output.cols;
            int miny = output.rows;
            int maxx = 0;
            int maxy = 0;
            for (std::vector<int>::const_iterator cit = chainit.components.begin(); cit != chainit.components.end(); cit++) {
                miny = std::min(miny,compBB[*cit].first.y);
                minx = std::min(minx,compBB[*cit].first.x);
                maxy = std::max(maxy,compBB[*cit].second.y);
                maxx = std::max(maxx,compBB[*cit].second.x);
            }
            Point2i p0(minx,miny);
            Point2i p1(maxx,maxy);
            SWTPointPair2i pair(p0,p1);
            bb.push_back(pair);
        }
        return bb;
    }

    std::vector<SWTPointPair2i > findBoundingBoxes( std::vector<std::vector<SWTPoint2d> > & components,
                                                    Mat& output) {
        std::vector<SWTPointPair2i > bb;
        bb.reserve(components.size());
        for (auto& compit : components) {
            int minx = output.cols;
            int miny = output.rows;
            int maxx = 0;
            int maxy = 0;
            for (auto& it : compit) {
                miny = std::min(miny,it.y);
                minx = std::min(minx,it.x);
                maxy = std::max(maxy,it.y);
                maxx = std::max(maxx,it.x);
            }
            Point2i p0(minx,miny);
            Point2i p1(maxx,maxy);
            SWTPointPair2i pair(p0,p1);
            bb.push_back(pair);
        }
        return bb;
    }

    void normalizeImage (const Mat& input, Mat& output) {
        assert ( input.depth() == CV_32F );
        assert ( input.channels() == 1 );
        assert ( output.depth() == CV_32F );
        assert ( output.channels() == 1 );

        float maxVal = 0;
        float minVal = 1e100;
        for ( int row = 0; row < input.rows; row++ ){
            const float* ptr = (const float*)input.ptr(row);
            for ( int col = 0; col < input.cols; col++ ){
                if (*ptr < 0) { }
                else {
                    maxVal = std::max(*ptr, maxVal);
                    minVal = std::min(*ptr, minVal);
                }
                ptr++;
            }
        }

        float difference = maxVal - minVal;
        for ( int row = 0; row < input.rows; row++ ) {
            const float* ptrin = (const float*)input.ptr(row);
            float* ptrout = (float*)output.ptr(row);
            for ( int col = 0; col < input.cols; col++ ) {
                if (*ptrin < 0) {
                    *ptrout = 1;
                } else {
                    *ptrout = ((*ptrin) - minVal)/difference;
                }
                ptrout++;
                ptrin++;
            }
        }
    }

    void renderComponents (const Mat& SWTImage, std::vector<std::vector<SWTPoint2d> > & components, Mat& output) {
        output.setTo(0);

        for (auto& component : components) {
            for (auto& pit : component) {
                output.at<float>(pit.y, pit.x) = SWTImage.at<float>(pit.y, pit.x);
            }
        }
        for( int row = 0; row < output.rows; row++ ){
            float* ptr = (float*)output.ptr(row);
            for ( int col = 0; col < output.cols; col++ ){
                if (*ptr == 0) {
                    *ptr = -1;
                }
                ptr++;
            }
        }
        float maxVal = 0;
        float minVal = 1e100;
        for( int row = 0; row < output.rows; row++ ){
            const float* ptr = (const float*)output.ptr(row);
            for ( int col = 0; col < output.cols; col++ ){
                if (*ptr == 0) { }
                else {
                    maxVal = std::max(*ptr, maxVal);
                    minVal = std::min(*ptr, minVal);
                }
                ptr++;
            }
        }
        float difference = maxVal - minVal;
        for( int row = 0; row < output.rows; row++ ){
            float* ptr = (float*)output.ptr(row);
            for ( int col = 0; col < output.cols; col++ ){
                if (*ptr < 1) {
                    *ptr = 1;
                } else {
                    *ptr = ((*ptr) - minVal)/difference;
                }
                ptr++;
            }
        }

    }

    void renderComponentsWithBoxes (Mat& SWTImage, std::vector<std::vector<SWTPoint2d> > & components,
                                    std::vector<SWTPointPair2d > & compBB, Mat& output) {
        Mat outTemp( output.size(), CV_32FC1 );

        renderComponents(SWTImage, components, outTemp);

        std::vector<SWTPointPair2i> bb;
        bb.reserve(compBB.size());
        for (auto& it : compBB) {
            Point2i p0 = cvPoint(it.first.x,  it.first.y);
            Point2i p1 = cvPoint(it.second.x, it.second.y);
            SWTPointPair2i pair(p0, p1);
            bb.push_back(pair);
        }

        Mat out( output.size(), CV_8UC1 );
        outTemp.convertTo(out, CV_8UC1, 255.);
        cvtColor (out, output, CV_GRAY2RGB);

        int count = 0;
        for (auto it : bb) {
            Scalar c;
            if (count % 3 == 0) {
                c = BLUE;
            }
            else if (count % 3 == 1) {
                c = GREEN;
            }
            else {
                c = RED;
            }
            count++;
            rectangle(output, it.first, it.second, c, 2);
        }
    }

    void renderChainsWithBoxes (Mat& SWTImage,
                                std::vector<std::vector<SWTPoint2d> > & components,
                                std::vector<Chain> & chains,
                                std::vector<SWTPointPair2d > & compBB,
                                Mat& output) {
        // keep track of included components
        std::vector<bool> included;
        included.reserve(components.size());
        for (unsigned int i = 0; i != components.size(); i++) {
            included.push_back(false);
        }
        for (Chain& it : chains) {
            for (std::vector<int>::iterator cit = it.components.begin(); cit != it.components.end(); cit++) {
                included[*cit] = true;
            }
        }
        std::vector<std::vector<SWTPoint2d> > componentsRed;
        for (unsigned int i = 0; i != components.size(); i++ ) {
            if (included[i]) {
                componentsRed.push_back(components[i]);
            }
        }
        Mat outTemp( output.size(), CV_32FC1 );

        std::cout << componentsRed.size() << " components after chaining" << std::endl;
        renderComponents(SWTImage, componentsRed, outTemp);
        std::vector<SWTPointPair2i > bb;
        bb = findBoundingBoxes(components, chains, compBB, outTemp);

        Mat out( output.size(), CV_8UC1 );
        outTemp.convertTo(out, CV_8UC1, 255);
        cvtColor (out, output, CV_GRAY2RGB);

        int count = 0;
        for (auto& it : bb) {
            CvScalar c;
            if (count % 3 == 0) {
                c = BLUE;
            }
            else if (count % 3 == 1) {
                c = GREEN;
            }
            else {
                c = RED;
            }
            count++;
            rectangle(output, it.first, it.second, c, 2);
        }
    }

    void renderChains (Mat& SWTImage,
                       std::vector<std::vector<SWTPoint2d> > & components,
                       std::vector<Chain> & chains,
                       Mat& output) {
        // keep track of included components
        std::vector<bool> included;
        included.reserve(components.size());
        for (unsigned int i = 0; i != components.size(); i++) {
            included.push_back(false);
        }
        for (std::vector<Chain>::iterator it = chains.begin(); it != chains.end();it++) {
            for (std::vector<int>::iterator cit = it->components.begin(); cit != it->components.end(); cit++) {
                included[*cit] = true;
            }
        }
        std::vector<std::vector<SWTPoint2d> > componentsRed;
        for (unsigned int i = 0; i != components.size(); i++ ) {
            if (included[i]) {
                componentsRed.push_back(components[i]);
            }
        }
        std::cout << componentsRed.size() << " components after chaining" << std::endl;
        Mat outTemp( output.size(), CV_32FC1 );
        renderComponents(SWTImage,componentsRed,outTemp);
        outTemp.convertTo(output, CV_8UC1, 255);

    }

    //--------方法入口
    std::vector<Mat> textDetection (const Mat& input, bool dark_on_light) {

        LOGI("开始swt算法");
        //  CV_8U是无符号8位/像素-即一个像素可以有0-255的值，这是大多数图像和视频格式的正常范围
        assert ( input.depth() == CV_8U );
        //assert ( input.channels() == 3 );
        std::vector<Mat> res;

        std::cout << "Running textDetection with dark_on_light " << dark_on_light << std::endl;

        //转为灰度图像
        Mat grayImage( input.size(), CV_8UC1 );
        cvtColor ( input, grayImage, CV_RGB2GRAY );
//        // Canny边缘检测
        double threshold_low = 175;
        double threshold_high = 320;
        Mat edgeImage( input.size(),CV_8UC1 );
        Canny(grayImage, edgeImage, threshold_low, threshold_high, 3) ;
//        //imwrite ( "canny.png", edgeImage);
//
//        // Create gradient X, gradient Y
        Mat gaussianImage( input.size(), CV_32FC1);
        grayImage.convertTo(gaussianImage, CV_32FC1, 1./255.);
        GaussianBlur( gaussianImage, gaussianImage, Size(5, 5), 0);
        Mat gradientX( input.size(), CV_32FC1 );
        Mat gradientY( input.size(), CV_32FC1 );
        Scharr(gaussianImage, gradientX, -1, 1, 0);
        Scharr(gaussianImage, gradientY, -1, 0, 1);
        GaussianBlur(gradientX, gradientX, Size(3, 3), 0);
        GaussianBlur(gradientY, gradientY, Size(3, 3), 0);
//
//        // Calculate SWT and return ray vectors
        std::vector<Ray> rays;
        Mat SWTImage( input.size(), CV_32FC1 );
        for( int row = 0; row < input.rows; row++ ){
            float* ptr = (float*)SWTImage.ptr(row);
            for ( int col = 0; col < input.cols; col++ ){
                *ptr++ = -1;
            }
        }
        strokeWidthTransform ( edgeImage, gradientX, gradientY, dark_on_light, SWTImage, rays );
        SWTMedianFilter ( SWTImage, rays );

        Mat output2( input.size(), CV_32FC1 );
        normalizeImage (SWTImage, output2);
        Mat saveSWT( input.size(), CV_8UC1 );
        output2.convertTo(saveSWT, CV_8UC1, 255);
//        //imwrite ( "SWT.png", saveSWT);
//
//
//
//        // Calculate legally connect components from SWT and gradient image.
//        // return type is a vector of vectors, where each outer vector is a component and
//        // the inner vector contains the (y,x) of each pixel in that component.
        std::vector<std::vector<SWTPoint2d> > components = findLegallyConnectedComponents(SWTImage, rays);
//
//        // Filter the components
        std::vector<std::vector<SWTPoint2d> > validComponents;
        std::vector<SWTPointPair2d > compBB;
        std::vector<Point2dFloat> compCenters;
        std::vector<float> compMedians;
        std::vector<SWTPoint2d> compDimensions;
        filterComponents(SWTImage, components, validComponents, compCenters, compMedians, compDimensions, compBB );

        Mat output3( input.size(), CV_8UC3 );
        renderComponentsWithBoxes (SWTImage, validComponents, compBB, output3);
//        //imwrite ( "components.png",output3);
//        //
//
//        // Make chains of components
        std::vector<Chain> chains;
        chains = makeChains(input, validComponents, compCenters, compMedians, compDimensions, compBB);

        Mat output4( input.size(), CV_8UC1 );
        renderChains ( SWTImage, validComponents, chains, output4 );
//        //imwrite ( "text.png", output4);
//
        Mat output5( input.size(), CV_8UC3 );
        cvtColor (output4, output5, CV_GRAY2RGB);

        res.push_back(grayImage);
        res.push_back(edgeImage);
        res.push_back(saveSWT);
        res.push_back(output3);
        res.push_back(output5);

        return res;
    }

    void strokeWidthTransform (const Mat& edgeImage,
                               Mat& gradientX,
                               Mat& gradientY,
                               bool dark_on_light,
                               Mat& SWTImage,
                               std::vector<Ray> & rays) {
        // First pass
        float prec = .05;
        for( int row = 0; row < edgeImage.rows; row++ ){
            const uchar* ptr = (const uchar*)edgeImage.ptr(row);
            for ( int col = 0; col < edgeImage.cols; col++ ){
                if (*ptr > 0) {
                    Ray r;

                    SWTPoint2d p;
                    p.x = col;
                    p.y = row;
                    r.p = p;
                    std::vector<SWTPoint2d> points;
                    points.push_back(p);

                    float curX = (float)col + 0.5;
                    float curY = (float)row + 0.5;
                    int curPixX = col;
                    int curPixY = row;
                    float G_x = gradientX.at<float>(row, col);
                    float G_y = gradientY.at<float>(row, col);
                    // normalize gradient
                    float mag = sqrt( (G_x * G_x) + (G_y * G_y) );
                    if (dark_on_light){
                        G_x = -G_x/mag;
                        G_y = -G_y/mag;
                    } else {
                        G_x = G_x/mag;
                        G_y = G_y/mag;

                    }
                    while (true) {
                        curX += G_x*prec;
                        curY += G_y*prec;
                        if ((int)(floor(curX)) != curPixX || (int)(floor(curY)) != curPixY) {
                            curPixX = (int)(floor(curX));
                            curPixY = (int)(floor(curY));
                            // check if pixel is outside boundary of image
                            if (curPixX < 0 || (curPixX >= SWTImage.cols) || curPixY < 0 || (curPixY >= SWTImage.rows)) {
                                break;
                            }
                            SWTPoint2d pnew;
                            pnew.x = curPixX;
                            pnew.y = curPixY;
                            points.push_back(pnew);

                            if (edgeImage.at<uchar>(curPixY, curPixX) > 0) {
                                r.q = pnew;
                                // dot product
                                float G_xt = gradientX.at<float>(curPixY,curPixX);
                                float G_yt = gradientY.at<float>(curPixY,curPixX);
                                mag = sqrt( (G_xt * G_xt) + (G_yt * G_yt) );
                                if (dark_on_light) {
                                    G_xt = -G_xt / mag;
                                    G_yt = -G_yt / mag;
                                } else {
                                    G_xt = G_xt / mag;
                                    G_yt = G_yt / mag;

                                }

                                if (acos(G_x * -G_xt + G_y * -G_yt) < PI/2.0 ) {
                                    float length = sqrt( ((float)r.q.x - (float)r.p.x)*((float)r.q.x - (float)r.p.x) + ((float)r.q.y - (float)r.p.y)*((float)r.q.y - (float)r.p.y));
                                    for (std::vector<SWTPoint2d>::iterator pit = points.begin(); pit != points.end(); pit++) {
                                        if (SWTImage.at<float>(pit->y, pit->x) < 0) {
                                            SWTImage.at<float>(pit->y, pit->x) = length;
                                        } else {
                                            SWTImage.at<float>(pit->y, pit->x) = std::min(length, SWTImage.at<float>(pit->y, pit->x));
                                        }
                                    }
                                    r.points = points;
                                    rays.push_back(r);
                                }
                                break;
                            }
                        }
                    }
                }
                ptr++;
            }
        }

    }

    void SWTMedianFilter (Mat& SWTImage, std::vector<Ray> & rays) {
        for (auto& rit : rays) {
            for (auto& pit : rit.points) {
                pit.SWT = SWTImage.at<float>(pit.y, pit.x);
            }
            std::sort(rit.points.begin(), rit.points.end(), &Point2dSort);
            float median = (rit.points[rit.points.size()/2]).SWT;
            for (auto& pit : rit.points) {
                SWTImage.at<float>(pit.y, pit.x) = std::min(pit.SWT, median);
            }
        }
    }

    bool Point2dSort (const SWTPoint2d &lhs, const SWTPoint2d &rhs) {
        return lhs.SWT < rhs.SWT;
    }

    std::vector< std::vector<SWTPoint2d> > findLegallyConnectedComponents (Mat& SWTImage, std::vector<Ray> & rays) {
        boost::unordered_map<int, int> map;
        boost::unordered_map<int, SWTPoint2d> revmap;

        typedef boost::adjacency_list<boost::vecS, boost::vecS, boost::undirectedS> Graph;
        int num_vertices = 0;
        // Number vertices for graph.  Associate each point with number
        for( int row = 0; row < SWTImage.rows; row++ ){
            float * ptr = (float*)SWTImage.ptr(row);
            for (int col = 0; col < SWTImage.cols; col++ ){
                if (*ptr > 0) {
                    map[row * SWTImage.cols + col] = num_vertices;
                    SWTPoint2d p;
                    p.x = col;
                    p.y = row;
                    revmap[num_vertices] = p;
                    num_vertices++;
                }
                ptr++;
            }
        }

        Graph g(num_vertices);

        for( int row = 0; row < SWTImage.rows; row++ ){
            float * ptr = (float*)SWTImage.ptr(row);
            for (int col = 0; col < SWTImage.cols; col++ ){
                if (*ptr > 0) {
                    // check pixel to the right, right-down, down, left-down
                    int this_pixel = map[row * SWTImage.cols + col];
                    if (col+1 < SWTImage.cols) {
                        float right = SWTImage.at<float>(row, col+1);
                        if (right > 0 && ((*ptr)/right <= 3.0 || right/(*ptr) <= 3.0))
                            boost::add_edge(this_pixel, map.at(row * SWTImage.cols + col + 1), g);
                    }
                    if (row+1 < SWTImage.rows) {
                        if (col+1 < SWTImage.cols) {
                            float right_down = SWTImage.at<float>(row+1, col+1);
                            if (right_down > 0 && ((*ptr)/right_down <= 3.0 || right_down/(*ptr) <= 3.0))
                                boost::add_edge(this_pixel, map.at((row+1) * SWTImage.cols + col + 1), g);
                        }
                        float down = SWTImage.at<float>(row+1, col);
                        if (down > 0 && ((*ptr)/down <= 3.0 || down/(*ptr) <= 3.0))
                            boost::add_edge(this_pixel, map.at((row+1) * SWTImage.cols + col), g);
                        if (col-1 >= 0) {
                            float left_down = SWTImage.at<float>(row+1, col-1);
                            if (left_down > 0 && ((*ptr)/left_down <= 3.0 || left_down/(*ptr) <= 3.0))
                                boost::add_edge(this_pixel, map.at((row+1) * SWTImage.cols + col - 1), g);
                        }
                    }
                }
                ptr++;
            }
        }

        std::vector<int> c(num_vertices);

        int num_comp = connected_components(g, &c[0]);

        std::vector<std::vector<SWTPoint2d> > components;
        components.reserve(num_comp);
        std::cout << "Before filtering, " << num_comp << " components and " << num_vertices << " vertices" << std::endl;
        for (int j = 0; j < num_comp; j++) {
            std::vector<SWTPoint2d> tmp;
            components.push_back( tmp );
        }
        for (int j = 0; j < num_vertices; j++) {
            SWTPoint2d p = revmap[j];
            (components[c[j]]).push_back(p);
        }

        return components;
    }

    std::vector< std::vector<SWTPoint2d> >
    findLegallyConnectedComponentsRAY (Mat& SWTImage,
                                       std::vector<Ray> & rays)
    {
        boost::unordered_map<int, int> map;
        boost::unordered_map<int, SWTPoint2d> revmap;

        typedef boost::adjacency_list<boost::vecS, boost::vecS, boost::undirectedS> Graph;
        int num_vertices = 0;
        // Number vertices for graph.  Associate each point with number
        for( int row = 0; row < SWTImage.rows; row++ ){
            float * ptr = (float*)SWTImage.ptr(row);
            for (int col = 0; col < SWTImage.cols; col++ ){
                if (*ptr > 0) {
                    map[row * SWTImage.cols + col] = num_vertices;
                    SWTPoint2d p;
                    p.x = col;
                    p.y = row;
                    revmap[num_vertices] = p;
                    num_vertices++;
                }
                ptr++;
            }
        }

        Graph g(num_vertices);

        // Traverse and add edges to graph
        for (std::vector<Ray>::const_iterator it = rays.begin(); it != rays.end(); it++) {
            float lastSW = 0;
            int lastRow = 0;
            int lastCol = 0;
            for (std::vector<SWTPoint2d>::const_iterator it2 = it->points.begin(); it2 != it->points.end(); it2++) {
                float currentSW = SWTImage.at<float>(it2->y, it2->x);
                if (lastSW == 0) {}
                else if (lastSW/currentSW<=3.0 || currentSW/lastSW<=3.0){
                    boost::add_edge(map.at(it2->y * SWTImage.cols + it2->x), map.at(lastRow * SWTImage.cols + lastCol), g);
                }
                lastSW = currentSW;
                lastRow = it2->y;
                lastCol = it2->x;
            }
            lastSW = 0;
            lastRow = 0;
            lastCol = 0;
        }

        std::vector<int> c(num_vertices);

        int num_comp = connected_components(g, &c[0]);

        std::vector<std::vector<SWTPoint2d> > components;
        components.reserve(num_comp);
        std::cout << "Before filtering, " << num_comp << " components and " << num_vertices << " vertices" << std::endl;
        for (int j = 0; j < num_comp; j++) {
            std::vector<SWTPoint2d> tmp;
            components.push_back( tmp );
        }
        for (int j = 0; j < num_vertices; j++) {
            SWTPoint2d p = revmap[j];
            (components[c[j]]).push_back(p);
        }

        return components;
    }

    void componentStats(Mat& SWTImage,
                        const std::vector<SWTPoint2d> & component,
                        float & mean, float & variance, float & median,
                        int & minx, int & miny, int & maxx, int & maxy)
    {
        std::vector<float> temp;
        temp.reserve(component.size());
        mean = 0;
        variance = 0;
        minx = 1000000;
        miny = 1000000;
        maxx = 0;
        maxy = 0;
        for (std::vector<SWTPoint2d>::const_iterator it = component.begin(); it != component.end(); it++) {
            float t = SWTImage.at<float>(it->y, it->x);
            mean += t;
            temp.push_back(t);
            miny = std::min(miny,it->y);
            minx = std::min(minx,it->x);
            maxy = std::max(maxy,it->y);
            maxx = std::max(maxx,it->x);
        }
        mean = mean / ((float)component.size());
        for (std::vector<float>::const_iterator it = temp.begin(); it != temp.end(); it++) {
            variance += (*it - mean) * (*it - mean);
        }
        variance = variance / ((float)component.size());
        std::sort(temp.begin(),temp.end());
        median = temp[temp.size()/2];
    }


    void filterComponents(Mat& SWTImage,
                          std::vector<std::vector<SWTPoint2d> > & components,
                          std::vector<std::vector<SWTPoint2d> > & validComponents,
                          std::vector<Point2dFloat> & compCenters,
                          std::vector<float> & compMedians,
                          std::vector<SWTPoint2d> & compDimensions,
                          std::vector<SWTPointPair2d > & compBB )
    {
        validComponents.reserve(components.size());
        compCenters.reserve(components.size());
        compMedians.reserve(components.size());
        compDimensions.reserve(components.size());
        // bounding boxes
        compBB.reserve(components.size());
        for (std::vector<std::vector<SWTPoint2d> >::iterator it = components.begin(); it != components.end();it++) {
            // compute the stroke width mean, variance, median
            float mean, variance, median;
            int minx, miny, maxx, maxy;
            componentStats(SWTImage, (*it), mean, variance, median, minx, miny, maxx, maxy);

            // check if variance is less than half the mean
            if (variance > 0.5 * mean) {
                continue;
            }

            float length = (float)(maxx-minx+1);
            float width = (float)(maxy-miny+1);

            // check font height
            if (width > 300) {
                continue;
            }

            float area = length * width;
            float rminx = (float)minx;
            float rmaxx = (float)maxx;
            float rminy = (float)miny;
            float rmaxy = (float)maxy;
            // compute the rotated bounding box
            float increment = 1./36.;
            for (float theta = increment * PI; theta<PI/2.0; theta += increment * PI) {
                float xmin,xmax,ymin,ymax,xtemp,ytemp,ltemp,wtemp;
                xmin = 1000000;
                ymin = 1000000;
                xmax = 0;
                ymax = 0;
                for (unsigned int i = 0; i < (*it).size(); i++) {
                    xtemp = (*it)[i].x * cos(theta) + (*it)[i].y * -sin(theta);
                    ytemp = (*it)[i].x * sin(theta) + (*it)[i].y * cos(theta);
                    xmin = std::min(xtemp,xmin);
                    xmax = std::max(xtemp,xmax);
                    ymin = std::min(ytemp,ymin);
                    ymax = std::max(ytemp,ymax);
                }
                ltemp = xmax - xmin + 1;
                wtemp = ymax - ymin + 1;
                if (ltemp*wtemp < area) {
                    area = ltemp*wtemp;
                    length = ltemp;
                    width = wtemp;
                }
            }
            // check if the aspect ratio is between 1/10 and 10
            if (length/width < 1./10. || length/width > 10.) {
                continue;
            }

            // compute the diameter TODO finish
            // compute dense representation of component
            std::vector <std::vector<float> > denseRepr;
            denseRepr.reserve(maxx-minx+1);
            for (int i = 0; i < maxx-minx+1; i++) {
                std::vector<float> tmp;
                tmp.reserve(maxy-miny+1);
                denseRepr.push_back(tmp);
                for (int j = 0; j < maxy-miny+1; j++) {\
                    denseRepr[i].push_back(0);
                }
            }
            for (std::vector<SWTPoint2d>::iterator pit = it->begin(); pit != it->end(); pit++) {
                (denseRepr[pit->x - minx])[pit->y - miny] = 1;
            }
            // create graph representing components
            const int num_nodes = it->size();
            /*
            E edges[] = { E(0,2),
                          E(1,1), E(1,3), E(1,4),
                          E(2,1), E(2,3),
                          E(3,4),
                          E(4,0), E(4,1) };

            Graph G(edges + sizeof(edges) / sizeof(E), weights, num_nodes);
            */
            Point2dFloat center;
            center.x = ((float)(maxx+minx))/2.0;
            center.y = ((float)(maxy+miny))/2.0;

            SWTPoint2d dimensions;
            dimensions.x = maxx - minx + 1;
            dimensions.y = maxy - miny + 1;

            SWTPoint2d bb1;
            bb1.x = minx;
            bb1.y = miny;

            SWTPoint2d bb2;
            bb2.x = maxx;
            bb2.y = maxy;
            SWTPointPair2d pair(bb1,bb2);

            compBB.push_back(pair);
            compDimensions.push_back(dimensions);
            compMedians.push_back(median);
            compCenters.push_back(center);
            validComponents.push_back(*it);
        }
        std::vector<std::vector<SWTPoint2d > > tempComp;
        std::vector<SWTPoint2d > tempDim;
        std::vector<float > tempMed;
        std::vector<Point2dFloat > tempCenters;
        std::vector<SWTPointPair2d > tempBB;
        tempComp.reserve(validComponents.size());
        tempCenters.reserve(validComponents.size());
        tempDim.reserve(validComponents.size());
        tempMed.reserve(validComponents.size());
        tempBB.reserve(validComponents.size());
        for (unsigned int i = 0; i < validComponents.size(); i++) {
            int count = 0;
            for (unsigned int j = 0; j < validComponents.size(); j++) {
                if (i != j) {
                    if (compBB[i].first.x <= compCenters[j].x && compBB[i].second.x >= compCenters[j].x &&
                        compBB[i].first.y <= compCenters[j].y && compBB[i].second.y >= compCenters[j].y) {
                        count++;
                    }
                }
            }
            if (count < 2) {
                tempComp.push_back(validComponents[i]);
                tempCenters.push_back(compCenters[i]);
                tempMed.push_back(compMedians[i]);
                tempDim.push_back(compDimensions[i]);
                tempBB.push_back(compBB[i]);
            }
        }
        validComponents = tempComp;
        compDimensions = tempDim;
        compMedians = tempMed;
        compCenters = tempCenters;
        compBB = tempBB;

        compDimensions.reserve(tempComp.size());
        compMedians.reserve(tempComp.size());
        compCenters.reserve(tempComp.size());
        validComponents.reserve(tempComp.size());
        compBB.reserve(tempComp.size());

        std::cout << "After filtering " << validComponents.size() << " components" << std::endl;
    }

    bool sharesOneEnd( Chain c0, Chain c1) {
        if (c0.p == c1.p || c0.p == c1.q || c0.q == c1.q || c0.q == c1.p) {
            return true;
        }
        else {
            return false;
        }
    }

    bool chainSortDist (const Chain &lhs, const Chain &rhs) {
        return lhs.dist < rhs.dist;
    }

    bool chainSortLength (const Chain &lhs, const Chain &rhs) {
        return lhs.components.size() > rhs.components.size();
    }

    std::vector<Chain> makeChains( const Mat& colorImage,
                                   std::vector<std::vector<SWTPoint2d> > & components,
                                   std::vector<Point2dFloat> & compCenters,
                                   std::vector<float> & compMedians,
                                   std::vector<SWTPoint2d> & compDimensions,
                                   std::vector<SWTPointPair2d > & compBB) {
        assert (compCenters.size() == components.size());
        // make vector of color averages
        std::vector<Point3dFloat> colorAverages;
        colorAverages.reserve(components.size());
        for (std::vector<std::vector<SWTPoint2d> >::iterator it = components.begin(); it != components.end();it++) {
            Point3dFloat mean;
            mean.x = 0;
            mean.y = 0;
            mean.z = 0;
            int num_points = 0;
            for (std::vector<SWTPoint2d>::iterator pit = it->begin(); pit != it->end(); pit++) {
                mean.x += (float) colorImage.at<uchar>(pit->y, (pit->x)*3 );
                mean.y += (float) colorImage.at<uchar>(pit->y, (pit->x)*3+1 );
                mean.z += (float) colorImage.at<uchar>(pit->y, (pit->x)*3+2 );
                num_points++;
            }
            mean.x = mean.x / ((float)num_points);
            mean.y = mean.y / ((float)num_points);
            mean.z = mean.z / ((float)num_points);
            colorAverages.push_back(mean);
        }

        // form all eligible pairs and calculate the direction of each
        std::vector<Chain> chains;
        for ( unsigned int i = 0; i < components.size(); i++ ) {
            for ( unsigned int j = i + 1; j < components.size(); j++ ) {
                // TODO add color metric
                if ( (compMedians[i]/compMedians[j] <= 2.0 || compMedians[j]/compMedians[i] <= 2.0) &&
                     (compDimensions[i].y/compDimensions[j].y <= 2.0 || compDimensions[j].y/compDimensions[i].y <= 2.0)) {
                    float dist = (compCenters[i].x - compCenters[j].x) * (compCenters[i].x - compCenters[j].x) +
                                 (compCenters[i].y - compCenters[j].y) * (compCenters[i].y - compCenters[j].y);
                    float colorDist = (colorAverages[i].x - colorAverages[j].x) * (colorAverages[i].x - colorAverages[j].x) +
                                      (colorAverages[i].y - colorAverages[j].y) * (colorAverages[i].y - colorAverages[j].y) +
                                      (colorAverages[i].z - colorAverages[j].z) * (colorAverages[i].z - colorAverages[j].z);
                    if (dist < 9*(float)(std::max(std::min(compDimensions[i].x,compDimensions[i].y),std::min(compDimensions[j].x,compDimensions[j].y)))
                               *(float)(std::max(std::min(compDimensions[i].x,compDimensions[i].y),std::min(compDimensions[j].x,compDimensions[j].y)))
                        && colorDist < 1600) {
                        Chain c;
                        c.p = i;
                        c.q = j;
                        std::vector<int> comps;
                        comps.push_back(c.p);
                        comps.push_back(c.q);
                        c.components = comps;
                        c.dist = dist;
                        float d_x = (compCenters[i].x - compCenters[j].x);
                        float d_y = (compCenters[i].y - compCenters[j].y);
                        /*
                        float d_x = (compBB[i].first.x - compBB[j].second.x);
                        float d_y = (compBB[i].second.y - compBB[j].second.y);
                        */
                        float mag = sqrt(d_x*d_x + d_y*d_y);
                        d_x = d_x / mag;
                        d_y = d_y / mag;
                        Point2dFloat dir;
                        dir.x = d_x;
                        dir.y = d_y;
                        c.direction = dir;
                        chains.push_back(c);

                        /*std::cerr << c.p << " " << c.q << std::endl;
                        std::cerr << c.direction.x << " " << c.direction.y << std::endl;
                        std::cerr << compCenters[c.p].x << " " << compCenters[c.p].y << std::endl;
                        std::cerr << compCenters[c.q].x << " " << compCenters[c.q].y << std::endl;
                        std::cerr << std::endl;
                        std::cerr << colorDist << std::endl; */
                    }
                }
            }
        }
        std::cout << chains.size() << " eligible pairs" << std::endl;
        std::sort(chains.begin(), chains.end(), &chainSortDist);

        std::cerr << std::endl;
        const float strictness = PI/6.0;
        //merge chains
        int merges = 1;
        while (merges > 0) {
            for (unsigned int i = 0; i < chains.size(); i++) {
                chains[i].merged = false;
            }
            merges = 0;
            std::vector<Chain> newchains;
            for (unsigned int i = 0; i < chains.size(); i++) {
                for (unsigned int j = 0; j < chains.size(); j++) {
                    if (i != j) {
                        if (!chains[i].merged && !chains[j].merged && sharesOneEnd(chains[i],chains[j])) {
                            if (chains[i].p == chains[j].p) {
                                if (acos(chains[i].direction.x * -chains[j].direction.x + chains[i].direction.y * -chains[j].direction.y) < strictness) {
                                    /*      if (chains[i].p == chains[i].q || chains[j].p == chains[j].q) {
                                              std::cout << "CRAZY ERROR" << std::endl;
                                          } else if (chains[i].p == chains[j].p && chains[i].q == chains[j].q) {
                                              std::cout << "CRAZY ERROR" << std::endl;
                                          } else if (chains[i].p == chains[j].q && chains[i].q == chains[j].p) {
                                              std::cout << "CRAZY ERROR" << std::endl;
                                          }
                                          std::cerr << 1 <<std::endl;

                                          std::cerr << chains[i].p << " " << chains[i].q << std::endl;
                                          std::cerr << chains[j].p << " " << chains[j].q << std::endl;
                                  std::cerr << compCenters[chains[i].q].x << " " << compCenters[chains[i].q].y << std::endl;
                                  std::cerr << compCenters[chains[i].p].x << " " << compCenters[chains[i].p].y << std::endl;
                                  std::cerr << compCenters[chains[j].q].x << " " << compCenters[chains[j].q].y << std::endl;
                                  std::cerr << std::endl; */

                                    chains[i].p = chains[j].q;
                                    for (std::vector<int>::iterator it = chains[j].components.begin(); it != chains[j].components.end(); it++) {
                                        chains[i].components.push_back(*it);
                                    }
                                    float d_x = (compCenters[chains[i].p].x - compCenters[chains[i].q].x);
                                    float d_y = (compCenters[chains[i].p].y - compCenters[chains[i].q].y);
                                    chains[i].dist = d_x * d_x + d_y * d_y;

                                    float mag = sqrt(d_x*d_x + d_y*d_y);
                                    d_x = d_x / mag;
                                    d_y = d_y / mag;
                                    Point2dFloat dir;
                                    dir.x = d_x;
                                    dir.y = d_y;
                                    chains[i].direction = dir;
                                    chains[j].merged = true;
                                    merges++;
                                    /*j=-1;
                                    i=0;
                                    if (i == chains.size() - 1) i=-1;
                                    std::stable_sort(chains.begin(), chains.end(), &chainSortLength);*/
                                }
                            } else if (chains[i].p == chains[j].q) {
                                if (acos(chains[i].direction.x * chains[j].direction.x + chains[i].direction.y * chains[j].direction.y) < strictness) {
/*
                                if (chains[i].p == chains[i].q || chains[j].p == chains[j].q) {
                                    std::cout << "CRAZY ERROR" << std::endl;
                                } else if (chains[i].p == chains[j].p && chains[i].q == chains[j].q) {
                                    std::cout << "CRAZY ERROR" << std::endl;
                                } else if (chains[i].p == chains[j].q && chains[i].q == chains[j].p) {
                                    std::cout << "CRAZY ERROR" << std::endl;
                                }
                                std::cerr << 2 <<std::endl;

                                std::cerr << chains[i].p << " " << chains[i].q << std::endl;
                                std::cerr << chains[j].p << " " << chains[j].q << std::endl;
                                std::cerr << chains[i].direction.x << " " << chains[i].direction.y << std::endl;
                                std::cerr << chains[j].direction.x << " " << chains[j].direction.y << std::endl;
                                std::cerr << compCenters[chains[i].q].x << " " << compCenters[chains[i].q].y << std::endl;
                                std::cerr << compCenters[chains[i].p].x << " " << compCenters[chains[i].p].y << std::endl;
                                std::cerr << compCenters[chains[j].p].x << " " << compCenters[chains[j].p].y << std::endl;
                                std::cerr << std::endl; */

                                    chains[i].p = chains[j].p;
                                    for (std::vector<int>::iterator it = chains[j].components.begin(); it != chains[j].components.end(); it++) {
                                        chains[i].components.push_back(*it);
                                    }
                                    float d_x = (compCenters[chains[i].p].x - compCenters[chains[i].q].x);
                                    float d_y = (compCenters[chains[i].p].y - compCenters[chains[i].q].y);
                                    float mag = sqrt(d_x*d_x + d_y*d_y);
                                    chains[i].dist = d_x * d_x + d_y * d_y;

                                    d_x = d_x / mag;
                                    d_y = d_y / mag;

                                    Point2dFloat dir;
                                    dir.x = d_x;
                                    dir.y = d_y;
                                    chains[i].direction = dir;
                                    chains[j].merged = true;
                                    merges++;
                                    /*j=-1;
                                    i=0;
                                    if (i == chains.size() - 1) i=-1;
                                    std::stable_sort(chains.begin(), chains.end(), &chainSortLength); */
                                }
                            } else if (chains[i].q == chains[j].p) {
                                if (acos(chains[i].direction.x * chains[j].direction.x + chains[i].direction.y * chains[j].direction.y) < strictness) {
                                    /*                           if (chains[i].p == chains[i].q || chains[j].p == chains[j].q) {
                                                                   std::cout << "CRAZY ERROR" << std::endl;
                                                               } else if (chains[i].p == chains[j].p && chains[i].q == chains[j].q) {
                                                                   std::cout << "CRAZY ERROR" << std::endl;
                                                               } else if (chains[i].p == chains[j].q && chains[i].q == chains[j].p) {
                                                                   std::cout << "CRAZY ERROR" << std::endl;
                                                               }
                                                               std::cerr << 3 <<std::endl;

                                                               std::cerr << chains[i].p << " " << chains[i].q << std::endl;
                                                               std::cerr << chains[j].p << " " << chains[j].q << std::endl;

                                                               std::cerr << compCenters[chains[i].p].x << " " << compCenters[chains[i].p].y << std::endl;
                                                               std::cerr << compCenters[chains[i].q].x << " " << compCenters[chains[i].q].y << std::endl;
                                                               std::cerr << compCenters[chains[j].q].x << " " << compCenters[chains[j].q].y << std::endl;
                                                               std::cerr << std::endl; */
                                    chains[i].q = chains[j].q;
                                    for (std::vector<int>::iterator it = chains[j].components.begin(); it != chains[j].components.end(); it++) {
                                        chains[i].components.push_back(*it);
                                    }
                                    float d_x = (compCenters[chains[i].p].x - compCenters[chains[i].q].x);
                                    float d_y = (compCenters[chains[i].p].y - compCenters[chains[i].q].y);
                                    float mag = sqrt(d_x*d_x + d_y*d_y);
                                    chains[i].dist = d_x * d_x + d_y * d_y;


                                    d_x = d_x / mag;
                                    d_y = d_y / mag;
                                    Point2dFloat dir;
                                    dir.x = d_x;
                                    dir.y = d_y;

                                    chains[i].direction = dir;
                                    chains[j].merged = true;
                                    merges++;
                                    /*j=-1;
                                    i=0;
                                    if (i == chains.size() - 1) i=-1;
                                    std::stable_sort(chains.begin(), chains.end(), &chainSortLength); */
                                }
                            } else if (chains[i].q == chains[j].q) {
                                if (acos(chains[i].direction.x * -chains[j].direction.x + chains[i].direction.y * -chains[j].direction.y) < strictness) {
                                    /*           if (chains[i].p == chains[i].q || chains[j].p == chains[j].q) {
                                                   std::cout << "CRAZY ERROR" << std::endl;
                                               } else if (chains[i].p == chains[j].p && chains[i].q == chains[j].q) {
                                                   std::cout << "CRAZY ERROR" << std::endl;
                                               } else if (chains[i].p == chains[j].q && chains[i].q == chains[j].p) {
                                                   std::cout << "CRAZY ERROR" << std::endl;
                                               }
                                               std::cerr << 4 <<std::endl;
                                               std::cerr << chains[i].p << " " << chains[i].q << std::endl;
                                               std::cerr << chains[j].p << " " << chains[j].q << std::endl;
                                               std::cerr << compCenters[chains[i].p].x << " " << compCenters[chains[i].p].y << std::endl;
                                               std::cerr << compCenters[chains[i].q].x << " " << compCenters[chains[i].q].y << std::endl;
                                               std::cerr << compCenters[chains[j].p].x << " " << compCenters[chains[j].p].y << std::endl;
                                               std::cerr << std::endl; */
                                    chains[i].q = chains[j].p;
                                    for (std::vector<int>::iterator it = chains[j].components.begin(); it != chains[j].components.end(); it++) {
                                        chains[i].components.push_back(*it);
                                    }
                                    float d_x = (compCenters[chains[i].p].x - compCenters[chains[i].q].x);
                                    float d_y = (compCenters[chains[i].p].y - compCenters[chains[i].q].y);
                                    chains[i].dist = d_x * d_x + d_y * d_y;

                                    float mag = sqrt(d_x*d_x + d_y*d_y);
                                    d_x = d_x / mag;
                                    d_y = d_y / mag;
                                    Point2dFloat dir;
                                    dir.x = d_x;
                                    dir.y = d_y;
                                    chains[i].direction = dir;
                                    chains[j].merged = true;
                                    merges++;
                                    /*j=-1;
                                    i=0;
                                    if (i == chains.size() - 1) i=-1;
                                    std::stable_sort(chains.begin(), chains.end(), &chainSortLength);*/
                                }
                            }
                        }
                    }
                }
            }
            for (unsigned int i = 0; i < chains.size(); i++) {
                if (!chains[i].merged) {
                    newchains.push_back(chains[i]);
                }
            }
            chains = newchains;
            std::stable_sort(chains.begin(), chains.end(), &chainSortLength);
        }

        std::vector<Chain> newchains;
        newchains.reserve(chains.size());
        for (std::vector<Chain>::iterator cit = chains.begin(); cit != chains.end(); cit++) {
            if (cit->components.size() >= 3) {
                newchains.push_back(*cit);
            }
        }
        chains = newchains;
        std::cout << chains.size() << " chains after merging" << std::endl;
        return chains;
    }

}
