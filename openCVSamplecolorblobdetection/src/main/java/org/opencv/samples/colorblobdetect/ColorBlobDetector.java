package org.opencv.samples.colorblobdetect;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.minAreaRect;

public class ColorBlobDetector {
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);
    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = .1;
    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(35, 55, 55, 0);
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
    private double[] stateBlock;
    double xD = 0;
    double yD = 0;
    int soundThresh = 2;
    double xD2 = 0;
    double yD2 = 0;
    // Cache
    Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();
    public void setColorRadius(Scalar radius) {
        mColorRadius = radius;
    }
    public void setHsvBlack(Scalar hsvColor) {
        //were looking for black color given doesnt matter but its easier if one is given trust me
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0] - mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0] + mColorRadius.val[0] <= 255) ? hsvColor.val[0] + mColorRadius.val[0] : 255;
        mLowerBound.val[0] = 0;//minH;
        mUpperBound.val[0] = 180;//maxH;
        mLowerBound.val[1] = 0;//hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = 255;//hsvColor.val[1] + mColorRadius.val[1];
        mLowerBound.val[2] = 0;//hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = 30;//hsvColor.val[2] + mColorRadius.val[2];
        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;
        Mat spectrumHsv = new Mat(1, (int) (maxH - minH), CvType.CV_8UC3);
        for (int j = 0; j < maxH - minH; j++) {
            byte[] tmp = {(byte) (minH + j), (byte) 255, (byte) 255};
            spectrumHsv.put(0, j, tmp);
        }
        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public Mat getSpectrum() {
        return mSpectrum;
    }


    public void setHsvColor(Scalar hsvColor) {
        //set custom color to look for
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0] - mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0] + mColorRadius.val[0] <= 255) ? hsvColor.val[0] + mColorRadius.val[0] : 255;

        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

        mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;

        Mat spectrumHsv = new Mat(1, (int) (maxH - minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH - minH; j++) {
            byte[] tmp = {(byte) (minH + j), (byte) 255, (byte) 255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public void setMinContourArea(double area) {
        mMinContourArea = area;
    }


    public boolean processWithBlob(Mat rgbaImage, Point point1, Point point2) {

//we finding contours
        Mat mat = rgbaImage.submat((int) Math.min(point1.y, point2.y), (int) Math.max(point1.y, point2.y), (int) Math.min(point1.x, point2.x), (int) Math.max(point1.x, point2.x));
        Imgproc.pyrDown(mat, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find max contour area
        Iterator<MatOfPoint> each = contours.iterator();
        mContours.clear();
        List<MatOfPoint> matOfPoints = new ArrayList<>();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            Core.multiply(contour, new Scalar(4, 4), contour);
            contour = translateMatOfPoints(contour, new Point((int) Math.min(point1.x, point2.x), (int) Math.min(point1.y, point2.y)));
            mContours.add(contour);
        }
        if (mContours.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean processWithThresh(Mat rgbaImage, Point point1, Point point2) {
//we finding contours
        Mat mat = rgbaImage.submat((int) Math.min(point1.y, point2.y), (int) Math.max(point1.y, point2.y), (int) Math.min(point1.x, point2.x), (int) Math.max(point1.x, point2.x));
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mat, contours, mHierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);
        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();

        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        mContours.clear();
        List<MatOfPoint> matOfPoints = new ArrayList<>();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea * maxArea) {
                Core.multiply(contour, new Scalar(4, 4), contour);
                contour = translateMatOfPoints(contour, new Point((int) Math.min(point1.x, point2.x), (int) Math.min(point1.y, point2.y)));
                mContours.add(contour);
            }
        }
        if (mContours.size() > 0) {
            return true;
        } else {
            return false;
        }
    }


    public static MatOfPoint translateMatOfPoints(MatOfPoint contour, Point translation) {
        org.opencv.core.Point[] points = contour.toArray();
        for (int i = 0; i < points.length; i++) {
            points[i].x += translation.x;
            points[i].y += translation.y;
        }
        contour.fromArray(points);
        return contour;
    }

    public static void setmMinContourArea(double mMinContourArea) {
        ColorBlobDetector.mMinContourArea = mMinContourArea;
    }

    public Point centerAverage() {
        int sumX = 0, sumY = 0;
        int count = 0;
        for (int i = 0; i < mContours.size(); i++) {
            org.opencv.core.Point[] points = mContours.get(i).toArray();
            for (int x = 0; x < points.length; x++) {
                sumX += points[x].x;
                sumY += points[x].y;
                count++;
            }
        }
        if (count == 0) {
            return new Point(-100, -100);
        }
        return new Point(sumX / count, sumY / count);
    }

    //whichCorner 0=TL, 1=TR, 2=BL, 3= BR
    public Point getCenterBlack(Mat mRgbaGr, Point point1, Point point2, Scalar color, int whichCorner) {
        List<MatOfPoint> contours = new ArrayList<>();
        setHsvColor(color);
        MatOfPoint matOfPointMax = null;
        if (processWithBlob(mRgbaGr, point1, point2)) {

            List<MatOfPoint> matOfPointList = getContours();
            if (matOfPointList.size() > 0) {
            }
            double areaMax = -1.0;

            for (MatOfPoint matOfPoint : matOfPointList) {
                double areaT = Imgproc.contourArea(matOfPoint);
                if (areaT > areaMax) {
                    matOfPointMax = matOfPoint;
                    areaMax = areaT;
                }
            }
        } else {
      //      Log.e("error corner", "couldnt find color contour");
        }
        contours.clear();
        contours.add(matOfPointMax);

        if (matOfPointMax != null) {
            setHsvBlack(color);
            MatOfPoint2f NewMtx = new MatOfPoint2f(matOfPointMax.toArray());
            Rect rect = minAreaRect(NewMtx).boundingRect();
            if (processWithBlob(mRgbaGr, rect.tl(), rect.br())) {
                List<MatOfPoint> matOfPointList = getContours();
                double minDistanceFromCorner = Double.MAX_VALUE;
                MatOfPoint minMat = null;
                for (MatOfPoint matOfPoint : matOfPointList) {
                    NewMtx = new MatOfPoint2f(matOfPoint.toArray());
                    Rect bounding = minAreaRect(NewMtx).boundingRect();
                    if (bounding.area() > 0) {
                        double temp = 0;
                        switch (whichCorner) {
                            case 0:
                                //   Log.e("Corner", "TL");
                                temp = Math.pow(
                                        Math.pow((Math.min(point1.x, point2.x) - ((bounding.x + bounding.width) / 2)), 2)
                                                + Math.pow(Math.min(point1.y, point2.y) - ((bounding.y + bounding.height) / 2), 2), .5);
                                break;
                            case 1:
                                //   Log.e("Corner", "TR");
                                temp = Math.pow(
                                        Math.pow((Math.min(point1.x, point2.x) - ((bounding.x + bounding.width) / 2)), 2)
                                                + Math.pow(Math.max(point1.y, point2.y) - ((bounding.y + bounding.height) / 2), 2), .5);
                                break;
                            case 2:

                                //   Log.e("Corner", "BL");
                                temp = Math.pow(
                                        Math.pow((Math.max(point1.x, point2.x) - ((bounding.x + bounding.width) / 2)), 2)
                                                + Math.pow(Math.min(point1.y, point2.y) - ((bounding.y + bounding.height) / 2), 2), .5);
                                break;
                            case 3:

                                //   Log.e("Corner", "BR");
                                temp = Math.pow(
                                        Math.pow((Math.max(point1.x, point2.x) - ((bounding.x + bounding.width) / 2)), 2)
                                                + Math.pow(Math.max(point1.y, point2.y) - ((bounding.y + bounding.height) / 2), 2), .5);

                                break;
                        }
                        //    Log.e("compare", "temp:" + temp + " " + "min dist: " + minDistanceFromCorner + " s:" + matOfPointList.size());
                        if (temp < minDistanceFromCorner) {
                            minDistanceFromCorner = temp;
                            minMat = matOfPoint;
                        }
                    }
                }
                mContours.clear();
                mContours.add(minMat);
            } else {

      //           Log.e("error corner", "couldnt find black contour");

                int blackCount = 0;
                double blackXSum = 0;
                double blackYSum = 0;
                for (int i = rect.x; i < rect.x + rect.width; i++) {
                    for (int j = rect.y; j < rect.y + rect.height; j++) {
                        if (checkBlack(mRgbaGr.get(j, i))) {
                            blackCount++;
                            blackXSum += i;
                            blackYSum += j;
                        }
                    }
                }

                if (blackCount != 0) {
                    return new Point(blackXSum / blackCount, blackYSum / blackCount);
                }
            }
        }
        return centerAverage();
    }

    public Point getCenterBlackThresehold(Mat mRgbaGr, Point point1, Point point2, Scalar color) {
        Imgproc.threshold(mRgbaGr, mRgbaGr, 127, 255, 0);
        processWithThresh(mRgbaGr, point1, point2);
        return centerAverage();
    }


    public List<Point> findTimingHorizontal(Mat mat, Point start, Point end) {
        List<Point> points = new ArrayList<>();
        double rise = start.y - end.y;
        double run = start.x - end.x;
        double slope = rise / run;
        double yIntercept = start.y - (start.x * slope);
        int startBlack = -1;

        int getOutBlack = (int) start.x;
        //while point is black keep moving cuz we dont wanna start here
        while (checkBlack(mat.get((int) ((slope * getOutBlack) + yIntercept), getOutBlack))) {
            getOutBlack += 1;
        }

        int getBeforeBlack = (int) end.x;

        while (checkBlack(mat.get((int) ((slope * getBeforeBlack) + yIntercept), getBeforeBlack))) {
            getBeforeBlack -= 1;
        }

        //move across x while solving y
        for (int i = (int) getOutBlack; i < getBeforeBlack; i++) {
            //WARNING! method bellow is .get(y,x) cuz opencv is stupid
            double[] point = mat.get((int) ((slope * i) + yIntercept), i);
            // is the point black
            if (checkBlack(point)) {
                //if we dont have a beggining point the startBlack is -1
                if (startBlack == -1) {
                    //ok dis is start point
                    startBlack = i;
                } else {
                    //so we are in a black region keep goin do nothing
                }
            } else {
                if (startBlack == -1) {
                    //if we arent in black and we havent seen a black we are in white space :O
                } else {
                    //so you found the end of the black space but is that region big enough to be a refrence block or just something weird
                   if (i - startBlack >= soundThresh) {

                        int blackX = (startBlack + ((i - startBlack) / 2));
                        if (points.size() >= 1) {
                            points.add(new Point(((points.get(points.size() - 1).x + blackX) / 2), (slope * ((points.get(points.size() - 1).x + blackX) / 2)) + yIntercept));
                        }
                        points.add(new Point(blackX, (slope * i) + yIntercept));
                    } else {

                    }
                    startBlack = -1;
                }
            }
        }
        return points;
    }


    public List<Point> findTimingVerticle(Mat mat, Point start, Point end) {
        //see comment above plz
        List<Point> points = new ArrayList<>();
        double rise = start.y - end.y;
        double run = start.x - end.x;
        double slope = rise / run;
        double yIntercept = start.y - (start.x * slope);
        int startBlack = -1;
        int getOutBlack = (int) start.y;
        int state = 0;
        while (checkBlack(mat.get(getOutBlack, (int) ((getOutBlack - yIntercept) / slope)))) {
            getOutBlack += 1;
        }
        int endBeforeBlack = (int) end.y;
        while (checkBlack(mat.get(endBeforeBlack, (int) ((endBeforeBlack - yIntercept) / slope)))) {
            endBeforeBlack -= 1;
        }
        for (int i = getOutBlack; i < endBeforeBlack; i++) {
            double[] point = mat.get(i, (int) ((i - yIntercept) / slope));
            if (checkBlack(point)) {
                if (startBlack == -1) {
                    startBlack = i;
                } else {
                }
            } else {
                if (startBlack == -1) {
                } else {
                    if (i - startBlack >=soundThresh) {
                        int blackY = startBlack + ((i - startBlack) / 2);
                        if (points.size() >= 1) {
                            if(i==getOutBlack){
                                xD = ((((points.get(points.size() - 1).y + blackY) / 2)) - yIntercept) / slope;
                                yD = (points.get(points.size() - 1).y + blackY) / 2;
                            }
                             xD = ((((points.get(points.size() - 1).y + blackY) / 2)) - yIntercept) / slope;
                            yD = (points.get(points.size() - 1).y + blackY) / 2;
                             points.add(new Point(xD, yD));
                        }
                        points.add(new Point(((int) (blackY - yIntercept) / slope), blackY));
                    }
                    startBlack = -1;
                }
            }
        }
        return points;

    }

    public double[] getStateBlock(Mat mat) {
        return mat.get((int) yD, (int) xD);
    }

    public double[] getStateBlock2(Mat mat) {
        return mat.get((int) yD, (int) xD);
    }

    public boolean checkBlack(double[] d) {
        //check if value is black
        if (d != null) {
            if (d.length == 4) {
                double[] valMin = new double[4];
                double[] valMax = new double[4];

                valMin[0] = 0;//minH;
                valMax[0] = 50;//maxH;
                valMin[1] = 0;//hsvColor.val[1] - mColorRadius.val[1];
                valMax[1] = 50;//hsvColor.val[1] + mColorRadius.val[1];
                valMin[2] = 0;//hsvColor.val[2] - mColorRadius.val[2];
                valMax[2] = 50;//hsvColor.val[2] + mColorRadius.val[2];
                valMin[3] = 0;
                valMax[3] = 255;

                for (int i = 0; i < 4; i++) {
                    if (valMin[i] <= d[i] && valMax[i] >= d[i]) {

                    } else {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    public List<MatOfPoint> getContours() {
        return mContours;
    }
}