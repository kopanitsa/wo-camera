#include<jni.h>
#include"CameraNativeUtil.h"

JNIEXPORT void JNICALL Java_com_kopanitsa_common_camera_CameraUtil_decodeYUV_1native
(JNIEnv *env, jclass class, jintArray out, jbyteArray fg, jint width, jint height){
	jboolean b;
	const int sz = width * height; 
	int i, j; 
	int Y, Cr = 0, Cb = 0; 

	jint* arrOut=(*env)->GetIntArrayElements(env,out,&b);
	jbyte* arrFg =(*env)->GetByteArrayElements(env,fg,&b);
	
	for(j = 0; j < height; j++) { 
		int pixPtr = j * width; 
		const int jDiv2 = j >> 1; 
		for(i = 0; i < width; i++) { 
			Y = arrFg[pixPtr]; 
			if(Y < 0) Y += 255; 
			// I dont understand clearly...
			// When it commented out here, bitmap is made but color is monochrome.
			// When it doesnt commented out, bitmap is monochrome and shape is broken
		    // (4images is drawn.)
			// it may cause jbyte type bit length or something...
//			if((i & 0x1) != 1) { 
//				const int cOff = sz + jDiv2 * width + (i >> 1) * 2; 
//				Cb = arrFg[cOff]; 
//				if(Cb < 0) Cb += 127; else Cb -= 128; 
//				Cr = arrFg[cOff + 1]; 
//				if(Cr < 0) Cr += 127; else Cr -= 128; 
//			} 

			int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5); 
//			if(R < 0) R = 0; else if(R > 255) R = 255; 
			
			int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4) + (Cr >> 5); 
//			if(G < 0) G = 0; else if(G > 255) G = 255; 
			
			int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6); 
//			if(B < 0) B = 0; else if(B > 255) B = 255; 
			
			arrOut[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
		} 
	} 

	(*env)->ReleaseIntArrayElements(env, out, arrOut, 0);
	(*env)->ReleaseByteArrayElements(env, fg, arrFg, 0);	

}
