package com.intellivision.adas.demo.utilities;


/**
 * Utility class for image conversion.
 * 
 * 
 */
public class ImageConverter {

    public static byte[] scaleYUV420SPByFactor( byte[] data, int imageWidth, int imageHeight, int factor ) {
        byte[] yuv = new byte[imageWidth / factor * imageHeight / factor * 3 / 2];
        int i = 0;
        for ( int y = 0; y < imageHeight; y += factor ) {
            for ( int x = 0; x < imageWidth; x += factor ) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        for ( int y = 0; y < imageHeight / factor; y += factor ) {
            for ( int x = 0; x < imageWidth; x += 4 ) {
                yuv[i] = data[ ( imageWidth * imageHeight ) + ( y * imageWidth ) + ( x + 1 )];
                i++;
                yuv[i] = data[ ( imageWidth * imageHeight ) + ( y * imageWidth ) + x];
                i++;
            }
        }
        data = null;
        return yuv;
    }

}
