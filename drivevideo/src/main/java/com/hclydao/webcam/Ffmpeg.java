package com.hclydao.webcam;

public class Ffmpeg {
	static public native int enablecamera();
	static public native int open(String devName);
	static public native int init(int width, int height,int numbuf);
	static public native int streamon();
	static public native V4L2VALUE dqbufstruct(byte[] videodata);
	static public native int dqbuf(byte[] videodata);
	static public native int dqbufdecode(byte[] videodata);
	static public native int yuvtorgb(byte[] yuvdata, byte[] rgbdata,int dwidth,int dheight);
	static public native int jpeg420torgb(byte[] yuvdata, byte[] rgbdata,int dwidth,int dheight);
	static public native int qbuf(int index);
	static public native int videoinit(byte[] filename);
	static public native int videostart(byte[] yuvdata);
	static public native int videoclose();
	static public native int streamoff();
	static public native int release();
	static public native int HideOSD(boolean hide);
	static {
		System.loadLibrary("ffmpegutils");
	}
}
