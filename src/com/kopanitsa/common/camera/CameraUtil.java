package com.kopanitsa.common.camera;

public class CameraUtil {
    public static void decodeYUV(int[] out, byte[] fg, int width, int height) throws NullPointerException, IllegalArgumentException { 
        final int sz = width * height; 
        if(out == null) throw new NullPointerException("buffer 'out' is null"); 
        if(out.length < sz) throw new IllegalArgumentException("buffer 'out' size " + out.length + " < minimum " + sz); 
        if(fg == null) throw new NullPointerException("buffer 'fg' is null"); 
        if(fg.length < sz) throw new IllegalArgumentException("buffer 'fg' size " + fg.length + " < minimum " + sz * 3/ 2); 
        int i, j; 
        int Y, Cr = 0, Cb = 0; 
        for(j = 0; j < height; j++) { 
                int pixPtr = j * width; 
                final int jDiv2 = j >> 1; 
                for(i = 0; i < width; i++) { 
                        Y = fg[pixPtr]; if(Y < 0) Y += 255; 
                        if((i & 0x1) != 1) { 
                                final int cOff = sz + jDiv2 * width + (i >> 1) * 2; 
                                Cb = fg[cOff]; 
                                if(Cb < 0) Cb += 127; else Cb -= 128; 
                                Cr = fg[cOff + 1]; 
                                if(Cr < 0) Cr += 127; else Cr -= 128; 
                        } 
                        int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5); 
                        if(R < 0) R = 0; else if(R > 255) R = 255; 
                        int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4) + (Cr >> 5); 
                        if(G < 0) G = 0; else if(G > 255) G = 255; 
                        int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6); 
                        if(B < 0) B = 0; else if(B > 255) B = 255; 
                        out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
                } 
        } 
    } 
}
