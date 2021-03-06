//
// Created by VSUS on 2022/2/28.
//

#ifndef OPENCVDEMO_TEXTDETECTION_H
#define OPENCVDEMO_TEXTDETECTION_H

#include "opencv2/core/core.hpp"

namespace DetectText {

    struct SWTPoint2d {
        int x;
        int y;
        float SWT;
    };

    typedef std::pair<SWTPoint2d, SWTPoint2d> SWTPointPair2d;
    typedef std::pair<cv::Point, cv::Point>   SWTPointPair2i;

    struct Point2dFloat {
        float x;
        float y;
    };

    struct Ray {
        SWTPoint2d p;
        SWTPoint2d q;
        std::vector<SWTPoint2d> points;
    };

    struct Point3dFloat {
        float x;
        float y;
        float z;
    };


    struct Chain {
        int p;
        int q;
        float dist;
        bool merged;
        Point2dFloat direction;
        std::vector<int> components;
    };

    bool Point2dSort (SWTPoint2d const & lhs,
                      SWTPoint2d const & rhs);

//    cv::Mat textDetection (const cv::Mat& input, bool dark_on_light);
    std::vector<cv::Mat> textDetection (const cv::Mat& input, bool dark_on_light);

    void strokeWidthTransform (const cv::Mat& edgeImage,
                               cv::Mat& gradientX,
                               cv::Mat& gradientY,
                               bool dark_on_light,
                               cv::Mat& SWTImage,
                               std::vector<Ray> & rays);

    void SWTMedianFilter (cv::Mat& SWTImage, std::vector<Ray> & rays);

    std::vector< std::vector<SWTPoint2d> > findLegallyConnectedComponents (cv::Mat& SWTImage, std::vector<Ray> & rays);

    std::vector< std::vector<SWTPoint2d> >
    findLegallyConnectedComponentsRAY (IplImage * SWTImage,
                                       std::vector<Ray> & rays);

    void componentStats(IplImage * SWTImage,
                        const std::vector<SWTPoint2d> & component,
                        float & mean, float & variance, float & median,
                        int & minx, int & miny, int & maxx, int & maxy);

    void filterComponents(cv::Mat& SWTImage,
                          std::vector<std::vector<SWTPoint2d> > & components,
                          std::vector<std::vector<SWTPoint2d> > & validComponents,
                          std::vector<Point2dFloat> & compCenters,
                          std::vector<float> & compMedians,
                          std::vector<SWTPoint2d> & compDimensions,
                          std::vector<SWTPointPair2d > & compBB );

    std::vector<Chain> makeChains( const cv::Mat& colorImage,
                                   std::vector<std::vector<SWTPoint2d> > & components,
                                   std::vector<Point2dFloat> & compCenters,
                                   std::vector<float> & compMedians,
                                   std::vector<SWTPoint2d> & compDimensions,
                                   std::vector<SWTPointPair2d > & compBB);

}

#endif //OPENCVDEMO_TEXTDETECTION_H
