/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_kubility_demo_MP3Recorder */

#ifndef _Included_com_example_lamemp3_MP3Recorder
#define _Included_com_example_lamemp3_MP3Recorder
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_kubility_demo_MP3Recorder
 * Method:    init
 * Signature: (IIIII)V
 */
JNIEXPORT void JNICALL Java_com_example_lamemp3_MP3Recorder_init
  (JNIEnv *, jclass, jint, jint, jint, jint, jint);

/*
 * Class:     com_kubility_demo_MP3Recorder
 * Method:    encode
 * Signature: ([S[SI[B)I
 */
JNIEXPORT jint JNICALL Java_com_example_lamemp3_MP3Recorder_encode
  (JNIEnv *, jclass, jshortArray, jshortArray, jint, jbyteArray);

/*
 * Class:     com_kubility_demo_MP3Recorder
 * Method:    flush
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_com_example_lamemp3_MP3Recorder_flush
  (JNIEnv *, jclass, jbyteArray);

/*
 * Class:     com_kubility_demo_MP3Recorder
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_example_lamemp3_MP3Recorder_close
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
